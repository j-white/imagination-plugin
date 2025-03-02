package ca.jessewhite.imaginationplugin.ai;

import dev.langchain4j.service.UserMessage;

public interface BlockGenerator {

    @UserMessage(
            """
            Generate a JSON array of Minecraft elements. Each element represents either a single block or an area of blocks.
            
            For a block, include:
            - "element_type": "block"
            - A "material" (must be one of the provided materials, in UPPERCASE like "STONE").
            - A "position" with x, y, and z coordinates (decimal values are allowed).
            
            For an area, include:
            - "element_type": "area"
            - A "material" (in UPPERCASE)
            - A "range" with x, y, and z objects, each having "min" and "max" properties (decimal values allowed).
            
            IMPORTANT FORMAT REQUIREMENTS:
            - Valid "element_type" values are only "block" and "area" (use snake_case with underscore)
            - Return a direct ARRAY of elements, NOT an object containing an "elements" property
            - All property names must use snake_case (element_type NOT elementType)
            - All material names must be UPPERCASE (STONE not stone)
            - You can use decimal values for coordinates (like 3.5) - they will be rounded to nearest blocks
            - For ranges, ALWAYS format x, y, and z as objects with "min" and "max" properties
            - Do NOT include comments in the JSON
            - Return ONLY valid JSON array without any additional text, explanations, or tags
            
            CORRECT FORMAT EXAMPLE:
            [
              {
                "element_type": "area",
                "material": "GRASS_BLOCK",
                "range": {
                  "x": { "min": 10.5, "max": 20 },
                  "y": { "min": 0, "max": 0.5 },
                  "z": { "min": 10, "max": 20.5 }
                }
              },
              {
                "element_type": "block",
                "material": "STONE",
                "position": { "x": 15.5, "y": 1.5, "z": 15.5 }
              }
            ]

            This is a fun kids game that allows them to see their ideas come to life in Minecraft.
            Be detailed with the rendering of their ideas as blocks.
            Opt to make the structures larger in order to express all the details we can imagine.
            Be semantically correct with the JSON.
            ---
            {{it}}
            """)
    MinecraftBlocks getBlocks(String text);
}
