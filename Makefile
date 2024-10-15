# Variáveis globais
DISPLAY := :0
JAVAC := javac
JAVA := java
SRC_DIR := src
UTILS_SRC := $(SRC_DIR)/utils/*.java
CLIENT_SRC := $(SRC_DIR)/client/*.java
NODE_SRC := $(SRC_DIR)/node/*.java
SERVER_SRC := $(SRC_DIR)/server/*.java
CLASSPATH := -cp $(SRC_DIR)

# Compilação
all: client node server

utils:
	$(JAVAC) $(UTILS_SRC)

client: utils
	$(JAVAC) $(CLASSPATH) $(CLIENT_SRC)

node: utils
	$(JAVAC) $(CLASSPATH) $(NODE_SRC)

server: utils
	$(JAVAC) $(CLASSPATH) $(SERVER_SRC)

# Execução usando a variável DISPLAY
run_client:
	DISPLAY=$(DISPLAY) $(JAVA) $(CLASSPATH) client.OClient

run_node:
	DISPLAY=$(DISPLAY) $(JAVA) $(CLASSPATH) node.ONode

run_server:
	DISPLAY=$(DISPLAY) $(JAVA) $(CLASSPATH) server.Server

clean:
	rm -f $(SRC_DIR)/utils/*.class $(SRC_DIR)/client/*.class $(SRC_DIR)/node/*.class $(SRC_DIR)/server/*.class