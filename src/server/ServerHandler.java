package server;

import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import utils.*;

/*
 * Classe que atende a pedidos de um utilizador conectado
 */
public class ServerHandler implements Runnable{
    private Socket s;
    private DatagramSocket ds;

    private DataInputStream dis;
    private DataOutputStream dos;
    private InetAddress address;
    private Manager database;

    private VideoStream video;
    private boolean activeStreaming;
    
    public ServerHandler(Socket s, Manager database) throws IOException{
        this.s = s;
        this.ds = new DatagramSocket();
        this.dis = new DataInputStream(s.getInputStream());
        this.dos = new DataOutputStream(s.getOutputStream());
        this.address = s.getInetAddress();
        this.database = database;
        this.video = null;
    }

    /*
     * Classe responsável por criar uma stream caso ela esteja inativa e enviar frames (packets RTP)
     */
    private void sendPackets(String portString){
        this.activeStreaming = true;
        this.database.createStream(this.address.getHostAddress(), video); // Cria a stream (endereço do nodo que pediu serve para atualizar o simulador)
        while(activeStreaming){
            try{
                byte[] frameData = new byte[65535];
                int frameLength = this.database.stream(video, frameData); // Lê o frame mais recente
                //int frame_length = this.video.getnextframe(b);

                // Criação do packet RTP
                int sequenceNumber = 0;
                int timestamp = (int) System.currentTimeMillis();

                RTPpacket rtpPacket = new RTPpacket(26, sequenceNumber, timestamp, frameData, frameLength);

                byte[] packetData = new byte[rtpPacket.getlength()];
                int packetLength = rtpPacket.getpacket(packetData);

                DatagramPacket packet = new DatagramPacket(packetData, packetLength, this.address, Integer.parseInt(portString));
                ds.send(packet);

                sequenceNumber++;

                Thread.sleep(40); // 40 milissegundos para simular 25fps
            }
            catch(Exception e){
                System.out.println("Error getting frame to stream");
                this.database.disconnectUser(this.address.getHostAddress(), video); // Caso haja algum erro, desconecta o utilizador para evitar streams abertas infinitamente
                return;
            }
        }
        this.database.disconnectUser(this.address.getHostAddress(), video);
    }

    /*
     * Função responsável por libertar recursos associados à conexão
     */
    private void closeSocket(){
        try{
            this.dis.close();
            this.dos.close();
            this.s.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
     * Função que espera ativamente por pedidos do cliente conectado
     */
    public void run(){
        boolean status = true;
        String requestedVideo;

        System.out.println("Cliente " + this.address + " conectado");
        while(status){
            try{
                String request = this.dis.readUTF();

                String[] requestSplit = request.split(" ");

                if(this.database.viewedMessages.containsKey(Integer.parseInt(requestSplit[0]))){ // Verifica se a mensagem que recebeu é repetida ou não (evitar redundância e loops)
                    this.dos.writeInt(0); // Avisa o cliente que a mensagem já foi lida
                    this.dos.flush();
                    continue;
                }

                System.out.println(requestSplit[0] + " " + requestSplit[1]);

                this.database.viewedMessages.put(Integer.parseInt(requestSplit[0]), request); // Se é a primeira vez que lê a mensagem, adiciona-a à base de dados

                // Verificação se um vídeo existe na base de dados ou não (retorna 1 se sim, 0 se não)
                if(requestSplit[1].equals(Messages.check_video)){
                    if(this.database.checkVideoExists(requestSplit[2])){
                        requestedVideo = requestSplit[2];
                        this.video = this.database.getVideo(requestedVideo);
                        this.dos.writeInt(1);
                    }
                    else this.dos.writeInt(0);
                    this.dos.flush();
                }

                if(requestSplit[1].equals(Messages.checkIfStreamOn)){
                    this.dos.writeInt(1);
                    this.dos.flush();
                }

                // Prepara a stream para enviar packets para o cliente
                if(requestSplit[1].equals(Messages.ready)){
                    this.video = this.database.getVideo(requestSplit[2]);
                    if(this.video != null){
                        Thread t = new Thread(() -> this.sendPackets(requestSplit[3])); // Thread responsável por criar a stream (se necessário) e conectar o cliente
                        t.start();
                    }
                    this.dos.writeInt(1);
                    this.dos.flush();
                }

                // Desconecta o cliente, pára os loops de transmissão de frames e o de espera de mensagens
                if(requestSplit[1].equals(Messages.disconnect)){
                    status = false;
                    activeStreaming = false;
                    continue;
                }

                // Responde ao pedido de ping com 1. Serve apenas para verificação de conexão
                if(requestSplit[1].equals(Messages.ping)){
                    this.dos.writeInt(1);
                    this.dos.flush();
                }
            }
            catch(IOException e){ // Se houver algum crash durante a conexão, pára a stream para o cliente e liberta recursos associados
                System.out.println("Cliente " + this.address + " desconectado inesperadamente");
                activeStreaming = false;
                this.closeSocket();
                return;
            }
        }
        System.out.println("Cliente " + this.address + " desconectado com sucesso");
        this.closeSocket();
    }
}
