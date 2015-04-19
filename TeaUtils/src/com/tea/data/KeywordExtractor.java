package com.tea.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.tea.analyzer.HtmlAnalyzer;
import com.tea.fileUtils.FileUtils;

public class KeywordExtractor {
	private final static int MAX_RESULTS_PER_FILE = 5000;
	final int interval = 50;
	
	String folderPath = "data/interval_keywords";
	String rulePath = "data/internalKeywords/rule/rule.csv";
	String resPath = "data/keywords_result/results";
	private String mKeyword1 = "pre tax";
	private String mKeyword2 = "income";
	private String mFilename;
	private boolean mIsHtml;
	private String mText;
	
	private ArrayList<Result> resultsArray = new ArrayList<KeywordExtractor.Result>();
	private ArrayList<Integer> keywordOneIndexes = new ArrayList<Integer>();
	private ArrayList<Integer> keywordTwoIndexes = new ArrayList<Integer>();
 	private int resultIndex = 0;
	
	
	class Result {
		private final String SEPERATOR = ",";
		public String filename;
		public String keyword1;
		public String keyword2;
		public int interval;
		public boolean isHtml;
		public String targetStr;
		public Result(String fileName, boolean isHtml, String targetStr, String keyword1, String keyword2, int interval) {
			this.filename = fileName;
			this.isHtml = isHtml;
			this.targetStr = targetStr;
			this.keyword1 = keyword1;
			this.keyword2 = keyword2;
		}
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(filename);
			sb.append(SEPERATOR);
			sb.append(keyword1);
			sb.append(SEPERATOR);
			sb.append(keyword2);
			sb.append(SEPERATOR);
			sb.append(interval);
			sb.append(SEPERATOR);
			sb.append(isHtml);
			sb.append(SEPERATOR);
			sb.append(targetStr);
			sb.append("\n");
			
			return sb.toString();
		}
	}
	
	private void analyser() {
		File folder = new File(folderPath);
		if (folder.isDirectory()) {
			ArrayList<String> content;
			String htmlTxt = null;
			for (File file : folder.listFiles()) {
				mFilename = file.getName();
				content = FileUtils.readFileLinesToArrayList(file);
				boolean isHtml = isHtmlText(content);
				mIsHtml = isHtml;
				htmlTxt = HtmlAnalyzer.getHtmlText(file);
				processTxt(file, htmlTxt, isHtml);
			}
			writeBack();
		}
	}
	
	private void writeBack() {
		File dir = new File(resPath);
			try {
				if (!dir.exists()) {
					dir.mkdir();
				}
				File ret = new File(dir, "result.csv");
				if (!ret.exists()) {
					ret.createNewFile();
				}
				FileWriter fw = new FileWriter(ret);
				for (Result result : resultsArray) {
					fw.append(result.toString());
					fw.flush();
				}
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private void processTxt(File file, String text, boolean isHtml) {
		if (file == null || text == null) {
			return;
		}
		String fileName = file.getName();
		text = text.trim().replaceAll(" +", " ");
//		String[] sentences = text.split(".");
//		for (int i = 0; i < sentences.length; i++) {
//			String sen1 = sentences[i];
//			int index = sen1.indexOf(mKeyword1);
//			if (index >= 0) {
//				if (sen1.indexOf(mKeyword2) > index) {
//					resultIndex++;
//					
//				}
//			}
//		}
		mText = text;
		fillKeyworkOneList(keywordOneIndexes, mKeyword1, text, 0);
		fillKeyworkOneList(keywordTwoIndexes, mKeyword2, text, 0);
		
		compareList();
	}

	private void compareList() {
		for (int i = 0; i < keywordOneIndexes.size(); i++) {
			int one = keywordOneIndexes.get(i).intValue();
			for (int j = 0; j < keywordTwoIndexes.size(); j++) {
				int two = keywordTwoIndexes.get(j).intValue();
				if (one < two && (two - one + mKeyword1.length()) <= interval) {
					int firstPoint = mText.substring(0, one).lastIndexOf(".");
					int lastPoint = mText.substring(two).indexOf(".") + two;
					Result ret = new Result(mFilename, mIsHtml, mText.substring(firstPoint, lastPoint + 1), mKeyword1, mKeyword2, interval);
					resultsArray.add(ret);
					System.out.println(mFilename + " : " + mIsHtml + " : " + mKeyword1 + " : " + mKeyword2 + " : " + interval + " : " + mText.substring(firstPoint, lastPoint + 1));
					resultIndex++;
				}
			}
		}
	}

	private void fillKeyworkOneList(ArrayList<Integer> keywordIndexes, String mKeyword, String text, int offset) {
		int index = text.indexOf(mKeyword);
		if (index >= 0) {
			keywordIndexes.add(index + offset);
			offset = index + mKeyword.length();
			fillKeyworkOneList(keywordIndexes, mKeyword, text.substring(offset), offset);
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
