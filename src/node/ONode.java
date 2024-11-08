package node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import utils.Extras;
import utils.Ports;

/*
 * Servidor básico
 */
public class ONode {
    public static ServerSocket ss;
    public static void main(String[] args) {
        try{
            ss = new ServerSocket(Ports.DEFAULT_NODE_TCP_PORT);

            System.out.println("Nodo ativo em: " + Extras.getLocalAddress() + " na porta " + ss.getLocalPort());

            NodeManager manager = new NodeManager();

            while(true){
                Socket s = ss.accept();
                Thread t = new Thread(new NodeHandler(s, manager));
                t.start();
            }
        }
        catch(IOException e){
            System.out.println("Nodo crash");
        }
    }
}
