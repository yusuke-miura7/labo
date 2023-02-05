package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class NSGA2_test {
	double r_share;
	int population_size;
	ArrayList<Node> nodelist = new ArrayList<Node>();
	ArrayList<Link> linklist = new ArrayList<Link>();
	ArrayList<Individual> population = new ArrayList<Individual>();
	ArrayList<Individual> selected = new ArrayList<Individual>();
	ArrayList<ArrayList<ArrayList<Double>>> record_population = new ArrayList<>();//全世代の母集団
	ArrayList<ArrayList<ArrayList<Double>>> record_pareto = new ArrayList<>();//各世代のパレート解
	ArrayList<ArrayList<Individual>> individuallist = new ArrayList<>();
	ArrayList<Long> timelist = new ArrayList<Long>();
	long time;

	//距離が小さい順(昇順)にソートする
	Comparator<Individual> comparator_distance = new Comparator<Individual>() {
		@Override
		public int compare(Individual obj1, Individual obj2) {
			return (int) -(obj2.eva_distance - obj1.eva_distance);
		}
	};

	//標高が小さい順(昇順)にソートする
	/*
	Comparator<Individual> comparator_height = new Comparator<Individual>() {
		@Override
		public int compare(Individual obj1, Individual obj2) {
			return  (int) -(obj2.eva_height - obj1.eva_height);
			}
	};
	*/

	Comparator<Individual> comparator_height = new Comparator<Individual>() {
		@Override
		public int compare(Individual obj1, Individual obj2) {
			int result = Double.valueOf(obj2.eva_height).compareTo(Double.valueOf(obj1.eva_height));
			return -result;
		}
	};

	//河川距離が小さい順(昇順)にソートする
	/*
	Comparator<Individual> comparator_river = new Comparator<Individual>() {
		@Override
		public int compare(Individual obj1, Individual obj2) {
			return (int) -(obj2.eva_river -obj1.eva_river);
		}
	};
	*/

	Comparator<Individual> comparator_river = new Comparator<Individual>() {
		@Override
		public int compare(Individual obj1, Individual obj2) {
			int result = Double.valueOf(obj2.eva_river).compareTo(Double.valueOf(obj1.eva_river));
			return -result;
		}
	};

	/* 混雑度が大きい順(降順)にソートする
	 * 型変換により比較結果に矛盾が生じる
	 * https://pgse.seesaa.net/article/400441842.html
	 */

	Comparator<Individual> comparator_crowing_error = new Comparator<Individual>() {
		@Override
		public int compare(Individual obj1, Individual obj2) {
			return (int) (obj2.crowing - obj1.crowing);
		}
	};

	/* 混雑度が大きい順(降順)にソートする
	 * 上記の解消
	 */
	Comparator<Individual> comparator_crowing = new Comparator<Individual>() {
		@Override
		public int compare(Individual obj1, Individual obj2) {
			int result = Double.valueOf(obj2.crowing).compareTo(Double.valueOf(obj1.crowing));
			return result;
		}
	};

	//コンストラクタ
	NSGA2_test(ArrayList<Node> list1, ArrayList<Link> list2, double r_share) {
		this.nodelist = list1;
		this.linklist = list2;
		this.r_share = r_share;
	}

	//経路ランダム(個体)生成
	public ArrayList<Individual> create_initial_group(int start, int end, int num) {
		Node startnode = nodelist.get(start);
		Node currentnode = startnode;
		Node endnode = nodelist.get(end);
		Node nextnode = null;
		ArrayList<Integer> already_node = new ArrayList<Integer>();//経由したノード番号リスト
		ArrayList<Integer> already_link = new ArrayList<Integer>();//経由したリンク番号リスト
		ArrayList<Integer> next_candidate = new ArrayList<Integer>();//現在ノードの移動可能ノード番号
		already_node.add(startnode.number);
		Random random = new Random();

		ArrayList<Individual> array = new ArrayList<>();
		while (array.size() < num) {
			Individual k = new Individual();

			while (currentnode.number != endnode.number) {

				//現在地からの移動可能ノードを格納
				for (Link alink : currentnode.linklist) {
					next_candidate.add(alink.node1.number);
					next_candidate.add(alink.node2.number);
				}

				//重複削除
				next_candidate = new ArrayList<Integer>(new LinkedHashSet<>(next_candidate));

				//経由したノードは削除
				for (Integer visited : already_node) {
					if (next_candidate.contains(visited)) {
						next_candidate.remove(visited);
					}
				}

				//候補がない場合は初期化して再探索
				if (next_candidate.size() == 0) {
					currentnode = startnode;
					already_node.clear();
					already_node.add(startnode.number);
					already_link.clear();
					next_candidate.clear();
				}

				else {

					//候補に目的地ノードがあった場合そのノードを選択
					if (next_candidate.contains(endnode.number)) {
						for (Link alink : currentnode.linklist) {
							if (alink.node1.number == endnode.number || alink.node2.number == endnode.number) {
								already_link.add(alink.number);
								break;
							}
						}
						currentnode = endnode;
						already_node.add(currentnode.number);
						next_candidate.clear();
					}

					else {

						//候補からランダムに選択
						int randomIndex = random.nextInt(next_candidate.size());
						nextnode = nodelist.get(next_candidate.get(randomIndex));
						for (Link alink : currentnode.linklist) {
							if (alink.node1.number == nextnode.number || alink.node2.number == nextnode.number) {
								already_link.add(alink.number);
								break;
							}
						}
						currentnode = nextnode;
						next_candidate.clear();
						already_node.add(currentnode.number);
					}
				}
			}
			ArrayList<Integer> clone = (ArrayList<Integer>) already_node.clone();
			ArrayList<Integer> clone2 = (ArrayList<Integer>) already_link.clone();
			k.nodelist = clone;
			k.linklist = clone2;
			array.add(k);

			//初期化して再探索
			currentnode = startnode;
			already_node.clear();
			already_node.add(startnode.number);
			already_link.clear();
			next_candidate.clear();
		}
		return array;
	}

	//非優越ソート
	public void non_dominated_sort() {
		ArrayList<Individual> copy_population = (ArrayList<Individual>) population.clone();
		ArrayList<ArrayList<Individual>> array = new ArrayList<>();
		ArrayList<Individual> pareto_group = new ArrayList<>();//ランク1のグループ
		ArrayList<Individual> delete_group = new ArrayList<>();
		ArrayList<Individual> parent_population = new ArrayList<>();//作成される親母集団
		//boolean is_super;
		int capacity_population = population.size() / 2; //個体数の容量
		//int rank = 1;//集団内のランク

		while (parent_population.size() != capacity_population) {
			array = select_rank1(copy_population);
			delete_group = array.get(0);//重複ありのrank1
			pareto_group = array.get(1);//重複なしのrank1
			evaluate_crowing(pareto_group);

			//容量を超える場合 混雑度ソート
			if (parent_population.size() + pareto_group.size() > capacity_population) {

				//混雑度距離でソート
				Collections.sort(pareto_group, comparator_crowing);
				for (Individual k : pareto_group) {
					//System.out.println(k.crowing);
					//容量値になった場合終了
					if (parent_population.size() == capacity_population) {
						break;
					}
					parent_population.add(k);
					copy_population.remove(k);//母集団から取り除く
				}
			}

			//そのまま追加
			else {
				for (Individual k : pareto_group) {
					parent_population.add(k);
					//copy_population.remove(individual);//母集団から取り除く
				}
				//母集団から取り除く
				for (Individual k : delete_group) {
					copy_population.remove(k);
				}
			}
			//rank++;
			pareto_group.clear();

			//母体数が不足した場合
			if (copy_population.size() == 0) {
				System.out.println("aa");
				pareto_group = create_initial_group(131, 21, capacity_population - parent_population.size());
				for (Individual k : pareto_group) {
					parent_population.add(k);
				}
				if (parent_population.size() == capacity_population) {
					System.out.println("ok");
				}
			}

		}
		population = parent_population;
	}

	//混雑度トーナメント選択
	public void tonament(int tonament_size) {
		Random random = new Random();
		for (int i = 0; i < tonament_size; i++) {
			int randomIndex1 = random.nextInt(population.size());
			int randomIndex2 = randomIndex1;

			while (randomIndex1 == randomIndex2) {
				randomIndex2 = random.nextInt(population.size());
			}

			Individual k1, k2;
			k1 = population.get(randomIndex1);
			k2 = population.get(randomIndex2);

			//ランクが低い,ランクが同じ場合は混雑度が大きい方を選出する
			if ((k1.rank < k2.rank) || (k1.rank == k2.rank && k1.crowing > k2.crowing)) {
				selected.add(k1);
			} else {
				selected.add(k2);
			}
		}
	}

	//混雑度トーナメント選択
	public void tonament2(int tonament_size) {
		Random random = new Random();
		double p_select = 0;
		double r = Math.random();
		for (int i = 0; i < tonament_size; i++) {
			int randomIndex1 = random.nextInt(population.size());
			int randomIndex2 = randomIndex1;

			while (randomIndex1 == randomIndex2) {
				randomIndex2 = random.nextInt(population.size());
			}

			Individual k1, k2;
			k1 = population.get(randomIndex1);
			k2 = population.get(randomIndex2);

			//ランクが低い,ランクが同じ場合は混雑度が大きい方を選出する
			if ((k1.rank < k2.rank)) { // || (k1.rank == k2.rank && k1.crowing > k2.crowing)) {
				selected.add(k1);
			} 
			else if(k1.rank==k2.rank) {
				p_select = k1.crowing / (k1.crowing + k2.crowing);
				if (r < p_select) {
					selected.add(k1);
				} else {
					selected.add(k2);
				}
			}
			else {
				selected.add(k2);
			}
		}
	}

	//交叉
	public Individual[] crossover(Individual parent1, Individual parent2) {
		Random random = new Random();
		Individual[] child = new Individual[2];
		ArrayList<Integer> common_node = new ArrayList<Integer>();

		//交叉点候補の共通ノードを追加する
		for (Integer node : parent1.nodelist) {
			if (parent2.nodelist.contains(node)) {
				common_node.add(node);
			}
		}

		//開始地点と目的地点削除
		common_node.remove(parent1.nodelist.get(0));
		common_node.remove(parent1.nodelist.get(parent1.nodelist.size() - 1));

		//共通ノードがなかった場合 交叉しない
		if (common_node.size() == 0) {
			child[0] = parent1;
			child[1] = parent2;
			return child;
		}

		Individual child1 = parent1.clone();
		Individual child2 = parent2.clone();

		//交叉点を一つランダムに選ぶ
		int chosed_Index = random.nextInt(common_node.size());
		int intersection = common_node.get(chosed_Index);

		int index_inter1 = child1.nodelist.indexOf(intersection);
		int index_inter2 = child2.nodelist.indexOf(intersection);

		//交叉点以降のノードを保存
		List<Integer> keep_node1 = new ArrayList<Integer>();
		List<Integer> keep_node2 = new ArrayList<Integer>();

		//交叉点以降のリンクを保存
		List<Integer> keep_link1 = new ArrayList<Integer>();
		List<Integer> keep_link2 = new ArrayList<Integer>();

		//交叉点以降を削除しkeepに代入
		for (int i = index_inter1 + 1; i < child1.nodelist.size(); i++) {
			keep_node1.add(child1.nodelist.get(i));
			keep_link1.add(child1.linklist.get(i - 1));
		}
		child1.nodelist.subList(index_inter1 + 1, child1.nodelist.size()).clear();
		child1.linklist.subList(index_inter1, child1.linklist.size()).clear();

		//交叉点以降を削除しkeepに代入
		for (int i = index_inter2 + 1; i < child2.nodelist.size(); i++) {
			keep_node2.add(child2.nodelist.get(i));
			keep_link2.add(child2.linklist.get(i - 1));
		}
		child2.nodelist.subList(index_inter2 + 1, child2.nodelist.size()).clear();
		child2.linklist.subList(index_inter2, child2.linklist.size()).clear();

		//交叉する
		for (int i = 0; i < keep_node2.size(); i++) {
			child1.nodelist.add(keep_node2.get(i));
			child1.linklist.add(keep_link2.get(i));
		}

		for (int i = 0; i < keep_node1.size(); i++) {
			child2.nodelist.add(keep_node1.get(i));
			child2.linklist.add(keep_link1.get(i));
		}

		//ループを削除
		remove_loop(child1);
		remove_loop(child2);

		child[0] = child1;
		child[1] = child2;
		return child;
	}

	//突然変異
	public void mutation(double r, ArrayList<Individual> array) {
		for (Individual k : array) {
			double p = Math.random();
			if (p < r) {
				Random random = new Random();

				/* FIXME
				 * randomIndexについて
				 * ノードリストのサイズが2以下の場合の例外処理が必要
				 */
				int randomIndex = random.nextInt(1, k.nodelist.size() - 1);
				int start = k.nodelist.get(randomIndex);
				int end = k.nodelist.get(k.nodelist.size() - 1);//一番後ろのインデックスから取得
				k.nodelist.subList(randomIndex, k.nodelist.size()).clear();
				k.linklist.subList(randomIndex, k.linklist.size()).clear();

				//初期集団生成部と同様の処理
				Node startnode = nodelist.get(start);
				Node currentnode = startnode;
				Node endnode = nodelist.get(end);
				Node nextnode = null;
				ArrayList<Integer> already_node = new ArrayList<Integer>();//経由したノード番号リスト
				ArrayList<Integer> already_link = new ArrayList<Integer>();//経由したリンク番号リスト
				ArrayList<Integer> next_candidate = new ArrayList<Integer>();//現在ノードの移動可能ノード番号

				for (Integer a : k.nodelist) {
					already_node.add(a);
				}
				already_node.add(startnode.number);

				while (currentnode.number != endnode.number) {
					for (Link alink : currentnode.linklist) {
						next_candidate.add(alink.node1.number);
						next_candidate.add(alink.node2.number);
					}
					//重複削除
					next_candidate = new ArrayList<Integer>(new LinkedHashSet<>(next_candidate));

					//経由したノードは削除
					for (Integer visited : already_node) {
						if (next_candidate.contains(visited)) {
							next_candidate.remove(visited);
						}
					}
					//候補がない場合
					if (next_candidate.size() == 0) {
						currentnode = startnode;
						already_node.clear();
						for (Integer a : k.nodelist) {
							already_node.add(a);
						}
						already_node.add(startnode.number);
						already_link.clear();
						next_candidate.clear();
					}

					else {
						if (next_candidate.contains(endnode.number)) {
							for (Link alink : currentnode.linklist) {
								if (alink.node1.number == endnode.number || alink.node2.number == endnode.number) {
									already_link.add(alink.number);
									break;
								}
							}
							currentnode = endnode;
							already_node.add(currentnode.number);
							next_candidate.clear();
						}

						else {
							int randomIndex2 = random.nextInt(next_candidate.size());
							nextnode = nodelist.get(next_candidate.get(randomIndex2));
							for (Link alink : currentnode.linklist) {
								if (alink.node1.number == nextnode.number || alink.node2.number == nextnode.number) {
									already_link.add(alink.number);
									break;
								}
							}
							currentnode = nextnode;
							next_candidate.clear();
							already_node.add(currentnode.number);
						}
					}
				}
				//経路生成後
				for (Integer a : k.nodelist) {
					already_node.remove(a);
				}
				for (Integer visited : already_node) {
					k.nodelist.add(visited);
				}
				for (Integer visited : already_link) {
					k.linklist.add(visited);
				}
				//ループ削除
				remove_loop(k);
			}
		}
	}

	//重複割合を返す
	public double r(Individual k1, Individual k2) {

		//和集合を求める
		Set<Integer> setAcopy = new HashSet<>(k1.nodelist);
		Set<Integer> setBcopy = new HashSet<>(k2.nodelist);
		setAcopy.addAll(setBcopy);

		//積集合を求める
		Set<Integer> setAcopy2 = new HashSet<>(k1.nodelist);
		setAcopy2.retainAll(setBcopy);
		return (setAcopy2.size() - 2) / (setAcopy.size() - 2);
	}

	public void evaluate_crowing(ArrayList<Individual> array) {
		/*
		for (Individual k : array) {
			k.crowing = m(k, array);
		}*/
		//1.目的関数:距離でソート
		Collections.sort(array, comparator_distance);
		double value;
		double max = 0;
		double min_dist = (array.get(0)).eva_distance;
		double max_dist = (array.get(array.size() - 1)).eva_distance;
		double normalize_dist = max_dist - min_dist;

		//個体の混雑度計算
		for (int i = 1; i < array.size() - 1; i++) {
			value = (Math.abs((array.get(i - 1)).eva_distance - (array.get(i + 1)).eva_distance))
					/ normalize_dist;
			(array.get(i)).crowing += value;
			if (max < value) {
				max = value;
			}
		}

		(array.get(0)).crowing += max;
		(array.get(array.size() - 1)).crowing += max;

		max = 0;

		//2.目的関数:標高でソート	
		Collections.sort(array, comparator_height);
		double min_height = (array.get(0)).eva_height;
		double max_height = (array.get(array.size() - 1)).eva_height;
		double normalize_height = max_height - min_height;
		//個体の混雑度計算

		for (int i = 1; i < array.size() - 1; i++) {
			value = (Math
					.abs((array.get(i - 1)).eva_height - (array.get(i + 1)).eva_height))
					/ normalize_height;
			(array.get(i)).crowing += value;
			if (max < value) {
				max = value;
			}
		}

		(array.get(0)).crowing = max;
		(array.get(array.size() - 1)).crowing = max;
		max = 0;

		//3.目的関数:河川距離でソート
		Collections.sort(array, comparator_river);
		double min_river = (array.get(0)).eva_river;
		double max_river = (array.get(array.size() - 1)).eva_river;
		double normalize_river = max_river - min_river;
	
		//個体の混雑度計算
		for (int i = 1; i < array.size() - 1; i++) {
			value = (Math
					.abs((array.get(i - 1)).eva_river - (array.get(i + 1)).eva_river))
					/ normalize_river;
			(array.get(i)).crowing += value;
			if (max < value) {
				max = value;
			}
		}

		(array.get(0)).crowing = max;
		(array.get(array.size() - 1)).crowing = max;
	}

	//シェアリング関数
	public int s(double r) {
		if (r >= r_share) {
			return 1;
		} else {
			return 0;
		}
	}

	//ニッチ数を返す
	public int m(Individual k1, ArrayList<Individual> array) {
		int sum = 0;
		for (Individual k2 : array) {
			sum += s(r(k1, k2));
		}
		return sum;
	}

	//個体のrankを更新
	public void evaluate_rank() {
		ArrayList<Individual> evaluated = new ArrayList<>();
		ArrayList<Individual> pareto_group = new ArrayList<>();//最低ランクのグループ
		boolean is_super;
		int rank = 1;
		while (population.size() > 0) {

			for (Individual k1 : population) {
				is_super = true;
				for (Individual k2 : population) {
					if (k1.eva_distance == k2.eva_distance && k1.eva_height == k2.eva_height
							&& k1.eva_river == k2.eva_river) {
						continue;
					} else if (k1.eva_distance >= k2.eva_distance && k1.eva_height <= k2.eva_height
							&& k1.eva_river <= k2.eva_river) {
						is_super = !is_super;
						break;
					}
				}
				//非優越解だった場合
				if (is_super) {
					k1.rank = rank;
					pareto_group.add(k1);
				}
			}
			//母集団から取り除く
			for (Individual k : pareto_group) {
				evaluated.add(k);
				population.remove(k);
			}
			rank++;
			pareto_group.clear();
		}
		population = evaluated;
	}

	//個体のshareF値更新
	public void evaluate_F() {
		int sum_rank = 0;

		//rankの合計
		for (Individual k : population) {
			sum_rank += k.rank;
		}

		//適応度F求める
		for (Individual k : population) {
			k.F += sum_rank / k.rank;
		}

		//類似性を考慮した適応度shareFを求める
		for (Individual k : population) {
			k.shareF = k.F / m(k, population);//ここに時間を取られる
		}
	}

	//累積配列の作成
	public double[] accumulation() {
		double[] array = new double[population.size()];
		double pselect = 0;
		double sum_shareF = 0;

		evaluate_F();//ここに時間をとられる

		//適応度の合計を求める
		for (Individual k : population) {
			sum_shareF += k.shareF;
		}

		for (int i = 0; i < population.size(); i++) {
			pselect += population.get(i).shareF / sum_shareF;
			array[i] = pselect;
		}
		return array;
	}

	public double[] accumulation2(ArrayList<ArrayList<Individual>> p) {
		double[] array = new double[p.size()];
		double pselect = 0;
		double sum_size = 0;

		for (ArrayList<Individual> p1 : p) {
			sum_size += p1.size();
		}
		//System.out.println("a");
		//System.out.println(sum_size);
		for (int i = 0; i < p.size(); i++) {
			pselect += (p.get(i)).size() / sum_size;
			array[i] = pselect;
		}
		return array;
	}

	//ルーレット選択
	public Individual roulette(double[] array, ArrayList<Individual> p) {
		double r = Math.random();
		int i = 0;
		while (array[i] < r) {
			i++;
		}
		return p.get(i);
	}

	public Individual roulette2(double[] array, ArrayList<ArrayList<Individual>> p) {
		double r = Math.random();
		int i = 0;
		while (array[i] < r) {//
			i++;
		}
		return (p.get(i)).get(0);
	}

	//次世代の集団を返す
	public ArrayList<Individual> next_population() {
		Random random = new Random();
		ArrayList<Individual> next_population = new ArrayList<>();

		//パレート保存
		next_population = select_rank1(population).get(1);

		if (next_population.size() > population_size) {
			while (next_population.size() != population_size) {
				int randomIndex = random.nextInt(next_population.size());
				next_population.remove(randomIndex);
			}
		}

		double[] array = accumulation();

		while (next_population.size() != population_size) {
			next_population.add(roulette(array, population));
		}

		return next_population;
	}

	public ArrayList<Individual> select_rank1v2() {
		boolean is_super;
		ArrayList<Individual> pareto_group = new ArrayList<>();

		for (Individual k1 : population) {
			is_super = true;
			for (Individual k2 : population) {
				if (k1.eva_distance == k2.eva_distance && k1.eva_height == k2.eva_height
						&& k1.eva_river == k2.eva_river) {
					continue;
				} else if (k1.eva_distance >= k2.eva_distance && k1.eva_height <= k2.eva_height
						&& k1.eva_river <= k2.eva_river) {
					is_super = !is_super;
					break;
				}
			}

			if (is_super) {
				k1.rank = 1;
				pareto_group.add(k1);
			}
		}

		//ArrayList<Individual> rank1 = new ArrayList<Individual>();
		//show(rank1);

		return pareto_group;
	}

	//rank1のものだけを抽出
	public ArrayList<ArrayList<Individual>> select_rank1(ArrayList<Individual> array) {
		boolean is_super;
		ArrayList<ArrayList<Individual>> list = new ArrayList<>();
		ArrayList<Individual> pareto_group = new ArrayList<>();

		for (Individual k1 : array) {
			is_super = true;
			for (Individual k2 : array) {
				if (k1.eva_distance == k2.eva_distance && k1.eva_height == k2.eva_height
						&& k1.eva_river == k2.eva_river) {
					continue;
				} else if (k1.eva_distance >= k2.eva_distance && k1.eva_height <= k2.eva_height
						&& k1.eva_river <= k2.eva_river) {
					is_super = !is_super;
					break;
				}
			}

			if (is_super) {
				k1.rank = 1;
				pareto_group.add(k1);
			}
		}

		ArrayList<Individual> rank1 = new ArrayList<Individual>();

		//同じ遺伝子を持つ個体の削除
		for (int i = 0; i < pareto_group.size(); i++) {
			Individual k = pareto_group.get(i);
			int j = 0;
			for (j = 0; j < rank1.size(); j++) {
				//同じ遺伝子か判定をする
				if (k.eva_distance == (rank1.get(j)).eva_distance && k.eva_height == (rank1.get(j)).eva_height
						&& k.nodelist.size() == (rank1.get(j)).nodelist.size()) {
					Individual k2 = rank1.get(j);
					int index = 0;
					//ノードリストの番号を精査
					for (Integer node_number : k.nodelist) {
						if (node_number.intValue() == k2.nodelist.get(index).intValue()) {
							index++;
						} else {
							break;
						}
					}
					//全て一致した場合(同一個体だった場合)
					if (index == k.nodelist.size()) {
						j = -1;
						break;
					}
				}
			}
			if (j == rank1.size()) {
				rank1.add(k);
			}
		}
		list.add(pareto_group);//重複ありrank1
		list.add(rank1);//重複なしrank1
		//show(rank1);

		return list;
	}

	public ArrayList<Individual> nsga2(int start, int end, int num_individual, int num_generation, boolean is_river,
			double r, long limit) {
		int mode = 0;
		if (limit > 0) {
			mode = 1;
		}
		//初期集団の生成
		if (num_individual % 2 != 0) {
			throw new NullPointerException("個体数は偶数である必要があります.");
		}
		long startTime1 = System.currentTimeMillis();
		long endTime;
		long pass = 0;
		population = create_initial_group(start, end, 2 * num_individual);
		System.out.println("初期生成後");

		//適応度更新
		for (Individual k : population) {
			k.evaluate(is_river);
		}

		//show(population);

		//集団保存
		add_array(population, record_population);
		ArrayList<Individual> pareto;
		pareto = select_rank1(population).get(1);
		add_array(pareto, record_pareto);

		//世代数
		int current_generation = 0;
		Individual parent1 = null;
		Individual parent2 = null;
		Individual[] child = null;
		ArrayList<Individual> children = new ArrayList<>();

		if (mode == 0) {
			while (current_generation != num_generation+1) {
				if(current_generation!=0 && current_generation%50==0) {
					System.out.println("WAO!");
					//rank1を抽出
					ArrayList<Individual> select = select_rank1(population).get(1);
					individuallist.add(select);
					timelist.add(time);
				}
				//非優劣ソート(上位半分を選出)
				non_dominated_sort();

				//混雑度トーナメント選択(親の選択)
				tonament2(population.size());

				//遺伝子操作
				for (int i = 0; i < selected.size(); i += 2) {
					parent1 = selected.get(i);
					parent2 = selected.get(i + 1);//
					if (parent1 == parent2) {
						parent2 = parent1.clone();
					}

					//交叉
					child = (crossover(parent1, parent2));
					children.add(child[0]);
					children.add(child[1]);

				}

				//突然変異
				mutation(r, children);

				for (Individual k : children) {
					population.add(k);
				}

				children.clear();

				//適応度更新
				for (Individual k : population) {
					k.evaluate(is_river);
				}

				//集団保存
				add_array(population, record_population);
				pareto = select_rank1(population).get(1);
				add_array(pareto, record_pareto);

				current_generation++;
				selected.clear();
				System.out.println(current_generation);
				endTime = System.currentTimeMillis();
				time = endTime-startTime1;
			}
			long endTime1 = System.currentTimeMillis();
			time = endTime1 - startTime1;
			//アルゴリズム終了後
			//rank1の個体を抽出する
			Collections.sort(population, comparator_distance);
			//show(population);
			ArrayList<Individual> select = select_rank1(population).get(1);
			show(select);

			return select;

		} else {
			while (pass < limit) {
				//非優劣ソート(上位半分を選出)
				non_dominated_sort();

				//混雑度トーナメント選択(親の選択)
				tonament2(population.size());

				//遺伝子操作
				for (int i = 0; i < selected.size(); i += 2) {
					parent1 = selected.get(i);
					parent2 = selected.get(i + 1);//
					if (parent1 == parent2) {
						parent2 = parent1.clone();
					}

					//交叉
					child = (crossover(parent1, parent2));
					children.add(child[0]);
					children.add(child[1]);

				}

				//突然変異
				mutation(r, children);

				for (Individual k : children) {
					population.add(k);
				}

				children.clear();

				//適応度更新
				for (Individual k : population) {
					k.evaluate(is_river);
				}

				//集団保存
				add_array(population, record_population);
				pareto = select_rank1(population).get(1);
				add_array(pareto, record_pareto);

				current_generation++;
				selected.clear();
				System.out.println(current_generation);
				endTime = System.currentTimeMillis();
				pass = endTime - startTime1;
			}
			long endTime1 = System.currentTimeMillis();
			time = endTime1 - startTime1;
			//アルゴリズム終了後
			//show(population);
			//rank1の個体を抽出する
			Collections.sort(population, comparator_distance);
			//show(population);
			ArrayList<Individual> select = select_rank1(population).get(1);
			show(select);

			return select;
		}
	}

	//既存手法
	public ArrayList<Individual> existing_method(int start, int end,
			int num_individual, int num_generation, boolean is_river, double r, long limit) {
		Random random = new Random();
		int mode = 0;
		if (limit > 0) {
			mode = 1;
		}
		population_size = num_individual;
		//初期集団の生成
		long startTime1 = System.currentTimeMillis();
		long endTime;
		long pass = 0;
		//long endTime = System.currentTimeMillis();
		population = create_initial_group(start, end, num_individual);
		System.out.println("初期生成後");

		//適応度更新
		for (Individual k : population) {
			k.evaluate(is_river);
		}

		//集団保存
		//			add_array(population,record_population);
		ArrayList<Individual> pareto;
		//			pareto=select_rank1();
		//			add_array(pareto,record_pareto);

		//世代数
		int current_generation = 0;
		int randomIndex1 = 0;
		int randomIndex2 = 0;
		Individual parent1 = null;
		Individual parent2 = null;
		Individual[] child = null;
		ArrayList<Individual> children = new ArrayList<>();

		if (mode == 0) {
			while (current_generation != num_generation + 1) {
				if(current_generation!=0 &&current_generation%50==0) {
					//rank1を抽出
					ArrayList<Individual> select = select_rank1(population).get(1);
					individuallist.add(select);
					timelist.add(time);
				}
				//rankを求める
				evaluate_rank();

				//次世代の集団を求める
				population = next_population();//
				//System.out.println(population.size());
				//集団保存
				add_array(population, record_population);
				pareto = select_rank1(population).get(1);
				add_array(pareto, record_pareto);

				//遺伝子操作
				for (int i = 0; i < population.size(); i++) {
					parent1 = population.get(i);

					int randomIndex = i;
					while (i == randomIndex) {
						randomIndex = random.nextInt(population.size());
					}

					parent2 = population.get(randomIndex);
					if (parent1 == parent2) {
						parent2 = parent1.clone();
					}

					//交叉
					child = (crossover(parent1, parent2));
					children.add(child[0]);
					children.add(child[1]);
				}

				mutation(r, children);

				for (Individual k : children) {
					population.add(k);
				}

				children.clear();

				//適応度更新
				for (Individual k : population) {
					k.evaluate(is_river);
				}

				current_generation++;
				System.out.println(current_generation);
				endTime = System.currentTimeMillis();
				time = endTime-startTime1;
			}
			long endTime1 = System.currentTimeMillis();
			time = endTime1 - startTime1;

			//show(population);
			//rank1の個体を抽出する
			Collections.sort(population, comparator_distance);
			ArrayList<Individual> select = select_rank1(population).get(1);
			show(select);
			return select;
			
		} else {
			while (pass < limit) {
				//rankを求める
				evaluate_rank();

				//次世代の集団を求める
				population = next_population();//
				//System.out.println(population.size());
				//集団保存
				add_array(population, record_population);
				pareto = select_rank1(population).get(1);
				add_array(pareto, record_pareto);

				//遺伝子操作
				for (int i = 0; i < population.size(); i++) {
					parent1 = population.get(i);

					int randomIndex = i;
					while (i == randomIndex) {
						randomIndex = random.nextInt(population.size());
					}

					parent2 = population.get(randomIndex);
					if (parent1 == parent2) {
						parent2 = parent1.clone();
					}

					//交叉
					child = (crossover(parent1, parent2));
					children.add(child[0]);
					children.add(child[1]);
				}

				mutation(r, children);

				for (Individual k : children) {
					population.add(k);
				}

				children.clear();

				//適応度更新
				for (Individual k : population) {
					k.evaluate(is_river);
				}

				current_generation++;
				System.out.println(current_generation);
				endTime = System.currentTimeMillis();
				pass = endTime - startTime1;
			}
			long endTime1 = System.currentTimeMillis();
			time = endTime1 - startTime1;

			//show(population);
			//rank1の個体を抽出する
			Collections.sort(population, comparator_distance);
			ArrayList<Individual> select = select_rank1(population).get(1);
			show(select);
			return select;
		}
	}

	public ArrayList<ArrayList<ArrayList<Double>>> return_record_population() {
		return record_population;
	}

	public ArrayList<ArrayList<ArrayList<Double>>> return_record_pareto() {
		return record_pareto;
	}
	
	//N世代ごとの時間
	public ArrayList<Long> return_timelist(){
		return timelist;
	}
	
	//N世代ごとの個体
	public ArrayList<ArrayList<Individual>> return_individuallist(){
		return individuallist;
	}
	
	public long return_time() {
		return time;
	}
	
	

	//ループを削除する
	public void remove_loop(Individual k) {
		int check_node1, check_node2;

		for (int i = 0; i < k.nodelist.size(); i++) {
			check_node1 = k.nodelist.get(i);

			for (int j = i + 1; j < k.nodelist.size(); j++) {
				check_node2 = k.nodelist.get(j);

				//ループが見つかった場合
				if (check_node1 == check_node2) {
					//(二つ目の)一致以降のノードとリンクを保存しておく
					ArrayList<Integer> keep_node = new ArrayList<Integer>();
					ArrayList<Integer> keep_link = new ArrayList<Integer>();
					for (int s = j; s < k.nodelist.size(); s++) {
						keep_node.add(k.nodelist.get(s));
						if (s < k.linklist.size()) {
							keep_link.add(k.linklist.get(s));
						}
					}

					//(一つ目の)一致以降のノードとリンクを削除
					k.nodelist.subList(i, k.nodelist.size()).clear();
					k.linklist.subList(i, k.linklist.size()).clear();

					//保存しておいた部分を追加
					for (int l = 0; l < keep_node.size(); l++) {
						k.nodelist.add(keep_node.get(l));
						if (l < keep_link.size()) {
							k.linklist.add(keep_link.get(l));
						}
					}
				}
			}
		}
	}

	//デバック用
	public void show(ArrayList<Individual> list) {
		System.out.println("集団のサイズは" + list.size());
		for (Individual k : list) {
			System.out.println("ノードリスト:" + k.nodelist);
			System.out.println("リンクリスト:" + k.linklist);
			System.out.println("距離:" + k.eva_distance);
			System.out.println("標高:" + k.eva_height);
			System.out.println("河川:" + k.eva_river);
			System.out.println("ランク:" + k.rank);
			System.out.println();
		}
		System.out.println("-------------");
	}

	//各世代の集団を記録
	public void add_array(ArrayList<Individual> population, ArrayList<ArrayList<ArrayList<Double>>> array) {
		ArrayList<ArrayList<Double>> one_generation = new ArrayList<>();
		ArrayList<Double> distance_array = new ArrayList<>();
		ArrayList<Double> height_array = new ArrayList<>();
		ArrayList<Double> river_array = new ArrayList<>();

		for (Individual k : population) {
			distance_array.add(k.eva_distance);
			height_array.add(k.eva_height);
			river_array.add(k.eva_river);
		}

		one_generation.add(distance_array);
		one_generation.add(height_array);
		one_generation.add(river_array);

		array.add(one_generation);
	}
}
