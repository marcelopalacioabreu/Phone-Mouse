public class Main {

    public static void main(String[] args) {
        Server server = new Server();
        while (true) {
            if (!server.isAlive()) {
                server.start();
            }
        }
    }
}
