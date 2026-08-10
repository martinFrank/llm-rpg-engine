package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.InvestigateCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.AndCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.IsCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.NotCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.ItemFlag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.KnowledgeFlag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.LocationFlag;

import java.util.List;

public class Buchenhain extends BaseAdventure {

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
                - Hinweis: zuerst kamen die monster zum Schmied (korrekt)
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
    protected List<Chapter> defineChapters() {
        return List.of(
                new Chapter.Builder()
                        .id("chapter.probleme-in-buchenwald")
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
                                getLocation("location.dorfplatz"),
                                GameTime.AFTERNOON
                        ))
                        .locationConditions(List.of(
                                new LocationCondition(
                                        getLocation("location.dorfplatz"),
                                        Condition.ALWAYS_TRUE)
                                ,
                                new LocationCondition(
                                        getLocation("location.haus-des-dorfvorstehers"),
                                        Condition.DAY_TIME)
                                ,
                                new LocationCondition(
                                        getLocation("location.dorfschmiede"),
                                        Condition.DAY_TIME)
                                ,
                                new LocationCondition(
                                        getLocation("location.dorfladen"),
                                        Condition.DAY_TIME)
                                ,
                                new LocationCondition(
                                        getLocation("location.wirtshaus-zum-adler"),
                                        Condition.ALWAYS_TRUE)
//                                , //diese Location habe ich ins nächste Chapter gepackt
//                                new LocationCondition(
//                                        getLocation("location.blumental"),
//                                        getCondition("condition.mit-dorfvorsteher-geredet"))
                        ))
                        .personConditions(List.of(
                                new PersonCondition(
                                        getPerson("person.ulf-stetten"),
                                        getLocation("location.haus-des-dorfvorstehers"),
                                        Condition.DAY_TIME)
                                ,
                                new PersonCondition(
                                        getPerson("person.ulf-stetten"),
                                        getLocation("location.wirtshaus-zum-adler"),
                                        Condition.NIGHT_TIME)
                                ,
                                new PersonCondition(
                                        getPerson("person.rangolf-klingbeil"),
                                        getLocation("location.dorfschmiede"),
                                        Condition.DAY_TIME)
                                ,
                                new PersonCondition(
                                        getPerson("person.rangolf-klingbeil"),
                                        getLocation("location.wirtshaus-zum-adler"),
                                        Condition.NIGHT_TIME)
                                ,
                                new PersonCondition(
                                        getPerson("person.kalgeria-mondlaeufer"),
                                        getLocation("location.wirtshaus-zum-adler"),
                                        Condition.ALWAYS_TRUE) //always there
                        ))
                        .dialogConditions(List.of(
                                new DialogCondition(
                                        getPerson("person.ulf-stetten"),
                                        getDialog("dialog.auftrag-des-ortsvorstehers"),
                                        Condition.ALWAYS_TRUE)
                                ,
                                new DialogCondition(
                                        getPerson("person.kalgeria-mondlaeufer"),
                                        getDialog("dialog.gefahr-fuer-das-dorf"),
                                        Condition.ALWAYS_TRUE)
                        ))
                        .investigateConditions(List.of (
                                new InvestigateCondition<>(
                                        getLocation("location.dorfplatz"),
                                        getInvestigation("investigation.marktplatz-schluessel"),
                                        getCondition("condition.schluessel-noch-nicht-gefunden")
                                )))
                        .chapterFinishedCondition(getCondition("condition.kapitel-eins-abgeschlossen"))
                        .build()
                ,
                new Chapter.Builder()
                        .id("chapter.ursache-der-probleme")
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
                                getLocation("location.dorfplatz"),
                                GameTime.IN_THE_EVENING
                        ))
                        .locationConditions(List.of(
                                new LocationCondition(
                                        getLocation("location.dorfplatz"),
                                        Condition.ALWAYS_TRUE)
                                ,
                                new LocationCondition(
                                        getLocation("location.haus-des-dorfvorstehers"),
                                        Condition.DAY_TIME)
                                ,
                                new LocationCondition(
                                        getLocation("location.dorfschmiede"),
                                        Condition.DAY_TIME)
                                ,
                                new LocationCondition(
                                        getLocation("location.dorfladen"),
                                        Condition.DAY_TIME)
                                ,
                                new LocationCondition(
                                        getLocation("location.wirtshaus-zum-adler"),
                                        Condition.ALWAYS_TRUE)
                                ,
                                new LocationCondition(
                                        getLocation("location.blumental"),
                                        getCondition("condition.kennt-weg-zum-blumental"))
                        ))
                        .personConditions(List.of(
                                new PersonCondition(
                                        getPerson("person.ulf-stetten"),
                                        getLocation("location.haus-des-dorfvorstehers"),
                                        Condition.DAY_TIME)
                                ,
                                new PersonCondition(
                                        getPerson("person.ulf-stetten"),
                                        getLocation("location.wirtshaus-zum-adler"),
                                        Condition.NIGHT_TIME)
                                ,
                                new PersonCondition(
                                        getPerson("person.rangolf-klingbeil"),
                                        getLocation("location.dorfschmiede"),
                                        Condition.DAY_TIME)
                                ,
                                new PersonCondition(
                                        getPerson("person.rangolf-klingbeil"),
                                        getLocation("location.wirtshaus-zum-adler"),
                                        Condition.NIGHT_TIME)
                                ,
                                new PersonCondition(
                                        getPerson("person.kalgeria-mondlaeufer"),
                                        getLocation("location.wirtshaus-zum-adler"),
                                        Condition.ALWAYS_TRUE) //always there
                        ))
                        .dialogConditions(List.of(
                                new DialogCondition(
                                        getPerson("person.kalgeria-mondlaeufer"),
                                        getDialog("dialog.weg-zum-buchenwald"),
                                        Condition.ALWAYS_TRUE)
                                ,
                                new DialogCondition(
                                        getPerson("person.ulf-stetten"),
                                        getDialog("dialog.weg-zum-buchenwald"),
                                        Condition.ALWAYS_TRUE)
                                ,
                                new DialogCondition(
                                        getPerson("person.rangolf-klingbeil"),
                                        getDialog("dialog.weg-zum-buchenwald"),
                                        Condition.ALWAYS_TRUE)
                        ))
                        .investigateConditions(List.of (
                                new InvestigateCondition<>(
                                        getLocation("location.dorfplatz"),
                                        getInvestigation("investigation.marktplatz-schluessel"),
                                        getCondition("condition.schluessel-noch-nicht-gefunden")
                                )))
                        //wissen über horndiebstahl und wissen über wiederherstellungs-ritual
                        .chapterFinishedCondition(getCondition("condition.kapitel-zwei-abgeschlossen"))
                        .build()

        );
    }

    @Override
    protected List<Person> definePersons() {
        return List.of(
                new Person.Builder()
                        .id("person.ulf-stetten")
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
                        .id("person.rangolf-klingbeil")
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
                        .id("person.kalgeria-mondlaeufer")
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
    protected List<Item> defineItems() {
        return List.of(
                new Item(
                        Id.of("item.eisenschluessel"),
                        "ein kleiner Schlüssel aus Eisen",
                        """
                                Entwickelt im traditionellen Stil, handelt es sich bei diesem kleinen Schlüssel um ein
                                solides Werkzeug aus hochwertigem Eisen. Er weist eine robuste Verarbeitung mit glatter
                                Oberfläche auf und ist durch seine geschmiedete Form eindeutig erkennbar.
                                """
                ),
                new Item(
                        Id.of("item.silberring-mit-rubin"),
                        "ein silbener Ring mit einen kleinen Rubin",
                        """
                                Der silberne Ring ist ein elegantes Schmuckstück mit einem kleinen rot glühenden Rubin
                                im Mittelpunkt. Sein schlankes Band hat eine feine Glanzlinie und passt perfekt zu allen
                                Stilrichtungen. Mit seinem feinen, glänzenden Schimmer wird der silbenerene Ring das
                                ideale Accessoires für jede Gelegenheit. Der kleine Rubin bietet ein bezauberndes
                                Aussehen und verleiht dem Ring einen edlen Touch.
                                """
                )
        );
    }

    @Override
    protected List<Dialog> defineDialogs() {
        return List.of(
                // Dialog.GENERIC (gossip) is not listed here: the engine always adds it.
                new Dialog(Id.of("dialog.auftrag-des-ortsvorstehers"),
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
                                getTrigger("trigger.bedrohung-fuer-das-dorf"),
                                getTrigger("trigger.auftrag-erhalten")
                        ))
                ,
                new Dialog(Id.of("dialog.gefahr-fuer-das-dorf"),
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
                                getTrigger("trigger.bedrohung-fuer-das-dorf")
                        ))
                ,
                //chapter 2
                new Dialog(Id.of("dialog.weg-zum-buchenwald"),
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
                                getTrigger("trigger.weg-zum-buchenwald")
                        ))
        );
    }

    @Override
    protected List<Location> defineLocations() {
        return List.of(
                new Location.Builder()
                        .id("location.dorfplatz")
                        .name("Buchenhain Dorfplatz")
                        .description("""
                                Der Dorfplatz von Buchenhain. Von hier aus erreicht man den kleinen
                                Dorfladen und die Schmiede, die beide direkt am Platz liegen. Es
                                spielen einige Kinder auf dem Dorfplatz aber ansonsten ist es ein
                                ruhiger Ort
                                """)
                        .destinations(
                                "location.haus-des-dorfvorstehers",
                                "location.dorfschmiede",
                                "location.wirtshaus-zum-adler",
                                "location.dorfladen",
                                "location.blumental")
                        .build()
                ,
                new Location.Builder()
                        .id("location.dorfladen")
                        .name("Der Dorfladen")
                        .description("""
                                Der Laden ist die vordere Stube eines Bauernhauses am Dorfplatz, in
                                der sich verkauft, was im Dorf gebraucht wird: Mehl, Salz und
                                getrocknete Früchte in offenen Säcken, daneben Seile, Kerzen, Decken
                                und einfaches Reisegerät. Es riecht nach Leinöl und Räucherspeck, und
                                über der Tür hängt eine hölzerne Tafel mit einem eingekerbten Korb.
                                """)
                        .destinations("location.dorfplatz")
                        .build(),
                new Location.Builder()
                        .id("location.haus-des-dorfvorstehers")
                        .name("Haus des Dorfvorstehers")
                        .description("""
                                Dieses Haus ist ein klein wenig grösser als die anderen Häuser
                                im Dorf. Das Dach ist mit Reed gedeckt, es ist ein zweistöckiges
                                Fachwerkhaus mit weiss verputzten Wänden. Innen ist es sehr
                                gemütlich, durch die offenen Fenster dringt viel Licht. Ihr werdet
                                ins Arbeitszimmer geführt, könnt dabei aber auch noch einen Blick
                                in die Küche werfen. Es duftet von dort nach deftigem Essen.
                                """)
                        .destinations("location.dorfplatz")
                        .build()
                ,
                new Location.Builder()
                        .id("location.dorfschmiede")
                        .name("Die Dorf Schmiede")
                        .description("""
                                Das kleine Gebäude mit schrägem Dach und weißem Fachwerk ist von
                                aussen nicht besonders beeindruckend, aber die Gerüche nach heißem
                                Metall und Kohle verraten den tatsächlichen Inhalt. Durch das offene
                                Tor gelangt man in eine kleine Vorhalle mit einem Tisch und zwei
                                Bänken, hinter dem sich ein weiteres Tor öffnet, durch das man direkt
                                in die Schmiede hineinkommt.
                                """)
                        .destinations("location.dorfplatz")
                        .build()
                ,
                new Location.Builder()
                        .id("location.wirtshaus-zum-adler")
                        .name("Wirtshaus zum kleinen Adler")
                        .description("""
                                Das Gasthaus Zum kleinen Ader liegt mitten im Herzen von Buchenhain,
                                umgeben von einer steinernen Stadtmauer und einigen alten Bäumen.
                                Die Fassade ist warm und hell beleuchtet, während der schmale
                                Eingangsbereich durch eine reich verzierte Holztür geschützt wird. Vor
                                dem Gasthaus führt ein breiter, leicht abschüssiger Weg bis zum Marktplatz.
                                """)
                        .destinations("location.dorfplatz")
                        .build()
                ,
                new Location.Builder()
                        .id("location.blumental")
                        .name("Blumental")
                        .description("""
                                Dieser Ort heisst Blumental, weil hier wirklich viele Blumen wachsen. Eigentlich könnte
                                man hier gut Ackerbau betreiben, aber das Tal liegt zu weit weg vom Dorf, dass es sich
                                nicht rentiert, dort Landwirtschaft zu betreiben. Mitten im Tal gibt es ein Wegweiser,
                                von hier aus kommt man zu vier verschiedenen orten: Zurück ins Dorf Buchenhain, zum
                                alten Steinbruch, zum Pferdebauer und in den Buchenwald.
                                """)
                        .destinations("location.dorfplatz")
//                                "location.buchenwald",
//                                "location.pferdebauer",
//                                "location.alter-steinbruch")
                        .triggers("trigger.blumental-betreten")
                        .build()
        );
    }

    @Override
    protected List<Condition> defineConditions() {
        return List.of(
                new IsCondition(
                        Id.of("condition.mit-dorfvorsteher-geredet"),
                        "prüft ob mit dem dorfvorsteher schon geredet wurde",
                        List.of(getFlag("flag.auftrag-erhalten")))
                ,
                new IsCondition(
                        Id.of("condition.kennt-bedrohung"),
                        "prüft ob mit die spieler wissen, welche Gefahr das Dorf bedroht",
                        List.of(getFlag("flag.kennt-bedrohung")))
                ,
                new AndCondition(
                        Id.of("condition.kapitel-eins-abgeschlossen"),
                        "Chapter 1 Finished Condition, muss den auftrag haben UND muss über Monster bescheid wissen",
                        List.of(
                                getFlag("flag.kennt-bedrohung"),
                                getFlag("flag.auftrag-erhalten")))
                //chapter 2
                ,
                new IsCondition(
                        Id.of("condition.kennt-weg-zum-blumental"),
                        "prüft ob mit die spieler den Weg zum Blumental kennen",
                        List.of(getFlag("flag.kennt-weg-zum-blumental")))
                ,
                new IsCondition(
                        Id.of("condition.kennt-horndiebstahl"),
                        "prüft ob mit die spieler wissen, dass sie das gestohlene Horn von Silena wieder besorgen müssen",
                        List.of(getFlag("flag.kennt-horndiebstahl")))
                ,
                new IsCondition(
                        Id.of("condition.kennt-ritual"),
                        "prüft ob mit die spieler wissen, dass sie das Ritual der Wiederherstellung durchführen müssen",
                        List.of(getFlag("flag.kennt-ritual")))
                ,
                new AndCondition(
                        Id.of("condition.kapitel-zwei-abgeschlossen"),
                        "Chapfter 2 Finished Condition, Spieler muss wissen, dass horn gestohlen wurde und muss wissen, dass das Ritual der wiederherstellung durchgeführt werden muss",
                        List.of(
                                getFlag("flag.kennt-horndiebstahl"),
                                getFlag("flag.kennt-ritual")))
                ,
                new NotCondition(
                        Id.of("condition.schluessel-noch-nicht-gefunden"),
                        "die Spieler haben den SChlüssel am Maktplatz noch NICHT gefunden",
                        List.of(getFlag("flag.schluessel-gefunden")))
        );
    }

    @Override
    protected List<Flag<?>> defineFlags() {
        return List.of(
                new KnowledgeFlag(
                        Id.of("flag.auftrag-erhalten"),
                        "hat mit Dorf-Vorsteher geredet und Quest erhalten",
                        new Knowledge(
                                "Auftrag des Ortsvorstehers",
                                """
                                        die Spieler wissen jetzt, dass ihr Auftrag ist, dass sie die Ursache der Bedrohung des
                                        Dorf herausfinden sollen und die Bedrohung abwenden.
                                        """))
                ,
                new KnowledgeFlag(
                        Id.of("flag.kennt-bedrohung"),
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
                        Id.of("flag.kennt-weg-zum-blumental"),
                        "wissen, dass der weg zum Buchenwald über das Blumental geht",
                        new Knowledge(
                                "Weg zum Buchenwald",
                                """
                                        die Spieler wissen jetzt, dass der Weg zum Buchenwald über das Blumental führt.
                                        """))
                ,
                new LocationFlag(
                        Id.of("flag.blumental-betreten"),
                        "Flag, dass das Blumental betreten wurde",
                        getLocation("location.blumental"))
                ,
                new KnowledgeFlag(
                        Id.of("flag.kennt-horndiebstahl"),
                        "wissen, dass das Horn der Silena gestohlen wurde",
                        new Knowledge(
                                "das Horn der Silena wurde geklaut",
                                """
                                        die Spieler wissen jetzt, dass das Horn der Silena geklaut wurde.
                                        """))
                ,
                new KnowledgeFlag(
                        Id.of("flag.kennt-ritual"),
                        "wissen, dass der das Ritual der Wiederherstellung durchgeführt werden muss",
                        new Knowledge(
                                "das Ritual der Wiederherstellung muss durchgeführt werden",
                                """
                                        die Spieler wissen jetzt, dass das Ritual der Wiederherstellung durchgeführt werden muss.
                                        """))
                ,
                new ItemFlag(
                        Id.of("flag.schluessel-gefunden"),
                        "signalisiert, ob der schlüssel gefunden wurde",
                        getItem("item.eisenschluessel"))

        );
    }

    @Override
    protected List<Trigger> defineTriggers() {
        return List.of(
                new Trigger(Id.of("trigger.bedrohung-fuer-das-dorf"),
                        "Bedrohung oder Gefahr für das Dorf",
                        new Event.Builder()
                                .raisedFlag(getFlag("flag.kennt-bedrohung"))
                                .build())
                ,
                new Trigger(Id.of("trigger.auftrag-erhalten"),
                        "Auftrag oder heikles Thema",
                        new Event.Builder()
                                .raisedFlag(getFlag("flag.auftrag-erhalten"))
                                .build())
                ,
                new Trigger(Id.of("trigger.marktplatz-untersuchen"),
                        "Untersuche den Marktplatz",
                        new Event.Builder()
                                .raisedFlag(getFlag("flag.schluessel-gefunden"))
                                .description("Als die Helden auf den Boden blicken, finden sie einen kleinen schlüssel aus metall")
                                .addedItems(List.of(
                                        getItem("item.eisenschluessel")
                                ))
                                .build())
                ,
                //chapter 2
                new Trigger(Id.of("trigger.weg-zum-buchenwald"),
                        "Weg zum Buchenwald",
                        new Event.Builder()
                                .raisedFlag(getFlag("flag.kennt-weg-zum-blumental"))
                                .description("Die helden lernen, welcher weg zum Buchenhain führt")
                                .build())
                ,
                new Trigger(Id.of("trigger.blumental-betreten"),
                        "ENTER", //LEAVE
                        new Event.Builder()
                                .description("die helden betreten zum ersten mal das Blumental und sind davon sehr angetan")
                                .raisedFlag(getFlag("flag.blumental-betreten"))
                                .build())
        );
    }

    @Override
    protected List<Investigation> defineInvestigations() {
        return List.of(
                new Investigation(
                        Id.of("investigation.marktplatz-schluessel"),
                        "investigate the market Place, find the key",
                        new SkillCheck(),
                        getTrigger("trigger.marktplatz-untersuchen")
                )
        );
    }

}
