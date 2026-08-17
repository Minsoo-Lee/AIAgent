package roadmap.aiagent.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubWorkerTool {

    private final ChatClient.Builder chatClientBuilder;
    private final ToolCallbackProvider toolCallbackProvider;

    @Tool(description = "Github 예제 코드 검색이 필요한 하위 작업을 Github 전문 Worker에게 위임합니다.")
    public String askGithubWorker(String subTask) {
        log.info("👷 Github Worker 호출: {}", subTask);

        List<ToolCallback> githubTools = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(SanitizedToolCallBack::new)
                .map(t -> (ToolCallback) t)
                .filter(t -> t.getToolDefinition().name().equals("search_repositories"))
                .toList();

        return chatClientBuilder.build()
                .prompt()
                .system("""
                        당신은 Github 코드 검색 전문 Worker 입니다.
                        오직 search_repositories Tool만 사용해서 답하세요.
                        """)
                .user(subTask)
                .tools(githubTools)
                .call()
                .content();
    }
}
