package simulation;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.LogNormalDistribution;

public class Gaussian {
	//リストに基づいてデータを表示
	private void printResults(List<Double> results) {
		for(int i=-10;i<20;i++) {
			int start = i*1;
			int end = (i+1)*1;
			double ratio = results.stream().filter(val -> (val >= start && val < end)).count() / 10000d *100;
			System.out.println(start + "〜" + end +": " +IntStream.rangeClosed(1, (int) ratio).mapToObj(val -> "■").collect(Collectors.joining("")));
		}
		double ratio = results.stream().filter(val -> (val >=10)).count();
		double ratio2 = results.stream().filter(val -> (val <10)).count();
		System.out.println((ratio/ratio2)*100);
	}
	
	//正規分布を表示
	public void nextGaussianTest() {
	Random random = new Random();
	List<Double> results = IntStream.rangeClosed(1,100000).boxed().
			map(i -> random.nextGaussian() * 5.0 + 5).collect(Collectors.toList());//1:大きいほど右にずれる2:大きいほどグラフが横に広がる
	printResults(results);
	}
		
	//対数正規分布を表示
	public void logNormalDistributionTest() {
		double MU = 1.4; // ln(x)の平均μ 大きいほどグラフの右側が伸びる
		double SIGMA = 0.8; // ln(x)の標準偏差σ 大きいほどグラフが横に広がる
	    LogNormalDistribution distribution = new LogNormalDistribution(MU, SIGMA);

	    List<Double> results = IntStream.rangeClosed(1, 100000).boxed()
	    		.map(i -> distribution.sample()).collect(Collectors.toList());    
	    printResults(results);    
	}
	
	public double lognormal(double mu, double sigma) {
		LogNormalDistribution distribution = new LogNormalDistribution(mu,sigma);
		return distribution.sample();	
	}
}
