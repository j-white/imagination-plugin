package ca.jessewhite.imaginationplugin;

import ca.jessewhite.imaginationplugin.ai.BlockGenerator;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

public class OllamaTest {

    @Disabled
    @Test
    public void canGenerateBlocks() {
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://172.23.254.196:11434/")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("deepseek-r1:1.5b")
                .timeout(Duration.ofSeconds(30))
                .build();
        BlockGenerator blockBuilder = AiServices.create(BlockGenerator.class, model);
        var blocks = blockBuilder.generate("a house");
        System.out.println(blocks);
        assertThat(blocks.blocks().size(), equalTo(1));
    }
}
