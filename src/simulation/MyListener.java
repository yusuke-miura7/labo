package simulation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by hiroq7 on 15/02/03.
 */
public class MyListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        switch (cmd) {
            case "start":
                Core.pauseFlag = false;
                break;
                
            case "pause":
                Core.pauseFlag = true;
                break;
                
            case "end":
            	Core.endflag =true;
            	break;
            	
            case "sleep_5":
                Core.sleepTime = 5;
                break;
                
            case "sleep_10":
                Core.sleepTime = 10;
                break;
                
            case "sleep_15":
                Core.sleepTime = 15;
                break;
                
            case "debugText":
                Core.debugTextFlag = true;
                break;
                
            case "2D":
            	if(Core.mapDimension== 3) {
            		Core.mapDimension =2;
            		Core.changeFlag=true;
            	}
            	break;
            	
            case "3D":
            	if(Core.mapDimension ==  2) {
            		Core.mapDimension =3;
            		Core.changeFlag=true;
            	}
            	break;
            	
            default:
                break;

        }
    }
}

