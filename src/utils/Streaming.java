package utils;

import java.util.HashMap;
import java.util.Arrays;

/*
 * Class that abstracts all the streaming logic for mutiple usage
 */
public class Streaming implements Runnable{
    private VideoStream video;
    private String videoName; // Para stream no nodo

    private byte[] lastFrameData;
    private int lastFrameSize;

    private int usersConnected;

    // Lógica focada para o streaming no lado de um nodo intermédio (sem acesso ao ficheiro do vídeo, recebe frames de outro nodo/servidor)
    public Streaming(String videoName){
        this.lastFrameData = new byte[65535];
        this.lastFrameSize = 0;
        this.usersConnected = 1;
        this.video = null;
        this.videoName = videoName;
    }

    public void setFrame(byte[] newFrame, int newFrameSize){
        System.arraycopy(newFrame, 0, this.lastFrameData, 0, this.lastFrameData.length);
        this.lastFrameSize = newFrameSize;
    }

    // Lógica focada para o streaming no lado do servidor (com acesso ao ficheiro do vídeo)
    public Streaming(VideoStream video){
        this.video = video;
        this.lastFrameData = new byte[65535];
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

    private int requestVideoToNeighbour(){
        return 0;
    }

    public void run(){
        while(this.usersConnected > 0){
            try{
                if(video != null) this.lastFrameSize = this.video.getnextframe(this.lastFrameData);
                else this.lastFrameSize = this.requestVideoToNeighbour();
                Thread.sleep(40);
            }
            catch(Exception e){
                if(video != null) System.out.println("Erro ao streamar video: " + video.getFilename() + ". A cancelar...");
                else System.out.println("Erro ao streamar video: " + videoName + ". A cancelar...");
                break;
            }
        }
    }
}
