package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class Astar {
	ArrayList<Node> nodelist = new ArrayList <Node>();
	ArrayList<Link> linklist = new ArrayList <Link>();
	
	Astar(ArrayList<Node> list1,ArrayList<Link> list2){
		this.nodelist=list1;
		this.linklist=list2;
	}
	
	public Individual search(int start,int destination) {
		ArrayList<Node> open_list = new ArrayList<Node>();
		ArrayList<Node> closed_list = new ArrayList<Node>();
		Node start_node = nodelist.get(start);
		start_node.parent=null;
		Node current_node = null;
		Node end_node = nodelist.get(destination);
		System.out.println("a");
		System.out.println(end_node.number);
		System.out.println(end_node.x);
		System.out.println(end_node.y);
		Individual k = new Individual();
		ArrayList<Integer> list= new ArrayList<Integer>();
		
		open_list.add(start_node);
		 
		long startTime = System.currentTimeMillis();
		long endTime;
		
		while(open_list.size()>0) {
			current_node = open_list.get(0);
			int current_index=0;
			int i=0;
			for(Node node:open_list) {
				if(node.f < current_node.f) {
					current_node=node;
					current_index=i;
				}
				i++;
			}
			
			open_list.remove(current_index);
			closed_list.add(current_node);
			
			//目的地が選ばれたら終了
			if(current_node.number==end_node.number) {
				endTime = System.currentTimeMillis();
				Node current = current_node;
				
				while(current!=null) {
					list.add(current.number);
					current = current.parent;
				}
				Collections.reverse(list);
				k.nodelist=list;
				//ノード番号からリンクリスト作成
				int node1,node2;
				for(int s=0;s<k.nodelist.size()-1;s++) {
					node1=k.nodelist.get(s);
					node2=k.nodelist.get(s+1);
					for(Link alink :linklist) {
						if((alink.node1.number==node1&&alink.node2.number==node2) ||(alink.node1.number==node2&&alink.node2.number==node1)) {
							k.linklist.add(alink.number);
						}
					}
				}
				//所要時間を計測
//				System.out.println(endTime-startTime);
				System.out.println(list);
				System.out.println(k.linklist);
				return k;
			}
			
			ArrayList<Node> children = new ArrayList<Node>();
			for(Link alink:current_node.linklist) {
				if(alink.node1.number==current_node.number) {
					Node new_node = new Node(alink.node2.number,alink.node2.x,alink.node2.y);
					new_node.linklist=alink.node2.linklist;
					new_node.parent = current_node;
					new_node.cost=alink.distance;
					children.add(new_node);
				}
				else {
					Node new_node = new Node(alink.node1.number,alink.node1.x,alink.node1.y);
					new_node.linklist=alink.node1.linklist;
					new_node.parent = current_node;
					new_node.cost=alink.distance;
					children.add(new_node);
				}
			}
			
			//重複削除
			children = new ArrayList<Node>(new LinkedHashSet<>(children));
			
			//隣接に対しての処理
			for(Node child:children) {
				boolean flag = false;
				//closeリストにある場合は何もしない
				for(Node closed_child : closed_list) {
					if(child.number==closed_child.number) {
						flag=true;
						break;
					}
				}
				if(flag) {
					continue;
				}
				
				//パラメータ計算
				child.g=current_node.g+child.cost;
				child.h=Math.max(Math.abs(child.x-end_node.x),Math.abs(child.y-end_node.y));//あまり良い予測値とは言えないが
				child.f=child.g+child.h;
				
				for(Node open_node:open_list) {
					if((child.number==open_node.number) && child.g > open_node.g) {
						flag=true;
						break;
					}
				}
				if(flag) {
					continue;
				}
				open_list.add(child);
			}
			//endTime = System.currentTimeMillis();
		}
		System.out.println("目的地にたどり着けません");
		return null;
	}
	
	
}
