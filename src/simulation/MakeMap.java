package simulation;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MakeMap implements Cloneable{
	String nodepath;
	String linkpath;
	String nodeh_path;
	String mapname;
	int [][] tmp_map_xy;
	double [][] tmp_map_z;
	static int xsize,ysize;
	static ArrayList<SimpleNode> riverlist = new ArrayList<SimpleNode>();
	static ArrayList<Node> nodelist = new ArrayList<Node>();
	static ArrayList<Node> nodelist2 = new ArrayList<Node>();
	static ArrayList<Link> linklist = new ArrayList<Link>();

	MakeMap(String node,String link,String height,String mapname){
		this.nodepath=node;
		this.linkpath=link;
		this.nodeh_path=height;
		this.mapname=mapname;
	}
	
	public void make_nodelist() throws IOException {
		try {
			File file = new File(nodepath);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
			
			String line;
			String data="";
			int number=0;//ノード番号
			int number2=118;//追加用
			
			int y=0;
			while((line=br.readLine()) != null){
				int x = 0;
				while(x<line.length()) {
					data=line.substring(x,x+1);
					if(Integer.parseInt(data)==5) {
						nodelist.add(new Node(number,x,y));
						number++;
					}
					if(Integer.parseInt(data)==8) {
						riverlist.add(new SimpleNode(0,x,y));
					}
					if(Integer.parseInt(data)==6) {
						nodelist2.add(new Node(number2,x,y));
						number2++;
					}
					x++;
				}xsize=x;
				y++;
			}
			ysize=y;
			Core.world_x=xsize;
			Core.world_y=ysize;
			//補充
			for(Node node:nodelist2) {
				nodelist.add(node);
			}
			br.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		//ノードに標高の情報を付与
		try {
			File file2 = new File(nodeh_path);
			FileReader filereader2 = new FileReader(file2);
			BufferedReader br2 = new BufferedReader(filereader2);
			
			String line2;
			String data2="";
			int number2=0;
			//System.out.println(nodelist.size());
			
			while((line2=br2.readLine()) != null) {
				//data2=line2.substring(4,8);
				data2=line2.substring(4,7);
				double d =Double.parseDouble(data2);
				(nodelist.get(number2)).height=d;
				number2++;
			}
			br2.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void make_linklist() throws IOException{
		try {
			File resultDir = new File("./src/simulation/makedfile");
			if(!resultDir.exists()) {
				resultDir.mkdir();
			}
			
			File file = new File(linkpath);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
			
			String line;
			int link_number;
			int node1_No,node2_No;
			int type_No;
			int valid;
			
			br.readLine();//一行読み込んでおく
			
			while((line = br.readLine()) != null){
				valid = Integer.parseInt((line.substring(21,22)).trim());
				
				if(valid == 1) {
					link_number = Integer.parseInt((line.substring(0,3)).trim());
					node1_No    = Integer.parseInt((line.substring(4,7)).trim());
					node2_No    = Integer.parseInt((line.substring(10,13)).trim());
					type_No     = Integer.parseInt((line.substring(16,17)).trim());
					
					Node node1 = nodelist.get(node1_No);
					Node node2 = nodelist.get(node2_No);
					Link link  = new Link(link_number, node1, node2, type_No);
					node1.addlink(link);
					node2.addlink(link);
					linklist.add(link);
				}
				else {
					linklist.add(null);
				}
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//ノードとリンク情報からマップ作成,ノードとノードを直線で結ぶ
	public void connect_node_to_node() throws IOException{
		try {
			File resultDir = new File("./src/simulation/makedfile");
			if(!resultDir.exists()) {
				resultDir.mkdir();
			}
			
			String fileName ="./src/simulation/makedfile/"+mapname;
			File outputFile = new File(fileName);
			if(outputFile.createNewFile()) {
				//System.out.println("ファイル作成");
			}
			
			String fileName2 ="./src/simulation/makedfile/"+mapname+"_H";
			File outputFile2 = new File(fileName2);
			if(outputFile2.createNewFile()) {
				//System.out.println("ファイル作成");
			}
			
			tmp_map_xy = new int[ysize][xsize];
			tmp_map_z  = new double[ysize][xsize];
			
			for(int y=0;y<ysize;y++) {
				for(int x=0;x<xsize;x++) {
					tmp_map_z[y][x]=0.00;
				}
			}
			
			for(Link alink : linklist) {
				if(alink == null) {
					continue;
				}
				
				int inc_x = (alink.x1 - alink.x2);//xの増加量
				int inc_y = (alink.y1 - alink.y2);//yの増加量
				double inc_h = (alink.node1.height - alink.node2.height);//zの増加量
				
				double point = alink.h1;//基準点
				
				double init  = 0;
				
				int type = alink.type_slope;
				int xsign = 0;
				int ysign = 0;
				double hsign = 0;
				
				if(inc_x<0 && inc_y>0) {
					xsign=1;
					ysign=-1;
				}else if(inc_x>0 && inc_y>0) {
					xsign=-1;
					ysign=-1;
				}else if(inc_x>0 && inc_y<0) {
					xsign=-1;
					ysign=1;
				}else if(inc_x<0 && inc_y<0) {
					xsign=1;
					ysign=1;
				}
				
				if(type == 1) {
					//1移動の変動量
					init = Math.abs(inc_h)/Math.max(Math.abs(inc_x),Math.abs(inc_y));
					if(inc_h<0) {
						hsign=1;//増加方向
					}
					else {
						hsign=-1;//減少方向
					}
				}
				
				//System.out.println("リンク番号は"+alink.number);
				//System.out.println(point);

				if(xsign!=0) {
					int slope = Math.abs(inc_y/inc_x);//傾き(切り捨て)
					int rest = Math.abs(inc_y)%Math.abs(inc_x);//余り
					int x=0;//xの総変動
					int y=0;//yの総変動
					int all_count=1;//全体の処理回数
					ArrayList<Integer> insert_list= new ArrayList<>();
					tmp_map_xy[alink.y1][alink.x1] = 1;
					tmp_map_z[alink.y1][alink.x1]  = point;

					// ①傾きが1の場合
					if(slope==1) {
						//slope×inc_x回斜め移動
						int naname=slope*Math.abs(inc_x);
						//rest回縦移動
						int sift=rest;
						int all=naname+sift;
						//少ない方
						int less = naname < sift ? naname : sift;
						Random random = new Random();
						int candidate;
						while(insert_list.size()!=less) {
							candidate =random.nextInt(all)+1;
							if(!(insert_list.contains(candidate))) {
								insert_list.add(candidate);
							}
						}
						Collections.sort(insert_list);
						
						while((all_count-1)!=all) {
							if(less==naname) {
								//斜め移動
								if(insert_list.size()!=0&&all_count==insert_list.get(0)) {
									insert_list.remove(0);
									x++;
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(xsign,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
									
								}
								//縦移動
								else {
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(0,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
									
								}
								all_count++;
							}else {
								//縦移動
								if(insert_list.size()!=0&&all_count==insert_list.get(0)) {
									insert_list.remove(0);
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(0,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
									
								}
								//斜め移動
								else {
									x++;
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(xsign,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
									
								}
								all_count++;
							}
						}
						
					//② 傾きが1より大きい場合
					}else if(slope>1) {
						int naname = Math.abs(inc_x);//inc_x回斜め移動
						int sift   = Math.abs(inc_y)-Math.abs(inc_x);//inc_y-inc_x回縦移動
						int all    = naname + sift;
						int less   = naname < sift ? naname:sift;//少ない方
						Random random = new Random();
						int candidate;
						while(insert_list.size()!=less) {
							candidate =random.nextInt(all)+1;
							if(!(insert_list.contains(candidate))) {
								insert_list.add(candidate);
							}
						}
						Collections.sort(insert_list);
						while((all_count-1)!=all) {
							if(less==naname) {
								//斜め移動
								if(insert_list.size()!=0&&all_count==insert_list.get(0)) {
									insert_list.remove(0);
									x++;
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(xsign,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
								}
								//縦移動
								else {
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(0,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
								}
								all_count++;
							}else {
								//縦移動
								if(insert_list.size()!=0&&all_count==insert_list.get(0)) {
									insert_list.remove(0);
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(0,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
								}
								//斜め移動
								else {
									x++;
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(xsign,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
								}
								all_count++;
							}
						}
					}
					//③ 傾きが1未満の場合
					else {
						//rest回斜め移動
						int naname=rest;
						//inc_x-rest回横移動
						int sift=Math.abs(inc_x)-rest;
						int all=naname+sift;
						//少ない方
						int less = naname < sift ? naname:sift;
						Random random = new Random();
						int candidate;
						while(insert_list.size()!=less) {
							candidate =random.nextInt(all)+1;
							if(!(insert_list.contains(candidate))) {
								insert_list.add(candidate);
							}
						}
						Collections.sort(insert_list);
						while((all_count-1)!=all) {
							if(less==naname) {
								//斜め移動
								if(insert_list.size()!=0&&all_count==insert_list.get(0)) {
									insert_list.remove(0);
									x++;
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(xsign,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
								}
								//横移動
								else {
									x++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(xsign,0);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
								}
								all_count++;
							}else {
								//横移動
								if(insert_list.size()!=0&&all_count==insert_list.get(0)) {
									insert_list.remove(0);
									x++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(xsign,0);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
								}
								//斜め移動
								else {
									x++;
									y++;
									tmp_map_xy[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=1;
									alink.move(xsign,ysign);
									if(type==1) {
										tmp_map_z[alink.y1+y*(ysign)][alink.x1+x*(xsign)]=((double)Math.round((point+init*all_count*hsign)*100))/100;
									}
									
								}
								all_count++;
							}
						}
					}
				}
						
				//iがall_countの代替
				if(inc_x<0 && inc_y==0) {
					for(int i=0; i <= -inc_x;i++) {
						tmp_map_xy[alink.y1][alink.x1+i]=1;
						alink.move(1,0);
						if(type==1) {
							tmp_map_z[alink.y1][alink.x1+i]=((double)Math.round((point+init*i*hsign)*100))/100;
						}
					}alink.movement.remove(0);
				}
				
				if(inc_x==0 && inc_y>0) {
					for(int i=0;i<=inc_y;i++) {
						tmp_map_xy[alink.y1-i][alink.x1]=1;
						alink.move(0,-1);
						if(type==1) {
							tmp_map_z[alink.y1-i][alink.x1]=((double)Math.round((point+init*i*hsign)*100))/100;
						}
					}alink.movement.remove(0);
				}
				
				if(inc_x>0 && inc_y==0) {
					for(int i=0;i<=inc_x;i++) {
						tmp_map_xy[alink.y1][alink.x1-i]=1;
						alink.move(-1,0);
						if(type==1) {
							tmp_map_z[alink.y1][alink.x1-i]=((double)Math.round((point+init*i*hsign)*100))/100;
						}
					}alink.movement.remove(0);
				}
				
				if(inc_x==0 && inc_y<0) {
					for(int i=0;i<=-inc_y;i++) {
						tmp_map_xy[alink.y1+i][alink.x1]=1;
						alink.move(0,1);
						if(type==1) {
							tmp_map_z[alink.y1+i][alink.x1]=((double)Math.round((point+init*i*hsign)*100))/100;
						}
					}alink.movement.remove(0);
				}
				alink.set_route();
			}
			
			/* 海の部分を読み込む
			 * HACK:関数として独立すべき
			 */
			File file = new File(nodepath);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
			
			String line;
			String data="";
			
			int y=0;
			while((line=br.readLine()) != null){
				int x = 0;
				while(x<line.length()) {
					data=line.substring(x,x+1);
					if(Integer.parseInt(data)==4) {//tsunami
						tmp_map_xy[y][x]=4;
					}
					if(Integer.parseInt(data)==8) {//river
						tmp_map_xy[y][x]=8;
					}
					x++;
				}
				y++;
			}
			br.close();
			
			//MAP書き込み
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			for(int t=0;t<ysize;t++) {
				for(int s=0;s<xsize;s++) {
					if(tmp_map_xy[t][s]==1) {
						pw.print(0);
					}
					else if(tmp_map_xy[t][s]==4 || tmp_map_xy[t][s]==3 || tmp_map_xy[t][s]==8) {
						pw.print(tmp_map_xy[t][s]);
					}
					else {
						pw.print(1);
					}
				}
				if(t!=ysize-1) {
					pw.println();
				}
			}
			pw.close();
			
			//MAP(H)ファイル書き込み
			PrintWriter pw2 = new PrintWriter(new FileWriter(outputFile2));
			for(int j=0;j<ysize;j++) {
				for(int i=0;i<xsize;i++) {
					
					if(tmp_map_z[j][i]>=10) {
						if(i==xsize-1) {
							pw2.print(String.format("%.1f",tmp_map_z[j][i]));
						}
						else {
							pw2.print(String.format("%.1f",tmp_map_z[j][i])+" ");
						}
						
					}
					else {
						if(i==xsize-1) {
							pw2.print(String.format("%.2f",tmp_map_z[j][i]));
						}
						else {
							pw2.print(String.format("%.2f",tmp_map_z[j][i])+" ");
						}
						
					}
				}
				pw2.println();
			}
			
			pw2.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void make_map() throws IOException{
		make_nodelist();
		make_linklist();
		connect_node_to_node();
	}
	
	public ArrayList<Node> return_nodelist (){
		return nodelist;
	}
	
	public ArrayList<Link> return_linklist (){
		return linklist;
	}
}

