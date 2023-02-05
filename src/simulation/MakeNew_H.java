package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MakeNew_H {
	String nodepath;
	String nodeh_path;
	static int xsize, ysize;

	static ArrayList<Node> nodelist = new ArrayList<Node>();

	MakeNew_H(String node, String height) {
		this.nodepath = node;
		this.nodeh_path = height;
	}

	public void make() throws IOException {
		try {
			File file = new File(nodepath);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);

			String line;
			String data = "";
			int number = 0;//ノード番号

			int y = 0;
			while ((line = br.readLine()) != null) {
				int x = 0;
				while (x < line.length()) {
					data = line.substring(x, x + 1);
					if (Integer.parseInt(data) == 5) {
						nodelist.add(new Node(number, x, y));
						number++;
					}
					x++;
				}
				xsize = x;
				y++;
			}
			ysize = y;
			Core.world_x = xsize;
			Core.world_y = ysize;
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//ノードリスト作成後,ファイル作成

		ArrayList<Double> height_array = new ArrayList<>();
		try {
			File file2 = new File(nodeh_path);
			FileReader filereader2 = new FileReader(file2);
			BufferedReader br2 = new BufferedReader(filereader2);

			String line2;
			String data2 = "";
			int number2 = 0;
			double d;
			double change = 0;

			while ((line2 = br2.readLine()) != null) {
				data2 = line2.substring(4, 7);
				d = Double.parseDouble(data2);
				change = (Math.abs((nodelist.get(number2)).x - 50));
				System.out.println(change);
				double d2= change/50;
				System.out.println(d2);
				if (change == 0) {
					change = 1;
				}
				//System.out.println(1 / 2 * (change / 50 + 1));
				d = d * 1 / 2 * (change / 50 + 1);
				d = Math.round(d*10.0)/10.0;
				height_array.add(d);
				number2++;
			}
			br2.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		//ファイル作成部
		String filename = "./src/simulation/demomap/demomap2_H_new.txt";
		try {
			File outputFile = new File(filename);
			if (outputFile.createNewFile()) {
			}
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			int number = 0;
			for (Double elem : height_array) {
				pw.print(String.format("%-4d", number));
				pw.println(elem);
				number++;
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		MakeNew_H test = new MakeNew_H("src/simulation/demomap/demomap2_v3.txt","src/simulation/demomap/demomap2_h.txt");
		test.make();
	}
}
