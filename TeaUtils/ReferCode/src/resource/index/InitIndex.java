package resource.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.InputSource;

import resource.database.DataBaseConnect;

public class InitIndex {
	
	// dataDir,indexDir分别为数据及索引的存放路径
	private DataBaseConnect dbc;
	private String logPageFileDir;
	private String indexPageFileDir;
	
	
	
	public InitIndex() {
		// TODO Auto-generated constructor stub
		
		dbc=new DataBaseConnect("conf/config.xml");
		
		XPath xpathEngine = XPathFactory.newInstance().newXPath();
		

		String indexDataDirectory = "/config/IndexFileDir/text()";
		
		
		String logpath = "/config/LogPath/text()";
	
		InputSource xmlSource = new InputSource("conf/config.xml"); 
		
		try{
			logPageFileDir=xpathEngine.evaluate(logpath, xmlSource);
			indexPageFileDir=xpathEngine.evaluate(indexDataDirectory, xmlSource);
			
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteFileUnderPath(String filePath)
	{
		File path=new File(filePath);
		if(path.isDirectory())
		{
			File[] files=path.listFiles();
			for(int i=0; i<files.length; i++)
			{
				files[i].delete();
			}
		}
		
	}
	

	
	
	public void initPageContent()
	{
		if (dbc.getConnection() )
			System.out.println("connect database OK!");
		
		
		deleteFileUnderPath(indexPageFileDir+"/cache");
		File indexData = new File(indexPageFileDir);
		int length=indexData.listFiles().length;
	    File f=new File(logPageFileDir+ "/lostPage.txt");
	    
	  //已经建立过索引的时候并且存在丢失信息的时候。	    
	    if(f.exists())
	    {
	    	
	    	int totalNum=0;
		    try {
		    	Directory directory =null;
		    	IndexReader reader=null;
		    	if(length>2)
		    	{
		    		File fIndex=new File(indexPageFileDir);
			    	directory = FSDirectory.getDirectory(fIndex);				 
					reader = IndexReader.open(directory);
					IndexReader.unlock(directory);
		    	}
		    	
				
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(logPageFileDir+ "/lostPage.txt")));
				
				String urlid="";
				try {
					while((urlid=in.readLine())!=null)
					{
						if(length>2)
						{
							Term term = new Term("urlid",urlid);
							totalNum+=reader.deleteDocuments(term);
						}
						
												
						String sql="UPDATE innernewsurl SET url_update = 3 WHERE url_id ="+ urlid;
						System.out.println(sql);
						dbc.executeUpdate(sql);
						
					}
					
					System.out.println("共删除索引数目："+totalNum);
					
					if(length>2)
					{
						reader.close();
						directory.close(); 
					}
					
					
					dbc.closeConnection();
					
					//顺利修复丢失网页后，把记录丢失的文件删除掉。	
					File lostPageRecord=new File(logPageFileDir+ "/lostPage.txt");
					if(lostPageRecord.exists())
					{
						lostPageRecord.delete();
					}
					
				} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
		        } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			    }catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	     }
	
	

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
