package server;

import java.util.HashMap;

import utils.VideoStream;

public class Manager {
    private HashMap<String, VideoStream> videos;

    public Manager(){
        this.videos = new HashMap<>();
    }

    public void addVideo(String name, VideoStream video){

    }

    public void addVideosFromFolder(String filepath){

    }

    public VideoStream getVideo(String name){
        return this.videos.get(name);
    }

    public boolean checkVideoExists(String video){
        return this.videos.containsKey(video);
    }
}
