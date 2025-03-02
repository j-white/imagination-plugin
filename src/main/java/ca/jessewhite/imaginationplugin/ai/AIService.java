package ca.jessewhite.imaginationplugin.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AIService.class);

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
        blockBuilder = AiServices.create(BlockGenerator.class, model);
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
