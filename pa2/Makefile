JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java
files = SRNode.java SRReceiver.java DVNode.java SDNode.java SDSR.java
default: $(files:.java=.class)
clean:
	rm -f *.class
