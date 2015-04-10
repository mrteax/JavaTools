package resource.index;

import java.io.IOException;
import java.text.NumberFormat;

import jeasy.analysis.MMAnalyzer;

public class Test {

	public static void main(String[] args) {
		/*String text = "据路透社报道，印度尼西亚社会事务部一官员星期二(29日)表示，"
				+ "日惹市附近当地时间27日晨5时53分发生的里氏6.2级地震已经造成至少5427人死亡，"
				+ "20000余人受伤，近20万人无家可归。";

		MMAnalyzer analyzer = new MMAnalyzer();
		try {
			System.out.println(analyzer.segment(text, " | "));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		NumberFormat format = NumberFormat.getIntegerInstance();   
//		设置数字的位数 由实际情况的最大数字决定   
		format.setMinimumIntegerDigits(10);   
//		是否按每三位隔开,如:1234567 将被格式化为 1,234,567。在这里选择 否   
		format.setGroupingUsed(false);   
		
		System.out.println(format.format(222));
		System.out.println(Integer.parseInt(format.format(222)));
	}
}
