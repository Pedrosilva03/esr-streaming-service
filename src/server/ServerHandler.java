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

    private void sendPackets(){

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
                        this.dos.writeInt(1);
                    }
                    else this.dos.writeInt(0);
                    this.dos.flush();
                }

                if(requestSplit[1].equals("READY")){
                    // TODO: Send video packets if confirmed
                    this.sendPackets();
                }

                if(requestSplit[1].equals(Messages.disconnect)){
                    status = false;
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
