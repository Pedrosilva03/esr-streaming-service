package utils;

import java.util.HashMap;
import java.util.Arrays;

/*
 * Class that abstracts all the streaming logic for mutiple usage
 */
public class Streaming implements Runnable{
    private VideoStream video;

    private byte[] lastFrameData;
    private int lastFrameSize;

    private int usersConnected;

    public Streaming(VideoStream video){
        this.video = video;
        this.lastFrameData = new byte[15000];
        this.lastFrameSize = 0;
        this.usersConnected = 1;
    }

    public String getVideo(){
        return this.video.getFilename();
    }

    public int getFrame(byte[] data) throws Exception{
        System.arraycopy(this.lastFrameData, 0, data, 0, data.length);
        return this.lastFrameSize;
    }

    public void addUser(){
        this.usersConnected++;
    }

    public void removeUser(){
        this.usersConnected--;
    }

    public void run(){
        while(this.usersConnected > 0){
            try{
                this.lastFrameSize = this.video.getnextframe(this.lastFrameData);
                Thread.sleep(40);
            }
            catch(Exception e){
                System.out.println("Erro ao streamar video: " + video.getFilename() + ". A cancelar...");
                break;
            }
        }
    }
}
