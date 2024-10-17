package server;

import java.util.HashMap;
import java.io.File;

import utils.VideoStream;

public class Manager {
    private HashMap<String, VideoStream> videos;
    private String filepath;

    public Manager(String filepath){
        this.videos = new HashMap<>();
        this.filepath = filepath;
        this.addVideosFromFolder();
    }

    public void addVideo(String name, String videoName){
        videos.put(videoName, null);
    }

    public void addVideosFromFolder(){
        File videoDirectory = new File(this.filepath);

        File[] videoFiles = videoDirectory.listFiles();

        for(File videoFile: videoFiles){
            try{
                videos.put(videoFile.getName(), new VideoStream(this.filepath + "/" + videoFile.getName()));
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    public VideoStream getVideo(String name){
        return this.videos.get(name);
    }

    public boolean checkVideoExists(String video){
        return this.videos.containsKey(video);
    }
}
