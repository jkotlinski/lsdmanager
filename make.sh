javac -Xlint:unchecked src/*.java
mv src/*.class .
jar -cfm lsdmanager.jar META-INF/MANIFEST.MF src/*.java *.class
rm *.class
