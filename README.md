<h1 align="center">Projeto da UC de Engenharia de Sistemas em Rede - 2024/2025</h1>
<h2 align="center">Serviço de Streaming</h2>

## Definição

Sistema Over-the-top (OTT) para distribuição e visualização de conteúdos.
- Solução para entrega massiva de conteúdos sem problemas de congestão
- Alternativa para contornar as limitações de recursos da rede

## Notas do projeto
- Este projeto foi desenvolvido tendo como base a ferramente CORE para simulação de uma topologia de rede real
- A descrição completa de todo o projeto pode ser encontrada no [relatório](https://github.com/Pedrosilva03/esr-streaming-service/blob/main/doc/ESR_TP2_PL14.pdf)

## Visão geral dos programas
Este repositório contém 3 programas base e vários outros programas auxiliares para o projeto
- Estes programas utilizam um ficheiro [bootstrapper](https://github.com/Pedrosilva03/esr-streaming-service/blob/main/config/bootstrapper.json) para obter os seus vizinhos
- Este bootstrapper é restrito à topologia fornecida
- Mais detalhes sobre estes programas e os programas auxiliares estão descritos nas [funcionalidades](#funcionalidades)
- O protocolo de mensagens será especificado [aqui](#protocolos)

### Cliente
- Cliente para consumo de conteúdos
- Fornece uma interface com opções de pause, play e stop.

### Nodo
- Nodo para distribuição de conteúdos
- Este programa não é interativo
- Analisa as condições de rede para fornecer a melhor qualidade de vídeo

### Servidor
- Servidor de streaming para hosting de conteúdos
- Guarda os vídeos originais para streaming

## Funcionalidades
Todos os comandos devem ser executados na raiz do repositório

### Compilação
#### DISCLAIMER
- - O código pode ter problemas com o classpath ao abrir num editor de texto. Para resolver isso basta adicionar ou remover a pasta ```src``` do classpath (um deles deve funcionar)
- - Isto não terá implicações nenhumas na execução visto que a makefile trata do classpath automaticamente. É apenas para efeitos de erros no editor de texto

- Para compilar o código basta correr o comando
```console
make
```

- Além disso é possível compilar cada um dos programas principais com os comandos
```console
make client
```
```console
make node
```
```console
make server
```

- Todos os artifactos de compilação podem ser apagados com o comando
```console
make clean
```

### Client
O cliente permite assistir conteúdos fornecidos pelo servidor e pode ser executado com o comando
```console
make run_client
```
- Este programa é de execução única pelo que é preciso fechar e abrir o programa para pedir um novo vídeo
- No início é pedido o nome do vídeo pretendido, que deve ser escrito com o formato.
```console
movie.Mjpeg
```
- O cliente enviará um pedido de verificação para o sistema
- Dependendo da resposta, o cliente fecha com um aviso ou abre a interface

### Node
O nodo é responsável por reproduzir conteúdos vindos de apenas uma stream para vários nodos, poupando assim recursos e favorecendo escalabilidade. Pode ser executado com o comando
```console
make run_node
```
- O nodo funciona como uma réplica do servidor, permitindo aliviar pressão do servidor principal
- Tem a capacidade de reencaminhar mensagens usando o método de inundação controlada
- O nodo consegue reconhecer mensagens que já leu, evitanto processamento desnecessário de mensagens e mensagens enviadas infinitamente de nodo para nodo
- Apenas distribui conteúdos caso existem clientes a consumir, deste modo poupando recursos da rede
- O nodo não é interativo sendo apenas possível interromper a sua execução com o atalho ```Ctrl+C```

### Server
O servidor é responsável pelo hosting das streams. Funciona como raiz das árvores de distribuição dos conteúdos e pode ser executado com o comando
```console
make run_server
```
- Apenas o servidor tem acesso às cópias originais dos vídeos
- Tal como o nodo, apenas distribui os vídeos caso hajam clientes conectados, reduzindo assim utilização desnecessária de recursos da rede
- Os vídeos disponíveis estão na pasta [videos](https://github.com/Pedrosilva03/esr-streaming-service/tree/main/videos) e devem estar no formato Mjpeg com tamanho de frame (conversor pode ser encontrado [aqui](#conversor))
- O servidor não é interativo sendo apenas possível interromper a sua execução com o atalho ```Ctrl+C```

### Programas auxiliares
Este projeto disponibiliza vários programas auxiliares (não fundamentais para o funcionamento base do projeto)

#### Visualizador da rede overlay
- Este programa permite ver uma representação dinâmica de toda a rede
- É possível ver quantas pessoas estão conectadas a cada nodo
- Também é possível ver os caminhos que as streams estão a fazer
- Suporta atualização em tempo real e funciona de forma independente
- Deve ser executado no Router1 da topologia fornecida com o comando
```console
make run_visualizer
```

#### Conversor
- Este programa lê vídeos numa variação do formato Mjpeg (código fornecido pelos professores)
- Este formato não é encontrado habitualmente pelo que os vídeos devem ser adaptados
- Este programa transforma um vídeo num formato Mjpeg normal num formato Mjpeg com tamanho de frame.
  - O novo vídeo terá o tamanho de cada frame antes do frame
  - Desta forma consegue ser lido pelo servidor
- Este programa apenas aceita vídeos Mjpeg. É possível converter videos para Mjpeg usando o ffmpeg através do seguinte comando:
```console
ffmpeg -i video_original.mp4 -vf scale=width:height -c:v mjpeg -q:v 2 -an video_convertido.Mjpeg
```
- O vídeo a converter deve ser colocado dentro da pasta vídeos sobre o nome ```output.Mjpeg```

## Protocolos
Esta secção explica o protocolo de mensagens utilizado no sistema
### Mensagens
- Verificação da existência de um vídeo
  - Esta mensagem serve para verificar se um vídeo existe na rede
  - O formato é o seguinte:
```txt
ID CHECK_VIDEO video_name
```
- Pedido de vídeo
  - Caso a verificação seja um sucesso, então o cliente está pronto para receber um vídeo
  - Prepara e envia uma mensagem para o seu vizinho
  - Nesta mensagem são enviados o nome do vídeo e a porta UDP onde o cliente pretende assistir ao vídeo.
```txt
ID READY video_name UDP_PORT
```
  - Por simplicidade, a porta UDP que o cliente gerar será utilizada nos nodos (probabilidade extremamemte baixa de haver conflitos com portas em uso)
  - Esta mensagem será espalhada pelos nodos caso estes não tenham a stream pedida ativa

- Pedido de desconexão
  - Quando o cliente pretende parar de assistir, envia um pedido de desconexão ao seu provedor
```txt
ID DISCONNECT
```
  - O seu provedor interrompe a stream para este cliente, o cliente liberta recursos e fecha.
  - Caso o provedor não tenha mais utilizadores conectados, também envia uma mensagem de desconexão para o seu provedor (assim sucessivamente até chegar ao servidor)

- Mensagem de ping
  - Para testes de qualidade das conexões, os nodos mandam pings uns aos outros para obter informações de qualidade
  - Depois estes usam essas informações para escolher os melhores vizinhos
```txt
ID PING
```

- Pedido de verificação de stream ligada
  - Utilizado também como parte da criação de estatísticas sobre os vizinhos
  - Um nodo pode perguntar se um vizinho tem a stream pedida ligada e irá priveligiar esse nodo
```txt
ID CHECK_STREAM video_name
```

## Conclusão
Trabalho realizado por Pedro Silva, Diogo Barros e Miguel Pinto
