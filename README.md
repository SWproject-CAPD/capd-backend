# capd-backend
Spring Boot와 FastAPI를 기반으로 복막투석(CAPD) 환자를 관리하는 서비스
- 의사에겐 환자 데이터 관리뿐만 아니라 AI 기반 이상치 탐지, RAG 기반 의료 챗봇, AI 설문 생성, 주간 보고서 생성 기능을 제공
- 환자에겐 투석기록 제출, RAG 기반 챗봇, 설문 답변, 예약 날짜 조회 기능 제공

## 프로젝트 소개

CAPD 환자의 투석 데이터를 효율적으로 관리하고 의료진의 업무를 지원하기 위해 개발한 프로젝트입니다.

백엔드는 Spring Boot와 FastAPI를 분리하여 구축하였으며,

- REST API 기반 환자 관리
- AI 기반 이상치 탐지
- RAG 기반 의료 챗봇
- AI 설문 생성
- PDF 보고서 생성
- Docker 및 GitHub Actions 기반 자동 배포까지 실제 서비스 운영을 고려하여 개발했습니다.

---

## 기술 스택

### Backend

- Java
- Spring Boot
- Spring Data JPA
- Spring Security

### AI Server

- Python
- FastAPI
- scikit-learn (Isolation Forest)
- Gemini API

### Database

- MySQL
- Redis
- ChromaDB

### Infrastructure

- Docker
- GitHub Actions
- GCP
- Nginx

---

## ERD
<img width="4240" height="1532" alt="복막투석ERD" src="https://github.com/user-attachments/assets/1c097f1a-d869-4104-84d7-9eda1889121d" />

## 시스템 구성도
<img width="13627" height="8440" alt="시스템 구성도" src="https://github.com/user-attachments/assets/8811051a-f432-41ea-a9f6-f1c8e4be0b50" />


## 🎯 Git Convention

- 🎉 **Start:** Start New Project [:tada:]
- ✨ **Feat:** 새로운 기능을 추가 [:sparkles:]
- 🐛 **Fix:** 버그 수정 [:bug:]
- 🎨 **Design:** CSS 등 사용자 UI 디자인 변경 [:art:]
- ♻️ **Refactor:** 코드 리팩토링 [:recycle:]
- 🔧 **Settings:** Changing configuration files [:wrench:]
- 🗃️ **Comment:** 필요한 주석 추가 및 변경 [:card_file_box:]
- ➕ **Dependency/Plugin:** Add a dependency/plugin [:heavy_plus_sign:]
- 📝 **Docs:** 문서 수정 [:memo:]
- 🔀 **Merge:** Merge branches [:twisted_rightwards_arrows:]
- 🚀 **Deploy:** Deploying stuff [:rocket:]
- 🚚 **Rename:** 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 [:truck:]
- 🔥 **Remove:** 파일을 삭제하는 작업만 수행한 경우 [:fire:]
- ⏪️ **Revert:** 전 버전으로 롤백 [:rewind:]

**🪴 Branch Convention (GitHub Flow)**

- `main`: 배포 가능한 브랜치, 항상 배포 가능한 상태를 유지
- `feature/{description}`: 새로운 기능을 개발하는 브랜치
    - 예: `feature/social-login`
- 브랜치 공유 X → 특수한 경우 팀원들에게 알리기
- 팀원이 짠 코드 리뷰 없이 수정 X → 수정 시 PR 남기고 리뷰 필수
    - ex) 본인이 정의한 클래스 아닌 곳에서 코드 작성 시 팀원들과 논의

**Flow**

1. `develop` 브랜치에서 새로운 브랜치를 생성. → `develop` 브랜치는 `main` 브랜치에서 생성
2. 작업을 완료하고 커밋 메시지에 맞게 커밋 후 푸시.
3. `develop` 으로 병합 시 Pull Request를 생성 / 팀원들의 리뷰.
4. 리뷰가 완료되면 `develop` 브랜치로 병합.
5. 병합 후, 배포 필요 시 `main` 브랜치로 Pull Request를 생성 / 팀원들의 리뷰 진행
6. 병합 후 배포.

**예시**:

```bash
# 새로운 기능 개발
git checkout -b feature/social-login

# 작업 완료 후, main 브랜치로 병합
git checkout main
git pull origin main
git merge feature/social-login
git push origin main
```
