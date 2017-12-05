import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.awt.*;
import java.io.*;

//https://github.com/tdelazzari/JavaBluetooth/blob/master/BluetoothServer/src/bluetoothserver/Server.java

public class Server extends Thread {

    private String serviceURL;
    private static UUID SERVICE_UUID = new UUID("0000110100001000800000805F9B34FB", false);
    private LocalDevice localDevice;
    private StreamConnectionNotifier streamNotifier;
    private StreamConnection streamConnection;

    private Mouse mouse;
    private double[] velocity;

    public Server() {
        mouse = new Mouse(MouseInfo.getPointerInfo().getLocation(), Toolkit.getDefaultToolkit().getScreenSize());

        this.serviceURL = "btspp://localhost:" + SERVICE_UUID + ";name=" + "btserver" + ";authorize=true";
        try {
            // Init Bluetooth device
            localDevice = LocalDevice.getLocalDevice();
            localDevice.setDiscoverable(DiscoveryAgent.GIAC);
            // Creating a Bluetooth service
            System.out.println("Service started on address: " + localDevice.getBluetoothAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {

            streamNotifier = (StreamConnectionNotifier) Connector.open(serviceURL);
            System.out.println("Waiting for client connection...");
            streamConnection = streamNotifier.acceptAndOpen();
            System.out.println("Bluetooth client is connected");
            BufferedReader in = new BufferedReader(new InputStreamReader(streamConnection.openDataInputStream()));
            while (true) {
                String data = in.readLine();
                if(data == null) {
                    break;
                }

                System.out.println(data);

                int action = Parser.parse(data);

                switch (action) {
                    case 0:
                        //velocity
                        velocity = Parser.parseVelocity(data);
                        mouse.updatePos(velocity);
                        break;
                    case 1:
                        //left press
                        mouse.leftPress();
                        break;
                    case 2:
                        //right press
                        mouse.rightPress();
                        break;
                    case 3:
                        //left release
                        mouse.leftRelease();
                        break;
                    case 4:
                        //right release
                        mouse.rightRelease();
                        break;
                }
            }
            System.out.println("Close");
            streamConnection.close();
            streamNotifier.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
