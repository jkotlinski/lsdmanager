# For Cygwin.
javac -Xbootclasspath:c:/Program\ Files/Java/jre6/lib/rt.jar -source 1.6 -target 1.6 src/*.java
mkdir -p com/littlesounddj/lsdmanager
mv src/*.class com/littlesounddj/lsdmanager
jar -cfm lsdmanager.jar META-INF/MANIFEST.MF com/littlesounddj/lsdmanager/*.class
rm -r com
