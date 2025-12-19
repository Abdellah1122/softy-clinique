import pandas as pd
from sqlalchemy import create_engine
from sklearn.linear_model import LinearRegression
import joblib

DB_URL = "postgresql://postgres:Williwonka11.@localhost:5432/clinique_db"
engine = create_engine(DB_URL)

print("Connexion à la BDD pour l'entraînement du modèle de timing...")

# 1. Écrire une requête SQL complexe
# On doit lier les RDV à leurs notes, et aussi à leur RDV *suivant*
sql = """
WITH patient_sessions AS (
    SELECT 
        a.patient_profile_id,
        a.session_date_time,
        cn.patient_progress_score,
        -- Trouver la date de la *prochaine* session pour ce patient
        LEAD(a.session_date_time) OVER(
            PARTITION BY a.patient_profile_id 
            ORDER BY a.session_date_time
        ) AS next_session_date
    FROM appointments a
    -- JOIN : On ne prend que les RDV qui ont une note
    JOIN clinical_notes cn ON a.id = cn.appointment_id
    WHERE a.status = 'COMPLETED' AND cn.patient_progress_score IS NOT NULL
)
-- 2. Calculer le délai (notre cible 'y')
SELECT 
    patient_progress_score,
    -- Calculer le nombre de jours entre cette session et la suivante
    EXTRACT(DAY FROM (next_session_date - session_date_time)) AS days_until_next_session
FROM patient_sessions
WHERE next_session_date IS NOT NULL;
"""

data = pd.read_sql(sql, engine)

if data.empty or len(data) < 2:
    print("Pas assez de données pour l'entraînement du modèle de timing.")
    print("Vous devez avoir au moins 2 RDV 'COMPLETED' avec des notes et des scores pour le *même* patient.")
    exit()

print(f"Trouvé {len(data)} paires de sessions pour l'entraînement.")

# 3. Préparer les données
# 'X' (la feature) est le score
X = data[['patient_progress_score']]
# 'y' (la cible) est le nombre de jours
y = data['days_until_next_session']

# 4. Entraîner le modèle
print("Entraînement du modèle de timing (Régression Linéaire)...")
model = LinearRegression()
model.fit(X, y)

# 5. Sauvegarder le nouveau modèle
joblib.dump(model, 'timing_model.joblib')

print("Entraînement terminé. Modèle 'timing_model.joblib' sauvegardé.")