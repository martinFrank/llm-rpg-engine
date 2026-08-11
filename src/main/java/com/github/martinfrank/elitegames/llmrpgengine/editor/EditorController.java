package com.github.martinfrank.elitegames.llmrpgengine.editor;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.ValidationResult;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.AdventureStore;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.AdventureYaml;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.IdSuggester;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.ModelSchema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What the editor page talks to. No endpoint here knows a field name of the adventure model: the
 * document and the schema are derived from the records, the validation is the one the engine runs
 * anyway, and a save is handed to {@link AdventureStore} whole.
 * <p>
 * That is deliberate. A change to the model has to reach the browser without passing through
 * here, or the editor becomes another place to maintain.
 */
@RestController
@RequestMapping("/api")
public class EditorController {

    private final AdventureStore store;
    private final ModelSchema schema;

    public EditorController(AdventureStore store, ModelSchema schema) {
        this.store = store;
        this.schema = schema;
    }

    /** Where the adventure comes from, and whether the editor may write it. */
    @GetMapping("/source")
    public Map<String, Object> source() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("writable", store.writable());
        response.put("file", store.file() == null ? null : store.file().toString());
        response.put("title", store.adventure().getMetadata().title());
        return response;
    }

    /** The adventure itself, with every reference written as the id it names. */
    @GetMapping("/adventure")
    public Map<String, Object> adventure() {
        return store.document();
    }

    /** The same adventure as the file it is stored in. */
    @GetMapping(value = "/adventure.yaml", produces = "text/yaml;charset=UTF-8")
    public String adventureYaml() {
        return AdventureYaml.write(store.document());
    }

    /**
     * Replaces the whole adventure. Whole rather than field by field because there is one author
     * and one document: a patch protocol would buy nothing and cost a merge.
     *
     * @return 200 with the warnings of what was saved, or 422 with the reasons it was not
     */
    @PutMapping("/adventure")
    public ResponseEntity<AdventureStore.SaveResult> save(@RequestBody Map<String, Object> document) {
        AdventureStore.SaveResult result = store.save(document);
        return result.saved()
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result);
    }

    /** Throws away unsaved changes and rereads the file. */
    @PostMapping("/reload")
    public Map<String, Object> reload() {
        store.reload();
        return store.document();
    }

    /** What the fields of each type are, so the page can build the forms without knowing the model. */
    @GetMapping("/schema")
    public Map<String, Object> schema() {
        List<Map<String, Object>> sections = ModelSchema.SECTIONS.stream()
                .map(section -> Map.<String, Object>of(
                        "name", section.name(),
                        "namespace", section.namespace(),
                        "type", section.type().getSimpleName(),
                        "polymorphic", section.polymorphic()))
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sections", sections);
        response.put("types", schema.types());
        return response;
    }

    /** The errors and warnings of the adventure as it stands – the author's to-do list. */
    @GetMapping("/validation")
    public ValidationResult validation() {
        return store.validation();
    }

    /**
     * An id for something being named, so the author never types one. Checked against the
     * distance rule, because a near-collision is an error the author would only meet on saving.
     */
    @PostMapping("/id-suggestion")
    public IdSuggester.Suggestion suggestId(@RequestBody IdRequest request) {
        return IdSuggester.suggest(store.adventure(), request.namespace(), request.name());
    }

    public record IdRequest(String namespace, String name) {
    }
}
