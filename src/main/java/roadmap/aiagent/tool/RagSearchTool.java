package roadmap.aiagent.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagSearchTool {

    private final VectorStore vectorStore;

    @Tool(description = "Spring AI 공식 문서에서 관련 내용을 검색합니다.")
    public String searchDocument(String query) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .similarityThreshold(0.5)
                        .build()
        );
        log.info("🔍 RAG 검색 결과: {}개", results.size());
        return results.stream()
                .map(doc -> doc.getText())
                .reduce("", (a, b) -> a + "\n\n" + b);
    }
}
