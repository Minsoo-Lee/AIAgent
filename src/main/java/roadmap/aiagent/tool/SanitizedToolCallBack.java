package roadmap.aiagent.tool;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

public class SanitizedToolCallBack implements ToolCallback {

    private final ToolCallback delegate;
    private final ToolDefinition sanitizedDefinition;

    public SanitizedToolCallBack(ToolCallback delegate) {
        this.delegate = delegate;
        String cleanedSchema = sanitizeSchema(delegate.getToolDefinition().inputSchema());
        this.sanitizedDefinition = ToolDefinition.builder()
                .name(delegate.getToolDefinition().name())
                .description(delegate.getToolDefinition().description())
                .inputSchema(cleanedSchema)
                .build();
    }

    private String sanitizeSchema(String schema) {
        // "default": null wprj
        return schema.replace(",\"default\":null", "")
                .replace("\"default\":null,", "");
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return sanitizedDefinition;
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return this.delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        return this.delegate.call(toolInput);
    }

    @Override
    public String call(String toolInput, @Nullable ToolContext toolContext) {
        return delegate.call(toolInput, toolContext);
    }
}
