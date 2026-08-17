package roadmap.aiagent.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roadmap.aiagent.tool.GithubSearchTool;
import roadmap.aiagent.tool.RagSearchTool;
import roadmap.aiagent.tool.SampleTool;
import roadmap.aiagent.tool.SanitizedToolCallBack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(produces = "application/json;charset=UTF-8")
public class AgentController {

    private final ChatClient.Builder chatClientBuilder;
    private final SampleTool sampleTool;
    private final RagSearchTool ragSearchTool;
    private final GithubSearchTool githubSearchTool;
    private final ChatMemory chatMemory;
    private final ToolCallbackProvider toolCallbackProvider;

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
        var chatResponse = chatClientBuilder.build()
                .prompt()
                .user(question)
                .tools(sampleTool, ragSearchTool, githubSearchTool)
                .call()
                .chatResponse();
        log.info("=== chatResponse: {} ===", chatResponse);
        return chatResponse.getResult().getOutput().getText();
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

    @GetMapping("/agent/v5")
    public String agentV5(@RequestParam String question,
                          @RequestParam String conversationId) {
        return chatClientBuilder.build()
                .prompt()
                .system("""
                        당신은 Spring AI 학습 도우미입니다.
                        다음 규칙을 따르세요:
                        1. Spring AI 개념이나 기능에 대한 질문 -> searchDocument Tool 사용
                        2. 예제 코드나 구현 방법에 대한 질문 -> searchGithubTool 사용
                        3. 두 가지 모두 필요하면 둘 다 사용
                        4. 반드시 한국어로 답변하세요
                        """)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(question)
                .tools(sampleTool, ragSearchTool, githubSearchTool)
                .call()
                .content();
    }

    @GetMapping("/agent/v6")
    public String agentV6(@RequestParam String question) {
        List<ToolCallback> sanitizedTools = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(SanitizedToolCallBack::new)
                .map(t -> (ToolCallback) t)
                .toList();

        return chatClientBuilder.build()
                .prompt()
                .system("""
                        당신은 Spring AI 학습 도우미입니다.
                        반드시 한국어로 답변하세요.
                        """)
                .user(question)
                .tools(sanitizedTools)
                .call()
                .content();
    }

    @GetMapping("/agent/v7")
    public String agentV7(@RequestParam String question) {
        List<ToolCallback> sanitizedTools =
                Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(SanitizedToolCallBack::new)
                .map(t -> (ToolCallback) t)
                .toList();

        return chatClientBuilder.build()
                .prompt()
                .system("""
                        당신은 Spring AI 학습 도우미입니다.
                        다음 규칙을 따르세요:
                        1. Spring AI 개념이나 기능에 대한 질문 → searchDocument Tool 사용
                        2. 예제 코드나 구현 방법에 대한 질문 → searchGithub Tool 사용
                        3. 최신 정보나 실시간 정보가 필요한 질문 → Tavily 웹 검색 Tool 사용
                        4. 반드시 한국어로 답변하세요.
                        """)
                .user(question)
                .tools(sampleTool, ragSearchTool, githubSearchTool)
                .tools(sanitizedTools)
                .call()
                .content();
    }

    @GetMapping("/agent/v8")
    public String agentV8(@RequestParam String question) {
        List<ToolCallback> sanitizedTools = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(SanitizedToolCallBack::new)
                .map(t -> (ToolCallback) t)
                .toList();

        return chatClientBuilder.build()
                .prompt()
                .system("""
                        당신은 Spring AI 학습 도우미입니다.
                        다음 규칙을 따르세요:
                        1. Spring AI 개념이나 기능에 대한 질문 → searchDocument Tool 사용
                        2. 예제 코드나 구현 방법에 대한 질문 → GitHub 검색 Tool 사용
                        3. 최신 정보나 실시간 정보가 필요한 질문 → Tavily 웹 검색 Tool 사용
                        4. 반드시 한국어로 답변하세요.
                        """)
                .user(question)
                .tools(sampleTool, ragSearchTool)
                .tools(sanitizedTools)
                .call()
                .content();
    }

    @GetMapping("/agent/v9")
    public String agentV9(@RequestParam String question) {
        List<ToolCallback> sanitizedTools = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(SanitizedToolCallBack::new)
                .map(t -> (ToolCallback) t)
                .toList();

        AtomicInteger iterationCount = new AtomicInteger(0);
        int maxIterations = 5;

        ToolCallingAdvisor toolCallingAdvisor = ToolCallingAdvisor.builder()
                .toolExecutionEligibilityChecker(response -> {
                    boolean hasToolCalls = response != null && response.hasToolCalls();
                    if (hasToolCalls && iterationCount.incrementAndGet() > maxIterations) {
                        log.warn("⚠️ 최대 반복 횟수({}) 초과, 루프 중단", maxIterations);
                        return false;
                    }
                    return hasToolCalls;
                })
                .build();

        return chatClientBuilder.build()
                .prompt()
                .system("""
                        당신은 Spring AI 학습 도우미입니다.
                        다음 규칙을 따르세요:
                        1. Spring AI 개념이나 기능에 대한 질문 → searchDocument Tool 사용
                        2. 예제 코드나 구현 방법에 대한 질문 → GitHub 검색 Tool 사용
                        3. 최신 정보나 실시간 정보가 필요한 질문 → Tavily 웹 검색 Tool 사용
                        4. 반드시 한국어로 답변하세요.
                        """)
                .advisors(toolCallingAdvisor)
                .user(question)
                .tools(sampleTool, ragSearchTool)
                .tools(sanitizedTools)
                .call()
                .content();
    }
}
