javac -Xlint:unchecked src/*.java
mkdir -p com/littlesounddj/lsdmanager
mv src/*.class com/littlesounddj/lsdmanager
jar -cfm lsdmanager.jar META-INF/MANIFEST.MF com/littlesounddj/lsdmanager/*.class
rm -r com
