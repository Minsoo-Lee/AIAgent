package roadmap.aiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import roadmap.aiagent.filter.McpGithubAuthFilter;

@SpringBootApplication
public class AiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }

    @Bean
    WebClient.Builder webClientBuilder(McpGithubAuthFilter filter) {
        return WebClient.builder().filter(filter);
    }
}
