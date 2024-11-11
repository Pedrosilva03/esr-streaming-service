package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;

import utils.Extras;
import utils.Messages;
import utils.Ports;
import utils.Streaming;
import utils.UpdateVisualizer;

public class NodeManager {
    private HashMap<String, Streaming> streamingCurrently;

    private List<String> neighbours;

    public HashMap<Integer, String> viewedMessages;
    public NodeManager(){
        this.streamingCurrently = new HashMap<>();
        this.neighbours = Extras.getNeighborsIPs(Extras.getLocalAddress());
        this.viewedMessages = new HashMap<>();
    }

    public void connectUser(String video){
        this.streamingCurrently.get(video).addUser();
    }

    public void disconnectUser(String requestAddress, String video){
        try{
            this.streamingCurrently.get(video).removeUser();
            UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 0);
        }
        catch(NullPointerException e){}
    }

    /*
     * Esta função verifica se este nodo está a streamar o vídeo pedido e se não, pergunta aos vizinhos se o vídeo existe na rede
     */
    public boolean checkVideoExists(String requestAddress, String video, String message, List<String> neighboursWithVideo){
        if(!this.checkIfStreamOn(video)){
            for(String neighbour: this.neighbours){ // Flooding da mensagem de verificação da existencia do vídeo
                if(neighbour.equals(requestAddress)) continue; // Caso para evitar enviar mensagens para trás. Quem pediu verificação a este nodo não volta a receber o mesmo pedido
                if(NodeRecursive.checkVideoOnNode(message, neighbour)) neighboursWithVideo.add(neighbour); // Se um nodo responder afirmativamente, adiciona a uma lista de possíveis nodos para pedir frames
            }
            if(neighboursWithVideo.isEmpty()) return false; // Caso não encontre, o vídeo não existe na rede.
            else return true;
        }
        else return true;
    }

    /*
     * Função responsável por avisar o nodo que estava a enviar frames para aqui que já não é preciso e por fechar os sockets e streams.
     */
    private void closeStream(DatagramSocket udpSocket, Socket aux, DataInputStream dis, DataOutputStream dos) throws IOException{
        dos.writeUTF(Messages.generateDisconnectMessage());
        dos.flush();

        dis.close();
        dos.close();
        aux.close();

        udpSocket.close();
    }

    /*
     * Função principal de criação e inicio de stream
     */
    public void createStream(String requestAddress, String video, List<String> neighboursWithVideo, String request){
        if(!this.streamingCurrently.containsKey(video)){
            List<String> neighbour;
            if(!neighboursWithVideo.isEmpty()) neighbour = Extras.pingNeighbours(requestAddress, neighboursWithVideo); // Inútil porque os nodos não guardam conexão então isto dá reset (preguiça de apagar)
            else neighbour = Extras.pingNeighbours(requestAddress, this.neighbours); // Isto é mais porque o if de cima é um bocado inútil (retorna os vizinhos por ordem de menor RTT)

            neighbour = NodeRecursive.checkIfStreamOn(neighbours, video);

            for(String neighbourFast: neighbour){ // Corre os vizinhos por ordem de prioridade e tenta estabelecer uma conexão com eles
                try{
                    Socket aux = new Socket(neighbourFast, Ports.DEFAULT_NODE_TCP_PORT);
                    aux.setSoTimeout(200); // Timeout para receber mensagens de erro
                    DataInputStream dis = new DataInputStream(aux.getInputStream());
                    DataOutputStream dos = new DataOutputStream(aux.getOutputStream());

                    DatagramSocket udpSocket = new DatagramSocket(Integer.parseInt(request.split(" ")[3]));

                    dos.writeUTF(request);
                    dos.flush();

                    try{
                        if(dis.readInt() == 0){ // Espera por um sinal de erro (caso o socket dê timeout, está tudo bem, continuar para a criação da stream)
                            dos.writeUTF(Messages.generateDisconnectMessage());
                            dos.flush();

                            dis.close();
                            dos.close();
                            aux.close();

                            udpSocket.close();
                            continue;
                        }
                    }
                    catch(SocketTimeoutException e){}
                
                    Streaming s = new Streaming(video, udpSocket);
                    this.streamingCurrently.put(video, s); // Cria a stream e coloca a na lista de streams ativas neste nodo
                    Thread t = new Thread(() -> { // Thread que vai ficar atenta ao fim da stream para notificar o fornecedor da stream
                        Thread tt = new Thread(s); // Thread dedicada a receber pacote e guardá-los
                        tt.start();
                        System.out.println("Streaming do video: " + video + " iniciada.");
                        try{
                            tt.join();
                            this.streamingCurrently.remove(video); // Remove a stream da lista de streams ativas neste nodo
                            System.out.println("Streaming do video: " + video + " fechada.");
                            UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 0); // Atualiza o simulador da rede overlay

                            this.closeStream(udpSocket, aux, dis, dos); // Notifica o fornecedor e liberta recursos
                        }
                        catch(InterruptedException | IOException e){
                            System.out.println(e.getMessage());
                        }
                    });
                    t.start();
                    break; // Encontrou o nodo fornecedor, então não precisa de correr outros vizinhos
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
        else this.connectUser(video); // Caso já esteja a streamar o vídeo pedido, apenas adiciona o cliente/nodo
        UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 1); // Atualiza o simulador da rede overlay
    }

    /*
     * Função que retorna o frame atual (double return: preenche o buffer com o frame, e retorna o tamanho desse frame ao mesmo tempo)
     */
    public int stream(String video, byte[] data) throws Exception{
        return this.streamingCurrently.get(video).getFrame(data);
    }

    public boolean checkIfStreamOn(String video){
        return this.streamingCurrently.containsKey(video);
    }
}
