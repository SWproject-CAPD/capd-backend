import os
import sys
import time
from dotenv import load_dotenv
import chromadb
from pypdf import PdfReader
import requests

# 환경변수 로드
load_dotenv(os.path.join(os.path.dirname(__file__), '..', '.env'))

GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')

if not GEMINI_API_KEY:
    print("GEMINI_API_KEY가 없어요. .env 파일을 확인해주세요.")
    sys.exit(1)

# 설정
PDF_DIR = os.path.join(os.path.dirname(__file__), '..', 'data', 'kdigo')
CHROMA_DIR = os.path.join(os.path.dirname(__file__), '..', 'chromadb')
CHUNK_SIZE = 500
CHUNK_OVERLAP = 50

# Gemini 임베딩 함수
def get_embedding(text):
    """Gemini API로 텍스트 임베딩 생성"""
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key={GEMINI_API_KEY}"
    
    payload = {
        "model": "models/gemini-embedding-001",
        "content": {
            "parts": [{"text": text}]
        }
    }
    
    response = requests.post(url, json=payload)
    
    if response.status_code == 200:
        return response.json()['embedding']['values']
    else:
        raise Exception(f"임베딩 생성 실패: {response.text}")

# PDF 텍스트 추출
def extract_text_from_pdf(pdf_path):
    reader = PdfReader(pdf_path)
    text = ""
    for page in reader.pages:
        extracted = page.extract_text()
        if extracted:
            text += extracted + "\n"
    return text

# 텍스트 청크 분할
def split_into_chunks(text, chunk_size=CHUNK_SIZE, overlap=CHUNK_OVERLAP):
    chunks = []
    start = 0
    while start < len(text):
        end = start + chunk_size
        chunk = text[start:end]
        if chunk.strip():
            chunks.append(chunk)
        start = end - overlap
    return chunks

# ChromaDB 저장
def save_to_chromadb(chunks, source_name, collection):
    existing = collection.get()
    existing_ids = set(existing['ids'])

    documents = []
    metadatas = []
    ids = []
    embeddings = []

    for i, chunk in enumerate(chunks):
        chunk_id = f"{source_name}_{i}"

        if chunk_id in existing_ids:
            continue

        print(f"    임베딩 생성 중: {i+1}/{len(chunks)}", end='\r')

        # 임베딩 생성
        embedding = get_embedding(chunk)
        time.sleep(0.1)  # API 호출 제한 방지

        documents.append(chunk)
        metadatas.append({"source": source_name, "chunk_index": i})
        ids.append(chunk_id)
        embeddings.append(embedding)

    if documents:
        collection.add(
            documents=documents,
            metadatas=metadatas,
            ids=ids,
            embeddings=embeddings
        )
        print(f"\n  → {len(documents)}개 청크 저장 완료")
    else:
        print(f"  → 이미 저장된 데이터, 스킵")

def main():
    print("=" * 50)
    print("KDIGO PDF 전처리 시작")
    print("=" * 50)

    # ChromaDB 클라이언트 생성
    client = chromadb.PersistentClient(path=CHROMA_DIR)

    # 컬렉션 생성 (임베딩 함수 없이 직접 임베딩 저장)
    collection = client.get_or_create_collection(
        name="kdigo_guidelines"
    )

    print(f"ChromaDB 컬렉션 준비 완료")

    pdf_files = [f for f in os.listdir(PDF_DIR) if f.endswith('.pdf')]

    if not pdf_files:
        print(f"PDF 파일이 없어요. {PDF_DIR} 폴더를 확인해주세요.")
        return

    print(f"\n총 {len(pdf_files)}개 PDF 파일 발견")

    total_chunks = 0

    for pdf_file in pdf_files:
        pdf_path = os.path.join(PDF_DIR, pdf_file)
        print(f"\n처리 중: {pdf_file}")

        text = extract_text_from_pdf(pdf_path)
        print(f"  텍스트 추출 완료: {len(text)}자")

        if not text.strip():
            print(f"  텍스트 없음, 스킵")
            continue

        chunks = split_into_chunks(text)
        print(f"  청크 분할 완료: {len(chunks)}개")
        total_chunks += len(chunks)

        source_name = pdf_file.replace('.pdf', '')
        save_to_chromadb(chunks, source_name, collection)

    print(f"\n" + "=" * 50)
    print(f"전처리 완료!")
    print(f"총 청크 수: {total_chunks}개")
    print(f"ChromaDB 저장된 총 문서 수: {collection.count()}개")
    print("=" * 50)

if __name__ == "__main__":
    main()