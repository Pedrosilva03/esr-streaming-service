package node;

public class ONode {
    public static void main(String[] args) {
        Thread server = new Thread(new NodeServer());
        server.start();
    }
}
