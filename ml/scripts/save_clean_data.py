import pandas as pd

# 원본 CSV 읽기
dr = pd.read_csv('../data/daily_records.csv')
es = pd.read_csv('../data/exchange_sessions.csv')

# ── daily_records 정제 ──
# quality_issue_count가 0인 것만 = 문제없는 정상 데이터
dr_clean = dr[dr['quality_issue_count'] == 0].copy()

# 머신러닝에 필요한 컬럼만 선택
dr_clean = dr_clean[[
    'patient_id',
    'date',
    'body_weight_kg',
    'systolic_bp_mmhg',
    'diastolic_bp_mmhg',
    'fasting_blood_sugar_mg_dl',
    'total_ultrafiltration_g',
    'urination_count',
    'cloudy_dialysate',
    'performed_exchange_count',
    'primary_pattern',
    'sex',
    'age'
]].reset_index(drop=True)

# ── exchange_sessions 정제 ──
# performed가 1인 것만 = 실제로 수행한 세션
es_clean = es[es['performed'] == 1].copy()

es_clean = es_clean[[
    'patient_id',
    'date',
    'session_number',
    'exchange_time',
    'dialysate_concentration_percent',
    'infused_fluid_weight_g',
    'drain_volume_g',
    'ultrafiltration_g'
]].reset_index(drop=True)

# 정제된 CSV 2개 저장
dr_clean.to_csv('../data/daily_records_clean.csv', index=False)
es_clean.to_csv('../data/exchange_sessions_clean.csv', index=False)

print(f"daily_records_clean.csv 저장 완료: {len(dr_clean)}행")
print(f"exchange_sessions_clean.csv 저장 완료: {len(es_clean)}행")