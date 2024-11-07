package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import utils.Messages;
import utils.RTPpacket;

public class NodeHandler implements Runnable{
    private Socket s;
    private NodeManager manager;

    private DatagramSocket ds;

    private DataInputStream dis;
    private DataOutputStream dos;
    private InetAddress address;

    private String video;
    private boolean activeStreaming;

    private List<String> neighboursWithVideo;

    public NodeHandler(Socket s, NodeManager manager) throws IOException{
        this.s = s;
        this.manager = manager;
        this.ds = new DatagramSocket();
        this.dis = new DataInputStream(s.getInputStream());
        this.dos = new DataOutputStream(s.getOutputStream());
        this.address = s.getInetAddress();
        this.video = new String();
        this.neighboursWithVideo = new ArrayList<>();
    }

    private void sendPackets(String request){
        this.activeStreaming = true;
        this.manager.createStream(this.address.getHostAddress(), this.video, this.neighboursWithVideo, request);
        while(activeStreaming){
            try{                
                byte[] frameData = new byte[65535];
                int frameLength = this.manager.stream(video, frameData);
                //int frame_length = this.video.getnextframe(b);

                int sequenceNumber = 0;
                int timestamp = (int) System.currentTimeMillis();

                RTPpacket rtpPacket = new RTPpacket(26, sequenceNumber, timestamp, frameData, frameLength);

                byte[] packetData = new byte[rtpPacket.getlength()];
                int packetLength = rtpPacket.getpacket(packetData);

                DatagramPacket packet = new DatagramPacket(packetData, packetLength, this.address, Integer.parseInt(request.split(" ")[3]));
                ds.send(packet);

                sequenceNumber++;

                Thread.sleep(40);
            }
            catch(Exception e){
                System.out.println("Error getting frame to stream");
                return;
            }
        }
        this.manager.disconnectUser(video);
    }

    public void run(){
        boolean status = true;
        System.out.println("Cliente " + this.address + " conectado");
        while(status){
            try{
                String request = this.dis.readUTF();

                String[] requestSplit = request.split(" ");

                if(this.manager.viewedMessages.containsKey(Integer.parseInt(requestSplit[0]))){
                    this.dos.writeInt(0);
                    this.dos.flush();
                    continue;
                }

                System.out.println(requestSplit[0] + " " + requestSplit[1]);

                this.manager.viewedMessages.put(Integer.parseInt(requestSplit[0]), request);

                if(requestSplit[1].equals(Messages.check_video)){
                    if(this.manager.checkVideoExists(address.getHostAddress(), requestSplit[2], request, this.neighboursWithVideo)){
                        this.video = requestSplit[2];
                        this.dos.writeInt(1);
                    }
                    else this.dos.writeInt(0);
                    this.dos.flush();
                }
                else if(requestSplit[1].equals(Messages.ready)){
                    this.video = new String(requestSplit[2]);
                    if(this.video != null){
                        Thread t = new Thread(() -> this.sendPackets(request));
                        t.start();
                    }
                }
                else if(requestSplit[1].equals(Messages.disconnect)){
                    status = false;
                    this.activeStreaming = false;
                    continue;
                }
                else if(requestSplit[1].equals(Messages.ping)){
                    this.dos.writeInt(1);
                    this.dos.flush();
                }
            }
            catch(Exception e){
                System.out.println("Cliente " + this.address + " desconectado inesperadamente");
                this.manager.disconnectUser(video);
                this.activeStreaming = false;
                this.closeSocket();
                return;
            }
        }
        this.closeSocket();
        System.out.println("Cliente " + this.address + " desconectado com sucesso");
    }

    private void closeSocket(){
        try{
            this.dis.close();
            this.dos.close();
            this.s.close();
            this.ds.close();
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
}
