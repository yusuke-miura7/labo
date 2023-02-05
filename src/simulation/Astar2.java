package simulation;

import java.util.*;

public class Astar2 {
	Space space;
    Position position;//実際に変化する現在地
    Position virtualposition;//A*用仮想ポジション
    Position destination;//ゴール座標
    Position[] wall;
    Position[] building;
    Position[] goal;
    Position[] dangerArea;
    Position[] fireArea;
    ArrayList<Position> astarpath = new ArrayList<Position>();
    
    //状態ステータス
    boolean isgoal;
    int type=0;
    int hitCount = 0;
    int reachedCount = 0;
    int isdead = 0;
    int deth_fire = 0;
    int deth_tsunami = 0;
    int escape=0;

    //コンストラクタ
    public Astar2(Space space,Position startPos,Position[] wall,Position[] building, Position[] safe, Position[] dangerArea) {
        position = startPos;
        virtualposition = startPos;
        destination = choice(position,safe);
        this.space = space;
        this.isgoal = false;
        this.wall = wall;
        this.building = building;
        this.goal = safe;
        this.dangerArea = dangerArea;
    }
    
    //一番近いゴール座標を選ぶ、最短ではない
    private Position choice(Position current,Position[] goal) {
    	double min = Double.POSITIVE_INFINITY;
    	int tmp;
    	Position chosen = current;
    	for(Position agoal: goal) {
    		tmp=getDistance(current,agoal);
    		if(tmp<=min) {
    			min=tmp;
    			chosen=agoal;
    		}
    	}
    	return chosen;
    }
    
    //2点間の距離を求める
    private int getDistance(Position A,Position B) {
    	return Math.abs(A.x-B.x)+Math.abs(A.y-B.y);
    	//return (int)Math.pow((Math.abs(A.x-B.x)+Math.abs(A.y-B.y)),2);
    }

    //2つのPositionが同じかどうか判定
    public boolean isSamePoint(Position pre, Position cur) {
        return !(pre == null || cur == null) && ((pre.x == cur.x) && (pre.y == cur.y));
    }
    
    //移動先positionを返す
    private Position positionByInt(int d) {
        Position newPosition = null;
        if (d == 0) newPosition = position.up();
        if (d == 1) newPosition = position.up_left();
        if (d == 2) newPosition = position.left();
        if (d == 3) newPosition = position.down_left();
        if (d == 4) newPosition = position.down();
        if (d == 5) newPosition = position.down_right();
        if (d == 6) newPosition = position.right();
        if (d == 7) newPosition = position.up_right();
        return newPosition;
    }
    
    //移動先positionを返す,A*用
    private Position positionByInt2(int d) {
        Position newPosition = null;
        if (d == 0) newPosition = virtualposition.up();
        if (d == 1) newPosition = virtualposition.up_left();
        if (d == 2) newPosition = virtualposition.left();
        if (d == 3) newPosition = virtualposition.down_left();
        if (d == 4) newPosition = virtualposition.down();
        if (d == 5) newPosition = virtualposition.down_right();
        if (d == 6) newPosition = virtualposition.right();
        if (d == 7) newPosition = virtualposition.up_right();
        return newPosition;
    }
    
    //障害物判定,A*用
    private boolean check_collision2(Position newPosition) {
    	for(Position a_wall : wall) {
    		if(isSamePoint(newPosition,a_wall)) {
    			return true;
    		}
    	}
    	for(Position a_building: building) {
        	if(isSamePoint(newPosition,a_building)) {
        		return true;
        	}
        }
    	
    	for(Position a_danger_area: dangerArea) {
        	if(isSamePoint(newPosition,a_danger_area)) {
        		return true;
        	}
        }
    	
    	for(Position a_fire_area: fireArea) {
        	if(isSamePoint(newPosition,a_fire_area)) {
        		return true;
        	}
        }
    	return false;
    }
    
    //移動可能な場所からランダムに一つ選び移動
    private void random() {
    	ArrayList<Integer> candidate = new ArrayList<>();
    	//8方向に対して移動できるか判定
    	for(int i=0;i<8;i++) {
    		Position newPosition = positionByInt(i);
    		if(newPosition!=null && !check_collision2(newPosition)) {
    			candidate.add(i);
    		}
    	}
    	//8方向精査後
    	int size = candidate.size();
    	Random rand = new Random();
    	int num = rand.nextInt(size);
    	int d = candidate.get(num);
    	//移動
    	position = positionByInt(d);
    }

    //ランダムに移動する
    public boolean randomwalk() {
    	if(this.isgoal || this.isdead ==1) return false;
    	
    	//死亡判定
    	if(this.isdead==0) {
    		for(Position aDangerArea : dangerArea) {
    			if(isSamePoint(position, aDangerArea)) {
    				this.deth_tsunami = 1;
    				this.isdead =1;
    			}
    		}
    		for(Position aFireArea : fireArea) {
    			if(isSamePoint(position,aFireArea)) {
    				this.deth_fire =1;
    				this.isdead = 1;
    			}
    		}
    	}
    	//移動
    	random();
    	//ゴール判定
    	for(Position aGoal : this.goal) {
    		if(isSamePoint(position,aGoal)) {
    			this.isgoal=true;
    			return true;
    		}
    	}
    	return false;
    }
    
    //A*アルゴリズムに基づき経路リストを生成する
    public boolean search() {
    	ArrayList<Node2> open_list = new ArrayList<Node2>();
    	ArrayList<Node2> closed_list = new ArrayList<Node2>();
		Node2 start_node = new Node2(null,position);//探索開始地点のノード
		Node2 current_node;
		
		open_list.add(start_node);
		System.out.println("開始地："+position.x+","+position.y);
		
		while(open_list.size()>0) {
			current_node=open_list.get(0);//取り出しておく、(すぐ更新される)
			int current_index=0;
			int i=0;
			//一番F値が小さいpositionを選ぶ
			for(Node2 node : open_list) {
				if (node.f < current_node.f) {
					current_node=node;
					current_index=i;
				}
				i++;
			}
			//選ばれたノードをオープンリストから削除してクローズドリストに追加する
			open_list.remove(current_index);
			closed_list.add(current_node);
			virtualposition=current_node.position;//仮想で移動するために取得
			
			//ゴールに到達していれば経路を表示して終了
			if(isSamePoint(virtualposition,destination)) {
				Node2 current = current_node;
				while(current!=null){
					astarpath.add(current.position);
					current = current.parent;
				}
				Collections.reverse(astarpath);
				astarpath.remove(0);//開始地点を削除
				
				System.out.println("道のりは");
				for(Position answer: astarpath) {
					System.out.println(answer.x+","+answer.y);
				}
				return true;
			}
			
			//ゴールではない場合
			ArrayList<Node2> children = new ArrayList<Node2>();
			
			//上下左右に処理を行う
			for(int d=0;d<8;d++) {
				Position newPosition = positionByInt2(d);
				if(newPosition.x<0 || newPosition.y<0|| newPosition.x>Core.world_x-1||newPosition.y>Core.world_y-1) {
					continue;
				}
				if(check_collision2(newPosition)) {
					continue;
				}
				Node2 new_node=new Node2(current_node,newPosition);
				children.add(new_node);
			}
			
			//格納されたものに対しての処理
			for(Node2 child : children) {
				boolean flag=false;
				//closeリストにある場合は無視
				for(Node2 closed_child : closed_list) {
					if(isSamePoint(child.position,closed_child.position)) {
						flag=true;
						break;
					}
				}
				if(flag) {
					continue;
				}
				
				//パラメータ計算
				child.g=current_node.g+1;
				child.h=getDistance(child.position,destination);
				child.f=child.g+child.h;
				
				//値が小さい場合更新
				for(Node2 open_node: open_list) {
					if(isSamePoint(child.position,open_node.position) && child.g>open_node.g) {
						flag=true;
						break;
					}
				}
				if(flag) {
					continue;
				}
				open_list.add(child);
			}
		}
		System.out.println("目的地にたどりつけません");
		return false;
	}
    
    //配列にしたがって動く
    public boolean astarwalk() {
    	//ゴールまたは死亡したら何もしない
    	if(this.isgoal || this.isdead==1)
    		return false;
    	
    	//ここでastarpathに対して判定必要
    	position = astarpath.get(0);
    	astarpath.remove(0);
    	
    	//ゴールした場合(各々のゴール、destionationであることに注意)
    	if (isSamePoint(position,destination)) {
    		this.isgoal=true;
    		this.escape=1;
    		reachedCount++;
    		astarpath.clear();
    	}
    	return true;
    }
    
    public Position returnPos() {
        return position;
    }

}
