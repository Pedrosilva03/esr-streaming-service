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

    private void sendPackets(){
        this.activeStreaming = true;
        while(activeStreaming){
            try{
                byte b[] = new byte[65335];
                int frame_length = this.video.getnextframe(b);

                DatagramPacket frame = new DatagramPacket(b, frame_length, this.address, Ports.DEFAULT_CLIENT_UDP_PORT);
                ds.send(frame);

                System.out.println("Frame enviado");

                Thread.sleep(40);
            }
            catch(Exception e){
                System.out.println("Error getting frame to stream");
                e.printStackTrace();
                break;
            }
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
                    // TODO: Send video packets if confirmed
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
