package server;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import utils.Streaming;
import utils.VideoStream;

public class Manager {
    private HashMap<VideoStream, Streaming> streamingCurrently;
    private HashMap<String, VideoStream> videos;
    private String filepath;

    public Manager(String filepath){
        this.streamingCurrently = new HashMap<>();
        this.videos = new HashMap<>();
        this.filepath = filepath;
        this.addVideosFromFolder();
    }

    public void addVideo(String name, VideoStream video){
        videos.put(name, video);
    }

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

    public void connectUser(VideoStream video){
        this.streamingCurrently.get(video).addUser();
    }

    public void disconnectUser(VideoStream video){
        this.streamingCurrently.get(video).removeUser();
    }

    public void createStream(VideoStream video){
        if(!this.streamingCurrently.containsKey(video)){
            Streaming s = new Streaming(video);
            this.streamingCurrently.put(video, s);

            Thread t = new Thread(() -> {
                Thread st = new Thread(s);
                st.start();
                try{
                    st.join();
                    streamingCurrently.remove(video);
                    System.out.println("Streaming do video: " + video.getFilename() + " fechada.");
                }
                catch(InterruptedException e){
                    System.out.println(e.getMessage());
                }
            });
            t.start();
        }
        else connectUser(video);
    }

    public int stream(VideoStream video, byte[] data) throws Exception{
        return this.streamingCurrently.get(video).getFrame(data);
    }

    public VideoStream getVideo(String name){
        return this.videos.get(name);
    }

    public boolean checkVideoExists(String video){
        return this.videos.containsKey(video);
    }
}
