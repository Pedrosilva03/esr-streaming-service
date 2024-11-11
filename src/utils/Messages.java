package utils;

import java.util.Random;

/**
 * Classe para guardar o protocolo de mensagens do sistema
 * Funções intuitivas
 */
public class Messages {
    public static final String check_video = "CHECK_VIDEO"; // Mensagem para verificar se um nodo ou o servidor têm o video.
    public static final String disconnect = "DISCONNECT"; // Mensagem para desconectar do servidor
    public static final String ready = "READY"; // Mensagem para desconectar do servidor
    public static final String ping = "PING"; // Mensagem para desconectar do servidor
    public static final String checkIfStreamOn = "CHECK_STREAM"; // Mensagem para desconectar do servidor
    
    public static Random randomIDGenerator = new Random(); // Gerador de IDs aleatórios para as mensagens

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

    public static String generateReadyMessage(String videoName, int port){
        StringBuilder sb = new StringBuilder();
        sb.append(randomIDGenerator.nextInt(65536) + 1);
        sb.append(" ");
        sb.append(ready);
        sb.append(" ");
        sb.append(videoName);
        sb.append(" ");
        sb.append(port);

        return sb.toString();
    }

    public static String generatePingMessage(){
        StringBuilder sb = new StringBuilder();
        sb.append(randomIDGenerator.nextInt(65536) + 1);
        sb.append(" ");
        sb.append(ping);

        return sb.toString();
    }

    public static String generateCheckIfStreamOn(String video){
        StringBuilder sb = new StringBuilder();
        sb.append(randomIDGenerator.nextInt(65));
        sb.append(" ");
        sb.append(checkIfStreamOn);
        sb.append(" ");
        sb.append(video);
        return sb.toString();
    }
}
