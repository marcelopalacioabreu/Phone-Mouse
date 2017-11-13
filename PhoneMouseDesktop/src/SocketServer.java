import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SocketServer {
    public static void main(String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];

        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        System.out.println(mousePos.toString());

        Robot robot = new Robot();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        while(true) {

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String data = new String(receivePacket.getData());
            data = data.substring(0,receivePacket.getLength());
            String[] velComponents = data.split(",");

            double[] velocity = new double[3];
            for(int i = 0; i < 3; i++) {
                velocity[i] = Double.parseDouble(velComponents[i]);
            }

            mousePos.x += (int)velocity[0];
            mousePos.y += -(int)velocity[1];

            mousePos.x = Math.max(0,Math.min((int)screenSize.getWidth(),mousePos.x));
            mousePos.y = Math.max(0,Math.min((int)screenSize.getHeight(),mousePos.y));
            robot.mouseMove(mousePos.x,mousePos.y);
            System.out.println(data);
        }
    }
}
