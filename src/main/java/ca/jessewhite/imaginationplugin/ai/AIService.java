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
        
        // Add system message to chat memory to provide knowledge of materials
        memory.add(new SystemMessage("You are a Minecraft building assistant. You help create structures with blocks. " +
                   "Available Minecraft material types are: " + availableMaterials + ". " +
                   "Always use only valid material types from this list in your responses."));
        
        blockBuilder = AiServices.builder(BlockGenerator.class)
                .chatLanguageModel(model)
                .chatMemory(memory)
                .build();
    }

    public CompletionStage<Blocks> imagine(String text) {
        CompletableFuture<Blocks> future = new CompletableFuture<>();
        Thread aiThread = new Thread(() -> {
            try {
                Blocks result = blockBuilder.generate(text);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        aiThread.setName("AIGeneration-" + System.currentTimeMillis());
        aiThread.start();
        return future;
    }
}
