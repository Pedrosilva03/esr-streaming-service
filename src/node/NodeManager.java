package node;

import java.util.HashMap;

import utils.Streaming;

public class NodeManager {
    private HashMap<String, Streaming> streamingCurrently;

    public NodeManager(){
        this.streamingCurrently = new HashMap<>();
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
}
