package com.tea.data;

import java.io.File;
import java.util.ArrayList;

import com.tea.analyzer.HtmlAnalyzer;
import com.tea.fileUtils.FileUtils;

public class KeywordExtractor {
	final int interval = 50;
	
	String folderPath = "data/interval_keywords";
	
	class Result {
		String filename;
		boolean isHtml;
		String targetStr;
	}
	
	private void analyser() {
		File folder = new File(folderPath);
		if (folder.isDirectory()) {
			ArrayList<String> content;
			for (File file : folder.listFiles()) {
				content = FileUtils.readFileLinesToArrayList(file);
				boolean isHtml = isHtmlText(content);
				if (isHtml) {
					HtmlAnalyzer.getHtmlText(file);
					return;
				}
			}
		}
	}
	
	private boolean isHtmlText(ArrayList<String> content) {
		for (int i = 0; i < content.size(); i++) {
			if (content.get(i).trim().toLowerCase().startsWith("<body")) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		KeywordExtractor ke = new KeywordExtractor();
		ke.analyser();
	}
}
