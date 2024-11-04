package node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utils.Extras;
import utils.Streaming;

public class NodeManager {
    private HashMap<String, Streaming> streamingCurrently;

    private List<String> neighbours;

    public HashMap<Integer, String> viewedMessages;
    public NodeManager(){
        this.streamingCurrently = new HashMap<>();
        this.neighbours = Extras.getNeighborsIPs(Extras.getLocalAddress());
        this.viewedMessages = new HashMap<>();
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

    public boolean checkVideoExists(String requestAddress, String video, String message, List<String> neighboursWithVideo){
        if(!this.streamingCurrently.containsKey(video)){
            for(String neighbour: this.neighbours){
                if(neighbour.equals(requestAddress)) continue;
                if(NodeRecursive.checkVideoOnNode(message, neighbour)) neighboursWithVideo.add(neighbour);
            }
            if(neighboursWithVideo.isEmpty()) return false;
            else return true;
        }
        else return true;
    }

    private void closeStream(){
        // TODO: Avisar o streamer que já não precisa de streamar para aqui
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

                    this.closeStream();
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
