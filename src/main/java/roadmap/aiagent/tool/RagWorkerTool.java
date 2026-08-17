package roadmap.aiagent.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagWorkerTool {

    private final ChatClient.Builder chatClientBuilder;
    private final RagSearchTool ragSearchTool;

    @Tool(description = "Spring AI 공식 문서 검색이 필요한 하위 작업을 RAG 전문 Worker에게 위임합니다.")
    public String askRagWorker(String subTask) {
        log.info("👷 RAG Worker 호출: {}", subTask);
        return chatClientBuilder.build()
                .prompt()
                .system("""
                        당신은 RAG(문서 검색) 전문 Worker 입니다.
                        오직 searchDocument Tool만 사용해서 답하세요.
                        """)
                .user(subTask)
                .tools(ragSearchTool)
                .call()
                .content();
    }
}
