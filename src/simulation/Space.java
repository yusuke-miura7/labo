package simulation;

import java.io.*;

public class Space {
    Position[][] space;
    Double[][] elevation;
    Position maxpheromone;
    Double MMAS_minPheromone;
    Double MMAS_maxPheromone;
    Double constPheromone;
    long time;
    long lasttime;
    long goaledtime;
    boolean canwalkin;

    //コンストラクタ
    public Space(int x, int y, String filepath, double MMAS_minPheromone, double MMAS_maxPheromone, double constPheromone) {
        this.MMAS_minPheromone=MMAS_minPheromone;
        this.MMAS_maxPheromone=MMAS_maxPheromone;
    	this.constPheromone = constPheromone;
        this.canwalkin = true;
        
        //フィールド生成
        space=new Position[y][x];
        elevation = new Double[y][x];
        
        //配列に高さ情報を格納しておく
        int index;
        double[] tmp = new double[x*y];
        //System.out.println(x*y);
        
        //ファイル読み込み
        try {
        	File file = new File(filepath);
        	FileReader filereader = new FileReader(file);
        	BufferedReader br = new BufferedReader(filereader);
        	
        	String text,data;
        	index=0;
        	
        	//1行ずつ読み込む
        	while((text = br.readLine()) != null ) {
        		int i = 0;
        		while(i<text.length()) {
        			data=text.substring(i,i+4);
        			i+=5;
        			tmp[index]=Double.parseDouble(data);//
        			index++;
        		}
        	}
        	br.close();
        }catch(IOException e) {
        	e.printStackTrace();
        }
        
        index=0;
        double z;
        
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                z=tmp[j*Core.world_x+i];
                //elevation[j][i] = z;
            	space[j][i] = new Position(i, j, z, this);
            	
            	index++;
            }
        }
        maxpheromone = space[0][0];
        
    }
    
    //位置情報を返す
    public Position position(int x, int y) {
    	//return space[y][x];
    	
    	//満たすような設定をするため,省略
        if ((x >= 0) && (y >= 0) && (x < Core.world_x) && (y < Core.world_y)) {
        	return space[y][x];
        }
        else
            return null;
        
    }
    
    public Double getElevation(int x, int y) {
    	System.out.println("get:"+x+","+y);
    	if ((x >= 0) && (y >= 0) && (x < space.length) && (y < space.length)) {
    		return elevation[y][x];
    	}
        else
            return null;
    }
    
    public double average(int x,int y) {
    	double total = getElevation(x-1,y-1);
    	total+=getElevation(x,y-1);
    	total+=getElevation(x+1,y-1);
    	total+=getElevation(x-1,y);
    	total+=getElevation(x,y);
    	total+=getElevation(x+1,y);
    	total+=getElevation(x-1,y+1);
    	total+=getElevation(x,y+1);
    	total+=getElevation(x+1,y+1);
    	return (double)total/9;
    }
    
    public String toString() {
        String tmp = "";
        for (Position[] aSpace : space) {
            for (int x = 0; x < space[0].length; x++)
                tmp += aSpace[x].toString() + ',';
            tmp += "\n";
        }
        return tmp;
    }
}
