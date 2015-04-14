package com.tea.analyzer;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlAnalyzer {
	public static String getHtmlText(String html) {
		String ret = null;
		Document doc = Jsoup.parse(html);
		ret = doc.text();
		System.out.println(ret);
		return ret;
	}
	
	public static String getHtmlText(File file) {
		String ret = null;
		Document doc;
		try {
			doc = Jsoup.parse(file, null);
			ret = doc.text();
			System.out.println(ret);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
