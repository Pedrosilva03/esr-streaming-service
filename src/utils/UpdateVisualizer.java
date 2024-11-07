package utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UpdateVisualizer {
    public static void updateVisualizer(String node1, String node2, int status){
        try{
            Socket aux = new Socket("10.0.1.1", 12345);
            DataInputStream dis = new DataInputStream(aux.getInputStream());
            DataOutputStream dos = new DataOutputStream(aux.getOutputStream());

            dos.writeUTF(node1 + " " + node2 + " " + status);
            dos.flush();

            dis.close();
            dos.close();
            aux.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
