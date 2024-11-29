package client;

import javax.swing.*;
import javax.imageio.ImageIO;

import utils.Messages;
import utils.Ports;
import utils.Extras;
import utils.RTPpacket;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;

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

    private static String video;

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
            dos.writeUTF(Messages.generateDisconnectMessage()); // Gera uma mensagem de desconexão e envia para o nodo onde está conectado
            dos.flush();

            dis.close();
            dos.close();
            tcpSocket.close();
        }
        catch(IOException io){
            System.out.println("Erro ao tentar desconectar, a sair forçadamente");
            System.exit(0);
        }
    }

    private static void setupConnection() throws IOException{
        //tcpSocket = new Socket("10.0.0.10", Ports.DEFAULT_SERVER_PORT); // Socket para o servidor
        //tcpSocket = new Socket("10.0.18.2", Ports.DEFAULT_NODE_TCP_PORT); // Socket para o PoP1
        //tcpSocket = new Socket("10.0.19.2", Ports.DEFAULT_NODE_TCP_PORT); // Socket para o PoP2
        //tcpSocket = new Socket("10.0.21.2", Ports.DEFAULT_NODE_TCP_PORT); // Socket para o PoP3
        //tcpSocket = new Socket("10.0.13.2", Ports.DEFAULT_NODE_TCP_PORT); // Socket para o O2

        tcpSocket = new Socket(neighbours.get(0), Ports.DEFAULT_NODE_TCP_PORT); // Conecta-se ao melhor nodo, predeterminado em função do RTT

        dis = new DataInputStream(tcpSocket.getInputStream());
        dos = new DataOutputStream(tcpSocket.getOutputStream());
    }

    private static int checkVideo(String video) throws IOException, IndexOutOfBoundsException{
        dos.writeUTF(Messages.generateCheckVideoMessage(video));
        dos.flush();
        return dis.readInt();
    }

    private static void recieveVideo(String videoString) throws SocketTimeoutException, IOException{
        playing = true;
        while(playing){
            while(pause){ // O pause para de ler packets
                try{
                    Thread.sleep(40); // Verifica todos os 40 milissegundos se houve unpause
                }
                catch(InterruptedException e){
                    System.out.println("Error pausing");
                }
            }

            // Receber pacote RTP
            byte[] packetData = new byte[65535]; // Buffer grande o suficiente para o cabeçalho + payload
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
            udpSocket.receive(packet);

            // Extrair o RTPpacket
            RTPpacket rtpPacket = new RTPpacket(packet.getData(), packet.getLength());

            // Verificar se o payload é do tipo esperado
            if (rtpPacket.getpayloadtype() == 26) { // Supondo que 26 é o tipo de payload do vídeo (ajustar conforme necessário)
                int frameSize = rtpPacket.getpayload_length();
                
                // Extrair payload (dados do frame)
                byte[] frameData = new byte[frameSize];
                rtpPacket.getpayload(frameData);

                // Converter os dados do frame para uma imagem
                ByteArrayInputStream bis = new ByteArrayInputStream(frameData);
                BufferedImage frame = ImageIO.read(bis);

                // Converter para RGB, se necessário, e atualizar o painel de vídeo
                if (videoString.equals("movie.Mjpeg"))
                    currentFrame = Extras.convertYUVtoRGB(frame);
                else
                    currentFrame = frame;
                
                videoPanel.repaint();
            } else {
                System.out.println("Recebido pacote com tipo de payload inesperado: " + rtpPacket.getpayloadtype());
            }
        }
        udpSocket.close();
    }

    /*
     * Função responsável por garantir que o cliente está sempre conectado ao mesmo PoP
     * Caso a função detete que existe um PoP melhor que o atual, envia um pedido de stream para o novo e depois envia um pedido de disconnect para o antigo
     * Para poupar recursos, esta verificação acontece todos os 3 segundos
     */
    private static void popMonitor(){
        while(playing){
            try{
                List<String> neighboursAux = Extras.pingNeighbours(null, new ArrayList<>(neighbours));
                if(!neighboursAux.get(0).equals(neighbours.get(0))){
                    // Trocar de PoP
                    Socket tmp = tcpSocket;
                    DataInputStream disTmp = dis;
                    DataOutputStream dosTmp = dos;

                    tcpSocket = new Socket(neighboursAux.get(0), Ports.DEFAULT_NODE_TCP_PORT);
                    dis = new DataInputStream(tcpSocket.getInputStream());
                    dos = new DataOutputStream(tcpSocket.getOutputStream());

                    dos.writeUTF(Messages.generateReadyMessage(video, udpSocket.getLocalPort())); // O gerador de mensagens "ready" envia para o nodo vizinho a porta para onde pode enviar packets

                    dosTmp.writeUTF(Messages.generateDisconnectMessage());

                    tmp.close();
                    disTmp.close();
                    dosTmp.close();
                }
                neighbours.clear();
                neighbours.addAll(neighboursAux);
                Thread.sleep(3000);
            }
            catch(InterruptedException e){
                System.out.println("Erro no timeout da monitorização: " + e.getMessage());
            }
            catch(IOException f){
                System.out.println(f.getMessage());
            }
        }
    }

    private static void requestVideo(String video) throws SocketTimeoutException, IOException{
        dos.writeUTF(Messages.generateReadyMessage(video, udpSocket.getLocalPort())); // O gerador de mensagens "ready" envia para o nodo vizinho a porta para onde pode enviar packets
        new Thread(() -> {
            popMonitor();
        }).start();
        recieveVideo(video);
    }

    private static void stopVideo(){
        disconnectFromServer();
        System.exit(0);
    }

    /*
     * A função atualiza a lista dos vizinhos por ordem de melhor qualidade de conexão
     */
    private static void importNeighbours() throws IOException{
        neighbours = Extras.pingNeighbours(null, Extras.getNeighborsIPs(Extras.getLocalAddress()));
    }

    public static void main(String[] args) {
        try{
            importNeighbours();
            setupConnection();
            try{
                udpSocket = new DatagramSocket(Extras.generateRandomPort()); // Abre o socket numa porta aleatória
                udpSocket.setSoTimeout(2000);
            }
            catch(IOException e){
                System.out.println("Erro ao iniciar conexão para receção");
                disconnectFromServer(); // Caso haja erro ao abrir o socket UDP, desconecta-se do servidor
            }
        }
        catch(IOException e){
            System.out.println("Erro ao conectar-se");
            System.exit(0);
        }
        catch(IndexOutOfBoundsException e){
            System.out.println("Vizinhos não encontrados");
            System.exit(0);
        }

        try{
            System.out.println("Bem vindo ao SRTube! - Escolha um vídeo para assistir");
            
            video = s.nextLine();

            int valid = checkVideo(video);
            
            if(valid == 1){
                setupWindow("SRTube");
                requestVideo(video);
            }
            else{
                System.out.println("Video não encontrado no sistema");
                disconnectFromServer();
            }
        }
        catch(SocketTimeoutException ss){
            System.out.println("Condições de rede demasiado instáveis para a transmissão");
            disconnectFromServer();
            System.exit(0);
        }
        catch(IOException e){
            System.out.println("Conexão perdida");
            System.exit(0);
        }
    }
}
