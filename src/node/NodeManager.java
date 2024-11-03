package node;

import java.util.HashMap;
import java.util.List;

import utils.Extras;
import utils.Streaming;

public class NodeManager {
    private HashMap<String, Streaming> streamingCurrently;

    private List<String> neighbours;

    public NodeManager(){
        this.streamingCurrently = new HashMap<>();
        this.neighbours = Extras.getNeighborsIPs(Extras.getLocalAddress());
    }

    public void connectUser(String video){
        this.streamingCurrently.get(video).addUser();
    }

    public void disconnectUser(String video){
        try{
            this.streamingCurrently.get(video).removeUser();
        }
        catch(NullPointerException e){}
    }

    public boolean checkVideoExists(String video){
        if(!this.streamingCurrently.containsKey(video)){
            // TODO: Check with the neighbours if the video exists somewhere
            return false;
        }
        else return true;
    }

    public void createStream(String video){
        if(!this.streamingCurrently.containsKey(video)){
            Streaming s = new Streaming(video);
            this.streamingCurrently.put(video, s);
            Thread t = new Thread(() -> {
                Thread tt = new Thread(s); // TODO: A classe streaming vai pedir a um vizinho por frames
                tt.start();
                try{
                    tt.join();
                    this.streamingCurrently.remove(video);
                    System.out.println("Streaming do video: " + video + " fechada.");
                    // TODO: Avisar o streamer que já não precisa de streamar para aqui
                }
                catch(InterruptedException e){
                    System.out.println(e.getMessage());
                }
            });
            t.start();
        }
        else this.connectUser(video);
    }

    public int stream(String video, byte[] data) throws Exception{
        return this.streamingCurrently.get(video).getFrame(data);
    }
}
