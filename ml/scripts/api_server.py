from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import joblib
import numpy as np
import pandas as pd
import os
import chromadb
import requests as req
from dotenv import load_dotenv

app = FastAPI(title="CAPD AI Doctor API")

# 환경변수 로드
load_dotenv(os.path.join(os.path.dirname(__file__), '..', '.env'))
GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')

# ChromaDB HTTP 클라이언트 연결
chroma_client = chromadb.HttpClient(host="chromadb", port=8000)
collection = chroma_client.get_collection(name="kdigo_guidelines")
print("ChromaDB 로드 완료!")

print("Loading AI Models...")
MODEL_DIR = os.path.join(os.path.dirname(__file__), '..', 'models')
model = joblib.load(os.path.join(MODEL_DIR, 'isolation_forest.pkl'))
scaler = joblib.load(os.path.join(MODEL_DIR, 'scaler.pkl'))
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

# 챗봇 Request 모델
class RecentRecord(BaseModel):
    date: str
    body_weight_kg: float
    systolic_bp_mmhg: float
    diastolic_bp_mmhg: float
    fasting_blood_sugar: float
    total_ultrafiltration: float

class PatientData(BaseModel):
    patient_name: str
    recent_records: List[RecentRecord]

class ChatRequest(BaseModel):
    user_type: str  # PATIENT or DOCTOR
    user_message: str
    patient_data: PatientData

# 임베딩 생성 함수
def get_embedding(text):
    """Gemini API로 텍스트 임베딩 생성"""
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key={GEMINI_API_KEY}"

    payload = {
        "model": "models/gemini-embedding-001",
        "content": {
            "parts": [{"text": text}]
        }
    }

    response = req.post(url, json=payload)

    if response.status_code == 200:
        return response.json()['embedding']['values']
    else:
        raise Exception(f"임베딩 생성 실패: {response.text}")

# ChromaDB 검색 함수
def search_kdigo(query, n_results=3):
    """ChromaDB에서 유사한 KDIGO 내용 검색"""

    # 질문을 임베딩으로 변환
    query_embedding = get_embedding(query)

    # 유사한 문서 검색
    results = collection.query(
        query_embeddings=[query_embedding],
        n_results=n_results
    )

    # 검색된 문서 텍스트 합치기
    documents = results['documents'][0]
    sources = [m['source'] for m in results['metadatas'][0]]

    return documents, sources

# Gemini 답변 생성 함수
def generate_answer(prompt):
    """Gemini API로 답변 생성"""
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key={GEMINI_API_KEY}"

    payload = {
        "contents": [
            {
                "parts": [{"text": prompt}]
            }
        ]
    }

    response = req.post(url, json=payload)

    if response.status_code == 200:
        candidates = response.json()['candidates']
        return candidates[0]['content']['parts'][0]['text']
    else:
        raise Exception(f"답변 생성 실패: {response.text}")

# 챗봇 엔드포인트
@app.post("/api/chat")
def chat(request: ChatRequest):

    print(f"\n=== 챗봇 요청 ===")
    print(f"user_type: {request.user_type}")
    print(f"user_message: {request.user_message}")
    print(f"patient_name: {request.patient_data.patient_name}")
    print(f"records: {request.patient_data.recent_records}")
    print(f"==================\n")

    # ChromaDB에서 관련 KDIGO 내용 검색
    kdigo_docs, sources = search_kdigo(request.user_message)
    kdigo_context = "\n\n".join(kdigo_docs)

    # 환자 데이터 텍스트로 변환
    patient_data_text = f"환자명: {request.patient_data.patient_name}\n\n최근 투석 데이터:\n"

    if request.patient_data.recent_records:
        for record in request.patient_data.recent_records:
            patient_data_text += (
                f"날짜: {record.date}, "
                f"체중: {record.body_weight_kg}kg, "
                f"혈압: {record.systolic_bp_mmhg}/{record.diastolic_bp_mmhg}mmHg, "
                f"혈당: {record.fasting_blood_sugar}mg/dL, "
                f"총초여과량: {record.total_ultrafiltration}g\n"
            )
    else:
        patient_data_text += "최근 투석 데이터 없음\n"

    # 사용자 유형에 따라 프롬프트 구성
    if request.user_type == "PATIENT":
        prompt = f"""당신은 CAPD(복막투석) 환자를 돕는 친절한 AI 어시스턴트입니다.
아래 KDIGO 가이드라인과 환자 데이터를 참고하여 환자의 질문에 쉽고 친절하게 답변해주세요.
전문 용어는 최대한 쉬운 말로 설명해주세요.

[KDIGO 가이드라인 참고 내용]
{kdigo_context}

[환자 데이터]
{patient_data_text}

[환자 질문]
{request.user_message}

답변은 한국어로 3~5문장으로 작성해주세요."""

    else:
        prompt = f"""당신은 CAPD(복막투석) 전문 의료 AI 어시스턴트입니다.
아래 KDIGO 가이드라인과 환자 데이터를 참고하여 의사의 질문에 전문적으로 답변해주세요.

[KDIGO 가이드라인 참고 내용]
{kdigo_context}

[환자 데이터]
{patient_data_text}

[의사 질문]
{request.user_message}

답변은 한국어로 전문적이고 명확하게 작성해주세요."""

    ai_answer = generate_answer(prompt)

    print(f"답변 생성 완료")

    return {
        "user_message": request.user_message,
        "ai_answer": ai_answer,
        "kdigo_sources": list(set(sources))
    }