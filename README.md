# Spring AI 학습 도우미 Agent

Spring AI의 Tool Calling, MCP(Model Context Protocol), Agent 패턴을 활용한 AI 학습 도우미 프로젝트입니다.
질문에 따라 공식 문서 검색, GitHub 예제 검색, 웹 검색을 스스로 판단해서 호출하고, 필요시 여러 Tool을 반복적으로 조합해 답을 찾습니다.

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.0 |
| AI Framework | Spring AI 2.0.0 |
| Chat Model | Gemini 2.5 Flash Lite (Google GenAI SDK) |
| Embedding Model | Gemini Embedding 001 (Google GenAI) |
| Vector Store | PostgreSQL + pgvector (ragPipeline과 공유) |
| Security | Spring Security + JWT |
| MCP | Tavily(웹 검색), GitHub(저장소 검색), Filesystem(로컬 stdio) |
| API Docs | Swagger UI (springdoc-openapi 3.0.3) |
| Infra | Docker Compose |
| Test | JUnit 5, MockMvc, AssertJ |

## 주요 기능

### Tool Calling
- LLM이 질문에 따라 스스로 Tool을 선택해서 호출
- RAG 검색 Tool: ragPipeline의 vector_store에서 Spring AI 공식 문서 검색
- 시스템 프롬프트로 Spring AI 학습 도우미 역할 고정
- MessageChatMemoryAdvisor로 이전 대화 기억 (멀티턴)

### MCP 연동
- **filesystem MCP**: 로컬 stdio 프로세스로 실행되는 파일시스템 접근 서버
- **Tavily MCP**: 원격 Streamable-HTTP 서버로 실시간 웹 검색
- **GitHub MCP**: 원격 공식 서버로 저장소 검색 (ExchangeFilterFunction으로 Bearer 인증, McpToolFilter로 필요한 Tool만 노출)
- SanitizedToolCallBack으로 Google GenAI SDK가 파싱하지 못하는 MCP 스키마(default: null) 우회

### AI Agent
- toolExecutionEligibilityChecker + AtomicInteger로 max_iterations(반복 횟수 제한) 안전장치 구현
- 각 반복마다 호출된 Tool을 로그로 관찰 (Observability)
- Orchestrator-Workers 패턴으로 멀티 Agent 구현 (RagWorkerTool, GithubWorkerTool을 별도 Tool로 감싸 Orchestrator가 위임)

### 프로덕션화
- JWT 인증 (ragPipeline과 동일 구조, 별도 JWT_SECRET 사용)
- Docker Compose로 ragPipeline과 함께 전체 스택 실행
- Swagger UI로 API 문서화
- HTML 테스트 페이지 (로그인 → 토큰 발급 → Agent 질문, 로딩 애니메이션, 마크다운 렌더링)
- 단위/통합 테스트 (JwtUtil, AuthController)

## 프로젝트 구조

```
src/main/java/roadmap/aiagent/
├── controller/
│   ├── AgentController.java        # Agent API (/agent/v1~v11)
│   └── AuthController.java         # JWT 토큰 발급 (/auth/login)
├── tool/
│   ├── SampleTool.java              # 기본 테스트용 Tool
│   ├── RagSearchTool.java           # Spring AI 공식 문서 검색 Tool
│   ├── RagWorkerTool.java           # RAG 전문 Worker (멀티 Agent용)
│   ├── GithubWorkerTool.java        # GitHub 전문 Worker (멀티 Agent용)
│   └── SanitizedToolCallBack.java   # MCP Tool 스키마 정제 Wrapper
├── config/
│   ├── SecurityConfig.java          # Spring Security 필터 체인
│   └── McpGithubAuthFilter.java     # GitHub MCP Bearer 토큰 인증 필터
├── filter/
│   ├── JwtFilter.java                # JWT 검증 필터
│   └── GithubToolFilter.java         # GitHub MCP Tool 필터링
└── util/
    └── JwtUtil.java                  # JWT 생성/검증

src/test/java/roadmap/aiagent/
├── util/JwtUtilTest.java            # JWT 단위 테스트
└── controller/AuthControllerTest.java # 로그인 통합 테스트

src/main/resources/static/
└── index.html                        # 브라우저 테스트 페이지
```

## 사전 요구사항

> **ragPipeline 프로젝트가 먼저 실행되어야 합니다.**
> ragPipeline의 Docker(postgres, redis)를 올리고 Spring Boot를 실행해서 문서를 vector_store에 저장해야 해요.

- Node.js (filesystem MCP 로컬 실행용, `npx` 필요 — Docker 이미지에도 설치되어 있음)
- Tavily API 키
- GitHub Personal Access Token (Fine-grained, Public repositories read-only)

## 실행 방법

### Docker Compose로 전체 스택 실행 (추천)

`ragPipeline`의 `docker-compose.yml`에 `aiagent` 서비스가 통합되어 있어 한 번에 실행됩니다.

```bash
cd ~/coding/springAI-projects/ragPipeline
docker-compose up --build -d
docker ps  # postgres-pgvector, redis, ragpipeline, aiagent 4개 컨테이너 확인
```

### 환경변수 설정

`aiAgent/.env`:

```
GEMINI_API_KEY=your_gemini_api_key_here
DB_USERNAME=postgres
DB_PASSWORD=postgres
TAVILY_API_KEY=your_tavily_api_key_here
GITHUB_TOKEN=your_github_pat_here
JWT_SECRET=your_jwt_secret_here
```

## API 엔드포인트

> 🔒 `/auth/login`, `/swagger-ui/**`, `/index.html`을 제외한 모든 엔드포인트는 JWT 토큰이 필요합니다.

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| POST | `/auth/login` | JWT 토큰 발급 |
| GET | `/agent/v1?question=질문` | SampleTool만 사용 |
| GET | `/agent/v2?question=질문` | SampleTool + RagSearchTool |
| GET | `/agent/v3?question=질문` | SampleTool + RagSearchTool + GithubSearchTool |
| GET | `/agent/v4?question=질문` | 시스템 프롬프트 + 모든 Tool |
| GET | `/agent/v5?question=질문&conversationId=ID` | 멀티턴 컨텍스트 |
| GET | `/agent/v6?question=질문` | filesystem MCP (로컬 stdio) |
| GET | `/agent/v7?question=질문` | Tavily MCP (웹 검색) |
| GET | `/agent/v8?question=질문` | GitHub MCP (Tool 필터링 적용) |
| GET | `/agent/v9?question=질문` | max_iterations 안전장치 |
| GET | `/agent/v10?question=질문` | Tool 호출 관찰 로그 |
| GET | `/agent/v11?question=질문` | 멀티 Agent (Orchestrator-Workers) |

## 브라우저 테스트

```
http://localhost:8081/index.html
```
로그인 → 토큰 발급 → v4/v10/v11 중 선택 → 질문 → 마크다운 답변 확인

```
http://localhost:8081/swagger-ui/index.html
```
전체 API 문서 확인

## 테스트

```bash
./gradlew test
```

- `JwtUtilTest`: 토큰 생성/추출/검증 단위 테스트
- `AuthControllerTest`: 로그인 성공/실패 통합 테스트 (MockMvc)

## 주의사항

- `aiAgent`의 `ddl-auto: none`으로 설정해야 ragPipeline의 vector_store 데이터가 유지됩니다.
- ragPipeline과 aiAgent는 별도의 `JWT_SECRET`을 사용합니다 (서비스 간 토큰 공유 방지).
- Google GenAI 설정에서 `project-id`를 넣으면 Vertex AI 모드로 전환되어 API 키 방식이 거부됩니다. API 키 방식만 쓸 경우 `project-id`, `location`을 넣지 마세요.
- Chat과 Embedding은 각각 별도의 `api-key` 설정이 필요합니다.
- ragPipeline도 동일하게 Spring AI 2.0.0 + Spring Boot 4.0.0으로 업그레이드되어 있어야 vector_store 스키마 호환성이 유지됩니다.
- 테스트 실행 시 `src/test/resources/application.yml`이 별도로 필요합니다 (`.env`는 IntelliJ 테스트 Run Configuration에 자동 적용되지 않음).