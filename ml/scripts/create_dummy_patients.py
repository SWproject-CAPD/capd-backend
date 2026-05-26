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

dr = pd.read_csv('../data/daily_records.csv')
patient_info = dr[['patient_id', 'sex', 'age', 'primary_pattern']].drop_duplicates('patient_id').reset_index(drop=True)

print("CSV 환자 목록:")
print(patient_info.to_string())
print()

dummy_password = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHuu"

with engine.connect() as conn:
    for _, row in patient_info.iterrows():
        patient_id_str = row['patient_id']
        num = patient_id_str.split('_')[1]
        email = f'patient{num}@test.com'
        phone = f'010-1111-{num}'
        name = f'테스트환자{num}'

        result = conn.execute(text(
            "SELECT user_id FROM user_info WHERE email = :email"
        ), {'email': email})
        existing = result.fetchone()

        if existing:
            user_id = existing[0]
            print(f"{patient_id_str} → 이미 존재 user_id={user_id}, 스킵")
        else:
            conn.execute(text("""
                INSERT INTO user_info (email, password, phone, user_name, role, created_at, updated_at)
                VALUES (:email, :password, :phone, :name, 'PATIENT', NOW(), NOW())
            """), {
                'email': email,
                'password': dummy_password,
                'phone': phone,
                'name': name
            })

            result = conn.execute(text(
                "SELECT user_id FROM user_info WHERE email = :email"
            ), {'email': email})
            user_id = result.fetchone()[0]
            
            conn.execute(text("""
                INSERT INTO patients (user_id, sex, age, primary_pattern, created_at, updated_at)
                VALUES (:user_id, :sex, :age, :pattern, NOW(), NOW())
            """), {
                'user_id': user_id,
                'sex': row['sex'],
                'age': int(row['age']),
                'pattern': row['primary_pattern']
            })
            
            print(f"{patient_id_str} 등록 완료 → user_id={user_id}")

    conn.commit()

print()
print("=== 등록 완료 후 환자 목록 ===")
result = pd.read_sql(
    "SELECT p.patient_id, u.email, u.user_name, p.sex, p.age FROM patients p JOIN user_info u ON p.user_id = u.user_id ORDER BY p.patient_id",
    engine
)
print(result.to_string())