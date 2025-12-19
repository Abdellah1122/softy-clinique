import pandas as pd
from sqlalchemy import create_engine
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import joblib
import numpy as np
from datetime import datetime, timedelta

# 1. Database Connection
DB_URL = "postgresql://postgres:Williwonka11.@localhost:5432/clinique_db"
engine = create_engine(DB_URL)

print("Connecting to database...")

# 2. Fetch Data (We need patient appointment history)
# We need to construct a dataset where each row is a patient "snapshot" and target is "did they leave?"
# For simplicity in this demo, we will simulate "Churn" based on "Has not visited in 3 months".

# 2. Generate Synthetic Data (for robust demonstration)
print("Generating synthetic training data...")
data = []
for i in range(200):
    # 50% Churned, 50% Active
    is_churn = i % 2 == 0
    days_since_last = np.random.randint(90, 365) if is_churn else np.random.randint(1, 60)
    total_visits = np.random.randint(1, 20)
    cancellation_rate = np.random.uniform(0, 0.5) if not is_churn else np.random.uniform(0.3, 1.0)
    
    data.append({
        'days_since_last_visit': days_since_last,
        'total_visits': total_visits,
        'cancellation_rate': cancellation_rate,
        'is_churn': 1 if is_churn else 0
    })
df_features = pd.DataFrame(data)

print(f"Training on {len(df_features)} samples.")

# 3. Train Model
X = df_features[['days_since_last_visit', 'total_visits', 'cancellation_rate']]
y = df_features['is_churn']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

y_pred = model.predict(X_test)
print(f"Model Accuracy: {accuracy_score(y_test, y_pred)}")

# 4. Save
joblib.dump(model, 'churn_model.joblib')
print("Model saved to churn_model.joblib")
