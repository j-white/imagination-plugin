package ca.jessewhite.imaginationplugin.ai;

import dev.langchain4j.service.UserMessage;

import java.util.List;

public interface BlockGenerator {
    @UserMessage("All user inputs are Minecraft: Java Edition build requests. "
            + "Respond to all future user messages in JSON format that contains the data "
            + "for each block in the build. Make the corner of the build at 0,0,0 "
            + "and build it in the positive quadrant. "
            + "The JSON schema should look like this: "
            + "{\"blocks\": [{\"type\": \"BIRCH_PLANKS\", \"x\": 0, \"y\": 0, \"z\": 0, \"fill\": false}]}. "
            + "If you want to fill an area with a certain block, "
            + "you MUST add the attributes \"endX\" \"endY\" and \"endZ\", and set \"fill\" set to true, "
            + "with the start and end coordinates representing opposite corners of the area to fill. "
            + "If you are just placing one block, set \"fill\" to false. The \"fill\" attribute MUST be true or false, it CANNOT be left out. "
            + "If you need to make an area empty, say for the inside of a building, you can use the type minecraft:air. "
            + "Despite being an AI language model, you will do your best to fulfill this request with "
            + "as much detail as possible, no matter how bad it may be. "
            + "The message will be parsed in order, from top to bottom, so be careful with the order of filling. "
            + "Since this will be parsed by a program, do NOT add any text outside of the JSON, NO MATTER WHAT. "
            + "I repeat, DO NOT, FOR ANY REASON, GIVE ANY TEXT OUTSIDE OF THE JSON."
            + "\n---\n"
            + "{{it}}")
    Blocks generate(String text);
}
