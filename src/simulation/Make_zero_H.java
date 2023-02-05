package simulation;

import java.io.*;

public class Make_zero_H {
	public static void main(String[] args) {
		
		int line_num = Integer.parseInt(args[0]);
		String name = args[0];
		
		File resultDir = new File("./src/simulation/map2");
		
		if(!resultDir.exists()) {
			System.out.println("ディレクトリを作成しました");
			resultDir.mkdir();
		}
		
		String fileName = name + "zero_H.txt";
		System.out.println(fileName);
		
		File outputFile = new File("./src/simulation/map2/" + fileName);
		
		FileWriter filewriter = null;
		
		try {
			filewriter = new FileWriter(outputFile);
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		BufferedWriter bw = new BufferedWriter(filewriter);
		PrintWriter pw = new PrintWriter(bw);
		for(int i=0; i<line_num;i++) {
			pw.print(String.format("%-4d",i));
			pw.println(String.format("%.2f",0.00));
		}
		pw.close();
	}
}
