package utils;

/**
 * Classe para guardar o protocolo de mensagens do sistema
 */
public class Messages {
    public static final String check_video = "CHECK_VIDEO"; // Mensagem para verificar se um nodo ou o servidor têm o video.
    public static final String disconnect = "DISCONNECT"; // Mensagem para desconectar do servidor

    public static String generateCheckVideoMessage(String videoName){
        StringBuilder sb = new StringBuilder();
        sb.append(check_video);
        sb.append(" ");
        sb.append(videoName);

        return sb.toString();
    }
}
