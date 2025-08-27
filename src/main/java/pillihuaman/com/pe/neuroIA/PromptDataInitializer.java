package pillihuaman.com.pe.neuroIA;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pillihuaman.com.pe.lib.common.AuditEntity;
import pillihuaman.com.pe.neuroIA.repository.promt.PromptTemplate;
import pillihuaman.com.pe.neuroIA.repository.promt.dao.PromptTemplateDAO;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Initializes default prompt templates in the database on application startup.
 * This process is idempotent; it will only create templates if they do not already exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptDataInitializer {

    private final PromptTemplateDAO promptTemplateDAO;

    /**
     * This method is executed automatically by Spring after the bean is constructed.
     * It orchestrates the creation of all necessary prompt templates.
     */
    @PostConstruct
    public void initializePrompts() {
        initializeGlobalSearchPrompt();
    }

    private void initializeGlobalSearchPrompt() {
        final String promptId = "GLOBAL_SEARCH_INTENT_V1";

        Optional<PromptTemplate> existing = promptTemplateDAO.findByPromptId(promptId);

        if (existing.isEmpty()) {
            log.info("✔️ Prompt template '{}' not found. Creating it...", promptId);
            createAndSaveGlobalSearchPrompt(promptId);
        } else {
            log.info("✔️ Prompt template '{}' already exists in the database.", promptId);
        }
    }

    private void createAndSaveGlobalSearchPrompt(String promptId) {
        PromptTemplate newPrompt = new PromptTemplate();

        newPrompt.setPromptId(promptId);
        newPrompt.setVersion(1);
        newPrompt.setDescription("Main prompt template to analyze search intent and generate a query plan.");
        newPrompt.setStatus("ACTIVE");

        newPrompt.setSystemPromptTemplate(
                """
                You are an advanced AI assistant for the "AlamodaPeru.online" e-commerce platform. Your only task is to analyze the user's query and return a single, valid JSON object. Do not provide any explanations or conversational text.
                
                The JSON structure you must return is:
                {{json_structure}}
        
                Here are key examples to guide your response:
                {{examples}}
                
                Now, analyze the following user query and return ONLY the JSON object.
                """
        );

        newPrompt.setParameters(buildPromptParameters());

        AuditEntity audit = new AuditEntity();
        audit.setMail("system_initializer");
        audit.setDateRegister(new Date());
        newPrompt.setAudit(audit);

        try {
            promptTemplateDAO.save(newPrompt);
            log.info("✅ Prompt template '{}' created and saved successfully.", promptId);
        } catch (Exception e) {
            log.error("❌ Failed to save prompt template '{}'. Error: {}", promptId, e.getMessage(), e);
        }
    }

    /**
     * THIS IS THE CORRECTED AND COMPLETE METHOD.
     * It contains the full instructions for the AI.
     */
    private Map<String, Object> buildPromptParameters() {

        // 1. Define the complete JSON structure the AI must follow.
        Map<String, Object> jsonStructure = Map.of(
                "primaryIntent", "FIND_PRODUCT | FIND_CLIENT | FIND_ORDER | NAVIGATE_TO | CREATE_ENTITY | UNKNOWN",
                "confidenceScore", "float (0.0 to 1.0)",
                "primaryEntity", "product | client | order | page",
                "entityIdentifier", "string | null (e.g., an order number)",
                "actionVerb", "find | register | view | create",
                "refinedProductQuery", "string | null (a cleaned-up version of the user query for text search)",
                "suggestedActions", List.of(
                        Map.of(
                                "type", "API_CALL | INTERNAL_LINK",
                                "target", "string (URL or service endpoint)",
                                "displayText", "string (user-facing text)",
                                "parameters", Map.of("key", "value")
                        )
                ),
                // We explicitly define the structure of the dynamicQueryPlan
                "dynamicQueryPlan", Map.of(
                        "collection", "string | null (e.g., 'product', 'client')",
                        "filter", "object | null (MongoDB filter document)",
                        "projection", "object | null (MongoDB projection document)",
                        "sort", "object | null (MongoDB sort document)"
                )
        );

        // 2. Provide high-quality examples that show the AI EXACTLY what to do.
        List<Map<String, String>> examples = List.of(
                // Example 1: Navigation (NO dynamicQueryPlan)
                Map.of(
                        "query", "register a new client",
                        "expected", """
                {"primaryIntent":"CREATE_ENTITY","primaryEntity":"client","actionVerb":"register","suggestedActions":[{"type":"INTERNAL_LINK","target":"/clients/new","displayText":"Register a New Client"}],"dynamicQueryPlan":null}
                """
                ),

                // Example 2: The query from your debugger ("casaca")
                Map.of(
                        "query", "casaca",
                        "expected", """
                {"primaryIntent":"FIND_PRODUCT","confidenceScore":0.95,"primaryEntity":"producto","actionVerb":"buscar","refinedProductQuery":"casaca","suggestedActions":[{"type":"API_CALL","target":"support-app/product","parameters":{"q":"casaca"},"displayText":"Search for casaca"}],"dynamicQueryPlan":{"collection":"product","filter":{"$text":{"$search":"casaca"}},"projection":{"name":1,"brand":1,"pricing":1,"media":1,"tags":1}}}
                """
                ),

                // Example 3: Complex Product Search (This is the key example)
                Map.of(
                        "query", "blue cotton shirts sorted by price descending",
                        "expected", """
                {"primaryIntent":"FIND_PRODUCT","primaryEntity":"shirt","actionVerb":"find","refinedProductQuery":"blue cotton shirts","dynamicQueryPlan":{"collection":"product","filter":{"tags":{"$in":["shirt","cotton"]},"specifications.attributes.value":"blue"},"projection":{"name":1,"pricing":1,"media.thumbnailUrl":1},"sort":{"pricing.sellingPrice":-1}}}
                """
                ),

                // Example 4: Client Search
                Map.of(
                        "query", "search for client John Doe",
                        "expected", """
                {"primaryIntent":"FIND_CLIENT","primaryEntity":"client","actionVerb":"search","refinedProductQuery":"John Doe","dynamicQueryPlan":{"collection":"client","filter":{"$text":{"$search":"\\"John Doe\\""}}}}
                """
                )
        );

        // 3. Return the final parameters map.
        return Map.of(
                "json_structure", jsonStructure,
                "examples", examples
        );
    }
}