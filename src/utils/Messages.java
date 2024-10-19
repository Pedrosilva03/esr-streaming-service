package utils;

import java.util.Random;

/**
 * Classe para guardar o protocolo de mensagens do sistema
 */
public class Messages {
    public static final String check_video = "CHECK_VIDEO"; // Mensagem para verificar se um nodo ou o servidor têm o video.
    public static final String disconnect = "DISCONNECT"; // Mensagem para desconectar do servidor
    
    public static Random randomIDGenerator = new Random();

    public static String generateCheckVideoMessage(String videoName){
        StringBuilder sb = new StringBuilder();
        sb.append(randomIDGenerator.nextInt(65536) + 1);
        sb.append(" ");
        sb.append(check_video);
        sb.append(" ");
        sb.append(videoName);

        return sb.toString();
    }

    public static String generateDisconnectMessage(){
        StringBuilder sb = new StringBuilder();
        sb.append(randomIDGenerator.nextInt(65536) + 1);
        sb.append(" ");
        sb.append(disconnect);

        return sb.toString();
    }
}
