package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import utils.Messages;
import utils.Ports;

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

    private void sendPackets(){
        this.activeStreaming = true;
        this.manager.createStream(this.video);
        while(activeStreaming){
            try{                
                byte b[] = new byte[65535];
                int frame_length = this.manager.stream(video, b);
                //int frame_length = this.video.getnextframe(b);

                // Envio do tamanho do frame
                ByteBuffer size = ByteBuffer.allocate(4);
                size.putInt(frame_length);

                DatagramPacket frame_size = new DatagramPacket(size.array(), size.array().length, this.address, Ports.DEFAULT_CLIENT_UDP_PORT);
                ds.send(frame_size);

                DatagramPacket frame = new DatagramPacket(b, frame_length, this.address, Ports.DEFAULT_CLIENT_UDP_PORT);
                ds.send(frame);

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

                this.manager.viewedMessages.put(Integer.parseInt(requestSplit[0]), request);

                if(requestSplit[1].equals(Messages.check_video)){
                    if(this.manager.checkVideoExists(address.getHostAddress(), requestSplit[2], request, this.neighboursWithVideo)){
                        this.video = requestSplit[2];
                        this.dos.writeInt(1);
                    }
                    else this.dos.writeInt(0);
                    this.dos.flush();
                }
                else if(requestSplit[1].equals("READY")){
                    if(this.video != null){
                        Thread t = new Thread(() -> this.sendPackets());
                        t.start();
                    }
                }
                else if(requestSplit[1].equals(Messages.disconnect)){
                    status = false;
                    this.activeStreaming = false;
                    continue;
                }
            }
            catch(Exception e){
                System.out.println("Cliente " + this.address + " desconectado inesperadamente");
                return;
            }
        }
        System.out.println("Cliente " + this.address + " desconectado com sucesso");
    }
}
