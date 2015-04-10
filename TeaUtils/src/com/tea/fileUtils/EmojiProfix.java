package com.tea.fileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class EmojiProfix {
	static String douEmoji = "u1f004,u1f17f,u1f21a,u1f22f,u203c,u2049,u2139,u2194,u2195,u2196,u2197,u2198,u2199,u21a9,u21aa,u231a,u231b,u24c2,u25ab,u25b6,u25c0,u25fb,u25fc,u25fd,u25fe,u2600,u2601,u260e,u2611,u2614,u2615,u261d,u263a,u2648,u2649,u264a,u264b,u264c,u264d,u264e,u264f,u2650,u2651,u2652,u2653,u2660,u2663,u2665,u2666,u2668,u267b,u267f,u2693,u26a0,u26a1,u26aa,u26ab,u26bd,u26be,u26c4,u26c5,u26d4,u26ea,u26f2,u26f3,u26f5,u26fa,u26fd,u2702,u2708,u2709,u270c,u270f,u2712,u2714,u2716,u2733,u2734,u2744,u2747,u2757,u2764,u27a1,u2934,u2935,u2b05,u2b06,u2b07,u2b1b,u2b1c,u2b50,u2b55,u303d,u3297,u3299";
	static String triEmoji = "u0023_fe0f_20e3,u0030_fe0f_20e3,u0031_fe0f_20e3,u0032_fe0f_20e3,u0033_fe0f_20e3,u0034_fe0f_20e3,u0035_fe0f_20e3,u0036_fe0f_20e3,u0037_fe0f_20e3,u0038_fe0f_20e3,u0039_fe0f_20e3";
	static String filePath = "/home/tea/workspace/568/ime-emoji_layout/TouchPal/assets/emoji_selected.lua.png";
	
	static ArrayList<String> mdouEmojiList;
	static String crlf = System.getProperty("line.separator");
	
	public static void init() {
		mdouEmojiList = new ArrayList<String>();
		String[] tempArray = douEmoji.split(",");
		for (String emoji : tempArray) {
			mdouEmojiList.add(emoji);
		}
		
	}
	
	public static String getFileContent(String filePath) {
		File targetFile = new File(filePath);
		String content = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(targetFile));
			String tempStr = "";
			while ((tempStr=reader.readLine()) != null) {
				content += tempStr;
				content += crlf;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content; 
	}
	
	public static String doReplace() {
		init();
		String content = getFileContent(filePath);
		for (String rep : mdouEmojiList) {
			String realRep = rep.trim();
			content = content.replaceAll(realRep, realRep+"_fe0f");
		}
		content = content.replaceAll("_fe0f_fe0f", "_fe0f");
		System.out.println(content);
		return content;
	}
	
	public static void main(String[] args) {
		doReplace();
	}
}
