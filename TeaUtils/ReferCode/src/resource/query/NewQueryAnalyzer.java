package resource.query;
import org.apache.lucene.analysis.Analyzer;
import jeasy.analysis.MMAnalyzer;
import java.io.*;
import java.net.*;

public class NewQueryAnalyzer{
	
/*public String[] analyzer(String test){
	
		String[] keywords = null;
		try{

			MMAnalyzer a= new MMAnalyzer();
			String str_analysis=a.segment(test," ");
			   if(str_analysis != null)
			   {  
		     keywords =new String[str_analysis.split(" ").length];
		      keywords = str_analysis.split(" ");
			   }
		    }
		 catch(Exception e )
                      {		
			e.printStackTrace();		
		      } 
	        return keywords;
	}
  */
public String compose(String test){
	String keystr = "";
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
        System.out.println(test);
        keystr=fromServerReader.readLine();  //receives response from the server
        long end=System.currentTimeMillis();
        System.out.println((end-start)+"ms");
        connection.close();
	    }
	 catch(Exception e )
          {		
		return "ERROR!";		
	      } 
	   return keystr;
	}



public static void main(String[] args){
	   String queryStr="中国石油总公司胜利油田";

	   NewQueryAnalyzer queryAnalyzer = new NewQueryAnalyzer();
	  /* String[] array = queryAnalyzer.analyzer(queryStr);
	   for(int i = 0;i<array.length;i++)
	   {
		   System.out.println(array[i]);
	   }
	   */
	   String str = queryAnalyzer.compose(queryStr); 
	   System.out.println(str);
	}

}