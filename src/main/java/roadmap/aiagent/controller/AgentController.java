package roadmap.aiagent.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roadmap.aiagent.tool.GithubSearchTool;
import roadmap.aiagent.tool.RagSearchTool;
import roadmap.aiagent.tool.SampleTool;

@RestController
@RequiredArgsConstructor
public class AgentController {

    private final ChatClient.Builder chatClientBuilder;
    private final SampleTool sampleTool;
    private final RagSearchTool ragSearchTool;
    private final GithubSearchTool githubSearchTool;

    @GetMapping("/agent/v1")
    public String agentV1(@RequestParam String question) {
        return chatClientBuilder.build()
                .prompt()
                .user(question)
                .tools(sampleTool)
                .call()
                .content();
    }

    @GetMapping("/agent/v2")
    public String agentV2(@RequestParam String question) {
        return chatClientBuilder.build()
                .prompt()
                .user(question)
                .tools(sampleTool, ragSearchTool)
                .call()
                .content();
    }

    @GetMapping("/agent/v3")
    public String agentV3(@RequestParam String question) {
        return chatClientBuilder.build()
                .prompt()
                .user(question)
                .tools(sampleTool, ragSearchTool, githubSearchTool)
                .call()
                .content();
    }

    @GetMapping("/agent/v4")
    public String agentV4(@RequestParam String question) {
        return chatClientBuilder.build()
                .prompt()
                .system("""
                    당신은 Spring AI 학습 도우미입니다.
                    다음 규칙을 따르세요:
                    1. Spring AI 개념이나 기능에 대한 질문 → searchDocument Tool 사용
                    2. 예제 코드나 구현 방법에 대한 질문 → searchGithub Tool 사용
                    3. 두 가지 모두 필요하면 둘 다 사용
                    4. 반드시 한국어로 답변하세요.
                    """)
                .user(question)
                .tools(sampleTool, ragSearchTool, githubSearchTool)
                .call()
                .content();
    }
}
