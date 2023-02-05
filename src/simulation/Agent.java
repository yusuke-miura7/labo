package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class Agent {
	int number;
	double eva_distance;
	double eva_height;
	Space space;
	Position position;
	Position virtualposition;//Astar用仮想ポジション
	Position emergency;//周囲で高い場所

	//遺伝的アルゴリズムで作成された経路
	ArrayList<Integer> nodelist = new ArrayList<Integer>();
	ArrayList<Integer> linklist = new ArrayList<Integer>();

	//参考元
	ArrayList<Node> map_node = new ArrayList<Node>();
	ArrayList<Link> map_link = new ArrayList<Link>();

	//経路格納
	LinkedList<Integer> process = new LinkedList<Integer>();
	ArrayList<Position> astarpath = new ArrayList<Position>();
	ArrayList<Position> traveled_path = new ArrayList<Position>();

	//マップ情報
	ArrayList<Position> wallArea = new ArrayList<Position>();
	ArrayList<Position> buildingArea = new ArrayList<Position>();
	ArrayList<Position> safeArea1 = new ArrayList<Position>();
	ArrayList<Position> tsunamiArea = new ArrayList<Position>();
	ArrayList<Position> riverArea = new ArrayList<Position>();

	int range_visible;
	double alive_height;
	int count_hop = 0;
	boolean is_stop = false;//emergencyにたどりついたか
	boolean is_emergency = false;
	boolean is_searched = false;
	int isgoal = 0;
	int isdead = 0;

	//マップを移動させるための初期化
	public Agent(Space space, int number, int range_visible, double alive_height, ArrayList<Integer> node,
			ArrayList<Integer> link, ArrayList<Node> map_n, ArrayList<Link> map_l,
			ArrayList<Position> wall, ArrayList<Position> building, ArrayList<Position> safe,
			ArrayList<Position> tsunami, ArrayList<Position> river) {
		this.space = space;
		this.number = number;
		this.range_visible = range_visible;
		this.alive_height = alive_height;
		this.nodelist = node;
		this.linklist = link;
		this.map_node = map_n;
		this.map_link = map_l;
		this.wallArea = wall;
		this.buildingArea = building;
		this.safeArea1 = safe;
		this.tsunamiArea = tsunami;
		this.riverArea = river;
		this.virtualposition = space.position(map_node.get(nodelist.get(0)).x, map_node.get(nodelist.get(0)).y);//コピーは×

		position = space.position(map_node.get(nodelist.get(0)).x, map_node.get(nodelist.get(0)).y);//初期位置
		traveled_path.add(position);
		set_route();
	}

	public void set_eval(double eva1, double eva2) {
		eva_distance = eva1;
		eva_height = eva2;
	}

	//2つのPositionが同じかどうか判定
	public boolean isSamePoint(Position pre, Position cur) {
		return !(pre == null || cur == null) && ((pre.x == cur.x) && (pre.y == cur.y));
	}

	public Position returnPos() {
		return position;
	}

	public ArrayList<Position> return_traveld_path() {
		return traveled_path;
	}

	//整数に基づいて移動する
	public void positionByInt(int d) {
		if (d == 1)
			position = position.up();
		if (d == 2)
			position = position.up_right();
		if (d == 3)
			position = position.right();
		if (d == 4)
			position = position.down_right();
		if (d == -1)
			position = position.down();
		if (d == -2)
			position = position.down_left();
		if (d == -3)
			position = position.left();
		if (d == -4)
			position = position.up_left();
	}

	//移動先positionを返す,A*用
	private Position positionByInt2(int d) {
		Position newPosition = null;
		if (d == 0)
			newPosition = virtualposition.up();
		if (d == 1)
			newPosition = virtualposition.up_left();
		if (d == 2)
			newPosition = virtualposition.left();
		if (d == 3)
			newPosition = virtualposition.down_left();
		if (d == 4)
			newPosition = virtualposition.down();
		if (d == 5)
			newPosition = virtualposition.down_right();
		if (d == 6)
			newPosition = virtualposition.right();
		if (d == 7)
			newPosition = virtualposition.up_right();
		return newPosition;
	}

	//障害物判定,A*用
	private boolean check_collision2(Position newPosition) {
		for (Position a_wall : wallArea) {
			if (isSamePoint(newPosition, a_wall)) {
				return true;
			}
		}
		for (Position a_building : buildingArea) {
			if (isSamePoint(newPosition, a_building)) {
				return true;
			}
		}

		for (Position a_danger_area : tsunamiArea) {
			if (isSamePoint(newPosition, a_danger_area)) {
				return true;
			}
		}
		return false;
	}

	//Astarアルゴリズムに基づき経路リストを生成する
	public boolean search() {
		ArrayList<Node2> open_list = new ArrayList<Node2>();
		ArrayList<Node2> closed_list = new ArrayList<Node2>();
		Node2 start_node = new Node2(null, position);//探索開始地点のノード
		Node2 current_node;

		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		open_list.add(start_node);

		//処理時間に制限を設けることによりたどりつけるかどうか判定する
		while (open_list.size() > 0 && (endTime - startTime) < 10) {
			current_node = open_list.get(0);
			int current_index = 0;
			int i = 0;
			//一番F値が小さいpositionを選ぶ
			for (Node2 node : open_list) {
				if (node.f < current_node.f) {
					current_node = node;
					current_index = i;
				}
				i++;
			}
			//選ばれたノードをオープンリストから削除してクローズドリストに追加する
			open_list.remove(current_index);
			closed_list.add(current_node);
			virtualposition = current_node.position;//仮想で移動するために取得

			//ゴールに到達していれば経路を表示して終了
			if (isSamePoint(virtualposition, emergency)) {
				Node2 current = current_node;
				while (current != null) {
					astarpath.add(current.position);
					current = current.parent;
				}
				Collections.reverse(astarpath);
				astarpath.remove(0);
				return true;
			}

			//ゴールではない場合
			ArrayList<Node2> children = new ArrayList<Node2>();

			//上下左右に処理を行う
			for (int d = 0; d < 8; d++) {
				Position newPosition = positionByInt2(d);//
				if (newPosition == null) {
					continue;
				}
				if (check_collision2(newPosition)) {
					continue;
				}
				Node2 new_node = new Node2(current_node, newPosition);
				children.add(new_node);
			}

			//格納されたものに対しての処理
			for (Node2 child : children) {
				boolean flag = false;
				//closeリストにある場合は無視
				for (Node2 closed_child : closed_list) {
					if (isSamePoint(child.position, closed_child.position)) {
						flag = true;
						break;
					}
				}
				if (flag) {
					continue;
				}

				//パラメータ計算
				child.g = current_node.g + 1;
				child.h = Math.max(Math.abs(child.position.x - emergency.x), Math.abs(child.position.y - emergency.y));
				child.f = child.g + child.h;

				//値が小さい場合更新
				for (Node2 open_node : open_list) {
					if (isSamePoint(child.position, open_node.position) && child.g > open_node.g) {
						flag = true;
						break;
					}
				}
				if (flag) {
					continue;
				}
				open_list.add(child);
			}
			endTime = System.currentTimeMillis();
		}
		return false;
	}

	//経路を設定する
	public void set_route() {
		if (count_hop < linklist.size()) {
			LinkedList<Integer> clone = (LinkedList<Integer>) map_link.get(linklist.get(count_hop)).movement.clone();
			process = clone;

			//現在ノードと一致しない場合は経路を逆にする
			if (position.x == map_link.get(linklist.get(count_hop)).x1
					&& position.y == map_link.get(linklist.get(count_hop)).y1) {
			} else {
				Collections.reverse(process);
				for (int i = 0; i < this.process.size(); i++) {
					this.process.set(i, (-1) * this.process.get(i));
				}
			}
			count_hop++;

		} else {
			isgoal = 1;
		}
	}

	//津波の認識
	public void notice_tsunami(int scope) {
		boolean is_tsunami_range = false;

		for (Position adanger : tsunamiArea) {
			//範囲内にいた場合
			if (Math.abs(position.x - adanger.x) < scope && Math.abs(position.y - adanger.y) < scope) {
				is_tsunami_range = !is_tsunami_range;
				break;
			}
		}

		//緊急避難場所を見つける
		if (is_tsunami_range) {
			int x, y;
			double tmp;
			double max_height = 0;
			Position max_position = position;

			/* HACK:
			 * 津波は下から来る想定なので上のみ
			 * emergencyはAstarで行くことのできない可能性があるため
			 * 次の候補を探すなど勘案する必要がある
			 * (今回は通常の避難経路を行かせるようにした)
			 */

			for (int j = position.y - (scope / 2); j < position.y; j++) {
				if (j < 0) {
					y = 0;
				} else if (j > Core.world_y - 1) {
					y = Core.world_y - 1;
				} else {
					y = j;
				}
				for (int i = position.x - (scope / 2); i < position.x + (scope / 2); i++) {
					if (i < 0) {
						x = 0;
					} else if (i > Core.world_x - 1) {
						x = Core.world_x - 1;
					} else {
						x = i;
					}
					tmp = space.position(x, y).z;
					if (tmp > max_height) {
						max_height = tmp;
						max_position = space.position(x, y);
					}
				}
			}

			this.emergency = max_position;

			//emergencyへの経路探索
			if (search()) {
				is_stop = false;
				is_emergency = true;
			}

			//見つからなかった場合(津波などで目的地にたどり着けなかった場合)
			else {
				//通常の避難にする
				is_searched = true;
				is_emergency = false;
			}
		}
	}

	//移動
	public void move() {
		if (isgoal == 0 && isdead == 0) {

			//通常避難
			if (!is_emergency) {

				//移動
				traveled_path.add(position);
				positionByInt(this.process.pop());

				//次の経路を設定
				if (this.process.size() == 0) {
					set_route();
				}

				//目視判定
				if (Core.start_up_time < Core.lasttime && !is_searched) {
					notice_tsunami(range_visible);
				}

				//避難判定
				for (Position safe : safeArea1) {
					if (isSamePoint(position, safe)) {
						System.out.println("ano");
						this.isgoal = 1;
						break;
					}
				}

				//死亡判定
				if (isgoal != 1) {
					for (Position adanger : tsunamiArea) {
						if (isSamePoint(position, adanger)) {
							//生存判定
							System.out.println("nn");
							if (position.z >= alive_height) {
								this.isgoal = 1;
								break;
							}
							this.isdead = 1;
							break;
						}
					}
					/*
					for(Position adanger: riverArea) {
						if(isSamePoint(position,adanger)) {
							if(position.z>=alive_height) {
								this.isgoal=1;
								break;
							}
							this.isdead=1;
							break;
						}
					}
					*/
				}
			}

			//緊急避難モード(近くの高いところへ避難)
			else if (is_emergency) {
				if (!is_stop) {
					if (astarpath.size() == 0) {
						is_stop = true;
					} else {
						position = astarpath.get(0);
						astarpath.remove(0);
					}
				}

				//避難判定
				for (Position safe : safeArea1) {
					if (isSamePoint(position, safe)) {
						this.isgoal = 1;
						break;
					}
				}

				//死亡判定
				if (isgoal != 1) {
					for (Position adanger : tsunamiArea) {
						if (isSamePoint(position, adanger)) {
							//生存判定
							//System.out.println(position.x+","+position.y+","+position.z);
							if (position.z >= alive_height) {
								this.isgoal = 1;
								break;
							}
							this.isdead = 1;
							break;
						}
					}
				}

			}
		}
	}
}
