package simulation;

import java.util.HashMap;
import java.util.Map;

public class NumberCombinationWithValue {
	Map<String,Integer> combinationMap = new HashMap<>();
	
	//指定されたキーと値を対応付ける
	public void addCombination(double a,double b, double c,int value) {
		String combination = a + "-" + b + "-" + c;
		combinationMap.put(combination,value);
	}
	
	//指定されたキーがマップされている値を返す
	public Integer getValue(double a,double b,double c) {
		String combination = a + "-" + b + "-" + c;
		//マッピングされていなかった場合nullを返す
		return combinationMap.get(combination);
	}

}
