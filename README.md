# Spring AI 학습 도우미 Agent

Spring AI와 Tool Calling을 활용한 AI 학습 도우미 Agent 프로젝트입니다.
질문에 따라 공식 문서 검색, GitHub 예제 코드 검색을 스스로 판단해서 호출합니다.

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| AI Framework | Spring AI 1.1.2 |
| Chat Model | Gemini 2.5 Flash Lite (OpenAI 호환 엔드포인트) |
| Embedding Model | Gemini Embedding 001 (Google GenAI) |
| Vector Store | PostgreSQL + pgvector (ragPipeline 공유) |
| Build | Gradle |

## 주요 기능

- **Tool Calling**: LLM이 질문에 따라 스스로 Tool을 선택해서 호출
- **RAG 검색 Tool**: ragPipeline의 vector_store에서 Spring AI 공식 문서 검색
- **GitHub 검색 Tool**: GitHub API로 Spring AI 예제 코드 검색
- **시스템 프롬프트**: LLM에게 Spring AI 학습 도우미 역할 고정
- **멀티턴 컨텍스트**: MessageChatMemoryAdvisor로 이전 대화 기억

## 프로젝트 구조

```
src/main/java/roadmap/aiagent/
├── controller/
│   └── AgentController.java   # Agent API (/agent/v1~v5)
└── tool/
    ├── SampleTool.java         # 기본 테스트용 Tool (add, getCurrentTime)
    ├── RagSearchTool.java      # Spring AI 공식 문서 검색 Tool
    └── GithubSearchTool.java   # GitHub 예제 코드 검색 Tool
```

## 사전 요구사항

> **ragPipeline 프로젝트가 먼저 실행되어야 합니다.**
> ragPipeline의 Docker(postgres, redis)를 올리고 Spring Boot를 실행해서 문서를 vector_store에 저장해야 해요.

## 실행 방법

### 1. ragPipeline Docker 실행

```bash
cd ~/coding/springAI-projects/ragPipeline
docker-compose up -d postgres redis
```

### 2. ragPipeline Spring Boot 실행

IntelliJ에서 ragPipeline 실행 → 콘솔에 `✅ 문서 임베딩 저장 완료!` 확인

### 3. 환경변수 설정

`.env` 파일 생성:

```
GEMINI_API_KEY=your_gemini_api_key_here
DB_USERNAME=postgres
DB_PASSWORD=postgres
TAVILY_API_KEY=your_tavily_api_key_here
```

### 4. aiAgent Spring Boot 실행

```bash
./gradlew bootRun
```

포트: `8081`

## API 엔드포인트

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| GET | `/agent/v1?question=질문` | SampleTool만 사용 |
| GET | `/agent/v2?question=질문` | SampleTool + RagSearchTool |
| GET | `/agent/v3?question=질문` | SampleTool + RagSearchTool + GithubSearchTool |
| GET | `/agent/v4?question=질문` | 시스템 프롬프트 + 모든 Tool |
| GET | `/agent/v5?question=질문&conversationId=ID` | 멀티턴 컨텍스트 + 모든 Tool |

## 주의사항

- `aiAgent`의 `ddl-auto: none`으로 설정해야 ragPipeline의 vector_store 데이터가 유지됩니다.
- ragPipeline Docker 실행 시 postgres, redis만 올리세요. (`docker-compose up -d postgres redis`)
- ragPipeline Spring Boot 실행 후 aiAgent를 실행해야 문서 검색이 정상 동작합니다.