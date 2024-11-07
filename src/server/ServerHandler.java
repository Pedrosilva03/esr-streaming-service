package server;

import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    private void sendPackets(String portString){
        this.activeStreaming = true;
        this.database.createStream(this.address.getHostAddress(), video);
        while(activeStreaming){
            try{
                byte[] frameData = new byte[65535];
                int frameLength = this.database.stream(video, frameData);
                //int frame_length = this.video.getnextframe(b);

                int sequenceNumber = 0;
                int timestamp = (int) System.currentTimeMillis();

                RTPpacket rtpPacket = new RTPpacket(26, sequenceNumber, timestamp, frameData, frameLength);

                byte[] packetData = new byte[rtpPacket.getlength()];
                int packetLength = rtpPacket.getpacket(packetData);

                DatagramPacket packet = new DatagramPacket(packetData, packetLength, this.address, Integer.parseInt(portString));
                ds.send(packet);

                sequenceNumber++;

                Thread.sleep(40);
            }
            catch(Exception e){
                System.out.println("Error getting frame to stream");
                this.database.disconnectUser(this.address.getHostAddress(), video);
                return;
            }
        }
        this.database.disconnectUser(this.address.getHostAddress(), video);
    }

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

    public void run(){
        boolean status = true;
        String requestedVideo;

        System.out.println("Cliente " + this.address + " conectado");
        while(status){
            try{
                String request = this.dis.readUTF();

                String[] requestSplit = request.split(" ");

                if(this.database.viewedMessages.containsKey(Integer.parseInt(requestSplit[0]))){
                    this.dos.writeInt(0);
                    this.dos.flush();
                    continue;
                }

                System.out.println(requestSplit[0] + " " + requestSplit[1]);

                this.database.viewedMessages.put(Integer.parseInt(requestSplit[0]), request);

                if(requestSplit[1].equals(Messages.check_video)){
                    if(this.database.checkVideoExists(requestSplit[2])){
                        requestedVideo = requestSplit[2];
                        this.video = this.database.getVideo(requestedVideo);
                        this.dos.writeInt(1);
                    }
                    else this.dos.writeInt(0);
                    this.dos.flush();
                }

                if(requestSplit[1].equals(Messages.ready)){
                    this.video = this.database.getVideo(requestSplit[2]);
                    if(this.video != null){
                        Thread t = new Thread(() -> this.sendPackets(requestSplit[3]));
                        t.start();
                    }
                }

                if(requestSplit[1].equals(Messages.disconnect)){
                    status = false;
                    activeStreaming = false;
                    continue;
                }
                if(requestSplit[1].equals(Messages.ping)){
                    this.dos.writeInt(1);
                    this.dos.flush();
                }
            }
            catch(IOException e){
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
