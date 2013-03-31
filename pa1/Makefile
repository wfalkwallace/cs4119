JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java
files = Client.java Server.java Player.java TicTacToeGame.java 
default: $(files:.java=.class)
clean:
	rm -f *.class
