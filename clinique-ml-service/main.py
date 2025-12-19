from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import pandas as pd
import numpy as np # Importez numpy

app = FastAPI(title="API de Prédiction Clinique")

# --- Modèle 1: Risque d'Annulation ---
try:
    cancel_model = joblib.load("cancellation_model.joblib")
    cancel_scaler = joblib.load("scaler.joblib")
    print("Modèle d'annulation chargé.")
except FileNotFoundError:
    print("ERREUR: Fichiers du modèle d'annulation non trouvés. Exécutez train.py")
    cancel_model = None
    cancel_scaler = None

# --- Modèle 2: Timing de Session ---
try:
    timing_model = joblib.load("timing_model.joblib")
    print("Modèle de timing chargé.")
except FileNotFoundError:
    print("ERREUR: Fichiers du modèle de timing non trouvés. Exécutez train_timing.py")
    timing_model = None

# --- DTOs pour le Modèle 1 ---
class AppointmentFeatures(BaseModel):
    lead_time_days: float
    day_of_week: int
    hour_of_day: int

class PredictionResponse(BaseModel):
    cancellation_risk_score: float

# --- DTOs pour le Modèle 2 ---
class TimingFeatures(BaseModel):
    # La seule feature est le dernier score de progression
    last_progress_score: int 

class TimingResponse(BaseModel):
    recommended_days_next_session: int

# --- Endpoint 1: Prédire l'Annulation ---
@app.post("/predict", response_model=PredictionResponse)
async def predict_cancellation(features: AppointmentFeatures):
    if not cancel_model or not cancel_scaler:
        return {"cancellation_risk_score": -1.0}

    input_data = pd.DataFrame([features.dict()])
    input_scaled = cancel_scaler.transform(input_data)
    prediction_proba = cancel_model.predict_proba(input_scaled)
    risk_score = prediction_proba[0][1]
    
    return {"cancellation_risk_score": risk_score}

# --- NOUVEL Endpoint 2: Prédire le Timing ---
@app.post("/predict-timing", response_model=TimingResponse)
async def predict_timing(features: TimingFeatures):
    if not timing_model:
        return {"recommended_days_next_session": -1} # Erreur

    # Préparer les données pour le modèle de régression
    # Note: .predict() attend un array 2D, d'où [[...]]
    input_data = np.array([[features.last_progress_score]])
    
    # Faire la prédiction
    predicted_days = timing_model.predict(input_data)
    
    # Arrondir au jour le plus proche et s'assurer qu'il est positif
    recommended_days = max(1, int(round(predicted_days[0])))
    
    return {"recommended_days_next_session": recommended_days}

# --- NOUVEL Endpoint 3: Analyse de Sentiment ---
from textblob import TextBlob

class SentimentRequest(BaseModel):
    text: str

class SentimentResponse(BaseModel):
    polarity: float # -1.0 (Négatif) à 1.0 (Positif)
    subjectivity: float # 0.0 (Objectif) à 1.0 (Subjectif)
    sentiment_label: str # "POSITIVE", "NEGATIVE", "NEUTRAL"

@app.post("/predict-sentiment", response_model=SentimentResponse)
async def predict_sentiment(request: SentimentRequest):
    blob = TextBlob(request.text)
    polarity = blob.sentiment.polarity
    subjectivity = blob.sentiment.subjectivity
    
    label = "NEUTRAL"
    if polarity > 0.1:
        label = "POSITIVE"
    elif polarity < -0.1:
        label = "NEGATIVE"
        
    return {
        "polarity": polarity,
        "subjectivity": subjectivity,
        "sentiment_label": label
    }

# --- Modèle 3: Churn Prediction ---
try:
    churn_model = joblib.load("churn_model.joblib")
    print("Modèle de churn chargé.")
except FileNotFoundError:
    print("ERREUR: 'churn_model.joblib' non trouvé.")
    churn_model = None

class ChurnFeatures(BaseModel):
    days_since_last_visit: int
    total_visits: int
    cancellation_rate: float

class ChurnResponse(BaseModel):
    is_churn_risk: bool
    churn_probability: float

@app.post("/predict-churn", response_model=ChurnResponse)
async def predict_churn(features: ChurnFeatures):
    if not churn_model:
        return {"is_churn_risk": False, "churn_probability": -1.0}

    input_data = pd.DataFrame([features.dict()])
    
    # Predict probability
    prob = churn_model.predict_proba(input_data)[0][1]
    is_risk = prob > 0.5
    
    return {
        "is_churn_risk": bool(is_risk),
        "churn_probability": float(prob)
    }


@app.get("/")
def root():
    return {"message": "Service de prédiction clinique (v2) en ligne"}