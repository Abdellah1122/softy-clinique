import pandas as pd
from sqlalchemy import create_engine
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler
import joblib

# 1. Connexion à la base de données (la même que votre app Java)
DB_URL = "postgresql://postgres:Williwonka11.@localhost:5432/clinique_db"
engine = create_engine(DB_URL)

print("Connexion à la base de données...")

# 2. Récupérer les données historiques
sql = """
    SELECT session_date_time, created_at, status, patient_profile_id, therapist_profile_id
    FROM appointments 
    WHERE status = 'COMPLETED' OR status = 'CANCELLED_BY_PATIENT'
"""
data = pd.read_sql(sql, engine)

if data.empty or len(data) < 2:
    print("Pas assez de données historiques (COMPLETED ou CANCELLED) pour l'entraînement. Arrêt.")
    print("Veuillez ajouter plus de fausses données dans la BDD.")
    exit()

print(f"Trouvé {len(data)} enregistrements historiques.")

# 3. Ingénierie des "Features"
data['session_date_time'] = pd.to_datetime(data['session_date_time'])
data['created_at'] = pd.to_datetime(data['created_at'])
data['is_cancelled'] = data['status'].apply(lambda x: 1 if x == 'CANCELLED_BY_PATIENT' else 0)
data['lead_time_days'] = (data['session_date_time'] - data['created_at']).dt.total_seconds() / (60*60*24)
data['day_of_week'] = data['session_date_time'].dt.dayofweek
data['hour_of_day'] = data['session_date_time'].dt.hour

# 4. Préparer les données pour le modèle
features = ['lead_time_days', 'day_of_week', 'hour_of_day']
target = 'is_cancelled'

X = data[features]
y = data[target]

scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# 5. Entraîner le modèle
print("Entraînement du modèle d'annulation...")
model = LogisticRegression()
model.fit(X_scaled, y)

# 6. Sauvegarder le modèle
joblib.dump(model, 'cancellation_model.joblib')
joblib.dump(scaler, 'scaler.joblib')

print("Entraînement terminé. Modèle 'cancellation_model.joblib' sauvegardé.")