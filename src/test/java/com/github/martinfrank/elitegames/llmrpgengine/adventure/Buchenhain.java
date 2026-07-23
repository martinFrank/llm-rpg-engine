package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.IsCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.RangeCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.BooleanFlag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;

import java.util.List;
import java.util.UUID;

import static com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag.GAME_TIME_FLAG;

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
                                        getCondition(Condition.ALWAYS_TRUE_CONDITION.id())
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
                                        getCondition(Condition.ALWAYS_TRUE_CONDITION.id())
                                ),
                                new LocationCondition(
                                        getLocation(UUID.fromString("5ea4584d-01ca-40fd-997c-66a9c6cbf471")), //Blumental
                                        getCondition(UUID.fromString("2beccf6d-6bfa-4924-a85c-48ddf0573a44")) //nachdem mit dem Dorfvorsteher geredet wurde
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
                                ),
                                new PersonCondition(
                                        getPerson(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61")), //Kalgeria Mondläufer
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        getCondition(Condition.ALWAYS_TRUE_CONDITION.id()) //always there
                                )

                        ))
                        .dialogConditions(List.of(
                                new DialogCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getDialog(UUID.fromString("16797009-af8d-4cda-9d1f-a2e7629e7e2e")), //dialog über den auftrag
                                        getCondition(Condition.ALWAYS_TRUE_CONDITION.id())
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
                                Kalgeria Mondläufer betreibt das Gasthaus in Buchenhain. Sie strahlt alleine durch
                                ihre Präsenz eine angenehme Atmosphäre aus.
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
    public List<Dialog> getDialogs() {
        return List.of(
                new Dialog(UUID.fromString("16797009-af8d-4cda-9d1f-a2e7629e7e2e"),
                        "Auftrag des Ortsvorstehers",
                        "dieser Dialog beschreibt den Auftrag, den der Dorfvorsteher den Helden am Anfang des Abenteuers gibt",
                        false,
                        """
                                Wenn die Helden über den Auftrag reden wird der Dorfvorsteher erzählen, dass über Nacht
                                grauenhaft mutierte Tiere um das Dorf schleichen. Der Dorfvorsteher möchte, dass ihr
                                herausfindet, wieso die Monster das Dorf angreifen und bittet euch, die Bedrohung zu
                                beenden.
                                
                                Wenn die Helden fragen, welche Monster das Dorf bedrohen, erfahren die Helden, dass
                                Wölfe, gross wie Rinder, mit glühenden Augen, Füchse, deren Rufe einem das Blut in den
                                Adern gefrieren lassen, Raben grösser und schwärzer wie alles was man kennt, mit rot
                                leuchtenden Augen das Dorf bedrohen. hier darf auch noch ähnliches dazu erfunden werden.
                                
                                Wenn die Helden fragen wo die Monster her kommen, so erfahren sie, dass man am abend
                                beobachten kann, dass die Monster aus dem Buchenwald kommen. Keiner weiss, warum sie das
                                machen.
                                """,

                        List.of(
                                getKnowledgeTrigger(UUID.fromString("409b408c-4b7a-4bcc-9a37-527d02bcdf7a")), //"wissen über die Bedrohung im Dorf
                                getKnowledgeTrigger(UUID.fromString("c92c0884-5af2-45c5-8927-03ae61f4c711")) //"wissen über Auftrag"
                        )
                ),
                new Dialog(UUID.fromString("7975bb9c-72f0-4038-a5f7-591241275826"),
                        "Gefahr für das Dorf",
                        "dieser Dialog beschreibt die Gefahr, in der sich das Dorf Buchenhain befindet",
                        true,
                        """
                                Wenn die Helden über die Gefahr für das Dorf reden, wird ihnen jeder erzählen, dass über
                                Nacht grauenhaft mutierte Tiere um das Dorf schleichen. Die Monster sind Wölfe, gross
                                wie Rinder, mit glühenden Augen, Füchse, deren Rufe einem das Blut in den Adern
                                gefrieren lassen, Raben grösser und schwärzer wie alles was man kennt, mit rot
                                leuchtenden Augen das Dorf bedrohen. hier darf auch noch ähnliches dazu erfunden werden.
                                
                                Wenn die Helden fragen wo die Monster her kommen, so erfahren sie, dass man am abend
                                beobachten kann, dass die Monster aus dem Buchenwald kommen. Keiner weiss, warum sie das
                                machen.
                                """,

                        List.of(
                                getKnowledgeTrigger(UUID.fromString("409b408c-4b7a-4bcc-9a37-527d02bcdf7a")) //"wissen über die Bedrohung im Dorf"
                        )
                )
        );
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
                        .build(),
                new Location.Builder()
                        .id(UUID.fromString("5ea4584d-01ca-40fd-997c-66a9c6cbf471"))
                        .name("Blumental")
                        .description("""
                                Dier Ort heisst Blumental, weil hier wirklich viele Blumen wachsen. Eigentlich
                                könnte man hier gut Ackerbau betreiben, aber das Tal liegt zu weit weg vom Dorf,
                                dass es sich nicht rentiert, dort Landwirtschaft zu betreiben. Der Weg führt hier
                                vom Dorf weiter in den Buchenwald hinein.
                                """)
                        .build()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Condition<?>> getConditions() {
        return List.of(
                Condition.ALWAYS_TRUE_CONDITION,
                new RangeCondition<>(
                        UUID.fromString("aadac5f8-9046-488b-9e36-77079bc83392"),
                        "dayTimeCondition",
                        List.of((Flag<GameTime>) getFlag(GAME_TIME_FLAG.id())),
                        List.of(GameTime.MORNING, GameTime.HIGH_NOON, GameTime.AFTERNOON)), //bedingung: es ist tagsüber
                new RangeCondition<>(
                        UUID.fromString("19fffd1b-6b46-4980-81a7-7432ddb9a6f8"),
                        "nightTimeCondition",
                        List.of((Flag<GameTime>) getFlag(GAME_TIME_FLAG.id())),
                        List.of(GameTime.IN_THE_EVENING, GameTime.AT_NIGHT, GameTime.MIDNIGHT)), //bedingung: es ist abends/nachts
                new IsCondition(
                        UUID.fromString("2beccf6d-6bfa-4924-a85c-48ddf0573a44"),
                        "prüft ob mit dem dorfvorsteher schon geredet wurde",
                        List.of((Flag<Boolean>) getFlag(UUID.fromString("8d824f02-f2ef-4ee2-93f7-89b7e69fef7b"))) // hat mit dorf-vorsteher geredet
                )

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
    public List<Knowledge> getKnowledges() {
        return List.of(
                new Knowledge(UUID.fromString("3f6adf43-57f0-4c93-9e54-0e6768e6b475"),
                        "wissen über die Bedrohung im Dorf",
                        """
                                die Spieler wissen jetzt, dass Monster das Dorf angreifen. Es handelt sich um mutierte
                                Tiere aus dem Wald, die Nachts über das Dorf belagern. Sie kommen aus dem Buchenwald.
                                """
                ),
                new Knowledge(UUID.fromString("4d5f9db4-39ae-400e-9371-6030c08edafa"),
                        "Auftrag des Ortsvorstehers",
                        """
                                die Spieler wissen jetzt, dass ihr Auftrag ist, dass sie die Ursache der Bedrohung des
                                Dorf herausfinden sollen und die Bedrohung abwenden.
                                """
                )
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<KnowledgeTrigger> getKnowledgeTriggers() {
        return List.of(
                new KnowledgeTrigger(UUID.fromString("409b408c-4b7a-4bcc-9a37-527d02bcdf7a"),
                        "Bedrohung oder Gefahr für das Dorf",
                        getKnowledge(UUID.fromString("3f6adf43-57f0-4c93-9e54-0e6768e6b475")),
                        List.of()
                ), //"wissen über die Bedrohung im Dorf"
                new KnowledgeTrigger(UUID.fromString("c92c0884-5af2-45c5-8927-03ae61f4c711"),
                        "Auftrag oder heikles Thema",
                        getKnowledge(UUID.fromString("4d5f9db4-39ae-400e-9371-6030c08edafa")),
                        List.of(
                                new FlagChange<>(
                                        UUID.randomUUID(),
                                        (Flag<Boolean>) getFlag(UUID.fromString("8d824f02-f2ef-4ee2-93f7-89b7e69fef7b")), //flag dorf-vorsteher besucht
                                        true
                                )
                        ))
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

    @Override
    public Dialog getDialog(UUID id) {
        return (Dialog) Identifiable.find(id, getDialogs());
    }

    @Override
    public Knowledge getKnowledge(UUID id) {
        return (Knowledge) Identifiable.find(id, getKnowledges());
    }

    @Override
    public KnowledgeTrigger getKnowledgeTrigger(UUID id) {
        return (KnowledgeTrigger) Identifiable.find(id, getKnowledgeTriggers());
    }
}