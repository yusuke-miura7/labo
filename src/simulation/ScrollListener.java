package simulation;

import java.awt.event.*;
import javax.swing.JScrollBar;

public class ScrollListener implements AdjustmentListener {
	private int mode;
	
	ScrollListener(int mode){
		this.mode=mode;
	}
	
	public void adjustmentValueChanged(AdjustmentEvent ae) {
		JScrollBar scroll_value = (JScrollBar)ae.getSource();
		
		//スピード変更
		if(mode==1)
		Core.sleepTime=(short)(100-scroll_value.getValue());
		
		//x座標オフセット変更
		else if(mode==2) {
			Step2Swing.offset_x=990- 10*scroll_value.getValue()* (int)Step2Swing.window_width/600;
		}
		
		//y座標オフセット変更
		else if(mode==3) {
			Step2Swing.offset_y=990-10*scroll_value.getValue()* (int)Step2Swing.window_height/600;
		}
		
		//倍率変更
		else if(mode==4) {
			Step2Swing.window_width=24*scroll_value.getValue()+600;
			Step2Swing.window_height=24*scroll_value.getValue()+600;
		}
	}

}
