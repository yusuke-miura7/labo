package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

public class RandomMake {
	static ArrayList<SimpleNode> nodelist = new ArrayList<>();
	ArrayList<SimpleLink> linklist = new ArrayList<>();
	int distance_array[][];
	int Node_number=0;//ノード番号
	int xsize,ysize=0;
	
	/*碁盤の目状のマップを作成する
	 * @param filename:出力するファイル名
	 * @param x,y:マップのサイズ
	 * @param se_x,se_y:ノードの間隔
	 */
	public void intersection(String filename,int x,int y,int se_x,int se_y) throws IOException{
		xsize=x;
		ysize=y;
		try {
			File resultDir = new File("./src/simulation/map");
			if(!resultDir.exists()) {
				resultDir.mkdir();
			}
			String fileName = "./src/simulation/map/"+filename;
			File outputFile = new File(fileName);
			if(outputFile.createNewFile()) {
				
			}
			
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			int separate_x=se_x;
			int separate_y=se_y;
			
			//1行目
			for(int i=0;i<x+2;i++) {
				pw.print(1);
			}
			pw.println();
			//2〜y+1行目
			int count_y=0;
			for(int j=0;j<y;j++) {
				int flag=0;
				if(count_y%separate_y==0) {
					flag=1;
				}
				int count_x=0;
				pw.print(1);
				
				for(int i=0;i<x;i++) {
					if(count_x%separate_x==0&&flag==1) {
						pw.print(5);
						Node_number++;
					}
					else {
						pw.print(1);
					}
					count_x++;
				}
				pw.print(1);
				pw.println();
				count_y++;
			}
			//y+2行目
			for(int i=0;i<x+2;i++) {
				pw.print(1);
			}
			pw.println();
			
			pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/*碁盤の目状のリンクを作成する
	 * @param filename:読み込むファイル名
	 */
	public void intersection_link(String filename) {
		try {
			//読み込み用
			String fileName = "./src/simulation/map/"+filename;
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
			
			//書き出し用
			String fileName2 = "./src/simulation/map/"+filename+"_link";
			File outputFile = new File(fileName2);
			if(outputFile.createNewFile()) {
			}
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			
			String line;
			String data;
			int x_num=0;
			int y_num=0;
			boolean check_x=false;
			boolean check_y=false;
			
			while((line=br.readLine()) != null) {
				int x=0;
				check_y=false;
				while(x<line.length()) {
					data=line.substring(x,x+1);
					if(Integer.parseInt(data)==5) {
						check_y=true;
						if(!check_x) {
							x_num++;
						}
						else {
							break;
						}
					}
					x++;
				}
				if(x_num>0) {
					check_x=true;
				}
				if(check_y) {
					y_num++;
				}
			}
			
			pw.print("No"+ "  ");
			pw.print("Node1"+" ");
			pw.print("Node2"+" ");
			pw.print("type" + " ");
			pw.print("valid" + " ");
			pw.println();
			
			int number=0;
			int first,second,third;
			for(int j=0;j<y_num;j++) {
				for(int i=0;i<x_num;i++) {
					first=j*x_num+i;
					//横隣り
					second=first+1;
					//下
					third=(j+1)*x_num+i;
					
					//横のリンクをつなぐ
					if(i<x_num-1) {
						pw.print(String.format("%-4d",number));
						pw.print(String.format("%-6d",first));
						pw.print(String.format("%-6d",second));
						pw.print(String.format("%-5d",1));
						pw.print(String.format("%-4d",1));
						pw.println();
						number++;
					}
					
					//縦のリンクをつなぐ
					if(j<y_num-1) {
						pw.print(String.format("%-4d",number));
						pw.print(String.format("%-6d",first));
						pw.print(String.format("%-6d",third));
						pw.print(String.format("%-5d",1));
						pw.print(String.format("%-4d",1));
						pw.println();
						number++;
					}
				}
			}
			br.close();
			pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	/*マップに海を追加する
	 *@param filename:読み込むファイル名
	 *@param y:追加するy座標
	 */
	public void add_sea(String filename,int y) {
		try {
			//読み込み用
			String fileName = "./src/simulation/map2/"+filename;
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
			
			//書き出し用
			String fileName2 = "./src/simulation/map2/"+filename+"_sea";
			File outputFile = new File(fileName2);
			if(outputFile.createNewFile()) {
			}
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			
			String line;
			int row=0;
			xsize=80;
			ysize=80;
			while((line=br.readLine()) != null) {
				if(y<=row&&row<=ysize) {
					pw.print(1);
					for(int i=0;i<xsize;i++) {
						pw.print(4);
					}
					pw.println(1);
				}
				else {
					pw.println(line);
				}
				row++;
			}
			pw.close();
			br.close();			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/*ランダムマップを作成する
	 * @param filename:出力するファイル名
	 * @param x,y:マップのサイズ
	 * @param start,end:ノード生成乱数の範囲
	 */
	public void random(String filename,int x,int y,int start,int end) throws IOException{
		xsize=x;
		ysize=y;
		try {
			File resultDir = new File("./src/simulation/map");
			if(!resultDir.exists()) {
				resultDir.mkdir();
			}
			String fileName = "./src/simulation/map/"+filename;
			File outputFile = new File(fileName);
			if(outputFile.createNewFile()) {
				
			}
			
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			int separate_x=5;
			//int separate_y=0;
			
			//1行目
			for(int i=0;i<x+2;i++) {
				pw.print(1);
			}
			pw.println();
			
			//2〜y+1行目
			Random random = new Random();
			//int count_y=0;
			int count_x=0;
			for(int j=0;j<y;j++) {
				int flag=1;
				separate_x=random.nextInt(start,end);
				/*
				if(count_y%separate_y==0) {
					flag=1;
				}
				*/
				pw.print(1);
				
				for(int i=0;i<x;i++) {
					if(count_x!=0&&count_x%separate_x==0&&flag==1) {
						pw.print(5);
						nodelist.add(new SimpleNode(Node_number,i,j));
						Node_number++;
						separate_x=random.nextInt(start,end);//再設定
						count_x=0;
					}
					else {
						pw.print(0);
						/*
						if(i==0||i==x-1) {
							pw.print(1);
						}
						else {
							pw.print(0);
						}*/
					}
					count_x++;
				}
				pw.print(1);
				pw.println();
				//count_y++;
			}
			//y+2行目
			for(int i=0;i<x+2;i++) {
				pw.print(1);
			}
			pw.println();
			pw.close();
			make_distance();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//対数正規分布に従う乱数により標高マップを作成
	public void random_H(String filename) {
		try {
			//読み込み用
			String fileName = "./src/simulation/map2/"+filename;
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
			
			//書き出し用
			String fileName2 = "./src/simulation/map2/"+filename+"_H";
			File outputFile = new File(fileName2);
			if(outputFile.createNewFile()) {
			}
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));

			String line;
			String data="";
			int number=0;
			double num;
			
			Gaussian test= new Gaussian();
			
			while((line=br.readLine()) != null) {
				int x=0;
				while(x<line.length()) {
					data=line.substring(x,x+1);
					if(Integer.parseInt(data)==5) {
						pw.print(String.format("%-4d",number));
						num=test.lognormal(1.6, 0.5);
						if(num>=10) {
							pw.println(String.format("%.1f", num));
						}
						else {
							pw.println(String.format("%.2f", num));
						}
						number++;
					}
					x++;
				}
			}
			pw.close();
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//各ノードの距離配列を作成
	public void make_distance() {
		int distance;
		SimpleNode node1,node2;
		System.out.println(nodelist.size());
		distance_array=new int[Node_number][Node_number];
		for(int i=0;i<nodelist.size();i++) {
			for(int j=i+1;j<nodelist.size();j++) {
				node1=nodelist.get(i);//
				node2=nodelist.get(j);
				distance=Math.max(Math.abs(node1.x-node2.x),Math.abs(node1.y-node2.y));
				distance_array[i][j]=distance_array[j][i]=distance;//対称
			}
		}
		/*
		//デバック用
		for(int k=0;k<Node_number;k++) {
			for(int s=0;s<Node_number;s++) {
				System.out.print(distance_array[k][s]+" ");
			}
			System.out.println();
		}
		*/
	}
	
	/*//Watts-Strongatzモデルに基づきリンクを生成
	 * @param outputfile:出力するファイル名
	 * @param k:平均次数
	 * @param p:リンクが張り替えられる確率
	 */
	public void ws_link(String outputfile,int k,double p) {
		if(k%2==1) {
			throw new InvalidParameterException("平均時数kは偶数である必要があります");
		}
		if(p<0 ||1< p) {
			throw new InvalidParameterException("確率pは0以上1以下である必要があります");
		}
		try {
			String filename="./src/simulation/map/"+outputfile+"_link";
			File outputFile = new File(filename);
			outputFile.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			
			//distance_arrayに基づいて近隣ノードのリンクを作る
			SimpleNode current,chosen;
			int count=0;
			int min;
			int chosen_index;
			
			/*
			//各ノードに対して指定個ずつ隣接ノードを付与していく場合
			for(int i=0;i<Node_number;i++) {
				//指定数に達するまでループ必要
				current=nodelist.get(i);
				count=0;
				while(count<k/2) {
					min=100000;
					selected_index=0;
					for(int j=i+1;i<Node_number;j++) {
						if(distance_array[i][j]<min&&!()) {
							min=distance_array[i][j];
							selected_index=j;
						}
					}
					current.link.add(selected_index);
					already.add(selected_index);
				}
				count++;
				already.clear();
			}*/
			
			//各ノードの隣接ノードの個数が指定個にする場合
			//後半のノードほど距離が反映されない、今までのノードとのリンクでつながれてしまうため
			/*
			for(int i=0;i<Node_number;i++) {
				//指定数に達するまでループ必要
				current=nodelist.get(i);
				while(current.link.size()<k/2) {
					min=100000;
					chosen_index=0;
					for(int j=i+1;i<Node_number;j++) {
						//距離が更新した場合
						if(distance_array[i][j]<min&&!(current.link.contains(j))&&(nodelist.get(j).link.size()<k/2)) {
							min=distance_array[i][j];
							chosen_index=j;
						}
					}
					//リンクが一つ決定
					//終点のリストの追加
					chosen=nodelist.get(chosen_index);
					chosen.link.add(i);
					//始点のリストの追加
					current.link.add(chosen_index);
				}
				//while終了
			}*/
			int tmp;
			//修正ver
			for(int i=0;i<Node_number;i++) {
				current=nodelist.get(i);
				while(current.link.size()<k/2) {
					min=100000;
					chosen_index=0;
					for(int j=0;j<Node_number;j++) {
						if((distance_array[i][j]!=0)&&distance_array[i][j]<min&&!(current.link.contains(j))&&(nodelist.get(j).link.size()<k/2)){
							min=distance_array[i][j];
							chosen_index=j;
						}
					}
					current.link.add(chosen_index);
					chosen=nodelist.get(chosen_index);
					chosen.link.add(i);
					if(i>chosen_index) {
						tmp=i;
						i=chosen_index;
						chosen_index=tmp;
						
					}
					linklist.add(new SimpleLink(count,i,chosen_index));
					count++;
				}
			}
			
			pw.print("No"+ "  ");
			pw.print("Node1"+" ");
			pw.print("Node2"+" ");
			pw.print("type" + " ");
			pw.print("valid" + " ");
			pw.println();
			/*
			for(SimpleNode node:nodelist) {
				for(int a:node.link) {
					pw.print(String.format("%-4d",number));
					pw.print(String.format("%-6d",node.number));
					pw.print(String.format("%-6d",a));
					pw.print(String.format("%-5d",1));
					pw.print(String.format("%-4d",1));
					pw.println();
					number++;
				}
			}*/
			
			for(SimpleLink link:linklist) {
				pw.print(String.format("%-4d",link.number));
				pw.print(String.format("%-6d",link.node1));
				pw.print(String.format("%-6d",link.node2));
				pw.print(String.format("%-5d",1));
				pw.print(String.format("%-4d",1));
				pw.println();
			}
			
			pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		ArrayList<ArrayList<ArrayList<Integer>>> arrays3 = new ArrayList<>(); 
		ArrayList<ArrayList<Integer>> arrays2 = new ArrayList<>();
		ArrayList<Integer> distance = new ArrayList<>();
		ArrayList<Integer> height = new ArrayList<>();
		for(int i=0;i<4;i++) {
			distance.add(i);
			height.add(i+4);
			//arrays.add(a);
		}
		arrays2.add(distance);
		arrays2.add(height);
		
		arrays3.add(arrays2);
		System.out.println(arrays2);
		System.out.println(arrays3);
		System.out.println(arrays3.get(0));
		/* arrays3.get(i).get(j)
		 * i世代の
		 * j:0 距離配列
		 *  :1 標高配列
		 */
		
		System.out.println(arrays3.get(0).get(0));
		//
		System.out.println(arrays3.get(0).get(1));
		/*
		Individual k= new Individual();
		k.nodelist.add(0);
		k.nodelist.add(1);
		k.nodelist.add(2);
		k.nodelist.add(3);
		k.nodelist.add(5);
		k.nodelist.add(2);
		k.nodelist.add(7);
		k.nodelist.add(9);
		k.linklist.add(10);
		k.linklist.add(11);
		k.linklist.add(12);
		k.linklist.add(13);
		k.linklist.add(14);
		k.linklist.add(15);
		k.linklist.add(16);
		for(int i=0;i<k.nodelist.size();i++) {
			int check1 = k.nodelist.get(i);
			for(int j=i+1;j<k.nodelist.size();j++) {
				int check2 = k.nodelist.get(j);
				//ループが見つかった場合
				if(check1==check2) {
					System.out.println("found_loop");
					
					ArrayList<Integer> keep_node = new ArrayList<Integer>();
					ArrayList<Integer> keep_link = new ArrayList<Integer>();
					for(int s=j;s<k.nodelist.size();s++) {
						keep_node.add(k.nodelist.get(s));
						if(s<k.linklist.size()) {
							keep_link.add(k.linklist.get(s));
						}
						
					}
					k.nodelist.subList(i,k.nodelist.size()).clear();//削除
					k.linklist.subList(i,k.linklist.size()).clear();
					for(int a=0;a<keep_node.size();a++) {
						k.nodelist.add(keep_node.get(a));
						if(a<keep_link.size()) {
							k.linklist.add(keep_link.get(a));
						}
					}
				}
			}
		}
		System.out.println(k.nodelist);
		System.out.println(k.linklist);
		*/
		/*
		String nodepath = "./src/simulation/map/test2";
    	String linkpath = "./src/simulation/map/test2_link";
    	String nodeH_path = "./src/simulation/map/test2_H";
		
    	String name = "result_test3";
    	MakeMap test = new MakeMap(nodepath,linkpath,nodeH_path,name);
		test.make();
		*/
		//RandomMake make = new RandomMake();
		/*碁盤の目状のマップを作成する
		 * @param filename:出力するファイル名
		 * @param x,y:マップのサイズ
		 * @param se_x,se_y:ノードの間隔
		 */
    	//make.intersection("intersection44",80,80,4,4);
    	//make.intersection("intersection55",80,80,5,5);
    	
    	/*マップに海を追加する
    	 *@param filename:読み込むファイル名
    	 *@param y:追加するy座標
    	 */
    	//make.add_sea("demomap",70);
		
		/*碁盤の目状のリンクを作成する
		 * @param filename:読み込むファイル名
		 */
		//make.intersection_link("intersection55");
		
    	//make.random("random",80,80,100,200);
    	
    	//make.random_H("random","random");
    	
    	//make.ws_link("random",4,0);
	}
}
