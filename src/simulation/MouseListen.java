package simulation;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by hirotaka on 2015/02/21.
 */

public class MouseListen extends MouseAdapter {

    public static int Point_X;
    public static int Point_Y;

    public void mouseClicked(MouseEvent e) {
        Point point = e.getPoint();
        if (point.x < Core.window_width && point.y < Core.window_width) {
            Point_X = point.x / (Core.window_width / Core.world_x);
            Point_Y = point.y / (Core.window_height / Core.world_y);
        }
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

}

