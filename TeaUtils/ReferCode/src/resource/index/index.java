package resource.index;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jeasy.analysis.MMAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.index.*;
import org.xml.sax.InputSource;

import resource.analyzer.DateFormat;
import resource.crawler.FileOperate;
import resource.crawler.urlBean;
import resource.database.DataBaseConnect;


public class index {
//	private String configpath="\\conf\\config.xml";
//	private String indexFileDir="\\data\\colletions";
	
	private String configpath="conf/config.xml";
	private String indexFileDir="data/collections";
	
	private Directory directory;
	private IndexWriter indexWriter;
	private IndexSearcher indexSearcher;
	private BooleanQuery booleanQuery;
	private QueryParser parser;
	private MMAnalyzer analyzer;
	
	DataBaseConnect dbc;
	
	
	public index()
	{	
		dbc=new DataBaseConnect(configpath);
		String specialWordsPath="";
		try {
			XPath xpathEngine=XPathFactory.newInstance().newXPath();
			String indexFileDir_conf="/config/IndexFileDir/text()";
			String specialwordpath_conf="/config/SpecialWordsPath/text()";
			InputSource xmlSource=new InputSource(configpath);//假设XML文件路径为
			
			
			try {
				indexFileDir=xpathEngine.evaluate(indexFileDir_conf,xmlSource);
				System.out.println(xpathEngine.evaluate(specialwordpath_conf,xmlSource));
				specialWordsPath=xpathEngine.evaluate(specialwordpath_conf,xmlSource);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			directory = FSDirectory.getDirectory(indexFileDir+"/cache");
			System.out.println("索引路径：" + indexFileDir);
			analyzer = new MMAnalyzer();		
			/**
			 * 特殊词先注释**************************
			 */
			File dic_file=new File(specialWordsPath);
	    	if(dic_file.exists())
	    	{
	    		analyzer.addDictionary(new FileReader(dic_file));
	    	}
			
			//把colletions中的文件拷贝到cache中
			FileOperate fo=new FileOperate();
			fo.delAllFile(indexFileDir+"/cache");
			fo.copyFolder(indexFileDir, indexFileDir+"/cache");
			
			
			File f =new File(indexFileDir+"/segments.gen");
			System.out.println(" f.exists():"+ f.exists());
			indexWriter = new IndexWriter(directory, analyzer, !f.exists());
			
			//indexWriter.setMergeFactor(2);
			
			//parser=new QueryParser("title",new MMAnalyzer());
		} catch (CorruptIndexException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		
	}
	
	public void indexNews(urlBean ub)
	{
		dbc.startConnection();
		if(ub.getDocsize()==0||ub.getContent().trim().length()<30)
		{
			String sql="update innernewsurl set URL_UPDATE=6 where URL_ID="+ub.getUrl_id();
			//	System.out.println("sql:"+sql);
			
			dbc.executeUpdate(sql);
//			dbc.closeConnection();
			return ;
		}
		
		NumberFormat format = NumberFormat.getIntegerInstance();   
//		设置数字的位数 由实际情况的最大数字决定   
		format.setMinimumIntegerDigits(10);   
//		是否按每三位隔开,如:1234567 将被格式化为 1,234,567。在这里选择 否   
		format.setGroupingUsed(false);    
		  
		 
		String s=ub.getDate();
		String sql;
	//	System.out.println("index date:"+s);
		
		if(s!=null && s.split("@").length==1)
		{
			sql="update innernewsurl set URL_UPDATE=5,TITLE='"+ ub.getTitle()+ "',CREATE_TIME=" +
					"to_date('"+ ub.getDate() + "','yyyy-mm-dd') where URL_ID="+ub.getUrl_id();
		//	System.out.println("sql:"+sql);
			dbc.executeUpdate(sql);
		}
		else
		{
			sql="update innernewsurl set URL_UPDATE =5,TITLE='" + ub.getTitle()+ "' where URL_ID="+ub.getUrl_id();
		//	System.out.println("sql:"+sql);
			dbc.executeUpdate(sql);
			sql="select * from innernewsurl where URL_ID="+ub.getUrl_id();
			ResultSet r = dbc.executeQuery(sql);
			try {
				DateFormat df= new DateFormat();
				String d= df.GetDate();
			//	System.out.println("CREATE_TIME:");
				if( r.next())//r!=null &&
				{
//					System.out.println("SSSSSS:"+s);
//					System.out.println(r.getDate("CREATE_TIME").toString());	
//					System.out.println(r.getDate("CREATE_TIME").toString().split(" ")[0]);			
					d=r.getDate("CREATE_TIME").toString().split(" ")[0];
					ub.setDate(d);
			//		System.out.println("CREATE_TIME:"+d);
				}

			} catch (SQLException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		}
		if(dbc!=null){
			dbc.closeConnection();
		}
		
		try {	
			indexSearcher=new IndexSearcher(directory);
			
			if(ub.getDate()==null)return ;
			int mustcount=0;
			System.out.println(" index succeed:");
			String contents=ub.getContent();
			String title=ub.getTitle();
			String query=analyzer.segment(title," ");
			String[] words=query.split(" ");
			
			booleanQuery=new BooleanQuery();
			for(int i=0;i<words.length;i++)
			{		
				if(contents.contains(words[i]))
				{
					booleanQuery.add(new TermQuery(new Term("title",words[i])),BooleanClause.Occur.MUST);
					mustcount++;
					//System.out.println(words[i]);
				}
				else
				{
					booleanQuery.add(new TermQuery(new Term("title",words[i])),BooleanClause.Occur.SHOULD);
				}
			}
			
			
			Term startc=new Term("count",format.format(0));
			Term endc=new Term("count",format.format(1000000000));
			RangeQuery rangeQuery=new RangeQuery(startc,endc,true);
			
			booleanQuery.add(rangeQuery,BooleanClause.Occur.MUST);
			
			System.out.println("内容中包含的词："+booleanQuery.toString());
			
			
			
			double control=0.75*words.length;
			if(words.length<=4)
			{
				control=words.length;
			}
			for(int iw=0;mustcount<control&&iw<words.length;iw++) //保证查询相同新闻时至少有4个必包含项
			{
				if(!contents.contains(words[iw]))
				{
					booleanQuery.add(new TermQuery(new Term("title",words[iw])),BooleanClause.Occur.MUST);
					mustcount++;
				}
				
			}
			System.out.println("检索相同新闻的查询："+booleanQuery.toString());
			Hits hits=indexSearcher.search(booleanQuery);
			System.out.println("HITS:"+hits.length());
			int mergecount=0;
			if(hits.length()>0)
			{
				Term term;
				for(int jw=0;jw<hits.length();jw++)
				{
					Document doc=hits.doc(jw);
					String doctitle=doc.get("title");
					String doctitleseg=analyzer.segment(doctitle," ");
					String[] doctitlesegs=doctitleseg.split(" ");
					int docmustcount=0;
					for(int counttemp=0;counttemp<doctitlesegs.length;counttemp++)
					{
						if(title.contains(doctitlesegs[counttemp]))
						{
							docmustcount++;
						}
								
					}
					
					/*如果不能双方相互包含0.5的词汇量，不予合并*/
					if(docmustcount<0.5*doctitlesegs.length)     
						continue;
					
					if(doc.get("count")==null)
					{
						//System.out.println("*");
						//System.out.println(doc.get("urlid"));
						//System.out.println("TITLE:"+doc.get("title"));
						//System.out.println("CONTENTS:"+doc.get("contents"));
						term=new Term("urlid",doc.get("urlid"));
						indexWriter.deleteDocuments(term);  //首先删除已经存在的相似新闻文档
						doc.add(new Field("count",""+format.format(-ub.getUrl_id()),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
						indexWriter.addDocument(doc);
						mergecount++;
					}
					else 
					{
						if(Integer.parseInt(doc.get("count"))==0)//可以添加判断内容时候相同的判断
						{
//							System.out.println("&");
							//System.out.println(doc.get("urlid"));
							//System.out.println("TITLE:"+doc.get("title"));
							//System.out.println("CONTENTS:"+doc.get("contents"));
							term=new Term("urlid",doc.get("urlid"));
							indexWriter.deleteDocuments(term);  //首先删除已经存在的相似新闻文档
							
							doc.removeField("count");
							doc.add(new Field("count",format.format(-ub.getUrl_id()),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
							indexWriter.addDocument(doc);
							mergecount++;
						}
						else
						{
							int findid=Integer.parseInt(doc.get("urlid"));
							TermQuery termquery=new TermQuery(new Term("count",format.format(-findid)));
							Hits hit=indexSearcher.search(termquery);
							for(int ww=0;ww<hit.length();ww++)
							{
								Document document=hit.doc(ww);
								
								term=new Term("urlid",document.get("urlid"));
								indexWriter.deleteDocuments(term);  //首先删除已经存在的相似新闻文档
										
								document.removeField("count");
								document.add(new Field("count",""+format.format(-ub.getUrl_id()),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
								indexWriter.addDocument(document);
								mergecount++;
							}
								
							term=new Term("urlid",doc.get("urlid"));
							indexWriter.deleteDocuments(term);  //首先删除已经存在的相似新闻文档
								
							doc.removeField("count");
							doc.add(new Field("count",""+format.format(-ub.getUrl_id()),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
							indexWriter.addDocument(doc);
							mergecount++;
						}
					}
				}
				
			}
			Document document = new Document();
			
			//UnitName un = new UnitName();
			String url = ub.getUrl();
			//String ipAddr = un.getIpAddress(un.getHostName(url));//单位IP
			//String unitName = un.getUnitName(ipAddr);//单位名称
			
			document.add(new Field("urlid",String.valueOf(ub.getUrl_id()), Field.Store.YES,
					Field.Index.UN_TOKENIZED));
			document.add(new Field("url",ub.getUrl(), Field.Store.YES,
					Field.Index.UN_TOKENIZED));
			
			/*document.add(new Field("ipAddress", ipAddr,
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			document.add(new Field("unitName", unitName,
					Field.Store.YES, Field.Index.UN_TOKENIZED));*/
			
			document.add(new Field("date",ub.getDate(),Field.Store.YES, Field.Index.UN_TOKENIZED));
			document.add(new Field("crawler_date",ub.getCrawler_date(),Field.Store.YES,Field.Index.NO));
			document.add(new Field("path", ub.getFirst_crwaler_time(),
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			document.add(new Field("title", title,Field.Store.YES, Field.Index.TOKENIZED));
			document.add(new Field("contents", contents,Field.Store.YES, Field.Index.TOKENIZED));
			document.add(new Field("length", String.valueOf(ub.getDocsize()), Field.Store.YES,Field.Index.NO));
			document.add(new Field("source",ub.getSite_name(),Field.Store.YES,Field.Index.UN_TOKENIZED));
			document.add(new Field("unit_priority",String.valueOf(ub.getUnit_priority()),Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			if(mergecount>0)
			{
				document.add(new Field("count",""+format.format(mergecount+1),Field.Store.YES,Field.Index.UN_TOKENIZED));
			}
			else
			{
				document.add(new Field("count",""+format.format(0),Field.Store.YES,Field.Index.UN_TOKENIZED));
			}
			//document.add(new Field("count",String.valueOf("0"),Field.Store.YES,Field.Index.UN_TOKENIZED));
			indexWriter.addDocument(document);
			indexWriter.flush();
			
		} catch (IOException e) {
			System.out.println("ub.getDate():"+ub.getDate());
			System.out.println("ub.getContent():"+ub.getContent());
			System.out.println("ub.getUrl():"+ub.getUrl());
			System.out.println("ub.getTitle():"+ub.getTitle());
			System.out.println("ub.getDocsize():"+ub.getDocsize());
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}

//		
//		"update inner_Domain_Max set Page_Num = "
//		+ String.valueOf(iMax + 1) + " where Domain_Name = '"
//		+ domain + "'";
		

	}
	
	public void searcherclose()
	{
		try{
			indexSearcher.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void indexclose(){
		try {
			//indexWriter.flush();
			indexWriter.optimize();
			indexWriter.close();
			
			directory.close();
			
			dbc.closeConnection();
		} catch (CorruptIndexException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		
		System.out.println("index colse");
		//把colletions/cache中的索引复制到colletions中
		FileOperate fo=new FileOperate();
		fo.delAllFiles(indexFileDir);
		fo.copyFolder(indexFileDir+"/cache", indexFileDir);

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		// TODO 自动生成方法存根
		long start=System.currentTimeMillis();
		File file=new File("E:/search/NewsSearch/data/txt/2010-06-13");
		File[] files=file.listFiles();
		urlBean urlbean=new urlBean();
		for(int j=0;j<100;j++)
		{
			for(int i=0;i<files.length;i++)
			{
				BufferedReader br=new BufferedReader(new FileReader(files[i]));
				urlbean.setTitle(br.readLine());
				urlbean.setDate(br.readLine());
				String content="";
				String aLine=br.readLine();
				while(aLine!=null)
				{
					content+=aLine;
					aLine=br.readLine();
				}
				urlbean.setContent(content);
				urlbean.setUrl_id(j*files.length+i);
				urlbean.setDocsize(1);
				urlbean.setUrl("1");
				urlbean.setSite_id(1);
				urlbean.setFirst_crwaler_time("");
				urlbean.setUrl_update(2);
			index index1=new index();
			index1.indexNews(urlbean);
			index1.indexclose();
			}
		}
		index index2=new index();
		index2.indexclose();
		long end=System.currentTimeMillis();
		System.out.println((end-start)+"ms");
	}

}
