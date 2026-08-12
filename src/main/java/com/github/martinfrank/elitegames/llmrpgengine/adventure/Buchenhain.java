package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.AdventureYaml;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.DocumentAdventure;

/**
 * The reference adventure, which is a file rather than code.
 * <p>
 * Until August 2026 this class was 711 lines of Java: nine {@code defineX()} blocks, and a person
 * spread over one {@code personCondition} per chapter. The content now lives in
 * {@value #RESOURCE} and is written in the editor. What is left here is the name to ask for it by,
 * so that anything wanting this adventure – the running application, a test – says
 * {@code new Buchenhain().build()} and does not care where it came from.
 *
 * @see <a href="../../../../../../../../../docs/editor.md">docs/editor.md</a>
 */
public class Buchenhain extends DocumentAdventure {

    public static final String RESOURCE = "adventures/buchenhain.yaml";

    public Buchenhain() {
        super(AdventureYaml.fromClasspath(RESOURCE));
    }
}
