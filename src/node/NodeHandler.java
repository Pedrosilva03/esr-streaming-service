package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import utils.Messages;

public class NodeHandler implements Runnable{
    private Socket s;
    private NodeManager manager;

    private DatagramSocket ds;

    private DataInputStream dis;
    private DataOutputStream dos;
    private InetAddress address;

    private String video;

    public NodeHandler(Socket s, NodeManager manager) throws IOException{
        this.s = s;
        this.manager = manager;
        this.ds = new DatagramSocket();
        this.dis = new DataInputStream(s.getInputStream());
        this.dos = new DataOutputStream(s.getOutputStream());
        this.address = s.getInetAddress();
        this.video = new String();
    }

    private void sendPackets(){

    }

    public void run(){
        boolean status = true;
        System.out.println("Cliente " + this.address + " conectado");
        while(status){
            try{
                String request = this.dis.readUTF();

                String[] requestSplit = request.split(" ");

                if(requestSplit[1].equals(Messages.check_video)){
                    if(true){

                    }
                }
                else if(requestSplit[1].equals("READY")){
                    if(this.video != null){
                        Thread t = new Thread(() -> this.sendPackets());
                        t.start();
                    }
                }
                else if(requestSplit[1].equals(Messages.disconnect)){
                    status = false;
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
