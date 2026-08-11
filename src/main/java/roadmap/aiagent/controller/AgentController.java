package roadmap.aiagent.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roadmap.aiagent.tool.SampleTool;

@RestController
@RequiredArgsConstructor
public class AgentController {

    private final ChatClient.Builder chatClientBuilder;
    private final SampleTool sampleTool;

    @GetMapping("/agent")
    public String agent(@RequestParam String question) {
        return chatClientBuilder.build()
                .prompt()
                .user(question)
                .tools(sampleTool)
                .call()
                .content();
    }
}
