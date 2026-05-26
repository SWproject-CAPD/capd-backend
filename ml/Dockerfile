FROM python:3.9-slim

WORKDIR /app

COPY . .

RUN pip install fastapi uvicorn joblib numpy pandas scikit-learn chromadb requests python-dotenv google-generativeai pypdf

EXPOSE 8000

CMD ["uvicorn", "api_server:app", "--host", "0.0.0.0", "--port", "8000", "--app-dir", "scripts"]