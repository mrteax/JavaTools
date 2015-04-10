package resource.query;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import jeasy.analysis.MMAnalyzer;

public class QueryAnalyzer{
	
	
	public String split(String test){
		String keystr = null;
		try{
			long start=System.currentTimeMillis();
			 SocketAddress socketAddress=new InetSocketAddress("127.0.0.1",1250);
	        Socket connection=new Socket();
	        connection.connect(socketAddress,5000);
	        connection.setSoTimeout(2000);
	        InputStreamReader readConnection=new InputStreamReader(connection.getInputStream(),"UTF-8");
	        BufferedReader fromServerReader=new BufferedReader(readConnection);
	        OutputStreamWriter osw=new OutputStreamWriter(connection.getOutputStream(),"UTF-8");
	        PrintWriter toServerWriter=new PrintWriter(osw,true);
	       
	        toServerWriter.println(test);  //sends the text to the server program
	        keystr=fromServerReader.readLine();  //receives response from the server
	        long end=System.currentTimeMillis();
	        System.out.println((end-start)+"ms");
	        connection.close();
		    }
		 catch(Exception e )
	          {	
	            return ("ERROR!");		
		      } 
		   return keystr;
	}
	
	public static void main(String[] args){
		   String queryStr ="中国石油";
	
		   QueryAnalyzer queryAnalyzer = new QueryAnalyzer();
		  /* String[] array = queryAnalyzer.analyzer(queryStr);
		   for(int i = 0;i<array.length;i++)
		   {
			   System.out.println(array[i]);
		   }
		   */
		   String str = queryAnalyzer.split(queryStr); 
		   System.out.println(str);
		}

}
