package server;

import utils.Ports;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static ServerSocket ss;
    public static void main(String[] args) {
        try {
            ss = new ServerSocket(Ports.DEFAULT_SERVER_PORT);

            System.out.println("Servidor de streaming ativo em: " + ss.getInetAddress().getHostAddress() + " na porta " + ss.getLocalPort());

            Manager database = new Manager();

            while(true){
                Socket s = ss.accept();
                Thread t = new Thread(new ServerHandler(s, database));
                t.start();
            }
        } 
        catch (IOException e) {
            
        }
    }
}
