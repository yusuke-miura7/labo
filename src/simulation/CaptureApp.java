package simulation;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

/**
 * Created by hirotaka on 2015/02/21.
 */
public class CaptureApp {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");

    public static void captureImage() {
        try {
            Rectangle bounds = new Rectangle(0, 0, 1920, 1080);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(bounds);
            String dirName ="./result/";
            String fileName = format.format(new Date()) + ".jpg";
            ImageIO.write(image, "jpg", new File(dirName, fileName));
            System.out.println("aaa");
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
