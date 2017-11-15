import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

    public static final int UDP_PORT = 9876;

    public static void main(String[] args) throws Exception {

        //create socket with port number 9876
        DatagramSocket serverSocket = new DatagramSocket(UDP_PORT);
        //create data buffer
        byte[] receiveData = new byte[1024];
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Mouse mouse = new Mouse(MouseInfo.getPointerInfo().getLocation(), screenSize);

        while(true) {
            //create packet to hold data
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            //put data in packet, this call blocks
            serverSocket.receive(receivePacket);
            String data = new String(receivePacket.getData());
            //parse data into action
            Actions action = Parser.parse(data, receivePacket.getLength());

            switch (action){
                case MOVE:
                    double[] velocity = Parser.parseVelocity(data,receivePacket.getLength());
                    mouse.updatePos(velocity);
                    break;
                case LEFT_PRESS:
                    mouse.leftPress();
                    break;
                case RIGHT_PRESS:
                    mouse.rightPress();
                    break;
                case LEFT_RELEASE:
                    mouse.leftRelease();
                    break;
                case RIGHT_RELEASE:
                    mouse.rightRelease();
                    break;
            }

            //System.out.println(data);
        }
    }
}
