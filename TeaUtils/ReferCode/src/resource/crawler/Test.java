package resource.crawler;

import java.net.*;
import java.io.*;
import java.sql.*;
import resource.analyzer.*;

import java.text.NumberFormat;
import java.util.*;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.tags.*;
import resource.database.*;



public class Test {
	
	public static void main(String args[]) throws Exception
	{
		BufferedReader br=new BufferedReader(new FileReader(new File("c:/1.html")));
		String aLine=br.readLine();
		String content="";
		while(aLine!=null)
		{
			content+=aLine;
			aLine=br.readLine();
		}
		System.out.println(content.trim().equals(""));
	}

}
