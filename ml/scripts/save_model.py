import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import joblib
import os
import warnings
warnings.filterwarnings('ignore')

dr = pd.read_csv('../data/daily_records_clean.csv')
dr['date'] = pd.to_datetime(dr['date'])

base_features = [
    'body_weight_kg',
    'systolic_bp_mmhg',
    'diastolic_bp_mmhg',
    'fasting_blood_sugar_mg_dl',
    'total_ultrafiltration_g',
    'urination_count',
]

df = dr.dropna(subset=base_features).copy()

result_dfs = []
for pid in df['patient_id'].unique():
    pdata = df[df['patient_id'] == pid].sort_values('date').copy()
    for col in base_features:
        pdata[f'{col}_diff'] = pdata[col].diff()
        pdata[f'{col}_roll_mean'] = pdata[col].rolling(window=7, min_periods=1).mean()
        pdata[f'{col}_roll_std'] = pdata[col].rolling(window=7, min_periods=1).std().fillna(0)
        pdata[f'{col}_residual'] = pdata[col] - pdata[f'{col}_roll_mean']
    result_dfs.append(pdata)

df = pd.concat(result_dfs).reset_index(drop=True)
df = df.dropna().copy()

all_features = base_features.copy()
for col in base_features:
    all_features += [f'{col}_diff', f'{col}_roll_mean', f'{col}_roll_std', f'{col}_residual']

scaler = StandardScaler()
X_scaled = scaler.fit_transform(df[all_features])

model = IsolationForest(n_estimators=100, contamination=0.05, random_state=42)
model.fit(X_scaled)

os.makedirs('../models', exist_ok=True)
joblib.dump(model, '../models/isolation_forest.pkl')
joblib.dump(scaler, '../models/scaler.pkl')

print("모델 저장 완료: models/isolation_forest.pkl")
print("스케일러 저장 완료: models/scaler.pkl")

# 저장된 모델 검증
model_loaded = joblib.load('../models/isolation_forest.pkl')
scaler_loaded = joblib.load('../models/scaler.pkl')
test = scaler_loaded.transform(X_scaled[:5])
result = model_loaded.decision_function(test)
print(f"\n저장된 모델 테스트 (첫 5행 점수): {result.round(3)}")
print("모델 저장 및 검증 완료")