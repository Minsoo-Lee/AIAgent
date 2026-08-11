package roadmap.aiagent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GithubSearchTool {

    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(description = "GitHub에서 Spring AI 관련 에제 코드를 검색합니다.")
    public String searchGithub(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        headers.set("User-Agent", "SpringAI-Agent");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = "https://api.github.com/search/repositories?q="
                + query + "+spring-ai&sort=stars&order=desc&per_page=3";

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, request, String.class);

        log.info("🔍 GitHub 검색 완료");

        return response.getBody();
    }
}
