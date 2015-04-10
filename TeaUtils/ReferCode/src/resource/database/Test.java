package resource.database;

import java.sql.*;
import java.io.*;
import java.util.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

public class Test {
	public static void main(String args[]) throws Exception
	{
		BufferedReader br=new BufferedReader(new FileReader(new File("c:/exception.log")));
		String content="";
		String aLine=br.readLine();
		while(aLine!=null)
		{
			content+=aLine;
			aLine=br.readLine();
		}
		if(!content.contains("http://www.xjklmy.com/news/category.asp?id=15"))
		{
			System.out.println(true);
		}
	}
}
