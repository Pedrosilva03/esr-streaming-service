package client;

import javax.swing.*;

import java.awt.*;
import java.util.Scanner;

public class OClient {

    // UI
    private static JFrame window;

    private static JPanel videoPanel;

    private static JPanel buttonPanel;
    private static JButton playButton;
    private static JButton pauseButton;
    private static JButton stopButton;

    private static Scanner s = new Scanner(System.in);

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

        window.setVisible(true);
    }

    public static void main(String[] args) {
        System.out.println("Bem vindo ao SRTube! - Escolha um vídeo para assistir");

        s.nextLine();
        String video = s.nextLine();
        
        setupWindow("SRTube");
    }
}
