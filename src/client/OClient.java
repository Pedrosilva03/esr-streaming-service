package client;

import javax.swing.*;

import utils.Messages;
import utils.Ports;
import utils.VideoStream;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Scanner;
import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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
    private static DataInputStream dis;
    private static DataOutputStream dos;
    private static VideoStream video;

    private static void setupWindow(String title){
        // Main window
        window = new JFrame(title);
        window.setSize(new Dimension(600, 400));
        window.setLayout(new BorderLayout());

        // Panel for the video setup
        videoPanel = new JPanel();
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
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                // Pause button logic
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                // Stop button logic
            }
        });

        window.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                try{
                    dos.writeUTF(Messages.generateDisconnectMessage());
                    dos.flush();
                }
                catch(IOException io){
                    System.out.println("Erro ao tentar desconectar, a sair forçadamente");
                    System.exit(0);
                }
            }
        });

        window.setVisible(true);
    }

    private static void setupConnection() throws IOException{
        tcpSocket = new Socket("10.0.0.10", Ports.DEFAULT_SERVER_PORT);

        dis = new DataInputStream(tcpSocket.getInputStream());
        dos = new DataOutputStream(tcpSocket.getOutputStream());
    }

    private static int checkVideo(String video) throws IOException{
        // TODO: Perguntar ao servidor se o video existe
        
        dos.writeUTF(Messages.generateCheckVideoMessage(video));
        dos.flush();
        return dis.readInt();
    }

    private static void recieveVideo(){

    }

    public static void main(String[] args) {
        try{
            setupConnection();
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
                recieveVideo();
            }
        }
        catch(IOException e){
            System.out.println("Conexão perdida");
            System.exit(1);
        }
    }
}
