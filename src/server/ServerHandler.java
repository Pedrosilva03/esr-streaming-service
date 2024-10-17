package server;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import utils.*;

public class ServerHandler implements Runnable{
    private Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;
    private InetAddress address;
    private Manager database;
    
    public ServerHandler(Socket s, Manager database) throws IOException{
        this.s = s;
        this.dis = new DataInputStream(s.getInputStream());
        this.dos = new DataOutputStream(s.getOutputStream());
        this.address = s.getInetAddress();
        this.database = database;
    }

    public void run(){
        boolean status = true;
        boolean ready = false;
        String requestedVideo;
        while(status){
            try{
                String request = dis.readUTF();
                String[] requestSplit = request.split(" ");

                if(requestSplit[0].equals(Messages.check_video)){
                    if(database.checkVideoExists(request)){
                        requestedVideo = requestSplit[1];
                        dos.writeInt(1);
                    }
                    else dos.writeInt(0);
                }

                if(requestSplit[0].equals("READY")){
                    status = false;
                    ready = true;
                }

                if(requestSplit[0].equals(Messages.disconnect)){
                    status = false;
                }
            }
            catch(IOException e){
                System.out.println("Cliente " + address + " desconectado inesperadamente");
            }
        }

        // Send video packets se tiver confirmação

        if(ready){
            
        }
    }


}
