package ca.jessewhite.imaginationplugin;

import ca.jessewhite.imaginationplugin.ai.AIService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BlockGeneratorTest {

    @Disabled
    @Test
    public void canGenerateBlocks() {
        AIService aiService = new AIService();
        aiService.init();
        var future = aiService.imagine("a house");
        var blocks = future.toCompletableFuture().join();
        System.out.println(blocks);
    }
}
