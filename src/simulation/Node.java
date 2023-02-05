package simulation;

import java.util.ArrayList;

public class Node {
	//識別No
	int number;
	//座標
	int x,y;
	//標高
	public double height=0;
	//保持しているリンク
	ArrayList<Link> linklist = new ArrayList<Link>();
	
	//Astarで使用
	public double cost; 
	public double g,h,f=0;
	public Node parent;
	
	Node(int number, int x,int y){
		this.number=number;
		this.x=x;
		this.y=y;
	}
	
	public void addlink(Link link) {
		linklist.add(link);
	}

}
