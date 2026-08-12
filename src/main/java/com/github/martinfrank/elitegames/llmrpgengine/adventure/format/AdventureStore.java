package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.AdventureDefinitionException;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.AdventureValidator;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The adventure being worked on, and the file it comes from.
 *
 * <h2>Never write something that will not load</h2>
 * A save builds the candidate first and writes only if that succeeds, so the file on disk is
 * always an adventure the engine can start. The alternative – write, then find out – would leave
 * the author with a broken file and the editor unable to open it, which is the one failure that
 * costs work rather than time.
 * <p>
 * The adventure in memory is swapped for the new one only after the write, so a rejected save
 * leaves everything as it was.
 *
 * <h2>Where the file is</h2>
 * <table>
 *   <tr><th>{@code rpg.adventure.file}</th><th>behaviour</th></tr>
 *   <tr><td>set, exists</td><td>read from it, saving writes it</td></tr>
 *   <tr><td>set, missing</td><td>read the packaged adventure, the first save creates the file</td></tr>
 *   <tr><td>not set</td><td>read the packaged adventure, saving is refused</td></tr>
 * </table>
 * The packaged adventure lives in the jar, which cannot be written to at runtime. Refusing to
 * save is therefore the honest answer, rather than appearing to work until the next restart.
 */
public class AdventureStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdventureStore.class);

    private final Path file;
    private final String resource;
    private final ModelSchema schema;
    private final AdventureDocument writer;

    private Map<String, Object> document;
    private Adventure adventure;

    public AdventureStore(Path file, String resource, ModelSchema schema, AdventureDocument writer) {
        this.file = file;
        this.resource = resource;
        this.schema = schema;
        this.writer = writer;
        load();
    }

    private void load() {
        boolean fromFile = file != null && Files.isRegularFile(file);
        document = fromFile ? AdventureYaml.fromFile(file) : AdventureYaml.fromClasspath(resource);
        adventure = new DocumentAdventure(document, schema).build();
        LOGGER.info("adventure '{}' read from {}{}", adventure.getMetadata().title(),
                fromFile ? file : "classpath:" + resource,
                writable() ? " (saving writes " + file + ")" : " (read only)");
    }

    /** Whether a save can be accepted at all; see the table above. */
    public boolean writable() {
        return file != null;
    }

    public Path file() {
        return file;
    }

    public synchronized Adventure adventure() {
        return adventure;
    }

    /** The document as it currently stands – what the editor reads and edits. */
    public synchronized Map<String, Object> document() {
        return writer.write(adventure);
    }

    public synchronized ValidationResult validation() {
        return AdventureValidator.validate(adventure);
    }

    /**
     * Builds the candidate, and on success writes it and adopts it.
     *
     * @return what happened, including the warnings of the accepted adventure – those do not
     *         block a save, because half-finished content is the normal state of writing
     */
    public synchronized SaveResult save(Map<String, Object> candidate) {
        if (!writable()) {
            return new SaveResult(false, List.of(
                    "Dieses Abenteuer ist nur zum Lesen geöffnet."
                            + " Setze rpg.adventure.file auf eine Datei, um es zu bearbeiten."),
                    List.of());
        }

        DocumentAdventure rebuilt = new DocumentAdventure(candidate, schema);
        try {
            rebuilt.build();
        } catch (AdventureDefinitionException rejected) {
            return new SaveResult(false, errorsOf(rebuilt, rejected), List.of());
        } catch (RuntimeException broke) {
            // A half-filled document can trip a rule that expects something to be there at all -
            // an intro without a start location, say. That is a rejected save like any other, and
            // never a failed request: the editor is where an author leaves things unfinished.
            LOGGER.debug("candidate adventure could not be built", broke);
            return new SaveResult(false, List.of("Der Stand ist noch nicht vollständig: "
                    + broke.getClass().getSimpleName()
                    + (broke.getMessage() == null ? "" : " - " + broke.getMessage())), List.of());
        }

        ValidationResult accepted = AdventureValidator.validate(rebuilt);
        Map<String, Object> normalised = writer.write(rebuilt);
        AdventureYaml.toFile(file, normalised);
        document = normalised;
        adventure = rebuilt;
        LOGGER.info("adventure '{}' saved to {}", adventure.getMetadata().title(), file);
        return new SaveResult(true, List.of(), accepted.warnings());
    }

    /** Rereads the file, throwing away anything unsaved. */
    public synchronized void reload() {
        load();
    }

    /**
     * Why a candidate was rejected, as a list rather than one blob where possible.
     * <p>
     * A build that fails on the validator has already filled its registries, so asking the
     * validator again yields the individual errors. A build that fails earlier – a reference that
     * names nothing, an unknown type – never got that far, and its one message is all there is.
     */
    private static List<String> errorsOf(DocumentAdventure rebuilt, AdventureDefinitionException thrown) {
        try {
            List<String> errors = AdventureValidator.validate(rebuilt).errors();
            return errors.isEmpty() ? List.of(thrown.getMessage()) : errors;
        } catch (RuntimeException neverGotThatFar) {
            return List.of(thrown.getMessage());
        }
    }

    /**
     * @param saved    whether the file was written
     * @param errors   why not, if it was not
     * @param warnings what is unfinished in the adventure that was saved
     */
    public record SaveResult(boolean saved, List<String> errors, List<String> warnings) {
    }
}
