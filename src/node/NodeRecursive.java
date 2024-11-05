package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import utils.Messages;
import utils.Ports;

/*
 * Classe que trata de todos os pedidos a outros nodos
 * Trata de pedir por frames a outros nodos
 * Pedidos de existência de um video na rede
 */
public class NodeRecursive {
    public static boolean checkVideoOnNode(String message, String address){
        try{
            Socket aux = new Socket(address, Ports.DEFAULT_NODE_TCP_PORT);
            DataInputStream dis = new DataInputStream(aux.getInputStream());
            DataOutputStream dos = new DataOutputStream(aux.getOutputStream());

            dos.writeUTF(message);
            dos.flush();

            int valid = dis.readInt();

            dos.writeUTF(Messages.generateDisconnectMessage());
            dos.flush();
            
            dis.close();
            dos.close();
            aux.close();
            
            if(valid == 1) return true;
            else return false;
        }
        catch(Exception e){
            //System.out.println(e.getMessage());
            return false;
        }
    }
}
