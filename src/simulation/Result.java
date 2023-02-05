package simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Result {
	Result() {
	}

	//集団保存用のファイルを作成する
	public void create_file(int population_N, int generation_N, double mutation_P, String str) {
		File resultDir = new File("./src/simulation/population");
		if (!resultDir.exists()) {
			resultDir.mkdir();
			System.out.println("ディレクトリを作成しました");
		}

		String p = Integer.valueOf(population_N).toString();
		String g = Integer.valueOf(generation_N).toString();
		String m = Double.valueOf(mutation_P).toString();

		String fileName0 = (p + "_" + g + "_" + m + "_" + "_final" + str + ".csv");
		String fileName1 = (p + "_" + g + "_" + m + "_" + "_pareto" + str + ".csv");
		String fileName2 = (p + "_" + g + "_" + m + "_" + "_population" + str + ".csv");
		Core.fileName_final = "./src/simulation/population/" + fileName0;
		Core.fileName_pareto = "./src/simulation/population/" + fileName1;
		Core.fileName_population = "./src/simulation/population/" + fileName2;
		File outputFile0 = new File(Core.fileName_final);
		File outputFile1 = new File(Core.fileName_pareto);
		File outputFile2 = new File(Core.fileName_population);

		FileWriter filewriter0 = null;
		FileWriter filewriter1 = null;
		FileWriter filewriter2 = null;

		try {
			filewriter0 = new FileWriter(outputFile0);
			filewriter1 = new FileWriter(outputFile1);
			filewriter2 = new FileWriter(outputFile2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedWriter bw0 = new BufferedWriter(filewriter0);
		BufferedWriter bw1 = new BufferedWriter(filewriter1);
		BufferedWriter bw2 = new BufferedWriter(filewriter2);
		PrintWriter pw0 = new PrintWriter(bw0);
		PrintWriter pw1 = new PrintWriter(bw1);
		PrintWriter pw2 = new PrintWriter(bw2);
		pw0.close();
		pw1.close();
		pw2.close();
	}

	//最終世代を保存
	public void write_final_population(String fileName, ArrayList<ArrayList<Double>> array) {
		try {
			PrintStream sysOut = System.out;
			FileOutputStream fos = new FileOutputStream(fileName);
			PrintStream ps = new PrintStream(fos);

			System.setOut(ps);
			System.out.println(array);

			ps.close();
			fos.close();

			System.setOut(sysOut);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	//集団をファイルに保存する
	public void write_in_population_file(String filename, ArrayList<ArrayList<ArrayList<Double>>> array) {
		try {
			PrintStream sysOut = System.out;
			FileOutputStream fos = new FileOutputStream(filename);
			PrintStream ps = new PrintStream(fos);

			System.setOut(ps);
			System.out.println(array);

			ps.close();
			fos.close();

			System.setOut(sysOut);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	//最終世代の集団ファイルから個体を生成する
	public ArrayList<Object> read_file(String filepath) {
		ArrayList<Object> array = new ArrayList<>();

		ArrayList<Individual> individuals = new ArrayList<>();
		ArrayList<ArrayList<Double>> population = new ArrayList<>();

		ArrayList<Double> eva_distance = new ArrayList<>();
		ArrayList<Double> eva_height = new ArrayList<>();
		ArrayList<Double> eva_river = new ArrayList<>();

		//集団ファイル読み込み(最終世代のみ)
		FileReader fr = null;
		try {
			//File file = new File(filepath);
			//FileReader filereader = new FileReader(file);
			//BufferedReader br = new BufferedReader(filereader);
			fr = new FileReader(filepath);

			int ch;
			int deep = 0;
			int index = 0;
			String str = "";

			while ((ch = fr.read()) != -1) {
				if ((char) ch == '[') {
					deep++;
				} else if ((char) ch == ']') {
					deep--;
				} else if ((char) ch == ',') {
					if (deep == 1) {
						index++;
					}
				}

				if (deep == 2) {
					while ((ch = fr.read()) != -1) {

						if ((char) ch == ',') {
							if (index == 0) {
								eva_distance.add(Double.parseDouble(str));
							} else if (index == 1) {
								eva_height.add(Double.parseDouble(str));
							} else {
								eva_river.add(Double.parseDouble(str));
							}
							str = "";
						}

						else if ((char) ch == ']') {
							if (index == 0) {
								eva_distance.add(Double.parseDouble(str));
							} else if (index == 1) {
								eva_height.add(Double.parseDouble(str));
							} else {
								eva_river.add(Double.parseDouble(str));
							}
							deep--;
							str = "";
							break;
						}

						else {
							str = str + (char) ch;
						}
					}
				}

			}
			population.add(eva_distance);
			population.add(eva_height);
			population.add(eva_river);
			fr.close();

		} catch (FileNotFoundException e) {
			System.out.println(e);

		} catch (IOException e) {
			System.out.println("error");
			System.out.println(e);
		}

		//個体生成
		for (int i = 0; i < eva_distance.size(); i++) {
			Individual k = new Individual();
			k.eva_distance = eva_distance.get(i);
			k.eva_height = eva_height.get(i);
			k.eva_river = eva_river.get(i);
			individuals.add(k);
		}

		array.add(population);
		array.add(individuals);
		return array;
	}

	//集団の比較結果をファイルに作成、書き込む
	public void create_result_file2(ArrayList<ArrayList<ArrayList<Double>>> record_pareto1, ArrayList<ArrayList<ArrayList<Double>>> record_pareto2,
			ArrayList<ArrayList<Individual>> individual1, ArrayList<ArrayList<Individual>> individual2, ArrayList<Long> timelist1,ArrayList<Long> timelist2, int region) {
		File resultDir = new File("./src/simulation/ganbattana");
		if (!resultDir.exists()) {
			System.out.println("ディレクトリを作成しました");
			resultDir.mkdir();
		}
		String p = Integer.valueOf(Core.population_N).toString();
		String g = Integer.valueOf(Core.generation_N).toString();
		String m = Double.valueOf(Core.mutation_P1).toString();

		//String fileName = day + "_" + hour + "_" + minute + "_result";
		String fileName = p + "_" + g + "_" + m + "_result";
		Core.fileName_result = "./src/simulation/ganbattana/" + fileName;

		File outputFile = new File(Core.fileName_result);

		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(filewriter);
		PrintWriter pw = new PrintWriter(bw);
		
		int generation=0;
		double cover1;
		double cover2;
		double[] value1_1;
		double[] value1_2;
		double[] value1_3;
		double[] value2_1;
		double[] value2_2;
		double[] value2_3;
		double[] dominated;
		
		pw.println("個体数");
		pw.println(Core.population_N);
		pw.println();
		
		pw.println("突然変異");
		pw.println(Core.mutation_P1);
		pw.println(Core.mutation_P2);
		pw.println();
		
		//各世代の評価値を求める
		for(int i=1;i<6;i++) {
			generation=i*50;
			pw.println("世代");
			pw.println(generation);
			pw.println();
			
			//被覆率
			cover1 = calculate_cover(record_pareto1.get(generation-1), region);//
			cover2 = calculate_cover(record_pareto2.get(generation-1), region);

			//最大値、最小値、平均値
			value1_1 = calculate_MMA(record_pareto1.get(generation-1).get(0));//距離
			value1_2 = calculate_MMA(record_pareto1.get(generation-1).get(1));//標高
			value1_3 = calculate_MMA(record_pareto1.get(generation-1).get(2));//河川
			value2_1 = calculate_MMA(record_pareto2.get(generation-1).get(0));
			value2_2 = calculate_MMA(record_pareto2.get(generation-1).get(1));
			value2_3 = calculate_MMA(record_pareto2.get(generation-1).get(2));

			//優越割合
		    dominated = calculate_RNI(individual1.get(i-1), individual2.get(i-1));

			pw.println("非劣解個数");
			pw.println(record_pareto1.get(generation-1).get(0).size());
			pw.println(record_pareto2.get(generation-1).get(0).size());
			pw.println();

			pw.println("被覆率");
			pw.println(cover1);
			pw.println(cover2);
			pw.println();

			pw.println("代表値(距離)");
			pw.println(value1_1[0] + "," + value1_1[1] + "," + value1_1[2]);
			pw.println(value2_1[0] + "," + value2_1[1] + "," + value2_1[2]);
			pw.println();

			pw.println("代表値(標高)");
			pw.println(value1_2[0] + "," + value1_2[1] + "," + value1_2[2]);
			pw.println(value2_2[0] + "," + value2_2[1] + "," + value2_2[2]);
			pw.println();

			pw.println("代表値(河川)");
			pw.println(value1_3[0] + "," + value1_3[1] + "," + value1_3[2]);
			pw.println(value2_3[0] + "," + value2_3[1] + "," + value2_3[2]);
			pw.println();

			pw.println("優越割合");
			pw.println(dominated[0]);
			pw.println(dominated[1]);
			pw.println();

			pw.println("処理時間(ms)");
			pw.println(timelist1.get(i-1));
			pw.println(timelist2.get(i-1));
			pw.println();
		}
		
		
		pw.close();
	}

	//集団の比較結果をファイルに作成、書き込む
	public void create_result_file(ArrayList<ArrayList<Double>> array1, ArrayList<ArrayList<Double>> array2,
			ArrayList<Individual> individual1, ArrayList<Individual> individual2, int region,
			long time1, long time2) {
		File resultDir = new File("./src/simulation/result31");
		if (!resultDir.exists()) {
			System.out.println("ディレクトリを作成しました");
			resultDir.mkdir();
		}
		/*
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		*/
		String p = Integer.valueOf(Core.population_N).toString();
		String g = Integer.valueOf(Core.generation_N).toString();
		String m = Double.valueOf(Core.mutation_P1).toString();

		//String fileName = day + "_" + hour + "_" + minute + "_result";
		String fileName = p + "_" + g + "_" + m + "_result";
		Core.fileName_result = "./src/simulation/result31/" + fileName;

		File outputFile = new File(Core.fileName_result);

		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(filewriter);
		PrintWriter pw = new PrintWriter(bw);

		//被覆率
		double cover1 = calculate_cover(array1, region);//
		double cover2 = calculate_cover(array2, region);

		//最大値、最小値、平均値
		double[] value1_1 = calculate_MMA(array1.get(0));
		double[] value1_2 = calculate_MMA(array1.get(1));
		double[] value1_3 = calculate_MMA(array1.get(2));
		double[] value2_1 = calculate_MMA(array2.get(0));
		double[] value2_2 = calculate_MMA(array2.get(1));
		double[] value2_3 = calculate_MMA(array2.get(2));

		//優越割合
		double[] dominated = calculate_RNI(individual1, individual2);

		pw.println("個体数");
		pw.println(Core.population_N);
		pw.println();

		pw.println("世代数");
		pw.println(Core.generation_N);
		pw.println();

		pw.println("突然変異");
		pw.println(Core.mutation_P1);
		pw.println(Core.mutation_P2);
		pw.println();

		pw.println("非劣解個数");
		pw.println(array1.get(0).size());
		pw.println(array2.get(0).size());
		pw.println();

		pw.println("被覆率");
		pw.println(cover1);
		pw.println(cover2);
		pw.println();

		pw.println("代表値(距離)");
		pw.println(value1_1[0] + "," + value1_1[1] + "," + value1_1[2]);
		pw.println(value2_1[0] + "," + value2_1[1] + "," + value2_1[2]);
		pw.println();

		pw.println("代表値(標高)");
		pw.println(value1_2[0] + "," + value1_2[1] + "," + value1_2[2]);
		pw.println(value2_2[0] + "," + value2_2[1] + "," + value2_2[2]);
		pw.println();

		pw.println("代表値(河川)");
		pw.println(value1_3[0] + "," + value1_3[1] + "," + value1_3[2]);
		pw.println(value2_3[0] + "," + value2_3[1] + "," + value2_3[2]);
		pw.println();

		pw.println("優越割合");
		pw.println(dominated[0]);
		pw.println(dominated[1]);
		pw.println();

		pw.println("処理時間(ms)");
		pw.println(time1);
		pw.println(time2);
		pw.close();
	}

	//被覆率を求める
	public static double calculate_cover(ArrayList<ArrayList<Double>> array, int region_num) {

		if (region_num == 0) {
			throw new NullPointerException("region_num must not be 0.");
		}

		int objective_num = array.size();//目的関数の数
		double min, max, init;
		double range;
		//int region;
		double sum = 0;
		//double sum_region=0;

		for (ArrayList<Double> objective_array : array) {
			min = Collections.min(objective_array);
			max = Collections.max(objective_array);
			range = max - min;
			init = range / region_num;

			int[] check = new int[region_num];
			double[] check_range = new double[region_num + 1];

			//精査範囲代入
			for (int i = 0; i < region_num + 1; i++) {
				check_range[i] = min + init * i;
			}

			for (Double value : objective_array) {
				for (int i = 0; i < region_num; i++) {
					if (check_range[i] <= value && value < check_range[i + 1]) {//
						check[i] = 1;
						break;
					}
				}
			}

			for (int i = 0; i < check.length; i++) {
				sum += check[i];
			}
		}
		return sum / (region_num * objective_num);
	}

	//被覆率を求める
	public static double calculate_cover2(ArrayList<ArrayList<Double>> array, int region_num) {

		if (region_num == 0) {
			throw new NullPointerException("region_num must not be 0.");
		}

		int objective_num = array.size();//目的関数の数
		//System.out.println(objective_num);
		double min, max, init;
		double range;
		int region;
		double sum = 0;
		double sum_region = 0;

		for (ArrayList<Double> objective_array : array) {
			min = Collections.min(objective_array);
			max = Collections.max(objective_array);
			range = max - min;
			//System.out.println(range);
			init = range / region_num;
			//System.out.println();
			if (init < 0.1) {
				region = (int) (range * 10);
				sum_region += region;
				System.out.println(min);
				System.out.println(max);
				System.out.println(region);
				init = 0.1;
			}
			if (0.1 < init && init < 1) {
				region = (int) range;
				sum_region += region;
				System.out.println("check");
				System.out.println(min);
				System.out.println(max);
				System.out.println(region);
				init = 1;
			} else {
				region = region_num;
				sum_region += region;
			}
			int[] check = new int[region];
			double[] check_range = new double[region + 1];

			//精査範囲代入
			for (int i = 0; i < region + 1; i++) {
				check_range[i] = min + init * i;
			}

			for (Double value : objective_array) {
				for (int i = 0; i < region; i++) {
					if (check_range[i] <= value && value < check_range[i + 1]) {//
						check[i] = 1;
						break;
					}
				}
			}

			for (int i = 0; i < check.length; i++) {
				sum += check[i];
			}
		}
		return sum / (sum_region / 3 * objective_num);
	}

	//最大値、最小値、平均値を求める
	public static double[] calculate_MMA(ArrayList<Double> array) {
		double[] values = new double[3];
		values[0] = Collections.max(array);
		values[1] = Collections.min(array);
		double sum = 0;
		for (Double value : array) {
			sum += value;
		}
		values[2] = sum / array.size();
		return values;
	}

	//優越個体の割合を求める
	public static double[] calculate_RNI(ArrayList<Individual> array1, ArrayList<Individual> array2) {
		double[] ratio = new double[2];
		Set<Individual> setAcopy = new HashSet<>(array1);
		//show(setAcopy);
		Set<Individual> setBcopy = new HashSet<>(array2);
		//show(setBcopy);
		setAcopy.addAll(setBcopy);//重複されていないか？和集合
		//show(setAcopy);
		//非劣解の個数
		double count1 = 0;
		double count2 = 0;
		boolean is_super;

		//HACK：あとで綺麗に
		for (Individual k1 : array1) {
			is_super = true;
			for (Individual k2 : setAcopy) {
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
				count1++;
			}
		}

		for (Individual k1 : array2) {
			is_super = true;
			for (Individual k2 : setAcopy) {
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
				count2++;
			}
		}

		System.out.println(count1);
		System.out.println(count2);
		ratio[0] = count1 / (count1 + count2);
		ratio[1] = count2 / (count1 + count2);
		return ratio;
	}

	//個体のデバック用
	public static void show(Set<Individual> list) {
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

}
