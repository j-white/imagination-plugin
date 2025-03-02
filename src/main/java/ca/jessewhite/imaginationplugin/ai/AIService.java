package ca.jessewhite.imaginationplugin.ai;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Material;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AIService {

    BlockGenerator blockBuilder;

    public void init() {
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://172.23.254.196:11434/")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("deepseek-r1:1.5b")
                .timeout(Duration.ofSeconds(30))
                .build();

        var memory = MessageWindowChatMemory.withMaxMessages(10);
        var availableMaterials = Strings.join(Arrays.asList(Material.values()), ',');
        System.out.println("Available materials: " + availableMaterials);
        // Add system message to chat memory to provide knowledge of materials
        memory.add(new SystemMessage(
                "You are a Minecraft building assistant. You help create structures with blocks. " +
                   "Available Minecraft material types are: " + availableMaterials + ". " +
                   "Always use only valid material types from this list in your responses, and ALWAYS use UPPERCASE for material names (like STONE, not stone). " +
                   "IMPORTANT: Return ONLY valid JSON without any additional text, explanation, comments, or tags. " +
                   """
                    The expected JSON structure MUST follow this exact format:
                    [
                      {
                        "element_type": "area",
                        "material": "GRASS_BLOCK",
                        "range": {
                          "x": { "min": 10, "max": 20 },
                          "y": { "min": 0, "max": 0 },
                          "z": { "min": 10, "max": 20 }
                        }
                      },
                      {
                        "element_type": "block",
                        "material": "STONE",
                        "position": { "x": 15, "y": 1, "z": 15 }
                      }
                    ]
                    
                    CRITICAL REQUIREMENTS:
                    1. Return a direct ARRAY of elements, NOT an object containing an "elements" property.
                    2. Use snake_case for property names (element_type NOT elementType).
                    3. Material names must be UPPERCASE (STONE not stone).
                    4. You MAY use decimal values for coordinates (like 3.5) - they will be rounded to the nearest block.
                    5. For areas, the 'range' MUST have x, y, and z properties as OBJECTS with 'min' and 'max' fields.
                    6. Do NOT include comments in the JSON - they are not valid.
                    7. Return ONLY the JSON array and nothing else - no commentary, no XML tags, no thinking tags.
                    8. Double-check that all brackets and braces are properly matched.
                    """));

blockBuilder = AiServices.builder(BlockGenerator.class)
.chatLanguageModel(model)
.chatMemory(memory)
.build();
}

public CompletionStage<MinecraftBlocks> imagine(String text) {
CompletableFuture<MinecraftBlocks> future = new CompletableFuture<>();
Thread aiThread = new Thread(() -> {
try {
var blocks = blockBuilder.getBlocks(text);
future.complete(blocks);
} catch (Exception e) {
future.completeExceptionally(e);
}
});
aiThread.setName("AIGeneration-" + System.currentTimeMillis());
aiThread.start();
return future;
}
}
