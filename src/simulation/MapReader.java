package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by hiroq7 on 15/03/26.
 */
public class MapReader {

    String filepath;
    Position[] wall;
    Position[] building;
    Position[] safe;
    Position[] danger;
    Position[] fire;
    
    ArrayList<ArrayList<Position>> map;
    ArrayList<Position> wall_array;
    ArrayList<Position> building_array;
    ArrayList<Position> safe_array;
    ArrayList<Position> tsunami_array;
    ArrayList<Position> river_array;
    
    //コンストラクタ
    MapReader(String file) {
        this.filepath = file;
    }
    
    public ArrayList<ArrayList<Position>> reader(Space space){
    	map            = new ArrayList<ArrayList<Position>>();
    	wall_array     = new ArrayList<Position>();
    	building_array = new ArrayList<Position>();
        safe_array     = new ArrayList<Position>();
    	tsunami_array  = new ArrayList<Position>();
    	river_array    = new ArrayList<Position>();
    	
    	try {
    		File file = new File(filepath);
    		FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);
            
            String line = "";
            String data = "";
            
            int y=0;
            while((line = br.readLine()) != null) {
            	int x =0;
            	while(x<line.length()) {
            		data=line.substring(x,x+1);
            		switch (data) {
            		case "1":
            			wall_array.add(space.position(x, y));
            			break;
            		case "2":
            			building_array.add(space.position(x, y));
            			break;
            		case "3":
            			safe_array.add(space.position(x, y));
            			break;
            		case "4":
            			tsunami_array.add(space.position(x, y));
            			break;
            		case "8":
            			river_array.add(space.position(x, y));
            			break;
            		default:
            			break;
            		}
            		x++;
            	}
            	y++;
            	Core.xSquareSize=(int)(x/10)+1;
            }
            Core.ySquareSize=(int)(y/10)+1;
            map.add(wall_array);
            map.add(building_array);
            map.add(safe_array);
            map.add(tsunami_array);
            map.add(river_array);
            br.close();
    	}catch(IOException e) {
    		e.printStackTrace();
    	}
    	return map;
    }
    
    /*
    public ArrayList<Position> expandwater(Space space,ArrayList<Position> array){
    	danger_array = new ArrayList<Position>();
    	int size = array.size();
    	for(int i=0;i<size;i++) {
    		danger_array.add(array.get(i));
    	}
    	for(int j=0;j<2;j++) {
    		danger_array.add(array.get(j).upnum(1));
    	}
    	return danger_array;
    }*/
    
    //マップのオブジェクトの個数を取得する 0:道 1:壁 2:建物 3:避難場所 4:危険区域
    /*
    public int[] reader() {
        int[] counter = new int[5];
        try {
            File file = new File(filepath);
            FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);

            String line = "";
            String data = "";
            int wallcnt = 0;
            int buildingcnt =0;
            int safecnt = 0;
            int dangercnt = 0;
            
            boolean check=true;//列数保存用フラグ

            int y = 0;
            while ((line = br.readLine()) != null) {
                int x = 0;
                while (x < line.length()) {
                    data = line.substring(x, x + 1);
                    switch (data) {
                        case "4":
                            dangercnt++;
                            break;
                        case "3":
                            safecnt++;
                            break;
                        case "2":
                            buildingcnt++;
                            break;
                        case "1":
                            wallcnt++;
                            break;
                        default:
                            break;
                    }
                    x++;
                }
                y++;
                if(check) {
                	Core.world_x=x;
                	System.out.println("X:"+Core.world_x);
                	Core.xSquareSize=(int)(x/10)+1;
                	check=false;
                }
            }
            
            Core.world_y=y;
            System.out.println("Y:"+Core.world_y);
            Core.ySquareSize=(int)(y/10)+1;
            counter[0] = 0;
            counter[1] = wallcnt;
            counter[2] = buildingcnt;
            counter[3] = safecnt;
            counter[4] = dangercnt;
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return counter;
    }*/
    
    //要素が壁の位置情報である配列を作る
    /*
    public Position[] createWall(Space space, int index) {
        wall = new Position[index];
        try {
            File file = new File(filepath);
            FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);

            String line = "";
            String data = "";
            int wallcnt = 0;
            int y = 0;
            while ((line = br.readLine()) != null) {
                int x = 0;
                while (x < line.length()) {
                    data = line.substring(x, x + 1);
                    switch (data) {
                        case "1":
                            wall[wallcnt] = space.position(x, y);
                            wallcnt++;
                            break;
                        default:
                            break;
                    }
                    x++;
                }
                y++;

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wall;
    }
    
    //要素が建物の位置情報である配列を作る
    public Position[] createBuilding(Space space, int index) {
        building = new Position[index];
        try {
            File file = new File(filepath);
            FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);

            String line = "";
            String data = "";
            int buildingcnt = 0;
            int y = 0;
            while ((line = br.readLine()) != null) {
                int x = 0;
                while (x < line.length()) {
                    data = line.substring(x, x + 1);
                    switch (data) {
                        case "2":
                            building[buildingcnt] = space.position(x, y);
                            buildingcnt++;
                            break;
                        default:
                            break;
                    }
                    x++;
                }
                y++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return building;
    }
    
    //要素が避難場所の位置情報である配列を作る
    public Position[] createSafe(Space space, int index){
        safe = new Position[index];
        try {
            File file = new File(filepath);
            FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);

            String line = "";
            String data = "";
            int safecnt = 0;
            int y = 0;
            while ((line = br.readLine()) != null) {
                int x = 0;
                while (x < line.length()) {
                    data = line.substring(x, x + 1);
                    switch (data) {
                        case "3":
                            safe[safecnt] = space.position(x, y);
                            safecnt++;
                            break;
                        default:
                            break;
                    }
                    x++;
                }
                y++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return safe;
    }
    
    //要素が危険場所の位置情報である配列を作る
    public Position[] createDanger(Space space, int index) {
        danger = new Position[index];
        try {
            File file = new File(filepath);
            FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);

            String line = "";
            String data = "";
            int dangercnt = 0;
            int y = 0;
            while ((line = br.readLine()) != null) {
                int x = 0;
                while (x < line.length()) {
                    data = line.substring(x, x + 1);
                    switch (data) {
                        case "4":
                            danger[dangercnt] = space.position(x, y);
                            dangercnt++;
                            break;
                        default:
                            break;
                    }
                    x++;
                }
                y++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return danger;
    }
    
    //要素が危険場所の位置情報であるリストを作る
    public ArrayList<Position> createDanger2(Space space){
    	ArrayList<Position> danger_array = new ArrayList<Position>();
    	try {
    		File file = new File(filepath);
    		FileReader filereader = new FileReader(file);
    		BufferedReader br = new BufferedReader(filereader);
    		String line = "";
    		String data = "";
    		int y=0;
    		while((line=br.readLine())!=null){
    			int x=0;
    			while(x<line.length()) {
    				data=line.substring(x,x+1);
    				switch(data) {
    				case "4":
    					danger_array.add(space.position(x, y));
    					break;
    				default:
    					break;
    				}
    				x++;
    			}
    			y++;
    		}
    		br.close();
    	}catch (IOException e) {
    		e.printStackTrace();
    	}
    	return danger_array;
    }
    
    //火拡大
    public Position[] expandfire(Space space, Position[] oldArea, int index, int trial){
        fire = new Position[index];
        switch (trial){
            case 2:
                for(int i = 0,cnt = 0; i< oldArea.length; i++){
                    for(int j=0;j<trial;j++) {
                        for (int h = 0; h<trial;h++) {
                            fire[cnt] = space.position(oldArea[i].x + h, oldArea[i].y + j);
                            cnt++;
                        }
                    }
                }
                break;
            case 3:
                for(int i = 0,cnt = 0; i< oldArea.length; i++){
                    for(int j=0;j<trial;j++) {
                        for (int h = 0; h<trial;h++) {
                            fire[cnt] = space.position(oldArea[i].x - 1+ h, oldArea[i].y - 1 + j);
                            cnt++;
                        }
                    }
                }
                break;
            case 4:
                for(int i = 0,cnt = 0; i< oldArea.length; i++){
                    for(int j=0;j<trial;j++) {
                        for (int h = 0; h<trial;h++) {
                            fire[cnt] = space.position(oldArea[i].x - 1 + h, oldArea[i].y - 1 + j);
                            cnt++;
                        }
                    }
                }
                break;
            case 5:
                for(int i = 0,cnt = 0; i< oldArea.length; i++){
                    for(int j=0;j<trial;j++) {
                        for (int h = 0; h<trial;h++) {
                            fire[cnt] = space.position(oldArea[i].x - 2 + h, oldArea[i].y - 2 + j);
                            cnt++;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return fire;
    }
    */
}

