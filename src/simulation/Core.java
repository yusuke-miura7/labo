package simulation;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
//import java.util.Random;
import java.util.Set;

public class Core {

	//UI表示(描画)を行うかどうか
	//public static boolean doUI = true;
	public static boolean doUI = false;

	//実験終了時にフェロモンMAPを出力するかどうか
	public static boolean phCheck = false;

	//処理の中断フラグ
	public static boolean pauseFlag = false;

	//表示するマップの次元
	public static int mapDimension = 2;

	//表示次元の変更検知
	public static boolean changeFlag = false;

	//デバック表示エリア
	public static JTextArea area1 = new JTextArea(5, 50);

	//ループにスリープ処理を入れる用
	public static short sleepTime = 100;

	//テキストエリアにデバックメッセージの表示をするかどうか
	public static boolean debugTextFlag = false;

	/**
	 * 提案手法ON/OFF*
	 * isProposalMode:消臭フェロモン塗布*
	 * isProposal Diffusiton:消臭フェロモンの拡散*
	 */
	public static boolean isProposalMode = false;
	public static boolean isProposalDiffusiton = false;

	/**
	 * 経路に対するフェロモンの置き方のちがい*
	 * 1:ゴールしたら塗布*
	 * 2:ステップごとに塗布*
	 */
	public static final int applymethod = 1;

	//frameのサイズ
	public static final int window_width = 1000;
	public static final int window_height = 1000;

	//MAPサイズ
	public static int world_x;
	public static int world_y;
	public static int xSquareSize;
	public static int ySquareSize;

	//実験の終了を管理
	public static boolean endflag = false;

	//目標地点に辿り着いたエージェントと危険区域に巻き込まれて志望したエージェントの数を保持
	static String goalAgents2string, deadAgents2string;

	//ログ吐き出し用のFile
	public static boolean is_createfile;
	public static File outputFileAddress;
	public static String fileName_final;
	public static String fileName_population;
	public static String fileName_pareto;
	public static String fileName_individual;
	public static String fileName_result;

	//探索結果の母集団保存
	static ArrayList<ArrayList<ArrayList<Double>>> record_population1 = new ArrayList<>();
	static ArrayList<ArrayList<ArrayList<Double>>> record_pareto1 = new ArrayList<>();
	static ArrayList<ArrayList<ArrayList<Double>>> record_population2 = new ArrayList<>();
	static ArrayList<ArrayList<ArrayList<Double>>> record_pareto2 = new ArrayList<>();
	static ArrayList<Long> timelist1 = new ArrayList<>();
	static ArrayList<Long> timelist2 = new ArrayList<>();
	static ArrayList<ArrayList<Individual>> individuallist1 = new ArrayList<>();
	static ArrayList<ArrayList<Individual>> individuallist2 = new ArrayList<>();

	//最終上他におけるフェロモンMAPの吐き出し用のFile
	public static File pheromoneOutputFileAddress;

	//シミュレータ上の時間表示
	public static short hour = 14;
	public static int min = 46;
	public static int extratime = 0;

	//火事や倒壊を起こす場所の数
	public static int firenum;

	//MAP情報を格納
	static ArrayList<Node> nodelist = new ArrayList<Node>();
	static ArrayList<Link> linklist = new ArrayList<Link>();
	static ArrayList<ArrayList<Position>> array_field;

	public static int start_up_time;
	public static int lasttime;
	public static int startposition;
	public static int destination;
	public static int population_N;
	public static int generation_N;
	public static double mutation_P1;
	public static double mutation_P2;
	public static boolean is_river;
	public static boolean is_height;
	public static boolean is_force;
	public static double r_share;
	public static double gamma;
	public static long limit;
	public static long time1;
	public static long time2;

	public static void main(String[] args) throws InterruptedException, IOException {
		double const_pheromone = 0.0005;
		doUI = false;
		isProposalMode = true;
		firenum = 0;
		extratime = 10;

		//設計図からMAP作成
		//1.node
		String nodepath = "";

		//2.link
		String linkpath = "";

		//3.height
		String nodeH_path = "";

		int create = 2;
		//ランダムマップ
		if (create == 0) {
			RandomMake make = new RandomMake();
			make.random("random", 80, 80, 100, 200);
			make.random_H("random");
			make.ws_link("random", 8, 0);
			nodepath = "./src/simulation/map/random";
			linkpath = "./src/simulation/map/random_link";
			nodeH_path = "./src/simulation/map/random_H";
		}

		create = 2;
		if (create == 2) {
			//②多賀城市(表示用)
			nodepath = "./src/simulation/map2/tagasiro.txt";
			linkpath = "./src/simulation/map2/tagasiro_link.txt";
			nodeH_path = "./src/simulation/map2/tagasiro_H.txt";

			//①デモマップ
			//nodepath = "src/simulation/demomap/demomap2_river.txt";
			nodepath = "src/simulation/demomap/demomap2_v2.txt";
			linkpath = "src/simulation/demomap/demomap2_link.txt";
			//nodeH_path = "src/simulation/demomap/demomap2_h.txt";
			nodeH_path = "src/simulation/demomap/demomap2_H_new.txt";

		}
		if (create == 3) {
			//②多賀城市(探索用)
			nodepath = "./src/simulation/test/tagasiro.txt";
			linkpath = "./src/simulation/test/tagasiro_new_link.txt";
			nodeH_path = "./src/simulation/test/tagasiro_new_H.txt";
		}

		//マップとして作成するファイル名
		String now = return_now();
		String map_name = "result_" + now;

		//ノード,リンク,標高ファイルからマップを作成
		MakeMap makemap = new MakeMap(nodepath, linkpath, nodeH_path, map_name);
		makemap.make_map();
		nodelist = makemap.return_nodelist();
		linklist = makemap.return_linklist();

		//作成したシミュレーションの舞台
		String mapfilepath = "src/simulation/makedfile/" + map_name;
		String mapfilepath_H = "src/simulation/makedfile/" + map_name + "_H";

		ArrayList<Individual> individuals1 = new ArrayList<Individual>();//シミュレーションさせる方
		ArrayList<Individual> individuals2 = new ArrayList<Individual>();

		//開始地点と目的地
		is_river = true;
		r_share = 0.5;//シェアリング割合
		is_force = false;

		Result result = new Result();
		boolean read = false;
		if (read) {
			ArrayList<Object> array = result.read_file("src/simulation/population/test.txt");
			ArrayList<ArrayList<Double>> pop = (ArrayList<ArrayList<Double>>) array.get(0);
			ArrayList<Individual> ind = (ArrayList<Individual>) array.get(1);
		}

		Astar astar = new Astar(nodelist, linklist);

		destination = 12;
		Individual s = astar.search(118, destination);
		s.evaluate(is_river);
		individuals1.add(s);

		//シミュレーション用
		boolean is_sim = false;
		//		boolean is_sim=true;
		if (is_sim) {
			startposition = 118;
			destination = 12;
			population_N = 100;
			generation_N = 100;
			limit = 0;
			gamma = 163;
			mutation_P1 = 0.10;
			//			is_force=true;
			NSGA2_test ga1 = new NSGA2_test(nodelist, linklist, r_share);
			individuals1 = ga1.nsga2(startposition, destination, population_N, generation_N, is_river,
					mutation_P1, limit);
			System.out.println("ok1");
			record_population1 = ga1.return_record_population();
			record_pareto1 = ga1.return_record_pareto();
			time1 = ga1.return_time();

			//集団の保存
			result.create_file(population_N, generation_N, mutation_P1, "check");
			result.write_final_population(fileName_final, record_pareto1.get(record_pareto1.size() - 1));

			result.write_in_population_file(fileName_pareto, record_pareto1);
			result.write_in_population_file(fileName_population, record_population1);
			System.out.println(result.calculate_cover(record_pareto1.get(record_pareto1.size() - 1), 30));
		}

		//比較
				boolean is_comparison = true;
//		boolean is_comparison = false;
		if (is_comparison) {
			startposition = 131;//確定
			destination = 21;//確定

			population_N = 100;//確定
			generation_N = 100;//これから

			population_N = 200;//確定
			generation_N = 100;//ms
			limit = 5000;
			limit = 0;

			mutation_P1 = 0.01;
			mutation_P2 = 0.01;
			gamma = 117;

			//一つ目
			NSGA2_test ga1 = new NSGA2_test(nodelist, linklist, r_share);
			individuals1 = ga1.nsga2(startposition, destination, population_N, generation_N, is_river,
					mutation_P1, limit);
			System.out.println("ok1");
			record_population1 = ga1.return_record_population();
			record_pareto1 = ga1.return_record_pareto();
			timelist1 = ga1.return_timelist();
			individuallist1 = ga1.return_individuallist();
			time1 = ga1.return_time();
			System.out.println(time1);

			//集団の保存
			result.create_file(population_N, generation_N, mutation_P1, "NSGA2");
			result.write_final_population(fileName_final, record_pareto1.get(record_pareto1.size() - 1));

			result.write_in_population_file(fileName_pareto, record_pareto1);
			result.write_in_population_file(fileName_population, record_population1);

			System.out.println(result.calculate_cover(record_pareto1.get(record_pareto1.size() - 1), 30));

			//二つ目
			//			boolean is_second=false;
			boolean is_second = true;
			if (is_second) {
				NSGA2_test ga2 = new NSGA2_test(nodelist, linklist, r_share);

				//既存手法
				//individuals2 = ga2.existing_method(startposition, destination, population_N, generation_N, is_river, mutation_P2);
				individuals2 = ga2.existing_method(startposition, destination, population_N, generation_N, is_river,
						mutation_P2, limit);
				System.out.println("ok2");
				record_population2 = ga2.return_record_population();
				record_pareto2 = ga2.return_record_pareto();
				timelist2 = ga2.return_timelist();
				individuallist2 = ga2.return_individuallist();
				time2 = ga2.return_time();
				System.out.println(time2);
				//集団の保存
				//result.create_file(population_N, generation_N, mutation_P2, "NSGA2");
				result.create_file(population_N, generation_N, mutation_P2, "EX");
				result.write_in_population_file(fileName_pareto, record_pareto2);
				result.write_in_population_file(fileName_population, record_population2);

				//結果
				//最終世代の比較
				result.create_result_file(record_pareto1.get(record_pareto1.size() - 1),
						record_pareto2.get(record_pareto2.size() - 1), individuals1, individuals2, 30, time1, time2);
				
//				result.create_result_file2(record_pareto1, record_pareto2, individuallist1, individuallist2, timelist1,
//						timelist2, 30);

			}
		}

		//強制終了
				boolean is_stop = false;
//		boolean is_stop = true;
		if (individuals1.size() == 0 || !is_stop) {
			System.out.println("強制終了");
			System.exit(0);
		}

		//フィールド生成(初期フェロモン配置や各座標に高さの代入を行う)
		Space space = new Space(world_x, world_y, mapfilepath_H, 1, 30, const_pheromone);

		//map読み込み
		MapReader generateMap = new MapReader(mapfilepath);
		array_field = generateMap.reader(space);
		ArrayList<Position> wall = array_field.get(0);
		ArrayList<Position> building = array_field.get(1);
		int destination_x = (nodelist.get(destination)).x;
		int destination_y = (nodelist.get(destination)).y;
		Position safe = space.position(destination_x, destination_y);
		ArrayList<Position> safeArea = new ArrayList<>();
		safeArea.add(safe);
		ArrayList<Position> tsunamiArea = array_field.get(3);
		ArrayList<Position> riverArea = array_field.get(4);

		//個体の初期化(Individualから移動Agentへ)
		ArrayList<Agent> agents = new ArrayList<>();
		double alive_height = 0;
		int range_visible = 6;
		int number = 0;
		for (Individual k : individuals1) {
			Agent agent = new Agent(space, number, range_visible, alive_height, k.nodelist, k.linklist,
					nodelist, linklist, wall, building, safeArea, tsunamiArea, riverArea);
			agents.add(agent);
			number++;
		}

		//シミュレーション結果のファイル作成
		//is_createfile=true;
		is_createfile = false;
		if (is_createfile) {
			create_result_file(agents);
		}

		//UIの設定
		doUI = true;
		if (doUI) {
			JFrame frame = new JFrame();//メインシミュレーション画面
			JFrame frame2 = new JFrame();//デバック表示用画面
			UISettings(frame, window_width, window_height);
			DebugSettings(frame2, 400, 400);
			frame.setVisible(true);
			frame2.setVisible(true);
		}

		int agent_num = agents.size();
		int speed_tsunami = 2;
		int width_tsunami = MakeMap.xsize;
		int max_upstream = 0;
		if (tsunamiArea.size() > 0) {
			max_upstream = tsunamiArea.get(0).y;
		}

		endflag = false;
		pauseFlag = false;
		sleepTime = 100;

		//int start_tsunami=200;

		for (double l = 10.0; l < 10.1; l += 0.5) {
			alive_height = l;
			//int start_tsunami=0;
			int start_tsunami = 1000;

			while (start_tsunami < 1001) {

				//初期化
				endflag = false;
				lasttime = 0;
				start_up_time = start_tsunami;
				int count_upstream = 0;

				//個体の初期化(Individualから移動Agentへ)
				tsunamiArea = array_field.get(3);
				riverArea = array_field.get(4);
				agents.clear();
				number = 0;
				for (Individual k : individuals1) {
					Agent agent = new Agent(space, number, range_visible, alive_height, k.nodelist, k.linklist,
							nodelist, linklist, wall, building, safeArea, tsunamiArea, riverArea);
					agents.add(agent);
					number++;
				}

				//シミュレーション部
				while (!endflag) {

					while (pauseFlag)
						Thread.sleep(1);

					//エージェント移動
					for (Agent k : agents) {
						k.move();
					}

					//河川氾濫
					ArrayList<Position> edge = new ArrayList<>();
					//riverArea = extract_edge(riverArea);//

					for (Agent k : agents) {
						k.riverArea = riverArea;
					}

					//System.out.println(riverArea.size());

					//津波の遡上
					if (start_up_time < lasttime) {

						if (count_upstream < max_upstream - 1) {
							int num_up = 0;

							while (num_up < speed_tsunami && count_upstream < max_upstream - 1) {
								tsunamiArea = expand_water(tsunamiArea, width_tsunami);
								num_up++;
								count_upstream++;
							}

							for (Agent k : agents) {
								k.tsunamiArea = tsunamiArea;
							}
						}
					}

					//エージェントのステータス
					int goal = 0;
					int dead = 0;

					for (Agent agent : agents) {
						goal += agent.isgoal;
						dead += agent.isdead;
					}

					goalAgents2string = "goalAgents:" + goal;
					deadAgents2string = "deadAgents:" + dead;

					//時間進行
					lasttime++;

					//1ステップごとに描画更新をかける
					if (doUI) {
						Step2Swing.update(world_x, world_y, space, wall, building, safeArea, agents, nodelist);
						Step2Swing.update_danger(agents.get(0).tsunamiArea, agents.get(0).riverArea);//
					}

					//終了判定
					if (lasttime >= 1000 || goal + dead == agent_num) {
						System.out.println(goal);
						System.out.println(dead);
						System.out.println("finish");
						endflag = true;
						//結果をファイルに書き込む
						if (is_createfile) {
							write_in_result_file(outputFileAddress, start_up_time, agents, alive_height);
						}
						//Thread.sleep(10);
						//CaptureApp.captureImage();
						break;
						//exit(1);
					}

					//確認用にスリープが必要なら
					Thread.sleep(sleepTime);

				}
				start_tsunami++;
			}
		}

	}//main関数終了

	//津波遡上
	public static ArrayList<Position> expand_water(ArrayList<Position> array, int width) {
		ArrayList<Position> danger_array = new ArrayList<Position>();
		int size = array.size();
		//まずコピー
		for (int i = 0; i < size; i++) {
			danger_array.add(array.get(i));
		}
		//最上部を上げて追加
		for (int j = 0; j < width; j++) {
			danger_array.add(0, array.get(j).upnum(1));
		}

		return danger_array;
	}

	//配列中から際となる場所を返す
	public static ArrayList<Position> extract_edge(ArrayList<Position> riverArea) {
		ArrayList<Position> edge_array = new ArrayList<Position>();
		boolean is_edge_down;
		boolean is_edge_left;

		//とりあえずコピー
		for (Position p : riverArea) {
			edge_array.add(p);
		}

		for (Position p : riverArea) {
			is_edge_down = true;
			is_edge_left = true;

			for (Position q : riverArea) {
				if (p.x - 1 == q.x && p.y == q.y) {
					is_edge_left = false;
				}
				if (p.x == q.x && p.y + 1 == q.y) {
					is_edge_down = false;
				}
			}

			//際だった場合
			if (is_edge_down && p.y < Core.world_y - 1) {
				edge_array.add(p.down());
			}
			if (is_edge_left && p.x > 1) {
				edge_array.add(p.left());
			}
		}

		//重複削除
		for (int i = 0; i < edge_array.size(); i++) {
			Position p = edge_array.get(i);
			for (int j = i + 1; j < edge_array.size(); j++) {
				Position q = edge_array.get(j);
				if (p.x == q.x && p.y == q.y) {
					edge_array.remove(q);
				}
			}
		}
		return edge_array;
	}

	private static String return_now() {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		String now = (month + 1) + "_" + day + "_" + hour + "_" + minute;
		return now;
	}

	//シミュレーション結果のファイル生成
	private static void create_result_file(ArrayList<Agent> agents) {
		File resultDir = new File("./src/simulation/result");
		if (!resultDir.exists()) {
			System.out.println("ディレクトリを作成しました");
			resultDir.mkdir();
		}

		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int mill = cal.get(Calendar.MILLISECOND);

		String fileName = ((month + 1) + "_" + day + "_" + hour + "_" + minute + "_" + second + mill + ".csv");
		File outputFile = new File("./src/simulation/result/" + fileName);
		outputFileAddress = outputFile;

		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(filewriter);
		PrintWriter pw = new PrintWriter(bw);
		for (Agent k : agents) {
			pw.println("nodelist: " + k.nodelist);
			pw.println("linklist: " + k.linklist);
			pw.println("eva_distance :" + k.eva_distance);
			pw.println("eva_height :" + k.eva_height);
			pw.println();
		}
		pw.close();
	}

	//シミュレーション結果を書き込む
	private static synchronized void write_in_result_file(File outfile, int start_time, ArrayList<Agent> agents,
			double l) {
		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(outfile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(filewriter);
		pw.print(String.format("%-4d", start_time) + " ");
		pw.print(l + " ");
		for (Agent agent : agents) {
			if (agent.isgoal == 1) {
				pw.print(1);
			} else if (agent.isdead == 1) {
				pw.print(0);
			}
			pw.println();
		}
		pw.close();
	}

	private static void UISettings(JFrame frame, int window_width, int window_height) {

		frame.setTitle("避難シミュレーション");
		//引数1,2：windowsにおける位置、3,4：画面のサイズ
		frame.setBounds(10, 10, window_width, window_height);
		//背景色
		frame.setBackground(Color.white);
		//フレームをwindow画面の中央に表示
		frame.setLocationRelativeTo(null);
		//閉じたときの処理
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//アイコン
		ImageIcon icon = new ImageIcon("src/simulation/mapfile/hinan.png");
		frame.setIconImage(icon.getImage());

		//メニューバー
		final JMenuBar menubar = new JMenuBar();

		JMenu view = new JMenu("表示");
		JMenu run = new JMenu("実行");
		JMenu debug = new JMenu("デバック");

		menubar.add(view);
		menubar.add(run);
		menubar.add(debug);

		final JCheckBoxMenuItem menuItem1_1, menuItem1_2, menuItem1_3, menuItem1_4, menuItem1_5;

		final JMenuItem menuItem2_1, menuItem2_2, menuItem2_3;

		final JMenu menuItem3_1;
		final JMenuItem menuItem3_1_1, menuItem3_1_2, menuItem3_1_3;
		final JMenuItem menuItem3_2;
		final JMenuItem menuItem3_3;
		final JMenuItem menuItem3_4;

		//1列目のアイテム
		menuItem1_1 = new JCheckBoxMenuItem("grid");
		menuItem1_2 = new JCheckBoxMenuItem("agents");
		menuItem1_3 = new JCheckBoxMenuItem("pheromone");
		menuItem1_4 = new JCheckBoxMenuItem("object");
		menuItem1_5 = new JCheckBoxMenuItem("route");

		menuItem1_1.setSelected(true);
		menuItem1_2.setSelected(true);
		menuItem1_3.setSelected(true);
		menuItem1_4.setSelected(true);
		menuItem1_5.setSelected(true);

		//2列目のアイテム
		menuItem2_1 = new JMenuItem("start");
		menuItem2_2 = new JMenuItem("pause");
		menuItem2_3 = new JMenuItem("end");

		//3列目のアイテム
		menuItem3_1 = new JMenu("sleepTime");
		menuItem3_2 = new JMenuItem("debugText");
		menuItem3_3 = new JMenuItem("2D");
		menuItem3_4 = new JMenuItem("3D");

		//3列目のサブアイテム
		menuItem3_1_1 = new JMenuItem("5");
		menuItem3_1_2 = new JMenuItem("10");
		menuItem3_1_3 = new JMenuItem("15");

		//1列目Listener、チェックが入ると変数がtrueになり描写される
		menuItem1_1.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Step2Swing.is_grid = menuItem1_1.isSelected();
					}
				});

		menuItem1_2.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Step2Swing.is_agents = menuItem1_2.isSelected();
					}
				});

		menuItem1_3.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Step2Swing.is_pheromone = menuItem1_3.isSelected();
					}
				});

		menuItem1_4.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Step2Swing.is_object = menuItem1_4.isSelected();
						Step2Swing.is_safe = menuItem1_4.isSelected();
					}
				});

		menuItem1_5.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Step2Swing.is_route = menuItem1_5.isSelected();
					}
				});

		//2列目Listener
		MyListener mylsn = new MyListener();

		menuItem2_1.addActionListener(mylsn);
		menuItem2_1.setActionCommand("start");

		menuItem2_2.addActionListener(mylsn);
		menuItem2_2.setActionCommand("pause");

		menuItem2_3.addActionListener(mylsn);
		menuItem2_3.setActionCommand("end");

		//3列目Lister
		menuItem3_1_1.addActionListener(mylsn);
		menuItem3_1_1.setActionCommand("sleep_5");
		menuItem3_1_2.addActionListener(mylsn);
		menuItem3_1_2.setActionCommand("sleep_10");
		menuItem3_1_3.addActionListener(mylsn);
		menuItem3_1_3.setActionCommand("sleep_15");

		menuItem3_2.addActionListener(mylsn);
		menuItem3_2.setActionCommand("debugText");

		menuItem3_3.addActionListener(mylsn);
		menuItem3_3.setActionCommand("2D");

		menuItem3_4.addActionListener(mylsn);
		menuItem3_4.setActionCommand("3D");

		//1列目追加
		view.add(menuItem1_1);
		view.add(menuItem1_2);
		view.add(menuItem1_3);
		view.add(menuItem1_4);
		view.add(menuItem1_5);

		//2列目追加
		run.add(menuItem2_1);
		run.add(menuItem2_2);
		run.add(menuItem2_3);

		//3列目追加
		debug.add(menuItem3_1);
		menuItem3_1.add(menuItem3_1_1);
		menuItem3_1.add(menuItem3_1_2);
		menuItem3_1.add(menuItem3_1_3);
		debug.add(menuItem3_2);
		debug.add(menuItem3_3);
		debug.add(menuItem3_4);

		frame.setJMenuBar(menubar);

		//スクロールバー
		//x座標オフセット変更
		JScrollBar scroll_bar2 = new JScrollBar(JScrollBar.HORIZONTAL, 0, 5, 0, 104);
		scroll_bar2.setPreferredSize(new Dimension(5, 20));
		ScrollListener scroll_listen2 = new ScrollListener(2);
		scroll_bar2.addAdjustmentListener(scroll_listen2);
		frame.getContentPane().add(BorderLayout.SOUTH, scroll_bar2);

		//y座標オフセット変更
		JScrollBar scroll_bar3 = new JScrollBar(JScrollBar.VERTICAL, 0, 5, 0, 104);
		scroll_bar3.setPreferredSize(new Dimension(20, 20));
		ScrollListener scroll_listen3 = new ScrollListener(3);
		scroll_bar3.addAdjustmentListener(scroll_listen3);
		frame.getContentPane().add(BorderLayout.EAST, scroll_bar3);

		//表示拡大率変更
		JScrollBar scroll_bar4 = new JScrollBar(JScrollBar.HORIZONTAL, 0, 5, 0, 104);
		scroll_bar4.setPreferredSize(new Dimension(20, 20));
		ScrollListener scroll_listen4 = new ScrollListener(4);
		scroll_bar4.addAdjustmentListener(scroll_listen4);
		frame.getContentPane().add(BorderLayout.NORTH, scroll_bar4);

		//キーボード入力受け付け
		KeyInput key_listen = new KeyInput();
		frame.addKeyListener(key_listen);
		frame.requestFocus();

		Step2Swing s2sw = new Step2Swing();
		MouseListen mouse = new MouseListen();
		s2sw.addMouseListener(mouse);

		frame.add(s2sw, BorderLayout.CENTER);
	}

	private static void DebugSettings(JFrame frame, int window_width, int window_height) {
		frame.setTitle("デバック");
		frame.setBounds(50, 250, window_width, window_height);
		frame.setBackground(Color.white);

		//シミュレーション速度変更
		JScrollBar scroll_bar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 5, 0, 104);
		scroll_bar.setPreferredSize(new Dimension(5, 20));
		ScrollListener scroll_listen = new ScrollListener(1);
		scroll_bar.addAdjustmentListener(scroll_listen);
		frame.getContentPane().add(BorderLayout.SOUTH, scroll_bar);

		JScrollPane scrollPane2 = new JScrollPane(area1);
		frame.add(scrollPane2, BorderLayout.CENTER);
	}

}
