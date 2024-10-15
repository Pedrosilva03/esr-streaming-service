X11_DISPLAY = export DISPLAY=:0

all: clientCompile nodeCompile serverCompile

clientCompile:

nodeCompile:

serverCompile:

client:
	@$(X11_DISPLAY)

node: 
	@$(X11_DISPLAY)

server:
	@$(X11_DISPLAY)