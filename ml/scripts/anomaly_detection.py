import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import warnings
warnings.filterwarnings('ignore')

# =============================================
# 데이터 로드
# =============================================

dr = pd.read_csv('../data/daily_records_clean.csv')
dr['date'] = pd.to_datetime(dr['date'])
print(f"데이터 로드 완료: {len(dr)}행, {dr['patient_id'].nunique()}명의 환자")

# =============================================
# 피처 엔지니어링 (멘토님 피드백 반영)
# 단순 수치 6개 + 시계열 특성 추가
# =============================================

base_features = [
    'body_weight_kg',
    'systolic_bp_mmhg',
    'diastolic_bp_mmhg',
    'fasting_blood_sugar_mg_dl',
    'total_ultrafiltration_g',
    'urination_count',
]

df = dr.dropna(subset=base_features).copy()

# 환자별로 시계열 피처 생성
# 환자마다 따로 계산해야 함 (다른 환자 데이터가 섞이면 안 됨)
result_dfs = []

for pid in df['patient_id'].unique():
    pdata = df[df['patient_id'] == pid].sort_values('date').copy()

    for col in base_features:
        # 1. 전날 대비 변화량 (difference)
        # 갑작스러운 변화 탐지
        pdata[f'{col}_diff'] = pdata[col].diff()

        # 2. 7일 이동평균 (rolling mean)
        # 주간 추세 파악
        pdata[f'{col}_roll_mean'] = pdata[col].rolling(window=7, min_periods=1).mean()

        # 3. 7일 이동표준편차 (rolling std)
        # 변동성 파악
        pdata[f'{col}_roll_std'] = pdata[col].rolling(window=7, min_periods=1).std().fillna(0)

        # 4. 이동평균 대비 잔차 (residual)
        # 현재 값이 최근 평균에서 얼마나 벗어났는지
        pdata[f'{col}_residual'] = pdata[col] - pdata[f'{col}_roll_mean']

    result_dfs.append(pdata)

df = pd.concat(result_dfs).reset_index(drop=True)

# 피처 엔지니어링으로 생긴 NaN 제거
df = df.dropna().copy()
print(f"피처 엔지니어링 후: {len(df)}행")

# 최종 피처 목록 (기본 6개 + 파생 피처)
all_features = base_features.copy()
for col in base_features:
    all_features += [
        f'{col}_diff',
        f'{col}_roll_mean',
        f'{col}_roll_std',
        f'{col}_residual',
    ]

print(f"사용할 피처 수: {len(all_features)}개")

# =============================================
# 표준화
# =============================================

scaler = StandardScaler()
X_scaled = scaler.fit_transform(df[all_features])

# =============================================
# Isolation Forest 학습
# =============================================

model = IsolationForest(
    n_estimators=100,
    contamination=0.05,
    random_state=42
)

model.fit(X_scaled)

# decision_function: 멘토님 권장 방식
# score > 0.05  → 정상
# score > -0.05 → 주의
# score <= -0.05 → 위험
df['anomaly_score'] = model.decision_function(X_scaled)
df['anomaly_label'] = model.predict(X_scaled)
df['is_anomaly'] = (df['anomaly_label'] == -1).astype(int)

# 3단계 상태 분류
def classify_status(score):
    if score > 0.05:
        return 'NORMAL'
    elif score > -0.05:
        return 'WARNING'
    else:
        return 'DANGER'

df['status'] = df['anomaly_score'].apply(classify_status)

# =============================================
# 결과 출력
# =============================================

print(f"\n[이상치 탐지 결과]")
print(f"  전체 데이터 : {len(df)}행")
print(f"  정상(NORMAL)  : {(df['status'] == 'NORMAL').sum()}행")
print(f"  주의(WARNING) : {(df['status'] == 'WARNING').sum()}행")
print(f"  위험(DANGER)  : {(df['status'] == 'DANGER').sum()}행")

print(f"\n[가장 심각한 위험 TOP 10]")
danger = df[df['status'] == 'DANGER'].sort_values('anomaly_score')
print(danger[['patient_id', 'date'] + base_features + ['anomaly_score', 'status']].head(10).to_string())

print(f"\n[환자별 상태 분포]")
status_count = df.groupby(['patient_id', 'status']).size().unstack(fill_value=0)
print(status_count.to_string())

# 결과 저장
df.to_csv('../data/anomaly_results.csv', index=False)
print(f"\n결과 저장 완료: data/anomaly_results.csv")