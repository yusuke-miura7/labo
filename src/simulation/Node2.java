package simulation;

public class Node2 {
	public Node2 parent;
	public Position position;
	public int g,h,f=0;
	
	//コンストラクタ
	public Node2(Node2 parent,Position position) {
		this.parent=parent;
		this.position=position;
	}
}
