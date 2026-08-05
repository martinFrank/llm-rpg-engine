package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.AndCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.IsCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.KnowledgeFlag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.LocationFlag;

import java.util.List;
import java.util.UUID;

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
                - Druide ist Gunver Eichblatt
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
    public Metadata getMetadata() {
        return new Metadata("Abenteuer in Buchenwald", "Martin Frank 2026");
    }

    @Override
    public List<Chapter> getChapters() {
        return List.of(
                new Chapter.Builder()
                        .id(UUID.fromString("4660eb1f-b98e-4a24-9c84-d323b64d5dd4"))
                        .name("Probleme in Buchenwald")
                        .summary("""
                                die Helden sollen vom dorf-vorsteher Ulf Stetten den Auftrag erhalten, die Ursache der
                                Probleme des Dorfes herauszufinden und zu beseitigen. Das schaffen die Helden einfach,
                                indem sie den Dorf-Vorsteher in seinem Haus besuchen.
                                
                                Die Helden können auch noch beim Laden und  beim Schmied Ausrüstung kaufen. Wenn die
                                sich auf den Weg machen in den Wald machen, um die Ursache herauszufinden, beginnt das
                                nächste Kapitel.
                                """)
                        .intro(new Intro(
                                """
                                        Bei euerer Reise kommt ihr am kleinen Ort Buchenhain vorbei. Der Ort besteht nur
                                        aus ein paar wenigen Häusern, die von den Bauern bewohnt werden. Einer der
                                        Bauern hat auch einen kleinen Laden und einen Schmied gibt es auch. Als ihr den
                                        Dorfplatz betretet, werdet ihr vom Ortsvorsteher begrüsst. Sein Name ist Ulf
                                        Stetten und er bittet euch, später bei ihm im Rathaus vorbei zu kommen, um mit
                                        ihm ein heikles Thema zu besprechen. Ihr versichert ihm, dass ihr ihm später
                                        einen Besuch abstatten werdet. Danach verlässt er den Platz.
                                        """,
                                getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee")),
                                GameTime.AFTERNOON
                        ))
                        .locationConditions(List.of(
                                new LocationCondition(
                                        getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee")), //marktplatz
                                        Condition.ALWAYS_TRUE)
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2")), //haus des ortsvorstehers
                                        Condition.DAY_TIME) //daytime condition
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3")), //dorf schmiede
                                        Condition.DAY_TIME) //daytime condition
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("9f3b7c21-5d84-4e0a-b6c7-1a2d3e4f5a6b")), //dorfladen
                                        Condition.DAY_TIME) //daytime condition
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        Condition.ALWAYS_TRUE)
//                                , //diese Location habe ich ins nächste Chapter gepackt
//                                new LocationCondition(
//                                        getLocation(UUID.fromString("5ea4584d-01ca-40fd-997c-66a9c6cbf471")), //Blumental
//                                        getCondition(UUID.fromString("2beccf6d-6bfa-4924-a85c-48ddf0573a44"))) //nachdem mit dem Dorfvorsteher geredet wurde
                        ))
                        .personConditions(List.of(
                                new PersonCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getLocation(UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2")), //haus des ortsvorstehers
                                        Condition.DAY_TIME) //daytime condition) //daytime condition
                                ,
                                new PersonCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        Condition.NIGHT_TIME) //nighttime condition) //evening time condition
                                ,
                                new PersonCondition(
                                        getPerson(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636")), //Rangolf Klingbeil
                                        getLocation(UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3")), //Schmiede
                                        Condition.DAY_TIME) //daytime condition) //daytime condition
                                ,
                                new PersonCondition(
                                        getPerson(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636")), //Rangolf Klingbeil
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //marktplatz
                                        Condition.NIGHT_TIME) //nighttime condition) //evening time condition
                                ,
                                new PersonCondition(
                                        getPerson(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61")), //Kalgeria Mondläufer
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        Condition.ALWAYS_TRUE) //always there
                        ))
                        .dialogConditions(List.of(
                                new DialogCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getDialog(UUID.fromString("16797009-af8d-4cda-9d1f-a2e7629e7e2e")), //dialog über den auftrag
                                        Condition.ALWAYS_TRUE)
                                ,
                                new DialogCondition(
                                        getPerson(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61")), //Kalgeria Mondläufer
                                        getDialog(UUID.fromString("7975bb9c-72f0-4038-a5f7-591241275826")), //dialog über Monster
                                        Condition.ALWAYS_TRUE)
                                ,
                                new DialogCondition(
                                        getPerson(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61")), //Kalgeria Mondläufer
                                        getDialog(UUID.fromString("7975bb9c-72f0-4038-a5f7-591241275826")), //dialog über Monster
                                        Condition.ALWAYS_TRUE)
                        ))
                        .chapterFinishedCondition(getCondition(UUID.fromString("83c10e5c-d2bc-4a96-a4e7-19e37f9928dc")))
                        .build()
                ,
                new Chapter.Builder()
                        .id(UUID.fromString("cc70b34b-92f6-4400-9ab9-04867b6a209d"))
                        .name("Ursache der Probleme")
                        .summary("""
                                Die Helden sollen das Grab des Druiden Gunver Eichblatt finden. Wenn es finden wird sein
                                Geist erscheinen und den Helden erklären, warum die Tiere das Dorf angreifen. Die Tiere
                                sind erbost, weil sein Schutzzauber nicht mehr wirkt und die Tiere für das Böse anfällig
                                werden. Der Zauber wirkt nicht mehr, weil das Horn von Silana gestohlen wurde, ein
                                magisches Artefakt, das für den Zauber verwendet wurde.
                                
                                Er erklärt den Helden, dass sie das Horn zurück bringen sollen. Danach müssen sie das
                                Ritual der Erneuerung durchführen, das seinen Geist zur Ruhe bettet und den Schutzzauber
                                erneuert.
                                """)
                        .intro(new Intro(
                                """
                                        Nach diesem sehr aufschlussreichen Gespräch mit Ulf Stetten begebt ihr euch auf
                                        den Marktplatz um eure nächsten Schritte zu planen. Es ist bereits abends
                                        geworden. Ihr solltet heute Abend im Gasthaus übernachten. Morgen könnt ihr dann
                                        den Weg zum Buchenwald suchen.
                                        """,
                                getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee")),//marktplatz
                                GameTime.IN_THE_EVENING
                        ))
                        .locationConditions(List.of(
                                new LocationCondition(
                                        getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee")), //marktplatz
                                        Condition.ALWAYS_TRUE)
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2")), //haus des ortsvorstehers
                                        Condition.DAY_TIME) //daytime condition
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3")), //dorf schmiede
                                        Condition.DAY_TIME) //daytime condition
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("9f3b7c21-5d84-4e0a-b6c7-1a2d3e4f5a6b")), //dorfladen
                                        Condition.DAY_TIME) //daytime condition
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        Condition.ALWAYS_TRUE)
                                ,
                                new LocationCondition(
                                        getLocation(UUID.fromString("5ea4584d-01ca-40fd-997c-66a9c6cbf471")), //Blumental
                                        getCondition(UUID.fromString("54aa8d6b-49a5-4665-b9a2-5bf1d3fecd8c"))) // flag/knowhow über weg zum Blumental
                        ))
                        .personConditions(List.of(
                                new PersonCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getLocation(UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2")), //haus des ortsvorstehers
                                        Condition.DAY_TIME) //daytime condition
                                ,
                                new PersonCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        Condition.NIGHT_TIME) //nighttime condition
                                ,
                                new PersonCondition(
                                        getPerson(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636")), //Rangolf Klingbeil
                                        getLocation(UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3")), //Schmiede
                                        Condition.DAY_TIME) //daytime condition
                                ,
                                new PersonCondition(
                                        getPerson(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636")), //Rangolf Klingbeil
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //marktplatz
                                        Condition.NIGHT_TIME) //nighttime condition
                                ,
                                new PersonCondition(
                                        getPerson(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61")), //Kalgeria Mondläufer
                                        getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f")), //wirtshaus zum kl. Adler
                                        Condition.ALWAYS_TRUE) //always there
                        ))
                        .dialogConditions(List.of(
                                new DialogCondition(
                                        getPerson(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61")), //Kalgeria Mondläufer
                                        getDialog(UUID.fromString("270ebaa5-08a9-4314-9e8c-7720a9c6f467")), //dialog weg zum buchenwald
                                        Condition.ALWAYS_TRUE)
                                ,
                                new DialogCondition(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //ulf stetten
                                        getDialog(UUID.fromString("270ebaa5-08a9-4314-9e8c-7720a9c6f467")), //dialog weg zum buchenwald
                                        Condition.ALWAYS_TRUE)
                                ,
                                new DialogCondition(
                                        getPerson(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636")), //Rangolf Klingbeil
                                        getDialog(UUID.fromString("270ebaa5-08a9-4314-9e8c-7720a9c6f467")), //dialog weg zum buchenwald
                                        Condition.ALWAYS_TRUE)
                        ))
                        .chapterFinishedCondition(getCondition(UUID.fromString("9661117e-163c-4cc6-940f-ed0d527fa9c5"))) //wissen über horndiebstahl und wissen über wiederherstellungs-ritual
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
                                Er ist der Dorfvorsteher. Er wurde gewählt weil er ein breites Vertrauen
                                in der Bevölkerung geniesst. Er ist gütig und weise.
                                """)
                        .role("""
                                Er ist der Auftraggeber dieses Abenteuers. Er bittet die Helden initial
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
                        .personality("Er ist ein herzensguter Mensch, der sich sehr für das Allgemeinwohl einsetzt.")
                        .build()
                ,
                new Person.Builder()
                        .id(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636"))
                        .name("Rangolf Klingbeil")
                        .description("""
                                Er ist der Schmied des Dorfes. Er sieht zwar sehr schlank und
                                schmächtig aus, aber seine Hammerschläge sind kräftig und präzise. Er hat die
                                Schmiede von seinem Vater übernommen und seine Qualität ist im Dorf sehr geschätzt.
                                """)
                        .role("""
                                Er ist ein Nebencharakter im Abenteuer. Seine Aufgabe ist es, den
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
                        .personality("Rangolf Klingbeil wirkt oft nervös und unsicher, aber er ist loyal und gutherzig")
                        .build()
                ,
                new Person.Builder()
                        .id(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61"))
                        .name("Kalgeria Mondläufer")
                        .description("""
                                Sie betreibt das Gasthaus in Buchenhain. Sie strahlt alleine durch
                                ihre Präsenz eine angenehme Atmosphäre aus.
                                """)
                        .role("""
                                Sie ist ein Nebencharakter im Abenteuer. Ihre Aufgabe ist es, den
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
                        .personality("Sie redet gerne, lacht viel und hat fast immer gute Laune.")
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
                Dialog.GOSSIP,
                new Dialog(UUID.fromString("16797009-af8d-4cda-9d1f-a2e7629e7e2e"),
                        "Auftrag des Ortsvorstehers",
                        "dieser Dialog beschreibt den Auftrag, den der Dorfvorsteher den Helden am Anfang des Abenteuers gibt",
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
                                getTrigger(UUID.fromString("409b408c-4b7a-4bcc-9a37-527d02bcdf7a")), //"wissen über die Bedrohung im Dorf
                                getTrigger(UUID.fromString("c92c0884-5af2-45c5-8927-03ae61f4c711")) //"wissen über Auftrag"
                        ))
                ,
                new Dialog(UUID.fromString("7975bb9c-72f0-4038-a5f7-591241275826"),
                        "Gefahr für das Dorf",
                        "dieser Dialog beschreibt die Gefahr, in der sich das Dorf Buchenhain befindet",
                        """
                                Wenn die Helden über die Gefahr für das Dorf reden, wird ihnen erzählt, dass über Nacht
                                grauenhaft mutierte Tiere um das Dorf schleichen. Die Monster sind Wölfe, gross wie
                                Rinder, mit glühenden Augen, Füchse, deren Rufe einem das Blut in den Adern gefrieren
                                lassen, Raben grösser und schwärzer wie alles was man kennt, mit rot leuchtenden Augen
                                das Dorf bedrohen. Hier darf auch noch ähnliches dazu erfunden werden.
                                
                                Wenn die Helden fragen wo die Monster her kommen, so erfahren sie, dass man am abend
                                beobachten kann, dass die Monster aus dem Buchenwald kommen. Keiner weiss, warum sie das
                                machen.
                                """,

                        List.of(
                                getTrigger(UUID.fromString("409b408c-4b7a-4bcc-9a37-527d02bcdf7a")) //"wissen über die Bedrohung im Dorf"
                        ))
                ,
                //chapter 2
                new Dialog(UUID.fromString("270ebaa5-08a9-4314-9e8c-7720a9c6f467"),
                        "Weg zum Buchenwald",
                        "dieser Dialog beschreibt den Weg zum Buchenwald",
                        """
                                Wenn die Helden über die den Weg zum Buchenwald reden, wird ihnen erzählt, dass über der
                                Weg dahin über das Blumental führt. Im Blumental gibt es Wegweiser, der zum Buchenwald
                                zeigt.
                                
                                Wenn die Helden fragen ob es noch weitere Orte im auf dem Weg gibt, so wird ihnen
                                erzählt, dass der Wegweiser im Blumental auch noch zum Steinbruch umd Zum Pferdebauer
                                führt.
                                """,

                        List.of(
                                getTrigger(UUID.fromString("fff178be-41e9-44b3-ace6-5069132a53d1")) //"trigger zum wissen über den Weg zum Buchenwald"
                        ))
        );
    }

    @Override
    public List<Location> getLocations() {
        return List.of(
                new Location.Builder()
                        .id(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))
                        .name("Buchenhain Dorfplatz")
                        .description("""
                                Der Dorfplatz von Buchenhain. Von hier aus erreicht man den kleinen
                                Dorfladen und die Schmiede, die beide direkt am Platz liegen. Es
                                spielen einige Kinder auf dem Dorfplatz aber ansonsten ist es ein
                                ruhiger Ort
                                """)
                        .destinations(List.of(
                                UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2"),//Haus des Dorfvorstehers
                                UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3"), //Schmiede
                                UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f"), //Wirtshaus zum Adler
                                UUID.fromString("9f3b7c21-5d84-4e0a-b6c7-1a2d3e4f5a6b"), //Der Dorfladen
                                UUID.fromString("5ea4584d-01ca-40fd-997c-66a9c6cbf471"))) //Blumental
                        .build()
                ,
                new Location.Builder()
                        .id(UUID.fromString("9f3b7c21-5d84-4e0a-b6c7-1a2d3e4f5a6b"))
                        .name("Der Dorfladen")
                        .description("""
                                Der Laden ist die vordere Stube eines Bauernhauses am Dorfplatz, in
                                der sich verkauft, was im Dorf gebraucht wird: Mehl, Salz und
                                getrocknete Früchte in offenen Säcken, daneben Seile, Kerzen, Decken
                                und einfaches Reisegerät. Es riecht nach Leinöl und Räucherspeck, und
                                über der Tür hängt eine hölzerne Tafel mit einem eingekerbten Korb.
                                """)
                        .destinations(List.of(
                                UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))) //Buchenhain Dorfplatz
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
                        .destinations(List.of(
                                UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))) //Buchenhain Dorfplatz
                        .build()
                ,
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
                        .destinations(List.of(
                                UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))) //Buchenhain Dorfplatz
                        .build()
                ,
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
                        .destinations(List.of(
                                UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))) //Buchenhain Dorfplatz
                        .build()
                ,
                new Location.Builder()
                        .id(UUID.fromString("5ea4584d-01ca-40fd-997c-66a9c6cbf471"))
                        .name("Blumental")
                        .description("""
                                Dieser Ort heisst Blumental, weil hier wirklich viele Blumen wachsen. Eigentlich könnte
                                man hier gut Ackerbau betreiben, aber das Tal liegt zu weit weg vom Dorf, dass es sich
                                nicht rentiert, dort Landwirtschaft zu betreiben. Mitten im Tal gibt es ein Wegweiser,
                                von hier aus kommt man zu vier verschiedenen orten: Zurück ins Dorf Buchenhain, zum
                                alten Steinbruch, zum Pferdebauer und in den Buchenwald.
                                """)
                        .destinations(List.of(
                                UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))) //Buchenhain Dorfplatz
//                                UUID.fromString("xxx")), //Buchenwald
//                                UUID.fromString("xxx")), //Pferdebauer
//                                UUID.fromString("xxx"))), //alter Steinbruch
                        .triggers(List.of(
                                UUID.fromString("f732bc8a-14ed-4f09-9df2-baef6f7a9867"))) //location Trigger on Enter Blumental
                        .build()
        );
    }

    @Override
    public List<Condition> getConditions() {
        return List.of(
                new IsCondition(
                        UUID.fromString("2beccf6d-6bfa-4924-a85c-48ddf0573a44"),
                        "prüft ob mit dem dorfvorsteher schon geredet wurde",
                        List.of(getFlag(UUID.fromString("8d824f02-f2ef-4ee2-93f7-89b7e69fef7b")))) // hat mit dorf-vorsteher geredet
                ,
                new IsCondition(
                        UUID.fromString("e4956157-cc1d-4b6e-817c-45a9e80c2aec"),
                        "prüft ob mit die spieler wissen, welche Gefahr das Dorf bedroht",
                        List.of(getFlag(UUID.fromString("9eaeccb2-5fa6-4780-8e4f-1820c07b0b6f")))) // knowledge über Bedrohung im Dorf
                ,
                new AndCondition(
                        UUID.fromString("83c10e5c-d2bc-4a96-a4e7-19e37f9928dc"),
                        "Chapter 1 Finished Condition, muss den auftrag haben UND muss über Monster bescheid wissen",
                        List.of(
                                getFlag(UUID.fromString("9eaeccb2-5fa6-4780-8e4f-1820c07b0b6f")), //  knowledge über Bedrohung im Dorf
                                getFlag(UUID.fromString("8d824f02-f2ef-4ee2-93f7-89b7e69fef7b")))) // hat mit dorf-vorsteher geredet
                //chapter 2
                ,
                new IsCondition(
                        UUID.fromString("54aa8d6b-49a5-4665-b9a2-5bf1d3fecd8c"),
                        "prüft ob mit die spieler den Weg zum Blumental kennen",
                        List.of(getFlag(UUID.fromString("56ad8098-64e0-4a3b-8775-1b2af08c76bb")))) // flag/knowledge über weg zum Blumental
                ,
                new IsCondition(
                        UUID.fromString("62a15028-bba2-41ef-b7a2-810a03a211e3"),
                        "prüft ob mit die spieler wissen, dass sie das gestohlene Horn von Silena wieder besorgen müssen",
                        List.of(getFlag(UUID.fromString("dd936532-6a33-4222-a98e-9c1b61bfd862")))) // knowledge über "gestohlenes horn"
                ,
                new IsCondition(
                        UUID.fromString("1675c611-ce0b-4813-873f-34bebff19eac"),
                        "prüft ob mit die spieler wissen, dass sie das Ritual der Wiederherstellung durchführen müssen",
                        List.of(getFlag(UUID.fromString("f9024313-30f6-4c0c-a04b-b729a1384887")))) // knowledge über "Ritual der wiederherstellung"
                ,
                new AndCondition(
                        UUID.fromString("9661117e-163c-4cc6-940f-ed0d527fa9c5"),
                        "Chapfter 2 Finished Condition, Spieler muss wissen, dass horn gestohlen wurde und muss wissen, dass das Ritual der wiederherstellung durchgeführt werden muss",
                        List.of(
                                getFlag(UUID.fromString("dd936532-6a33-4222-a98e-9c1b61bfd862")), //  flag/knowledge das horn wurde geklaut
                                getFlag(UUID.fromString("f9024313-30f6-4c0c-a04b-b729a1384887")))) // flag/Knowledge das ritual muss durchgeführt werden
        );
    }

    @Override
    public List<Flag<?>> getFlags() {
        return List.of(
                new KnowledgeFlag(
                        UUID.fromString("8d824f02-f2ef-4ee2-93f7-89b7e69fef7b"),
                        "hat mit Dorf-Vorsteher geredet und Quest erhalten",
                        new Knowledge(
                                "Auftrag des Ortsvorstehers",
                                """
                                        die Spieler wissen jetzt, dass ihr Auftrag ist, dass sie die Ursache der Bedrohung des
                                        Dorf herausfinden sollen und die Bedrohung abwenden.
                                        """))
                ,
                new KnowledgeFlag(
                        UUID.fromString("9eaeccb2-5fa6-4780-8e4f-1820c07b0b6f"),
                        "weiss, welche art monster es das dorf bedrohen",
                        new Knowledge(
                                "wissen über die Bedrohung im Dorf",
                                """
                                        die Spieler wissen jetzt, dass Monster das Dorf angreifen. Es handelt sich um mutierte
                                        Tiere aus dem Wald, die Nachts über das Dorf belagern. Sie kommen aus dem Buchenwald.
                                        """))
                ,

                //chapter 2 - suche nach der Ursache
                new KnowledgeFlag(
                        UUID.fromString("56ad8098-64e0-4a3b-8775-1b2af08c76bb"),
                        "wissen, dass der weg zum Buchenwald über das Blumental geht",
                        new Knowledge(
                                "Weg zum Buchenwald",
                                """
                                        die Spieler wissen jetzt, dass der Weg zum Buchenwald über das Blumental führt.
                                        """))
                ,
                new LocationFlag(
                        UUID.fromString("eab94d20-440a-473b-8984-5b48f5e78693"),
                        "Flag, dass das Blumental betreten wurde",
                        getLocation(UUID.fromString("5ea4584d-01ca-40fd-997c-66a9c6cbf471"))) //location BLumental wurde betreten
                ,
                new KnowledgeFlag(
                        UUID.fromString("dd936532-6a33-4222-a98e-9c1b61bfd862"),
                        "wissen, dass das Horn der Silena gestohlen wurde",
                        new Knowledge(
                                "das Horn der Silena wurde geklaut",
                                """
                                        die Spieler wissen jetzt, dass das Horn der Silena geklaut wurde.
                                        """))
                ,
                new KnowledgeFlag(
                        UUID.fromString("f9024313-30f6-4c0c-a04b-b729a1384887"),
                        "wissen, dass der das Ritual der Wiederherstellung durchgeführt werden muss",
                        new Knowledge(
                                "das Ritual der Wiederherstellung muss durchgeführt werden",
                                """
                                        die Spieler wissen jetzt, dass das Ritual der Wiederherstellung durchgeführt werden muss.
                                        """))

        );
    }

    @Override
    public List<Trigger> getTriggers() {
        return List.of(
                new Trigger(UUID.fromString("409b408c-4b7a-4bcc-9a37-527d02bcdf7a"),
                        "Bedrohung oder Gefahr für das Dorf",
                        new Event.Builder()
                                .raisedFlag(getFlag(UUID.fromString("9eaeccb2-5fa6-4780-8e4f-1820c07b0b6f"))) //flag wissen über monster
                                .build())
                ,
                //"wissen über die Bedrohung im Dorf"
                new Trigger(UUID.fromString("c92c0884-5af2-45c5-8927-03ae61f4c711"),
                        "Auftrag oder heikles Thema",
                        new Event.Builder()
                                .raisedFlag(getFlag(UUID.fromString("8d824f02-f2ef-4ee2-93f7-89b7e69fef7b"))) //flag dorf-vorsteher besucht
                                .build())
                ,
                //chapter 2
                //"wissen über den Weg"
                new Trigger(UUID.fromString("fff178be-41e9-44b3-ace6-5069132a53d1"),
                        "Weg zum Buchenwald",
                        new Event.Builder()
                                .raisedFlag( getFlag(UUID.fromString("56ad8098-64e0-4a3b-8775-1b2af08c76bb"))) //flag Weg zum Blumental bekannt
                                .build())
                ,
                //"blumental betreten"
                new Trigger(UUID.fromString("f732bc8a-14ed-4f09-9df2-baef6f7a9867"),
                        "ENTER", //LEAVE
                        new Event.Builder()
                                .raisedFlag(getFlag(UUID.fromString("eab94d20-440a-473b-8984-5b48f5e78693"))) //flag Weg zum Blumental betreten
                                .build())
        );
    }

    @Override
    public Condition getCondition(UUID id) {
        return (Condition) Identifiable.find(id, getConditions());
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
    public Trigger getTrigger(UUID id) {
        return (Trigger) Identifiable.find(id, getTriggers());
    }
}