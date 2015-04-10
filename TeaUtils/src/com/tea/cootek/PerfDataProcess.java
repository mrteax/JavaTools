package com.tea.cootek;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.tea.fileUtils.FileUtils;

public class PerfDataProcess {
	String targetPath = "data/performance";
	
	private void processData() {
		ArrayList<String> data = FileUtils.readFileLinesToArrayList(new File(targetPath));
		if (data != null) {
			for (String line : data) {
				try {
					JSONObject object = new JSONObject(line);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
