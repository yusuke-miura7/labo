package simulation;

import java.util.ArrayList;

public class SimpleNode {
	int number;
	int x,y;
	ArrayList<Integer> link = new ArrayList<>();
	
	SimpleNode(int number,int x,int y){
		this.number=number;
		this.x=x;
		this.y=y;
	}
}
