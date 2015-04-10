package com.tea.fileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class EmojiArtList {
	String folder = "/home/tea/workspace/570/trunk-ime/TouchPal/assets/";
	String[] files = {"emoji_art_fun","emoji_art_greeting","emoji_art_holiday","emoji_art_life","emoji_art_love","emoji_art_school"};
	ArrayList<String> artList = new ArrayList<String>();
	static int id = 0;
	
	public void readEmojiArt() {
		for (String file : files) {
			String absPath = folder + file + ".txt";
			ArrayList<String> arts = readFile(absPath);
			for (int i = 0; i < arts.size(); i++) {
				artList.add("art" + id + " = \"" + arts.get(i) + "\"");
				id++;
			}
		}
	}
	
	private ArrayList<String> readFile(String absPath) {
		ArrayList<String>  iconString = new ArrayList<String>();
		File file = new File(absPath);
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new FileReader(file));
			StringBuilder builder = new StringBuilder();
			String tempString;
			while (null != (tempString = bufferedReader.readLine())) {
				if (tempString.contains("-----")) {
					iconString.add(new String(builder.delete(
							builder.length() - 1, builder.length())));
					builder.delete(0, builder.length());
				} else {
					builder.append(tempString).append("\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return iconString;
	}

	public static void main(String[] args) {
		EmojiArtList art = new EmojiArtList();
		art.readEmojiArt();
		for (String emoji : art.artList) {
			System.out.println(emoji);
		}
	}
}
