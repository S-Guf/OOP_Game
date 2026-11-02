# Adventure Journal (Java Swing, OOP)

## Compile & run (JDK 11+), from this folder
    cd AdventureJournal
    javac -d out src/*.java        # Windows: javac -d out src\*.java
    java -cp out Main

Run from the project root so that `images/` is found.

## Run from the JAR
`java -jar AdventureJournal.jar` (Java 11+). The JAR contains the images; if an `images/` folder sits next to it, those files are used first (so art can be swapped without rebuilding).

Rebuild the JAR (from this folder):

    mkdir out
    javac -d out src/*.java          (PowerShell: javac -d out (Get-ChildItem src -Filter *.java).FullName)
    xcopy images out\images /E /I    (macOS/Linux: cp -r images out/images)
    jar cfm AdventureJournal.jar manifest.txt -C out .

`manifest.txt` contains one line: `Main-Class: Main`
