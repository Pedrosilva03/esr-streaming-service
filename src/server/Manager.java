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

    public void disconnectUser(String requestAddress, VideoStream video){
        try{
            this.streamingCurrently.get(video).removeUser();
            UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 0);
        }
        catch(NullPointerException e){}
    }

    public void createStream(String requestAddress, VideoStream video){
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
                    video.resetVideo();
                    UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 0);

                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            });
            t.start();
        }
        else connectUser(video);
        UpdateVisualizer.updateVisualizer(Extras.getHost(requestAddress), Extras.getHost(Extras.getLocalAddress()), 1);
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
