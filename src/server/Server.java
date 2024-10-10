package server;

import utils.Ports;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static ServerSocket ss;
    public static void main(String[] args) {
        try {
            ss = new ServerSocket(Ports.DEFAULT_PORT);

            System.out.println("Servidor de streaming ativo em: " + ss.getInetAddress().getHostAddress() + " na porta " + ss.getLocalPort());
        } 
        catch (IOException e) {
            
        }
    }
}
