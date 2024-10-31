package client;

import javax.swing.*;
import javax.imageio.ImageIO;

import utils.Messages;
import utils.Ports;
import utils.Extras;
import utils.RTPpacket;
import utils.VideoStream;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.util.Scanner;
import java.util.List;

import java.net.Socket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class OClient {

    // UI
    private static JFrame window;

    private static JPanel videoPanel;

    private static JPanel buttonPanel;
    private static JButton playButton;
    private static JButton pauseButton;
    private static JButton stopButton;

    private static Scanner s = new Scanner(System.in);

    private static Socket tcpSocket;
    private static DatagramSocket udpSocket;

    private static DataInputStream dis;
    private static DataOutputStream dos;

    private static boolean playing;
    private static boolean pause;

    private static BufferedImage currentFrame;

    private static List<String> neighbours;

    private static void setupWindow(String title){
        // Main window
        window = new JFrame(title);
        window.setSize(new Dimension(600, 400));
        window.setLayout(new BorderLayout());

        // Panel for the video setup
        videoPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentFrame != null) {
                    g.drawImage(currentFrame, 0, 0, this.getWidth(), this.getHeight(), null);
                }
            }
        };
        videoPanel.setBackground(Color.BLACK);
        window.add(videoPanel, BorderLayout.CENTER);

        // Button setup
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        playButton = new JButton("Play");
        buttonPanel.add(playButton);
        pauseButton = new JButton("Pause");
        buttonPanel.add(pauseButton);
        stopButton = new JButton("Stop");
        buttonPanel.add(stopButton);

        window.add(buttonPanel, BorderLayout.SOUTH);

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                // Play button logic
                pause = false;
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                // Pause button logic
                pause = true;
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                // Stop button logic
                playing = false;
                stopVideo();
            }
        });

        window.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                disconnectFromServer();
            }
        });

        window.setVisible(true);
    }

    private static void disconnectFromServer(){
        try{
            dos.writeUTF(Messages.generateDisconnectMessage());
            dos.flush();
        }
        catch(IOException io){
            System.out.println("Erro ao tentar desconectar, a sair forçadamente");
            System.exit(0);
        }
    }

    private static void setupConnection() throws IOException{
        tcpSocket = new Socket("10.0.0.10", Ports.DEFAULT_SERVER_PORT);

        dis = new DataInputStream(tcpSocket.getInputStream());
        dos = new DataOutputStream(tcpSocket.getOutputStream());
    }

    private static int checkVideo(String video) throws IOException{
        dos.writeUTF(Messages.generateCheckVideoMessage(video));
        dos.flush();
        return dis.readInt();
    }

    private static void recieveVideo(String videoString) throws IOException{
        playing = true;
        while(playing){
            while(pause){
                try{
                    Thread.sleep(40);
                }
                catch(InterruptedException e){
                    System.out.println("Error pausing");
                }
            }

            // Recebe o tamanho do packet
            byte[] frameSizeByte = new byte[4];
            DatagramPacket frameSizePacket = new DatagramPacket(frameSizeByte, frameSizeByte.length);
            udpSocket.receive(frameSizePacket);

            // Converter os bytes recebidos para um inteiro
            ByteBuffer wrapped = ByteBuffer.wrap(frameSizeByte);
            int frameSize = wrapped.getInt();

            byte[] data = new byte[frameSize];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            udpSocket.receive(packet);

            ByteArrayInputStream bis = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
            BufferedImage frame = ImageIO.read(bis);

            if(videoString.equals("movie.Mjpeg"))
                currentFrame = Extras.convertYUVtoRGB(frame);
            else
                currentFrame = frame;
            videoPanel.repaint();
        }
    }

    private static void requestVideo(String video) throws IOException{
        dos.writeUTF(Messages.generateReadyMessage());
        recieveVideo(video);
    }

    private static void stopVideo(){
        disconnectFromServer();
        System.exit(0);
    }

    private static void importNeighbours() throws IOException{
        // Obter o seu próprio IP
        //neighbours = Extras.getNeighborsIPs(Extras.getLocalAddress());
    }

    public static void main(String[] args) {
        try{
            //importNeighbours();
            setupConnection();
            try{
                udpSocket = new DatagramSocket(Ports.DEFAULT_CLIENT_UDP_PORT);
            }
            catch(IOException e){
                System.out.println("Erro ao iniciar conexão para receção");
                disconnectFromServer();
            }
        }
        catch(IOException e){
            System.out.println("Erro ao conectar-se");
            System.exit(1);
        }

        try{
            System.out.println("Bem vindo ao SRTube! - Escolha um vídeo para assistir");
            
            String video = s.nextLine();

            int valid = checkVideo(video);
            
            if(valid == 1){
                setupWindow("SRTube");
                requestVideo(video);
            }
        }
        catch(IOException e){
            System.out.println("Conexão perdida");
            System.exit(1);
        }
    }
}
