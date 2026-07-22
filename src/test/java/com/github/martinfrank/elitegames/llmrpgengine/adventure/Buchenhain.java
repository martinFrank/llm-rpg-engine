package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.BaseCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.EqualsCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.RangeCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.BooleanFlag;

import java.util.List;
import java.util.UUID;

import static com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.BaseFlag.GAME_TIME_FLAG;

public class Buchenhain implements Adventure {

    @Override
    public String getPlotSummary() {
        return """
                vor langer zeit lebte ein druide im wald und beschütze ihn. damit seine schützende magie auch noch nach
                seinem tod weiter wirkt, entwickelte er einen zauber, der den wald weiter schützt auch wenn er tot ist.
                dieser zauber war ein ein artefakt gebunden, ein einhorn horn, das auf sein grab gelegt wurde.
                
                ein sorgenloser schmied hat das horn aus dem Wald genommen und nach hause gebracht. Das hat den frieden
                im Wald gestört. Der schmied wird nun über von wilden tieren bedroht, deshalb entsorgt er das horn in
                einer alten mine.
                
                diese Tat verschlimmert die Situation im Wald, es erscheinen gefährlichere kreaturen und bedrohen jetzt
                sogar das ganze Dorf. In dieser Lage erscheinen die Helden im Dorf und helfen bei der entschärfung der
                Lage. Sie finden den Grund der Störungen heraus, holen das horn zurück und beruhigen die kreaturen.
                
                Chapter 1: Probleme in Buchenwald
                - Die Helden werden vom Dorfältesten um hilfe gebeten, die Störung zu untersuchen und zu lösen
                - Infos:
                	- Kreaturen aus dem wald
                	- Orte und Personen aus dem Dorf und Umland
                
                Chapter 2: Herausfinden der Ursache
                - Die Helden untersuchen das Grab vom Druiden und sein Geist erscheint - er "erklärt" die Lage (inkl.
                Ritual zur reinigung), kann die Kreaturen aber nicht mehr zurück halten
                - Danach werden die Helden von Kreatuern angegriffen
                
                Chapter 3a: Suche nach dem Horn
                - Personen befragung (hinweise, unwichtig, falsch und korrekt)
                - Hinweis: zuerst kamen sie zum Schmied (korrekt)
                - Der Schmied gesteht und erklärt den Weg zum Horn (tiefe Mine)
                
                Chapter 3b: Suche nach dem Ritual
                - Personen befragung (hinweis auf bibliothek)
                - Bibliothek lesen (hinweis auf elfen lied, Hinweis auf Blumen, Hinweis auf tanz, inkl. Tanzschritte)
                - Wandernder elf-barde (kann das lied beibringen)
                - Gegenstände Sammeln (blumen)
                
                Chapter 4: Wiederbeschaffung des Horns
                - klassischer dungeon raid
                
                Chapter 5: Wiederherstellung des waldfriedens
                - die kreaturen wollen nun nicht mehr gehen, müssen mit gewalt vertrieben werden
                - danach die durchführung des rituals und besänftigung (blumen, lied, tanz)
                - schätze vom druiden
                - rückkehr zum dorf, schätze vom dorf
                """;
    }

    @Override
    public Intro getIntro() {
        return new Intro(
                "Abenteuer in Buchenwald",
                "Martin Frank 2026",
                """
                        Bei euerer Reise kommt ihr am kleinen Ort Buchenhain vorbei.
                        Der Ort besteht nur aus ein paar wenigen Häusern, die von den
                        Bauern bewohnt werden. Einer der Bauern hat auch einen kleinen
                        Laden und einen Schmied gibt es auch. Als ihr den Dorfplatz
                        betretet, werdet ihr vom Ortsvorsteher begrüsst. Er bittet
                        euch, später bei ihm im Rathaus vorbei zu kommen, um mit ihm
                        ein heikles Thema zu besprechen. Ihr versichert ihm, dass ihr
                        ihm später einen Besuch abstatten werdet. Danach verlässt er
                        den Platz.
                        """,
                getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee")),
                GameTime.AFTERNOON
        );
    }

    @Override
    public List<Chapter> getChapters() {
        return List.of(
                new Chapter.Builder()
                        .name("Probleme in Buchenwald")
                        .summary("""
                                die Helden sollen vom dorf-vorsteher Ulf Stetten den Auftrag erhalten, die Ursache der
                                Probleme des Dorfes herauszufinden und zu beseitigen. Das schaffen die Helden einfach,
                                indem sie den Dorf-Vorsteher in seinem Haus besuchen.
                                
                                Die Helden können auch noch beim Laden und  beim Schmied Ausrüstung kaufen. Wenn die
                                sich auf den Weg machen in den Wald machen, um die Ursache herauszufinden, beginnt das
                                nächste Kapitel.
                                """)
                        .locationConditions(List.of(
                                new LocationCondition(
                                        getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee")), //marktplatz
                                        getCondition(BaseCondition.ALWAYS_TRUE_CONDITION.getId())
                                ),
                                new LocationCondition(
                                        getLocation(UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2")), //haus des ortsvorstehers
                                        getCondition(UUID.fromString("aadac5f8-9046-488b-9e36-77079bc83392")) //daytime condition
                                ),
                                new LocationCondition(
                                        getLocation(UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3")), //dorf schmiede
                                        getCondition(UUID.fromString("aadac5f8-9046-488b-9e36-77079bc83392")) //daytime condition
                                ),
                                new LocationCondition(
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        getCondition(BaseCondition.ALWAYS_TRUE_CONDITION.getId())
                                )
                        ))
                        .personConditions(List.of(
                                new PersonCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getLocation(UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2")), //haus des ortsvorstehers
                                        getCondition(UUID.fromString("aadac5f8-9046-488b-9e36-77079bc83392")) //daytime condition
                                ),
                                new PersonCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        getCondition(UUID.fromString("19fffd1b-6b46-4980-81a7-7432ddb9a6f8")) //evening time condition
                                ),
                                new PersonCondition(
                                        getPerson(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636")), //Rangolf Klingbeil
                                        getLocation(UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3")), //Schmiede
                                        getCondition(UUID.fromString("aadac5f8-9046-488b-9e36-77079bc83392")) //daytime condition
                                ),
                                new PersonCondition(
                                        getPerson(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636")), //Rangolf Klingbeil
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //marktplatz
                                        getCondition(UUID.fromString("19fffd1b-6b46-4980-81a7-7432ddb9a6f8")) //evening time condition
                                )
                        ))
                        .build()

        );
    }

    @Override
    public List<Person> getPersons() {
        return List.of(
                new Person.Builder()
                        .id(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4"))
                        .name("Ulf Stetten")
                        .description("""
                                Ulf Stetten ist der Dorfvorsteher. Er wurde gewählt weil er ein breites Vertrauen
                                in der Bevölkerung geniesst. Er ist gütig und weise.
                                """)
                        .role("""
                                Ulf Stetten ist der Auftraggeber dieses Abenteuers. Er bittet die Helden initial
                                um Hilfe, um wieder für Ruhe im Dorf zu sorgen. Weiterhin versucht er den Helden
                                bei allen Möglichkeiten zu helfen und ihnen alle Informationen geben, die er
                                verfügbar hat, falls er danach gefragt wird.
                                """)
                        .appearance("""
                                Er ist ein älterer Mann mit wachen Augen. Er trägt schöne traditionelle Kleidung.
                                Er ist 1.80, gross und sieht noch sportlich & fit aus. Er lächelt oft und gerne.
                                """)
                        .background("""
                                Ulf Stetten wohnt schon seit seiner Geburt im Dorf Buchenhain.
                                """)
                        .build(),
                new Person.Builder()
                        .id(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636"))
                        .name("Rangolf Klingbeil")
                        .description("""
                                Rangolf Klingbeil ist der Schmied des Dorfes. Er sieht zwar sehr schlank und
                                schmächtig aus, aber seine Hammerschläge sind kräftig und präzise. Er hat die
                                Schmiede von seinem Vater übernommen und seine Qualität ist im Dorf sehr geschätzt.
                                """)
                        .role("""
                                Rangolf Klingbeil ist ein Nebencharakter im Abenteuer. Seine Aufgabe ist es, den
                                Spieler Waffen, Rüstungen und Gegenstände zu verkaufen.
                                """)
                        .appearance("""
                                Er ist ein Mann im mittleren Alter, mit längeren strähnigen blonden Haaren. Er ist
                                nur von durchschnittlicher Grösse und für einen Schmied nicht sehr stark gebaut.
                                Er schafft es nicht, Augenkontakt mit Gesprächspartner aufzubauen, sondern sieht
                                immer wieder auf den Boden. Er trägt gewöhnliche Kleidung, blaue Hosen, ein weisses
                                Hemd und einen Armreif aus Metall.
                                """)
                        .background("""
                                Rangolf Klingbeil wohnt schon seit seiner Geburt im Dorf Buchenhain. Er hat die
                                Schmiede seines Vaters übernommen.
                                """)
                        .build(),
                new Person.Builder()
                        .id(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61"))
                        .name("Kalgeria Mondläufer")
                        .description("""
                                Kalgeria Mondläufer betreibt das Gasthaus in Buchenhain.
                                """)
                        .role("""
                                Kalgeria Mondläufer ist ein Nebencharakter im Abenteuer. Ihre Aufgabe ist es, den
                                Spieler Essen/Proviant und Trinken zu verkaufen. Zusätzlich bietet ihr Gasthaus
                                für die Spieler eine Übernachtungsmöglichkeit.
                                """)
                        .appearance("""
                                Die Wirtin ist eine dicke, freundliche Frau mit roten Wangen und einem Lächeln,
                                das ihre Grübchen hervorhebt. Sie hat ihr Haar in einen dicken Zopf geflochten
                                und trägt ein blau-gestreiftes Leinenkleid, das mit weißen Spitzen besetzt ist.
                                Mit ihrer warmen Stimme begrüßt sie jeden Gast bei der Tür und fragt nach seinem
                                Wunsch, während sie mit geschickten Händen Bierkrüge füllt oder Suppen serviert.
                                """)
                        .background("""
                                Vor 10 Jahren ist Kalgeria Mondläufer aus der Baronstadt in das Dorf Buchenhain
                                gezogen und führt seit dem die Kneipe.
                                """)
                        .build()
        );
    }

    @Override
    public List<Item> getItems() {
        return List.of();
    }

    @Override
    public List<Location> getLocations() {
        return List.of(
                new Location.Builder()
                        .id(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))
                        .name("Buchenhain Dorfplatz")
                        .description("""
                                Der Dorfplatz von Buchenhain. Hier findet man einen kleinen Laden
                                und der Schmied hat hier seine Schmiede. Es spielen einige Kinder
                                auf dem Dorfplatz aber ansonsten ist es ein ruhiger Ort
                                """)
                        .build(),
                new Location.Builder()
                        .id(UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2"))
                        .name("Haus des Dorfvorstehers")
                        .description("""
                                Dieses Haus ist ein klein wenig grösser als die anderen Häuser
                                im Dorf. Das Dach ist mit Reed gedeckt, es ist ein zweistöckiges
                                Fachwerkhaus mit weiss verputzten Wänden. Innen ist es sehr
                                gemütlich, durch die offenen Fenster dringt viel Licht. Ihr werdet
                                ins Arbeitszimmer geführt, könnt dabei aber auch noch einen Blick
                                in die Küche werfen. Es duftet von dort nach deftigem Essen.
                                """)
                        .build(),
                new Location.Builder()
                        .id(UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3"))
                        .name("Die Dorf Schmiede")
                        .description("""
                                Das kleine Gebäude mit schrägem Dach und weißem Fachwerk ist von
                                aussen nicht besonders beeindruckend, aber die Gerüche nach heißem
                                Metall und Kohle verraten den tatsächlichen Inhalt. Durch das offene
                                Tor gelangt man in eine kleine Vorhalle mit einem Tisch und zwei
                                Bänken, hinter dem sich ein weiteres Tor öffnet, durch das man direkt
                                in die Schmiede hineinkommt.
                                """)
                        .build(),
                new Location.Builder()
                        .id(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f"))
                        .name("Wirtshaus zum kleinen Adler")
                        .description("""
                                Das Gasthaus Zum kleinen Ader liegt mitten im Herzen von Buchenhain,
                                umgeben von einer steinernen Stadtmauer und einigen alten Bäumen.
                                Die Fassade ist warm und hell beleuchtet, während der schmale
                                Eingangsbereich durch eine reich verzierte Holztür geschützt wird. Vor
                                dem Gasthaus führt ein breiter, leicht abschüssiger Weg bis zum Marktplatz.
                                """)
                        .build()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Condition<?>> getConditions() {
        return List.of(
                BaseCondition.ALWAYS_TRUE_CONDITION,
                new RangeCondition<>(
                        UUID.fromString("aadac5f8-9046-488b-9e36-77079bc83392"),
                        "dayTimeCondition",
                        List.of((Flag<GameTime>) getFlag(GAME_TIME_FLAG.getId())),
                        List.of(GameTime.MORNING, GameTime.HIGH_NOON, GameTime.AFTERNOON)),
                new EqualsCondition<>(
                        UUID.fromString("19fffd1b-6b46-4980-81a7-7432ddb9a6f8"),
                        "nightTimeCondition",
                        List.of((Flag<GameTime>) getFlag(GAME_TIME_FLAG.getId())),
                        GameTime.IN_THE_EVENING)

        );
    }

    @Override
    public List<Flag<?>> getFlags() {
        return List.of(
                GAME_TIME_FLAG,
                new BooleanFlag(UUID.fromString("8d824f02-f2ef-4ee2-93f7-89b7e69fef7b"),
                        "hat mit Dorf-Vorsteher geredet und Quest erhalten",
                        false)
        );
    }

    @Override
    public Condition<?> getCondition(UUID id) {
        return (Condition<?>) Identifiable.find(id, getConditions());
    }

    @Override
    public Location getLocation(UUID id) {
        return (Location) Identifiable.find(id, getLocations());
    }

    @Override
    public Person getPerson(UUID id) {
        return (Person) Identifiable.find(id, getPersons());
    }

    @Override
    public Flag<?> getFlag(UUID id) {
        return (Flag<?>) Identifiable.find(id, getFlags());
    }
}