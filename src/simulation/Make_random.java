package simulation;

import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class Make_random {
	
	public void make(int node_num) {
		
		//標高作成部
		ArrayList<Double> height_array = new ArrayList<>();
		Random random = new Random();
		double height;
		double b;
		
		while(height_array.size()!=node_num) {
			height=random.nextDouble()*10;
			b = ((double)Math.round(height*10))/10;
			height_array.add(b);
		}
		
		Collections.sort(height_array,Collections.reverseOrder());
		
		//ファイル作成部
		String filename = "./src/simulation/demomap/demomap2_H.txt";
		try {
			File outputFile = new File(filename);
			if(outputFile.createNewFile()) {
			}
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			int number=0;
			for(Double elem : height_array) {
				pw.print(String.format("%-4d", number));
				pw.println(elem);
				number++;
			}
			pw.close();
		}
		
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Make_random test = new Make_random();
		test.make(135);
		System.out.println("maked!");
	}
}
