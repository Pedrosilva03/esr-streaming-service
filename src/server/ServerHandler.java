package server;

import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.InetAddress;

import utils.*;

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

    private void sendPackets(){
        this.activeStreaming = true;
        this.database.createStream(video);
        while(activeStreaming){
            try{
                byte b[] = new byte[15000];
                int frame_length = this.database.stream(video, b);
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
        this.database.disconnectUser(video);
    }

    public void run(){
        boolean status = true;
        String requestedVideo;

        System.out.println("Cliente " + this.address + " conectado");
        while(status){
            try{
                String request = this.dis.readUTF();

                String[] requestSplit = request.split(" ");

                if(requestSplit[1].equals(Messages.check_video)){
                    if(this.database.checkVideoExists(requestSplit[2])){
                        requestedVideo = requestSplit[2];
                        this.video = this.database.getVideo(requestedVideo);
                        this.dos.writeInt(1);
                    }
                    else this.dos.writeInt(0);
                    this.dos.flush();
                }

                if(requestSplit[1].equals("READY")){
                    if(this.video != null){
                        Thread t = new Thread(() -> this.sendPackets());
                        t.start();
                    }
                }

                if(requestSplit[1].equals(Messages.disconnect)){
                    status = false;
                    activeStreaming = false;
                    continue;
                }
            }
            catch(IOException e){
                System.out.println("Cliente " + this.address + " desconectado inesperadamente");
                return;
            }
        }
        System.out.println("Cliente " + this.address + " desconectado com sucesso");
    }
}
