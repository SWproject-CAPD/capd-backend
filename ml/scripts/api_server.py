from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import joblib
import numpy as np
import pandas as pd

app = FastAPI(title="CAPD AI Doctor API")

print("Loading AI Models...")
model = joblib.load('../models/isolation_forest.pkl')
scaler = joblib.load('../models/scaler.pkl')
print("Model Load Complete!")

class DailyRecord(BaseModel):
    date: str
    body_weight_kg: float
    systolic_bp_mmhg: float
    diastolic_bp_mmhg: float
    fasting_blood_sugar_mg_dl: float
    total_ultrafiltration_g: float
    urination_count: int

class AnomalyRequest(BaseModel):
    patient_id: str
    records: List[DailyRecord]

base_features = [
    'body_weight_kg',
    'systolic_bp_mmhg',
    'diastolic_bp_mmhg',
    'fasting_blood_sugar_mg_dl',
    'total_ultrafiltration_g',
    'urination_count',
]

# anomaly_detection.py와 동일한 순서
all_features = base_features.copy()
for col in base_features:
    all_features += [
        f'{col}_diff',
        f'{col}_roll_mean',
        f'{col}_roll_std',
        f'{col}_residual',
    ]

feature_names_kor = {
    'body_weight_kg': '체중',
    'systolic_bp_mmhg': '수축기혈압',
    'diastolic_bp_mmhg': '이완기혈압',
    'fasting_blood_sugar_mg_dl': '공복혈당',
    'total_ultrafiltration_g': '총초여과량',
    'urination_count': '소변횟수',
}

@app.post("/api/ml/anomaly")
def predict_anomaly(request: AnomalyRequest):

    if len(request.records) < 1:
        raise HTTPException(
            status_code=400,
            detail="최소 1개 이상의 기록이 필요합니다."
    )
    
    # 디버그용 — 받은 데이터 출력
    print(f"\n=== 받은 데이터 ===")
    print(f"patient_id: {request.patient_id}")
    print(f"records 수: {len(request.records)}")
    for r in request.records:
        print(f"  {r.date} | 체중:{r.body_weight_kg} | 혈압:{r.systolic_bp_mmhg} | UF:{r.total_ultrafiltration_g}")
    print(f"==================\n")

    # 리스트 → DataFrame 변환
    df = pd.DataFrame([r.dict() for r in request.records])
    df['date'] = pd.to_datetime(df['date'])
    df = df.sort_values('date').reset_index(drop=True)

    # 시계열 피처 계산
    for col in base_features:
        df[f'{col}_diff'] = df[col].diff().fillna(0)
        df[f'{col}_roll_mean'] = df[col].rolling(window=7, min_periods=1).mean()
        df[f'{col}_roll_std'] = df[col].rolling(window=7, min_periods=1).std().fillna(0)
        df[f'{col}_residual'] = df[col] - df[f'{col}_roll_mean']

    # 오늘(마지막 행)만 분석
    today = df.iloc[[-1]]
    input_data = today[all_features].values

    # 표준화
    scaled_data = scaler.transform(input_data)

    # decision_function으로 점수 계산
    score = float(model.decision_function(scaled_data)[0])

    # 3단계 상태 분류
    if score > 0.07:
        status = "정상 (Normal)"
        level = 1
    elif score > 0.04:
        status = "주의 (Warning) - 관심이 필요합니다."
        level = 2
    else:
        status = "위험 (Danger) - 즉각적인 조치가 필요합니다!"
        level = 3

    # 원인 분석
    top_causes = []
    if level >= 2:
        z_scores = scaled_data[0]
        feature_impacts = []

        for i, fname in enumerate(all_features):
            if fname in base_features:
                feature_impacts.append({
                    'feature_name': feature_names_kor[fname],
                    'z_score': float(z_scores[i]),
                    'absolute_impact': float(abs(z_scores[i]))
                })

        feature_impacts.sort(key=lambda x: x['absolute_impact'], reverse=True)

        for cause in feature_impacts[:3]:
            direction = '급상승' if cause['z_score'] > 0 else '급감소'
            top_causes.append({
                'feature': cause['feature_name'],
                'direction': direction,
                'impact_score': round(cause['absolute_impact'], 2)
            })

    # 최종 응답
    return {
        'patient_id': request.patient_id,
        'analysis_date': str(df.iloc[-1]['date'].date()),
        'status_message': status,
        'risk_level': level,
        'anomaly_score': round(score, 3),
        'is_anomaly': level >= 2,
        'top_causes': top_causes
    }

@app.get("/health")
def health_check():
    return {"status": "ok", "model": "isolation_forest"}