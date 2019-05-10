# Opptelling

De som teller fyller inn i et excel ark slik:

| candidate order |     |     |       |      |
| --------------- | --- | --- | ----- | ---- |
| NAME            | Per | Pål | Nils  | Erik |
| Stemme1         |     | 3   | 2     |  1   |
| Stemme2         |     | 3   | 2     |      |
| Stemme3         | 2   | 1   |       |  3   |
| Stemme4         | 3   | 1   | 2     |      |
| Stemme5         | 1   | 3   | 2     |      |

Når alle stemmene er talt opp, eksporterer man filen i .csv-format og gir den navnet **fagutvalget-cand.csv**.
(Sjekk at csv-filen er delt på komma og ikke semi-kolon).

# For å kjøre programmet

* Klon dette github-repoet
* Gå inn i mappen Valg i terminalen
* Pass på at fagutvalget-cand.csv ligger i denne mappen
* Kjør kommandoen: *java -cp Vote-0-4.jar VoteMain -system stvse-meek -nostrict-min-ranks -logfile log.txt fagutvalget-cand.csv*
* Hvis programmet kjører riktig, skal det printes en rekkefølge. Dette er rekkefølgen kandidatene er rangert, og nr. 1 - 12 blir valgt inn.

# Problemer som kan oppstå
* CSV-filen blir separert med semi-kolon i stedet for komma. Løsning: Search and replace ; med ,
* Programmet sier at første linje ikke starter med "Candidate eller rank ordering". Dette kan være fordi (spesielt på mac) så blir newline-symbolet feil. Løsning: På mac kan man åpne med TextEdit, lagre, og så kjøre på ny. Ellers kan man (kanskje) bruke unix2dos eller dos2unix.
* Har du andre problemer? Husk å legge det inn her når du finner utav de.
