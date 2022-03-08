# con10t  

Das vorliegende Repositorium bildet die strukturelle und inhaltliche Grundlage für Arachne-Projektseiten, die unter https://arachne.dainst.org/projects zugänglich sind. 
 
## Kurztutorial: Neue Projektseite erstellen

### Projekttemplate
Um eine neue Projekte zu erstellen, wird die Projektvorlagedatei "template.html" in ihren sprachenspezifischen Ordner kopiert und der Dateiname der kopierten Datei in einen Namen ohne Leer- und Sonderzeichen geänder. Repräsentiert durch ihre Sprachkürzel, bieten die verschiedenen Ordner die Möglichkeit, mehrsprachige Projektseiten zu gestalten und umzusetzen.
So befinden sich arabischsprachige Projektseiten im Ordner "ar", deutsch-, englisch- und italienischsprachige Projektseiten in den Ordner "de", "en" und "it".

Jede Projektseite besteht aus einem Kopf- bzw. Teaserbereich, in dem ein hoch aufgelöstes Bild dargestellt wird, sowie einem Inhaltsbereich, in dem eine Seitenleiste weiterführende Informationen zu Inhalten des Inhaltsbereiches bereitstellt:

```HTML
   
   <!-- Kopfbereich der Projektseite -->
   <idai-header
           image="/con10t/headerimages/hoch-aufgeloestes-bild.jpg"
           description="Kurze Beschreibung des Bildes"
           link="http://verweis-auf-ressource.de">
   </idai-header>
   
   <!-- Hauptbereich der Projektseite -->
   <div class="con10t-page teaserpage container">
   
       <div class="con10t-row">
   
           <div class="con10t-title">
               <!-- Haupttitel des Projektes: Überschrift der Größe <h1> oder Logo / Bild -->
           </div>
   
           <div class="con10t-sidebar">
               <!-- Rechte Seitenleiste -->
           </div>
   
           <div class="con10t-content">
               <!-- Inhalt der Projektseite -->
           </div>
   
       </div>
   </div>
```

### Aktivierung der Projektseite
Um die neu erstellte Projektseite auf der Projektübersichtsseite https://arachne.dainst.org/projects zu aktivieren, muss die Projektseite abschließend in die Datei "content.json" eingehangen werden. Abgebildet in der JSON-Datei "content.json" ist sowohl die Struktur der Seite als auch Verweise auf die einzelnen Projektseiten:
```JSON
{
    "id": "",
    "title": {
        "de": "Inhalte",
        "en": "Content"
    },
    "children": [
        {
            "id": "",
            "title": {
                "de": "DAI - Objektdatenbank",
                "en": "DAI - Objectdatabase"
            },
            "children": [
                {
                    "id": "emagines",
                    "title": {
                        "de": "Emagines",
                        "en": "Emagines"
                    }
                },
                {
                    "id": "fotoistanbul",
                    "title": {
                        "de": "Fotothek DAI Istanbul",
                        "en": "DAI Istanbul photo archive"
                    }
                }
                // [...]
            ]
        }
    ]
}
```
