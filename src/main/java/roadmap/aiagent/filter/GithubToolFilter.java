package roadmap.aiagent.filter;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.McpConnectionInfo;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.stereotype.Component;

@Component
public class GithubToolFilter implements McpToolFilter {

    @Override
    public boolean test(McpConnectionInfo connectionInfo, McpSchema.Tool tool) {
        // github 연결에서만 search_repositories Tool만 허용
        String clientName = connectionInfo.clientInfo().name();
        if (clientName != null && clientName.contains("github")) {
            return tool.name().equals("search_repositories");
        }
        // 다른 연결(tavily, filesystem)은 전부 허용
        return true;
    }
}
