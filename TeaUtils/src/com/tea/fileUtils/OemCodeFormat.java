package com.tea.fileUtils;


public class OemCodeFormat {
	public static int[] versions = {5607,5607,5504,5607,5608};
	public static String[] channels = {"OEM 049 A17 006","OEM 00B A17 001","OEM 001 031 015","OEM 001 A14 000","OEM 001 A03 002","OEM 11B A15 001","OEM 11B A15 002","OEM 07E A15 001"};
	
	

	public static void main(String[] args) {
		String localVersion;
		for (int i = 0; i < versions.length; i++) {
			localVersion = String.valueOf(versions[i]) + "__" + channels[i];
			System.out.println(localVersion);
		}
	}
}
