package utils;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

public class Extras {
    public static BufferedImage convertYUVtoRGB(BufferedImage yuvImage) {
        if(yuvImage == null) return null;
        int width = yuvImage.getWidth();
        int height = yuvImage.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Criar arrays para armazenar os valores RGB
        int[] rgbPixels = new int[width * height];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int yuvPixel = yuvImage.getRGB(x, y);
                
                // Extraindo componentes YUV do pixel
                int yy = (yuvPixel >> 16) & 0xFF;  // Luminância
                int u = (yuvPixel >> 8) & 0xFF;   // Crominância (U)
                int v = yuvPixel & 0xFF;          // Crominância (V)
    
                // Calcular as diferenças uma vez
                int uDiff = u - 128;
                int vDiff = v - 128;
    
                // Convertendo de YUV para RGB
                int r = (int) (yy + 1.402 * vDiff);
                int g = (int) (yy - 0.344136 * uDiff - 0.714136 * vDiff);
                int b = (int) (yy + 1.772 * uDiff);
    
                // Clamping para garantir que os valores estejam no intervalo [0, 255]
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
    
                // Armazenar o pixel RGB no array
                rgbPixels[y * width + x] = (r << 16) | (g << 8) | b;
            }
        }
        
        // Definindo todos os pixels de uma vez
        rgbImage.setRGB(0, 0, width, height, rgbPixels, 0, width);
        
        return rgbImage;
    }

    /*
     * Função alternativa para obter o IP porque o localhost dá NullPointerException
     */
    public static String getLocalAddress(){
        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Ignorar interfaces de loopback ou interfaces desligadas
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        String ipAddress = address.getHostAddress();
                        
                        // Filtrar apenas endereços IPv4 (evitar endereços IPv6)
                        if (!ipAddress.contains(":")) {
                            if(networkInterface.getName().equals("eth0")) return ipAddress;
                        }
                    }
                }
            }
        } 
        catch(SocketException se){
            System.out.println("Erro ao listar as interfaces de rede.");
            se.printStackTrace();
        }
        return null;
    }

    public static List<String> getNeighborsIPs(String nodeIP) {
        String jsonFilePath = "config/bootstrapper.json";
        List<String> neighborsIPs = new ArrayList<>();

        try {
            // Lê o conteúdo do arquivo JSON
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

            // Cria um objeto JSON a partir do conteúdo lido
            JSONObject jsonObject = new JSONObject(jsonContent);

            // Acessa o array de nós
            JSONArray nodesArray = jsonObject.getJSONArray("nodes");

            // Procura pelo nó que corresponde ao IP fornecido
            for (int i = 0; i < nodesArray.length(); i++) {
                JSONObject node = nodesArray.getJSONObject(i);

                // Compara o IP do nó com o IP fornecido
                if (node.getString("ip").equals(nodeIP)) {
                    // Obtém a lista de vizinhos
                    JSONArray neighborsArray = node.getJSONArray("neighbors");

                    // Adiciona cada IP vizinho à lista
                    for (int j = 0; j < neighborsArray.length(); j++) {
                        neighborsIPs.add(neighborsArray.getString(j));
                    }

                    // Saia do loop, pois já encontrou o nó correspondente
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return neighborsIPs;
    }

    public static List<String> pingNeighbours(String address, List<String> neighbours){
        HashMap<String, Long> fastestNodes = new HashMap<>();

        for(String neighbour: neighbours){
            if(address != null && neighbour.equals(address)) continue;
            try{
                Socket aux = new Socket(neighbour, Ports.DEFAULT_NODE_TCP_PORT);
                DataInputStream dis = new DataInputStream(aux.getInputStream());
                DataOutputStream dos = new DataOutputStream(aux.getOutputStream());

                dos.writeUTF(Messages.generatePingMessage());
                dos.flush();
                long start = System.currentTimeMillis();

                aux.setSoTimeout(1000);
                dis.readInt();
                aux.setSoTimeout(0);
                long diff = System.currentTimeMillis() - start;

                fastestNodes.put(neighbour, Long.valueOf(diff));

                dos.writeUTF(Messages.generateDisconnectMessage());
                dos.flush();

                dis.close();
                dos.close();
                aux.close();
            }
            catch(IOException e){
                //System.out.println("Erro ao conectar-se ao nodo: " + neighbour + " " + e.getMessage());
            }
        }
        List<Entry<String, Long>> fastestNodesSorted = new ArrayList<>(fastestNodes.entrySet());
        fastestNodesSorted.sort(Entry.comparingByValue());
        List<String> fastestAddresses = new ArrayList<>();
        for(Entry<String, Long> addresss: fastestNodesSorted){
            fastestAddresses.add(addresss.getKey());
        }
        return fastestAddresses;
    }

    public static int generateRandomPort() {
        Random random = new Random();
        return 1024 + random.nextInt(49151 - 1024 + 1);
    }
}
