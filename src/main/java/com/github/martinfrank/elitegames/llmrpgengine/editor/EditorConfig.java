package com.github.martinfrank.elitegames.llmrpgengine.editor;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.AdventureDocument;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.AdventureStore;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.ModelSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Wiring for the editor.
 * <p>
 * Which adventure is being worked on, and whether it can be written, is decided here and nowhere
 * else. Everything downstream – schema, document, validation, the page – works against
 * {@link com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure Adventure} rather
 * than against where it came from.
 */
@Configuration
public class EditorConfig {

    /**
     * The file the editor reads and writes. Unset means the packaged adventure is opened read
     * only, which is what a built jar gets: a resource inside it cannot be written to.
     * <p>
     * While writing an adventure, point this at the file in the source tree – then a save lands
     * in the working copy and goes through git like any other change.
     */
    @Value("${rpg.adventure.file:}")
    private String adventureFile;

    /** The adventure shipped with the application, used when no file is configured. */
    @Value("${rpg.adventure.resource:" + Buchenhain.RESOURCE + "}")
    private String adventureResource;

    @Bean
    public ModelSchema modelSchema() {
        return new ModelSchema();
    }

    @Bean
    public AdventureDocument adventureDocument(ModelSchema schema) {
        return new AdventureDocument(schema);
    }

    /**
     * The adventure being worked on.
     * <p>
     * Deliberately the only bean: there is no {@code Adventure} bean, because a save replaces the
     * adventure and anything holding the instance from startup would quietly go on using the old
     * one. Whoever needs it asks {@link AdventureStore#adventure()} at the time.
     */
    @Bean
    public AdventureStore adventureStore(ModelSchema schema, AdventureDocument document) {
        Path file = adventureFile == null || adventureFile.isBlank() ? null : Path.of(adventureFile);
        return new AdventureStore(file, adventureResource, schema, document);
    }
}
