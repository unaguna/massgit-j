# Build an executable file by launch4j

The executable file to be released is created using the following procedure.

1. Edit [massgit-launch4j.xml](massgit-launch4j.xml); update version number in the filepath of jar.

2. Run `./gradlew shadowJar`; this will make the fat jar file into `./build/libs/`.

3. Run `launch4jc.exe ".\launch4j\massgit-launch4j.xml"`; this will build the executable file into `./launch4j/dist/`.
