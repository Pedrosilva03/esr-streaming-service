package utils;

import java.util.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

/*
 * Class that abstracts all the streaming logic for mutiple usage
 */
public class Streaming implements Runnable{
    private VideoStream video;
    private String videoName; // Para stream no nodo

    private DatagramSocket udpSocket;

    private byte[] lastFrameData;
    private int lastFrameSize;

    private int usersConnected;

    // Lógica focada para o streaming no lado de um nodo intermédio (sem acesso ao ficheiro do vídeo, recebe frames de outro nodo/servidor)
    public Streaming(String videoName, DatagramSocket udpSocket){
        this.lastFrameData = new byte[65535];
        this.lastFrameSize = 0;
        this.usersConnected = 1;
        this.video = null;
        this.videoName = videoName;
        this.udpSocket = udpSocket;
    }

    public void setFrame(byte[] newFrame, int newFrameSize){
        System.arraycopy(newFrame, 0, this.lastFrameData, 0, newFrameSize);
        this.lastFrameSize = newFrameSize;
    }

    private int requestVideoToNeighbour(){
        try{
            // Receber pacote RTP
            byte[] packetData = new byte[65535]; // Buffer grande o suficiente para o cabeçalho + payload
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
            udpSocket.receive(packet);

            // Extrair o RTPpacket
            RTPpacket rtpPacket = new RTPpacket(packet.getData(), packet.getLength());

            // Verificar se o payload é do tipo esperado
            if (rtpPacket.getpayloadtype() == 26) { // Supondo que 26 é o tipo de payload do vídeo (ajustar conforme necessário)
                int frameSize = rtpPacket.getpayload_length();
                
                // Extrair payload (dados do frame)
                byte[] frameData = new byte[frameSize];
                rtpPacket.getpayload(frameData);

                this.setFrame(frameData, frameSize);

                return frameSize;
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return 0;
    }

    // Lógica focada para o streaming no lado do servidor (com acesso ao ficheiro do vídeo)
    public Streaming(VideoStream video){
        this.video = video;
        this.lastFrameData = new byte[65535];
        this.lastFrameSize = 0;
        this.usersConnected = 1;
    }

    public String getVideo(){
        return this.video.getFilename();
    }

    public int getFrame(byte[] data) throws Exception{
        System.arraycopy(this.lastFrameData, 0, data, 0, data.length);
        return this.lastFrameSize;
    }

    public void addUser(){
        this.usersConnected++;
    }

    public void removeUser(){
        this.usersConnected--;
    }

    public void run(){
        while(this.usersConnected > 0){
            try{
                if(video != null) this.lastFrameSize = this.video.getnextframe(this.lastFrameData);
                else this.lastFrameSize = this.requestVideoToNeighbour();
                Thread.sleep(40);
            }
            catch(Exception e){
                if(video != null) System.out.println("Erro ao streamar video: " + video.getFilename() + ". A cancelar..."); // Caso para o servidor
                else System.out.println("Erro ao streamar video: " + videoName + ". A cancelar..."); // Caso para o nodo
                break;
            }
        }
        if(this.udpSocket != null) this.udpSocket.close();
    }
}
