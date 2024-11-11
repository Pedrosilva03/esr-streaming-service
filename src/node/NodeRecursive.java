package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.util.List;
import java.util.ArrayList;

import utils.Messages;
import utils.Ports;

/*
 * Classe que trata de todos os pedidos a outros nodos
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

    /*
     * Função que verifica que vizinhos de um nodo estão com uma stream ativa.
     */
    public static List<String> checkIfStreamOn(List<String> neighbours, String video){
        List<String> orderedNeighbours = new ArrayList<>();
        for(String node: neighbours){
            try{
                Socket aux = new Socket(node, Ports.DEFAULT_NODE_TCP_PORT);
                DataInputStream dis = new DataInputStream(aux.getInputStream());
                DataOutputStream dos = new DataOutputStream(aux.getOutputStream());

                dos.writeUTF(Messages.generateCheckIfStreamOn(video));
                dos.flush();

                int valid = dis.readInt();
                if(valid == 1) orderedNeighbours.add(node);

                dos.writeUTF(Messages.generateDisconnectMessage());
                dos.flush();

                dis.close();
                dos.close();
                aux.close();
            }
            catch(IOException e){
                //e.printStackTrace();
            }
        }
        if(!orderedNeighbours.isEmpty()) return orderedNeighbours;
        return neighbours;
    }
}
