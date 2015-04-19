package com.tea.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import com.tea.analyzer.HtmlAnalyzer;
import com.tea.fileUtils.FileUtils;

public class KeywordExtractor {
	private final static int MAX_RESULTS_PER_FILE = 5000;
	
	String folderPath = "data/interval_keywords";
	
	private String mKeyword1 = "";
	private String mKeyword2 = "";
	private int interval = 0;
	private String mFilename;
	private boolean mIsHtml;
	private String mText;
	
//	private ArrayList<String> resultsSet = new ArrayList<String>();
	private ArrayList<Integer> keywordOneIndexes = new ArrayList<Integer>();
	private ArrayList<Integer> keywordTwoIndexes = new ArrayList<Integer>();
	private TreeSet<String> resultsSet = new TreeSet<String>();
 	private int resultIndex = 0;
	private String configPath;
	
	
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
			this.interval = interval;
		}
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(filename).append(SEPERATOR).append(keyword1).append(SEPERATOR).append(keyword2).append(SEPERATOR)
			.append(interval).append(SEPERATOR).append(isHtml).append(SEPERATOR).append("\"").append(targetStr)
			.append("\"").append("\n");
			
			return sb.toString();
		}
	}
	//FOLDER,KEYWORD1,KEYWORD2,INTERVAL
	public KeywordExtractor(String path) {
		configPath = path;
	}

	private void analyser() {
		if (configPath == null || configPath.length() == 0) {
			System.out.println("config file path error");
			return;
		}
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			System.out.println("config file path error");
			return;
		}
		ArrayList<String> config = FileUtils.readFileLinesToArrayList(configFile);
		//FOLDER,KEYWORD1,KEYWORD2,INTERVAL
		folderPath = config.get(0);
		mKeyword1 = config.get(1).toLowerCase();
		mKeyword2 = config.get(2).toLowerCase();
		interval = Integer.parseInt(config.get(3));
		
		File folder = new File(folderPath);
		if (folder.isDirectory()) {
			ArrayList<String> content;
			mText = null;
			for (File file : folder.listFiles()) {
				if (file.isDirectory()) {
					System.out.println(file.getPath() + " is a directory");
					continue;
				}
				mFilename = file.getName();
				System.out.println("parsing file: " + mFilename);
				content = FileUtils.readFileLinesToArrayList(file);
				if (content == null) {
					System.out.println(file.getPath() + " : read failed");
					continue;
				}
				boolean isHtml = isHtmlText(content);
				mIsHtml = isHtml;
				mText = HtmlAnalyzer.getHtmlText(file);
				processTxt(isHtml);
				resultIndex++;
				clear();
			}
			writeBack();
		}
	}
	
	private void clear() {
		keywordOneIndexes.clear();
		keywordTwoIndexes.clear();
		mText = "";
		mFilename = "";
		mIsHtml = false;
	}

	private void writeBack() {
		File dir = new File(folderPath, "results");
			try {
				if (!dir.exists()) {
					dir.mkdir();
				}
				File ret = new File(dir, "result.csv");
				if (!ret.exists()) {
					ret.createNewFile();
				}
				FileWriter fw = new FileWriter(ret);
				for (String result : resultsSet) {
					fw.append(result);
					fw.flush();
				}
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private void processTxt(boolean isHtml) {
		if (mText == null) {
			return;
		}
		mText = mText.trim().replaceAll(" +", " ").toLowerCase();
		fillKeyworkOneList(keywordOneIndexes, mKeyword1, mText, 0);
		fillKeyworkOneList(keywordTwoIndexes, mKeyword2, mText, 0);
		
		compareList();
	}

	private void compareList() {
		for (int i = 0; i < keywordOneIndexes.size(); i++) {
			int one = keywordOneIndexes.get(i).intValue();
			for (int j = 0; j < keywordTwoIndexes.size(); j++) {
				int two = keywordTwoIndexes.get(j).intValue();
				if (one < two && (two - one + mKeyword1.length()) <= interval) {
					int firstPoint = mText.substring(0, one).lastIndexOf(".") + 1;
					int lastPoint = mText.substring(two).indexOf(".") + two + 1;
					Result ret = new Result(mFilename, mIsHtml, mText.substring(firstPoint, lastPoint), mKeyword1, mKeyword2, interval);
					resultsSet.add(ret.toString());
				}
			}
		}
	}

	private void fillKeyworkOneList(ArrayList<Integer> keywordIndexes, String mKeyword, String text, int offset) {
		int index = text.indexOf(mKeyword);
 		if (index >= 0) {
			keywordIndexes.add(index + offset);
			int start = index + mKeyword.length();
			offset = index + offset + mKeyword.length();
			fillKeyworkOneList(keywordIndexes, mKeyword, text.substring(start), offset);
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
		if (args.length < 1) {
			System.out.println("please input config path");
			return;
		}
		KeywordExtractor ke = new KeywordExtractor(args[0]);
		ke.analyser();
	}
}
