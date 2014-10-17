all: corpus text

corpus:
	javac -source 1.7 -target 1.7 -cp "dep/*" src/corpus/*.java

text:
	javac -source 1.7 -target 1.7 src/text/*.java
