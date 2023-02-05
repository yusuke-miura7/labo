package simulation;

import java.util.ArrayList;
import java.util.LinkedList;

public class Individual implements Cloneable {
	int rank;
	//開始地点
	int start_x = 50;
	int start_y = 86;
	int destination_x = 55;
	int destination_y = 16;
	double F;
	double crowing;
	double eva_distance;
	double eva_height;
	double eva_river;
	double shareF;

	ArrayList<Integer> nodelist = new ArrayList<Integer>();
	ArrayList<Integer> linklist = new ArrayList<Integer>();
	ArrayList<Link> maplink = MakeMap.linklist;

	@Override
	public Individual clone() {
		Individual k = new Individual();
		k.rank = this.rank;
		k.F = this.F;
		k.crowing = this.crowing;
		k.eva_distance = this.eva_distance;
		k.eva_height = this.eva_height;
		k.eva_river = this.eva_river;
		k.shareF = this.shareF;
		ArrayList<Integer> clone1 = (ArrayList<Integer>) nodelist.clone();
		ArrayList<Integer> clone2 = (ArrayList<Integer>) linklist.clone();
		ArrayList<Link> clone3 = (ArrayList<Link>) maplink.clone();
		k.nodelist = clone1;
		k.linklist = clone2;
		k.maplink = clone3;
		return k;
	}

	//意図的なノードリストを作成する場合
	public void set_nodelist(ArrayList<Integer> array) {
		nodelist = array;
	}

	//意図的なリンクリストを作成する場合
	public void set_linklist(ArrayList<Integer> array) {
		linklist = array;
	}

	//引数の座標とriverリストの中から最短距離を求める
	public double return_distance_river(int x, int y) {
		int min = 100000;
		int tmp;
		if (MakeMap.riverlist.size() == 0) {
			return 0;
		}
		for (SimpleNode river : MakeMap.riverlist) {
			tmp = Math.max(Math.abs(x - river.x), Math.abs(y - river.y));
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}

	public boolean is_getclose(int current_x, int current_y, int next_x, int next_y) {
		int check_x = destination_x - current_x;
		int check_y = destination_y - current_y;
		//上に行くのだめ
		//目的地が下にある場合
		if (check_y > 0) {
			//下に行く
			if (current_y - next_y <= 0) {
				return true;
			} else {
				return false;
			}
		}
		//下に行くのだめ
		//目的地が上にある場合
		else if (check_y < 0) {
			//上に行く
			if (current_y - next_y >= 0) {
				return true;
			} else {
				return false;
			}
		}
		//同じy軸にいる場合
		else {
			//右にいる場合
			if (check_x < 0) {
				if (current_x - next_x > 0) {
					return true;
				} else {
					return false;
				}
			}
			//左にいる場合
			else {
				if (current_x - next_x < 0) {
					return true;
				} else {
					return false;
				}
			}
		}
	}

	public boolean is_getclose2(int current_x, int current_y, int next_x, int next_y) {
		int x = Math.abs(current_x - destination_x) + Math.abs(current_y - destination_y);
		int y = Math.abs(next_x - destination_x) + Math.abs(next_y - destination_y);
		//近づいている場合
		if (x <= y) {
			return true;
		} else {
			return false;
		}
	}

	public boolean is_getclose3(int next_y) {
		if (next_y >= start_y) {
			return true;
		} else {
			return false;
		}
	}

	//適応度を求める
	public void evaluate(boolean is_river) {
		int sum_distance = 0;
		double sum_height = 0;
		double sum_distance_river = 0;

		//距離の適応度
		//総移動距離,標高
		Link link;
		int current_x = start_x;
		int current_y = start_y;
		for (Integer linknumber : linklist) {
			sum_distance += (maplink.get(linknumber)).distance;

			link = maplink.get(linknumber);
			//違う方を選択していく

			if (link.x1 == current_x && link.y1 == current_y) {
				sum_height += link.local_sum_height;
				//一つ一つのセル
				for (SimpleNode node : link.route) {
					sum_distance_river += return_distance_river(node.x, node.y);
				}
				current_x = link.x2;
				current_y = link.y2;
				
			} else {
				sum_height += link.local_sum_height;
				//一つ一つのセル
				for (SimpleNode node : link.route) {
					sum_distance_river += return_distance_river(node.x, node.y);
				}
				current_x = link.x1;
				current_y = link.y1;
			}
		}
		this.eva_distance = sum_distance;
		this.eva_height = Math.round((sum_height / (sum_distance)) * 10.0) / 10.0;
		this.eva_river = Math.round(sum_distance_river / (sum_distance));
		if (eva_distance > Core.gamma) {
			this.eva_height = 0;
			this.eva_river = 0;
		}

		//初期化
		this.rank = 0;
		this.crowing = 0;

	}

	//適応度を求める
	public void evaluate2(boolean is_river) {
		int sum_distance = 0;
		double sum_height = 0;
		double sum_distance_river = 0;

		//距離の適応度
		//総移動距離,標高
		Link link;
		int current_x = start_x;
		int current_y = start_y;
		for (Integer linknumber : linklist) {
			sum_distance += (maplink.get(linknumber)).distance;

			link = maplink.get(linknumber);
			//違う方を選択していく

			if (link.x1 == current_x && link.y1 == current_y) {
				//代入前に計算
				if (Core.is_force) {

					if (is_getclose3(link.y2)) {
						sum_height += link.local_sum_height;
						//一つ一つのセル
						for (SimpleNode node : link.route) {
							sum_distance_river += return_distance_river(node.x, node.y);
						}
					}
					current_x = link.x2;
					current_y = link.y2;
				} else {
					if (is_getclose(current_x, current_y, link.x2, link.y2)) {
						sum_height += link.local_sum_height;
						//一つ一つのセル
						for (SimpleNode node : link.route) {
							sum_distance_river += return_distance_river(node.x, node.y);
						}
					}
					current_x = link.x2;
					current_y = link.y2;
				}

			} else {
				if (Core.is_force) {
					if (is_getclose3(link.y1)) {
						sum_height += link.local_sum_height;
						//一つ一つのセル
						for (SimpleNode node : link.route) {
							sum_distance_river += return_distance_river(node.x, node.y);
						}
					}
					current_x = link.x1;
					current_y = link.y1;
				} else {
					if (is_getclose(current_x, current_y, link.x1, link.y1)) {
						sum_height += link.local_sum_height;
						//一つ一つのセル
						for (SimpleNode node : link.route) {
							sum_distance_river += return_distance_river(node.x, node.y);
						}
					}
					current_x = link.x1;
					current_y = link.y1;
				}

			}

			//sum_height += (maplink.get(linknumber)).local_sum_height;//安易に足してはダメ?
		}
		//this.eva_distance = Math.round(sum_distance * 1000.0) / 1000.0;
		//this.eva_height = sum_height / (sum_distance);
		//this.eva_river = sum_distance_river / (sum_distance);
		//this.eva_height = Math.round(sum_height / (sum_distance));
		//this.eva_river = Math.round((sum_distance_river / (sum_distance)) * 10.0) / 10.0;
		this.eva_distance = sum_distance;
		this.eva_height = Math.round((sum_height / (sum_distance)) * 10.0) / 10.0;
		this.eva_river = Math.round(sum_distance_river / (sum_distance));
		if (eva_distance > 160) {
			this.eva_height = 0;
			this.eva_river = 0;
		}

		//初期化
		this.rank = 0;
		this.crowing = 0;

	}
}
