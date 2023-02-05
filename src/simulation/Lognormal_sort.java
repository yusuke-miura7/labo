package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

//対数正規分布に従う乱数を降順とした標高ファイルとして作成する
public class Lognormal_sort {
	public static void main(String[] args) {
		
		RandomMake make = new RandomMake();
		make.random_H("lognormal");
		String filepath = "./src/simulation/demomap2/lognormal_H";
		ArrayList<Double> height_array = new ArrayList<>();
		String line;
	    double height;
	    
		try {
			File file = new File(filepath);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
		
			while((line = br.readLine())!= null) {
				height = Double.parseDouble((line.substring(4,8)).trim());
				height_array.add(height);
			}
			br.close();
		}
		
		catch(IOException e) {
			e.printStackTrace();
		}
		
		Collections.sort(height_array,Collections.reverseOrder());
		System.out.println(height_array);
		
		try {
			String fileName2 = "./src/simulation/map2/sortH";
			File outputFile = new File(fileName2);
			if(outputFile.createNewFile()) {
			}
			
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			int number =0;
			
			for(Double elem : height_array) {
				pw.print(String.format("%-4d",number));
				pw.println(elem);
				number++;
			}
			
			pw.close();
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
