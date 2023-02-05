package simulation;

import java.awt.event.*;

public class KeyInput implements KeyListener{
	int KeyCode;
	public void keyPressed(KeyEvent event) {
		KeyCode = event.getKeyCode();
		System.out.println("「" + KeyCode + "」が押されました。");
	}
	
	public void keyReleased(KeyEvent event){
		KeyCode = event.getKeyCode();
		System.out.println("「" + KeyCode + "」が離されました。");
	}
	
	public void keyTyped(KeyEvent event){
		System.out.println("押されました。");
	}
}