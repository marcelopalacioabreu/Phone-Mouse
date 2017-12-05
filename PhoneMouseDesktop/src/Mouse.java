import com.sun.xml.internal.ws.resources.DispatchMessages;

import java.awt.*;
import java.awt.event.InputEvent;

public class Mouse {
    private Robot robot;
    private double mouseX;
    private double mouseY;
    private Dimension screenSize;

    /**
     * Create mouse
     * @param mousePos initial mouse position
     * @param screenSize
     */
    public Mouse(Point mousePos, Dimension screenSize) {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        mouseX = mousePos.x;
        mouseY = mousePos.y;

        this.screenSize = screenSize;
    }

    /**
     * Update mouse position through velocity
     * @param velocity double array size 3
     */
    public void updatePos(double[] velocity) {
        mouseX += velocity[0] * .01;
        mouseY += velocity[1] * .01;

        mouseX = Math.max(0,Math.min(screenSize.getWidth(),mouseX));
        mouseY = Math.max(0,Math.min(screenSize.getHeight(),mouseY));


        robot.mouseMove((int)mouseX,(int)mouseY);
    }

    public void leftPress() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
    }

    public void rightPress() {
        robot.mousePress(InputEvent.BUTTON3_MASK);
    }

    public void leftRelease() {
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public void rightRelease() {
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
    }
}
