package simulation;

import java.util.ArrayList;
import java.util.LinkedList;

public class Link {
	public int number;
	Node node1,node2;
	int x1,x2,y1,y2;
	int distance;
	double h1,h2;
	double local_sum_height;

	/* 勾配のタイプ
	 * 1:直線の坂
	 * 2:多少の凸凹あり
	 */
	int type_slope;
	
	//ArrayList<Double> height_list = new ArrayList<Double>();
	LinkedList<Integer> movement = new LinkedList<Integer>();
	ArrayList<SimpleNode> route = new ArrayList<>();
	
	Link(int number, Node node1, Node node2, int type){
		this.number = number;
		this.node1  = node1;
		this.node2  = node2;
		this.x1     = node1.x;
		this.x2     = node2.x;
		this.y1     = node1.y;
		this.y2     = node2.y;
		this.h1     = node1.height;
		this.h2     = node2.height;
		this.type_slope = type;
		
		//マンハッタン距離
		//this.distance = Math.abs(node1.x-node2.x)+Math.abs(node1.y-node2.y);
		
		//仕様に基づく最短距離
		this.distance = Math.max(Math.abs(node1.x-node2.x), Math.abs(node1.y-node2.y));
		
		//標高
		double point;//低い方
		point= h1 < h2 ? h1 : h2;
		double init,difference;
		difference=Math.abs(h1-h2);
		init = difference/distance;
		double sum=0;
		
		if(type_slope==1) {
			for(int i=0;i<distance;i++) {
				sum+=point+init*(i+1);
			}
		}
		
		this.local_sum_height = sum;
	}
	
	public boolean set_local_sum_height() {
		if(type_slope!=2) {
			return false;
		}
		else {
			return true;
		}
	}
	
	/*数字と方向の対応は以下
	 *     1
	 *  -4    2
	 *-3        3
	 *  -2    4
	 *     -1
	 */
	public void move(int x,int y) {
		if(x==1&&y==-1) {
			movement.add(2);
		}
		else if(x==1&&y==1) {
			movement.add(4);
		}
		else if(x==-1&&y==1) {
			movement.add(-2);
		}
		else if(x==-1&&y==-1) {
			movement.add(-4);
		}
		else if(x==0&&y==-1) {
			movement.add(1);
		}
		else if(x==1&&y==0) {
			movement.add(3);
		}
		else if(x==0&&y==1) {
			movement.add(-1);
		}
		else {
			movement.add(-3);
		}
	}
	
	//経路の設定
	public void set_route() {
		route.add(new SimpleNode(0,x1,y1));
		int x=0;//xの総移動距離
		int y=0;//yの総移動距離
		for(Integer move :movement) {
			if(move==1) {
				y-=1;
			}
			else if(move==2) {
				x+=1;
				y-=1;
			}
			else if(move==3) {
				x+=1;
			}
			else if(move==4) {
				x+=1;
				y+=1;
			}
			else if(move==-1) {
				y+=1;
			}
			else if(move==-2) {
				x-=1;
				y+=1;
			}
			else if(move==-3) {
				x-=1;
			}
			else if(move==-4) {
				x-=1;
				y-=1;
			}
			route.add(new SimpleNode(0,x1+x,y1+y));
		}
	}
}
