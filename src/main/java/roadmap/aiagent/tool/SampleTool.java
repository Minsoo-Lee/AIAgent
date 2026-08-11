package roadmap.aiagent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SampleTool {

    @Tool(description = "두 숫자를 더합니다.")
    public int add(int a, int b) {
        return a + b;
    }

    @Tool(description = "현재 시간을 반환합니다.")
    public String getCurrentTime() {
        return LocalDateTime.now().toString();
    }
}
