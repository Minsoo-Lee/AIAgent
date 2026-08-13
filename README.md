# Spring AI 학습 도우미 Agent

Spring AI의 Tool Calling과 MCP(Model Context Protocol)를 활용한 AI 학습 도우미 Agent 프로젝트입니다.
질문에 따라 공식 문서 검색, GitHub 예제 코드 검색, 웹 검색을 스스로 판단해서 호출합니다.

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.0 |
| AI Framework | Spring AI 2.0.0 |
| Chat Model | Gemini 3.5 Flash Lite (Google GenAI SDK) |
| Embedding Model | Gemini Embedding 001 (Google GenAI) |
| Vector Store | PostgreSQL + pgvector (ragPipeline 공유) |
| MCP | Tavily(웹 검색), GitHub(저장소 검색), Filesystem(로컬 stdio) |
| Build | Gradle 8.14+ |

## 주요 기능

- **Tool Calling**: LLM이 질문에 따라 스스로 Tool을 선택해서 호출
- **RAG 검색 Tool**: ragPipeline의 vector_store에서 Spring AI 공식 문서 검색
- **시스템 프롬프트**: LLM에게 Spring AI 학습 도우미 역할 고정
- **멀티턴 컨텍스트**: MessageChatMemoryAdvisor로 이전 대화 기억
- **MCP 연동**: 표준화된 프로토콜로 외부 서버에 연결
    - **filesystem MCP**: 로컬 stdio 프로세스로 실행되는 파일시스템 접근 서버
    - **Tavily MCP**: 원격 Streamable-HTTP 서버로 실시간 웹 검색
    - **GitHub MCP**: 원격 공식 서버로 저장소 검색 (Week 5의 RestTemplate 기반 GithubSearchTool을 MCP로 교체)
- **Tool 스키마 정제**: `SanitizedToolCallBack`으로 Google GenAI SDK가 파싱하지 못하는 MCP 스키마(`default: null`) 우회
- **Tool 필터링**: `McpToolFilter`로 GitHub MCP의 수십 개 Tool 중 `search_repositories`만 선택적으로 노출

## 프로젝트 구조

```
src/main/java/roadmap/aiagent/
├── controller/
│   └── AgentController.java       # Agent API (/agent/v1~v8)
├── tool/
│   ├── SampleTool.java             # 기본 테스트용 Tool (add, getCurrentTime)
│   ├── RagSearchTool.java          # Spring AI 공식 문서 검색 Tool
│   └── SanitizedToolCallBack.java  # MCP Tool 스키마 정제 Wrapper
├── config/
│   └── McpGithubAuthFilter.java    # GitHub MCP Bearer 토큰 인증 필터
└── filter/
    └── GithubToolFilter.java       # GitHub MCP Tool 필터링
```

## 사전 요구사항

> **ragPipeline 프로젝트가 먼저 실행되어야 합니다.**
> ragPipeline의 Docker(postgres, redis)를 올리고 Spring Boot를 실행해서 문서를 vector_store에 저장해야 해요.

- Node.js (filesystem MCP 로컬 실행용, `npx` 필요)
- Tavily API 키
- GitHub Personal Access Token (Fine-grained, Public repositories read-only)

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
GITHUB_TOKEN=your_github_pat_here
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
| GET | `/agent/v6?question=질문` | filesystem MCP Tool (로컬 stdio) |
| GET | `/agent/v7?question=질문` | Tavily MCP Tool (웹 검색) |
| GET | `/agent/v8?question=질문` | GitHub MCP Tool (저장소 검색, Tool 필터링 적용) |

## 주의사항

- `aiAgent`의 `ddl-auto: none`으로 설정해야 ragPipeline의 vector_store 데이터가 유지됩니다.
- ragPipeline Docker 실행 시 postgres, redis만 올리세요. (`docker-compose up -d postgres redis`)
- ragPipeline Spring Boot 실행 후 aiAgent를 실행해야 문서 검색이 정상 동작합니다.
- Google GenAI 설정에서 `project-id`를 넣으면 Vertex AI 모드로 전환되어 API 키 방식이 거부됩니다. API 키 방식만 쓸 경우 `project-id`, `location`을 넣지 마세요.
- Chat과 Embedding은 각각 별도의 `api-key` 설정이 필요합니다.
- ragPipeline도 aiAgent와 동일하게 Spring AI 2.0.0 + Spring Boot 4.0.0으로 업그레이드되어 있어야 vector_store 스키마 호환성이 유지됩니다.