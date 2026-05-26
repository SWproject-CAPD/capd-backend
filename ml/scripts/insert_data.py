import pandas as pd
from sqlalchemy import create_engine, text
from urllib.parse import quote_plus

DB_USER = "root"
DB_PASSWORD = quote_plus("c*2851433")
DB_HOST = "localhost"
DB_PORT = "3306"
DB_NAME = "capd_db"

engine = create_engine(
    f'mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}'
)

# CSV 읽기
dr = pd.read_csv('../data/daily_records.csv')
es = pd.read_csv('../data/exchange_sessions.csv')

# 정상 데이터만 필터링
dr_clean = dr[dr['quality_issue_count'] == 0].copy()
es_clean = es[es['performed'] == 1].copy()

print(f"daily_records 정상 행: {len(dr_clean)} / 전체 {len(dr)}")
print(f"exchange_sessions 실제 수행 세션: {len(es_clean)} / 전체 {len(es)}")

# patient_id 매핑
patient_map = {
    'patient_001': 5,
    'patient_002': 6,
    'patient_003': 7,
    'patient_004': 8,
    'patient_005': 9,
    'patient_006': 10,
    'patient_007': 11,
    'patient_008': 12,
    'patient_009': 13,
    'patient_010': 14,
}

# capd_commons 삽입
dr_clean['patient_id_fk'] = dr_clean['patient_id'].map(patient_map)

commons = pd.DataFrame({
    'patient_id':            dr_clean['patient_id_fk'],
    'date':                  pd.to_datetime(dr_clean['date']).dt.date,
    'cloudy_dialysate':      dr_clean['cloudy_dialysate'].astype(bool),
    'urination_count':       dr_clean['urination_count'],
    'total_ultrafiltration': dr_clean['total_ultrafiltration_g'],
    'body_weight':           dr_clean['body_weight_kg'],
    'blood_pressure_sys':    dr_clean['systolic_bp_mmhg'],
    'blood_pressure_dia':    dr_clean['diastolic_bp_mmhg'],
    'fasting_blood_sugar':   dr_clean['fasting_blood_sugar_mg_dl'],
    'note':                  None,
    'status':                'SUBMITTED',
    'created_at':            pd.Timestamp.now(),
    'updated_at':            pd.Timestamp.now(),
})

commons.to_sql('capd_commons', engine, if_exists='append', index=False)
print(f"capd_commons 삽입 완료: {len(commons)}행")

# ── capd_sessions 삽입 ──
commons_db = pd.read_sql(
    "SELECT capd_id, patient_id, date FROM capd_commons", engine
)
commons_db['date'] = pd.to_datetime(commons_db['date']).dt.date

es_clean['patient_id_fk'] = es_clean['patient_id'].map(patient_map)
es_clean['date_parsed'] = pd.to_datetime(es_clean['date']).dt.date

# patient_id + date로 capd_id 매핑
es_merged = es_clean.merge(
    commons_db,
    left_on=['patient_id_fk', 'date_parsed'],
    right_on=['patient_id', 'date'],
    how='inner'
)

sessions = pd.DataFrame({
    'capd_id':               es_merged['capd_id'],
    'session_number':        es_merged['session_number'],
    'exchange_time':         es_merged['exchange_time'],
    'drain_volume':          es_merged['drain_volume_g'],
    'dialysate_concentration': es_merged['dialysate_concentration_percent'],
    'infused_fluid_weight':  es_merged['infused_fluid_weight_g'],
    'ultrafiltration':       es_merged['ultrafiltration_g'],
    'created_at':            pd.Timestamp.now(),
    'updated_at':            pd.Timestamp.now(),
})

sessions.to_sql('capd_sessions', engine, if_exists='append', index=False)
print(f"capd_sessions 삽입 완료: {len(sessions)}행")

# 결과 확인
print()
print("=== 삽입 결과 확인 ===")
commons_count = pd.read_sql("SELECT COUNT(*) as cnt FROM capd_commons", engine).iloc[0]['cnt']
sessions_count = pd.read_sql("SELECT COUNT(*) as cnt FROM capd_sessions", engine).iloc[0]['cnt']
print(f"capd_commons 전체 행: {commons_count}")
print(f"capd_sessions 전체 행: {sessions_count}")