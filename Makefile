JAVAC = javac
JAVA = java
SRC = backend/src/*.java
BIN = backend/bin
LIB = backend/lib/h2-2.4.240.jar
MAIN = Main

$(BIN):
	mkdir -p $(BIN)

compile: $(BIN)
	$(JAVAC) -d $(BIN) -cp $(LIB) $(SRC)

run: compile
	$(JAVA) -cp $(BIN):$(LIB) $(MAIN) $(ARGS)

clean:
	rm -rf $(BIN)

