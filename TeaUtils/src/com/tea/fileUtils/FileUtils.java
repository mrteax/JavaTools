package com.tea.fileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.tea.LocalConst;

public class FileUtils {
	private static final boolean LOG = LocalConst.LOG;

	public static ArrayList<String> readFileLinesToArrayList(File file) {
		ArrayList<String> ret = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String tempStr;
			while ((tempStr = reader.readLine()) != null){
				ret.add(tempStr);
			}
			reader.close();
			return ret;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readStringFromFile(File file) {
		StringBuilder sb = new StringBuilder();
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if (reader != null) {
			char[] buf = new char[1024];
			try {
				int size = 0;
				while (true) {
					size = reader.read(buf);
					if (size > 0) {
						sb.append(buf, 0, size);
					} else {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (LOG) {
			System.out.println("readFileContent ret: " + sb.toString());
		}
		return sb.toString();
	}
}
