package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.AdventureDefinitionException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The file an adventure lives in.
 * <p>
 * YAML rather than JSON for one reason that matters at this size: a {@code description} is a
 * paragraph of German prose, and a block scalar keeps it readable and reviewable in a diff, where
 * JSON would give one line full of {@code \n}. The editor's own API stays JSON – it is the same
 * Jackson with a different factory, so the two can never disagree about the shape.
 * <p>
 * Nothing here knows the adventure model. It turns text into a tree and back;
 * {@link DocumentAdventure} and {@link AdventureDocument} own the meaning.
 */
public final class AdventureYaml {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory()
            // No leading "---": the file holds one adventure, and the marker is noise.
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            // The point of the format: multi-line prose as a "|" block instead of escapes.
            .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));

    private AdventureYaml() {
    }

    public static String write(Map<String, Object> document) {
        try {
            return YAML.writeValueAsString(document);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write the adventure as yaml", e);
        }
    }

    public static Map<String, Object> parse(String yaml) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> document = YAML.readValue(yaml, LinkedHashMap.class);
            return document;
        } catch (IOException e) {
            throw new AdventureDefinitionException("the adventure file is not readable yaml: "
                    + e.getMessage());
        }
    }

    /** Reads an adventure shipped with the application, e.g. {@code adventures/buchenhain.yaml}. */
    public static Map<String, Object> fromClasspath(String resource) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new AdventureDefinitionException("no adventure file '" + resource + "' on the classpath");
            }
            return parse(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AdventureDefinitionException("cannot read '" + resource + "': " + e.getMessage());
        }
    }

    public static Map<String, Object> fromFile(Path path) {
        try {
            return parse(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AdventureDefinitionException("cannot read '" + path + "': " + e.getMessage());
        }
    }

    public static void toFile(Path path, Map<String, Object> document) {
        try {
            Files.writeString(path, write(document), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write '" + path + "'", e);
        }
    }
}
