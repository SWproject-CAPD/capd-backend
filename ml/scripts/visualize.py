import pandas as pd
import matplotlib.pyplot as plt
import os
import warnings
warnings.filterwarnings('ignore')

plt.rcParams['font.family'] = 'AppleGothic'
plt.rcParams['axes.unicode_minus'] = False

# =============================================
# 데이터 로드
# =============================================

df = pd.read_csv('../data/anomaly_results.csv')
df['date'] = pd.to_datetime(df['date'])
print(f"결과 데이터 로드 완료: {len(df)}행")

os.makedirs('../figures', exist_ok=True)

# =============================================
# 시계열 시각화 — 3단계 색상으로 표시
# =============================================

patients = sorted(df['patient_id'].unique())

fig, axes = plt.subplots(2, 5, figsize=(28, 10))
axes = axes.flatten()

# 상태별 색상 정의
status_colors = {
    'NORMAL':  'steelblue',   # 정상 → 파란색
    'WARNING': 'orange',      # 주의 → 주황색
    'DANGER':  'red',         # 위험 → 빨간색
}

for i, pid in enumerate(patients):
    pdata = df[df['patient_id'] == pid].sort_values('date')

    # 정상 데이터 → 파란 선
    normal = pdata[pdata['status'] == 'NORMAL']
    axes[i].plot(
        normal['date'],
        normal['total_ultrafiltration_g'],
        color='steelblue',
        linewidth=0.8,
        alpha=0.6,
        label='정상'
    )

    # 주의 → 주황 점
    warning = pdata[pdata['status'] == 'WARNING']
    if len(warning) > 0:
        axes[i].scatter(
            warning['date'],
            warning['total_ultrafiltration_g'],
            color='orange',
            s=30,
            zorder=5,
            label=f'주의 ({len(warning)}건)'
        )

    # 위험 → 빨간 점
    danger = pdata[pdata['status'] == 'DANGER']
    if len(danger) > 0:
        axes[i].scatter(
            danger['date'],
            danger['total_ultrafiltration_g'],
            color='red',
            s=40,
            zorder=6,
            label=f'위험 ({len(danger)}건)'
        )

    # 0 기준선
    axes[i].axhline(y=0, color='black', linestyle='--', alpha=0.3, linewidth=0.8)

    n_count = len(normal)
    w_count = len(warning)
    d_count = len(danger)

    axes[i].set_title(
        f'{pid}\n정상:{n_count} 주의:{w_count} 위험:{d_count}',
        fontsize=8
    )
    axes[i].set_xlabel('날짜', fontsize=7)
    axes[i].set_ylabel('총초여과량 (g)', fontsize=7)
    axes[i].legend(fontsize=6)
    axes[i].tick_params(axis='x', rotation=45, labelsize=6)
    axes[i].tick_params(axis='y', labelsize=7)

plt.suptitle(
    '환자별 총초여과량 시계열 이상치 탐지\n(파란선=정상 / 주황점=주의 / 빨간점=위험)',
    fontsize=13
)
plt.tight_layout()
plt.savefig('../figures/anomaly_timeseries.png', dpi=150, bbox_inches='tight')
plt.close()

print("시각화 저장 완료: figures/anomaly_timeseries.png")