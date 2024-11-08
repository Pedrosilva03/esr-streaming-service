package server;

import java.util.HashMap;
import java.util.List;
import java.io.File;

import utils.Extras;
import utils.Streaming;
import utils.UpdateVisualizer;
import utils.VideoStream;

public class Manager {
    private HashMap<VideoStream, Streaming> streamingCurrently;
    private HashMap<String, VideoStream> videos;
    private String filepath;

    private List<String> neighbours;

    public HashMap<Integer, String> viewedMessages;

    public Manager(String filepath){
        this.streamingCurrently = new HashMap<>();
        this.videos = new HashMap<>();
        this.filepath = filepath;
        this.addVideosFromFolder();
        this.neighbours = Extras.getNeighborsIPs(Extras.getLocalAddress());
        this.viewedMessages = new HashMap<>();

    }

    /*
     * Função que adiciona um vídeo previamente importado à base de dados do servidor
     */
    public void addVideo(String name, VideoStream video){
        videos.put(name, video);
    }

    /*
     * Função que importa videos para a memória do servidor
     */
    public void addVideosFromFolder(){
        File videoDirectory = new File(this.filepath);

        File[] videoFiles = videoDirectory.listFiles();

        for(File videoFile: videoFiles){
            try{
                addVideo(videoFile.getName(), new VideoStream(this.filepath + "/" + videoFile.getName()));
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    /*
     * Função que adicionar um viewer a uma stream
     */
    public void connectUser(VideoStream video){
        this.streamingCurrently.get(video).addUser();
    }

    /*
     * Função que retira um viewer a uma stream
     */
    public void disconnectUser(String requestAddress, VideoStream video){
        try{
            this.streamingCurrently.get(video).removeUser();
            UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 0); // Atualiza o simulador da rede overlay
        }
        catch(NullPointerException e){}
    }

    /*
     * Função principal que trata de iniciar a stream e adicionar viewers
     */
    public void createStream(String requestAddress, VideoStream video){
        if(!this.streamingCurrently.containsKey(video)){ // Caso em que a stream está inativa
            Streaming s = new Streaming(video);
            this.streamingCurrently.put(video, s); // Cria a stream e coloca-a na lista de streams ativas

            Thread t = new Thread(() -> { // Thread responsável por esperar pelo fim da stream para atualizar o sistema
                Thread st = new Thread(s); // Thread responsável por atualizar o frame atual
                st.start();
                try{
                    st.join();
                    streamingCurrently.remove(video); // Remove a stream da lista de streams ativas
                    System.out.println("Streaming do video: " + video.getFilename() + " fechada.");
                    video.resetVideo(); // Reinicia o vídeo para que inicie do zero (caso a stream seja iniciada novamente)
                    UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 0); // Atualiza o simulador da rede overlay
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            });
            t.start();
        }
        else connectUser(video); // Caso a stream já esteja ativa, apenas conecta o viewer
        UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 1); // Atualiza o simulador da rede overlay
    }

    /*
     * Função que devolve o frame atual da stream (double return: Preenche o buffer com dados do frame e retorna o tamanho do frame)
     */
    public int stream(VideoStream video, byte[] data) throws Exception{
        return this.streamingCurrently.get(video).getFrame(data);
    }

    /*
     * Getter normal para devolver o objeto associado a um vídeo através do seu nome
     */
    public VideoStream getVideo(String name){
        return this.videos.get(name);
    }

    /*
     * Função que verifica se um vídeo existe na base de dados do servidor
     */
    public boolean checkVideoExists(String video){
        return this.videos.containsKey(video);
    }
}
