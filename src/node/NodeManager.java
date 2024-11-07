package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utils.Extras;
import utils.Messages;
import utils.Ports;
import utils.Streaming;

public class NodeManager {
    private HashMap<String, Streaming> streamingCurrently;

    private List<String> neighbours;

    private DatagramSocket udpSocket;

    public HashMap<Integer, String> viewedMessages;
    public NodeManager(){
        this.streamingCurrently = new HashMap<>();
        this.neighbours = Extras.getNeighborsIPs(Extras.getLocalAddress());
        this.viewedMessages = new HashMap<>();
        try{
            this.udpSocket = new DatagramSocket(Ports.DEFAULT_NODE_UDP_PORT);
        }
        catch(SocketException e){
            System.out.println("Erro ao abrir socket UDP");
        }
    }

    public void connectUser(String video){
        this.streamingCurrently.get(video).addUser();
    }

    public void disconnectUser(String video){
        try{
            this.streamingCurrently.get(video).removeUser();
        }
        catch(NullPointerException e){}
    }

    public boolean checkVideoExists(String requestAddress, String video, String message, List<String> neighboursWithVideo){
        if(!this.streamingCurrently.containsKey(video)){
            for(String neighbour: this.neighbours){
                if(neighbour.equals(requestAddress)) continue;
                if(NodeRecursive.checkVideoOnNode(message, neighbour)) neighboursWithVideo.add(neighbour);
            }
            if(neighboursWithVideo.isEmpty()) return false;
            else return true;
        }
        else return true;
    }

    private void closeStream(DatagramSocket udpSocket, Socket aux, DataInputStream dis, DataOutputStream dos) throws IOException{
        dos.writeUTF(Messages.generateDisconnectMessage());
        dos.flush();

        dis.close();
        dos.close();
        aux.close();

        udpSocket.close();
    }

    public void createStream(String requestAddress, String video, List<String> neighboursWithVideo, String request){
        if(!this.streamingCurrently.containsKey(video)){
            List<String> neighbour;
            if(!neighboursWithVideo.isEmpty()) neighbour = Extras.pingNeighbours(requestAddress, neighboursWithVideo);
            else neighbour = Extras.pingNeighbours(requestAddress, this.neighbours);

            for(String neighbourFast: neighbour){
                try{
                    Socket aux = new Socket(neighbourFast, Ports.DEFAULT_NODE_TCP_PORT);
                    aux.setSoTimeout(200);
                    DataInputStream dis = new DataInputStream(aux.getInputStream());
                    DataOutputStream dos = new DataOutputStream(aux.getOutputStream());

                    DatagramSocket udpSocket = new DatagramSocket(Integer.parseInt(request.split(" ")[3]));

                    dos.writeUTF(request);
                    dos.flush();

                    try{
                        if(dis.readInt() == 0){
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
                    this.streamingCurrently.put(video, s);
                    Thread t = new Thread(() -> {
                        Thread tt = new Thread(s);
                        tt.start();
                        System.out.println("Streaming do video: " + video + " iniciada.");
                        try{
                            tt.join();
                            this.streamingCurrently.remove(video);
                            System.out.println("Streaming do video: " + video + " fechada.");

                            this.closeStream(udpSocket, aux, dis, dos);
                        }
                        catch(InterruptedException | IOException e){
                            System.out.println(e.getMessage());
                        }
                    });
                    t.start();
                    break;
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
        else this.connectUser(video);
    }

    public int stream(String video, byte[] data) throws Exception{
        return this.streamingCurrently.get(video).getFrame(data);
    }
}
