package resource.index;

import java.io.*;

import java.sql.ResultSet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.lucene.document.Document;

import org.apache.lucene.document.Field;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;


import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.xml.xpath.*;

import org.xml.sax.*;

import resource.database.DataBaseConnect;

import jeasy.analysis.MMAnalyzer;

public class Indextask {
	
private DataBaseConnect dbc;

private BooleanQuery booleanQuery;


	public Indextask()
	{
		dbc=new DataBaseConnect("conf/config.xml");		
	}

	void execute() {
		NumberFormat format = NumberFormat.getIntegerInstance();   
//		设置数字的位数 由实际情况的最大数字决定   
		format.setMinimumIntegerDigits(10);
//		是否按每三位隔开,如:1234567 将被格式化为 1,234,567。在这里选择 否   
		format.setGroupingUsed(false);    
		try {
			
			if (dbc.getConnection() )
				System.out.println("connect database OK!");
			
			InitIndex ii=new InitIndex();
			
			ii.initPageContent();
			
			
			// dataDir,indexDir分别为数据及索引的存放路径
			XPath xpathEngine = XPathFactory.newInstance().newXPath();
			String installpath = "/config/installPath/text()";  // /opt/oilsearch/InnerNewsSearch

			String analyzerPath = "/config/UrlTxtPath/text()";   //  data/txt/
			String indexDataDirectory = "/config/IndexFileDir/text()";  //  /opt/oilsearch/InnerNewsSearch/data/collections/
			String specialwordpath_conf="/config/SpecialWordsPath/text()";
			String logpath = "/config/LogPath/text()";    //   data/log/
			
			InputSource xmlSource = new InputSource(
					"conf/config.xml"); // 假设 XML 文件路径为
																
			String analyzerFileDir=xpathEngine.evaluate(analyzerPath, xmlSource);
			
			String indexFileDir=xpathEngine.evaluate(indexDataDirectory, xmlSource);
			
			String logFileDir=xpathEngine.evaluate(logpath, xmlSource);
	
			String specialWordsPath=xpathEngine.evaluate(specialwordpath_conf,xmlSource);
	
			BufferedWriter lostPageWriter = new BufferedWriter(new FileWriter(logFileDir+ "/lostPage.txt", true));

			File dataDir = new File(analyzerFileDir);

			File indexDir = new File(indexFileDir+ "/cache");

			File indexData = new File(indexFileDir);
			
			
			Directory directory = FSDirectory.getDirectory(indexDir);

			// 定义分词器及用于索引创建的对象

			// Analyzer analyzer = new StandardAnalyzer();
			long startTime = new Date().getTime();
			
			MMAnalyzer analyzer = new MMAnalyzer();
			
			File dic_file=new File(specialWordsPath);
			File dic_file2=new File("/opt/oilsearch/outer/analyzer/readfile/newAddedDic.txt");
	    	if(dic_file.exists()&&dic_file2.exists())
	    	{
	    		analyzer.addDictionary(new FileReader(dic_file));
				analyzer.addDictionary(new FileReader(dic_file2));
	    	}
						
			
			DecimalFormat df1 = new DecimalFormat();
			DecimalFormat df2 = new DecimalFormat("###.000");
			DecimalFormat df3 = new DecimalFormat("00");
			File[] dataFiles = indexData.listFiles();
			
			IndexWriter indexWriter;
			IndexSearcher indexSearcher;
			
			if(dataFiles.length>2){
				for (int k = 0; k < dataFiles.length; k++) {
					if (dataFiles[k].isFile()) {
	  			      System.out.println(dataFiles[k].getAbsolutePath()
								+ "文件的大小为" + dataFiles[k].length() + "Byte");
	
						FileInputStream input = new FileInputStream(dataFiles[k].getAbsolutePath());
						FileOutputStream output = new FileOutputStream(xpathEngine.evaluate(installpath, xmlSource)	+ xpathEngine.evaluate(indexDataDirectory,xmlSource)
								+ "/cache/"
								+ (dataFiles[k].getName()).toString());
						byte[] b = new byte[1024 * 5];
						int len;
						while ((len = input.read(b)) != -1) {
							output.write(b, 0, len);
						}
						output.flush();
						output.close();
						input.close();
					}
				}
				indexWriter = new IndexWriter(directory, analyzer, false);//已经存在索引，则进行增量
			}
			else
			{		
				 indexWriter = new IndexWriter(directory, analyzer, true);//否则进行全部重建
					
			}
			
			indexWriter.setMaxFieldLength(25000);
			
			File[] dataDirs = dataDir.listFiles();

			// 遍历文件目录并添加为索引
			int i, j;
			int m = 0;
			int m1=0;
			int m2=0;
			String scr;
			
			if(dataFiles.length<2){
				//新建索引
				for (j = 0; j < dataDirs.length; j++) {
	                
					if (dataDirs[j].getAbsolutePath().contains("2008-04-01")){
						System.out.println("不索引文件夹:"+dataDirs[j].getAbsolutePath());
						
					}
					
					else if (dataDirs[j].isDirectory()) {
						 dataFiles = dataDirs[j].listFiles();
	
						for (i = 0; i < dataFiles.length; i++, m++) {
							int url_id;
							String sql;
							ResultSet rs = null;
							int docsize = 1;
							
							
							if (dataFiles[i].isFile()) {
	
								System.out.println(dataFiles[i].getAbsoluteFile());
	
								url_id = Integer.valueOf(dataFiles[i].getName().substring(0,dataFiles[i].getName().indexOf('.'))).intValue();
								
	
								
								sql="SELECT docsize,url , create_time , first_crawler_time , innernewsurl.title,innernewssite.site_name,unit_priority " +
								"FROM innernewsurl,innernewssite WHERE innernewsurl.site_id=innernewssite.site_id and url_id="+url_id;
	
								
								
								rs = dbc.executeQuery(sql);
								if(rs.next())
								{
									System.out.println("索引文件:" + dataFiles[i].getPath()+ "\t" + url_id);
									m1++;
									
									indexSearcher=new IndexSearcher(directory);
		
		
									if (rs.getInt("docsize") > 1024)
										docsize = rs.getInt("docsize") / 1024;
									int mustcount=0;
									
									BufferedReader read = new BufferedReader(new FileReader(dataFiles[i].getPath()));

									scr = read.readLine();
									scr=read.readLine();
									String contents=read.readLine();
									
									String title=rs.getString("title");
									//标题为空则返回（有可能会读取到）
									if(title==null || title.equals("")){
										System.out.println("title 读取为空！");
										continue;
									}
										
									
									int urlid=url_id;
									
									String query=analyzer.segment(title," ");//title分词后用空格分隔
									String[] words=query.split(" ");
									
									booleanQuery=new BooleanQuery();
									for(int iw=0;iw<words.length;iw++)
									{
										if(contents.contains(words[iw]))
										{
											booleanQuery.add(new TermQuery(new Term("title",words[iw])),BooleanClause.Occur.MUST);
											mustcount++;
											//System.out.println(words[i]);
										}
										else
										{
											booleanQuery.add(new TermQuery(new Term("title",words[iw])),BooleanClause.Occur.SHOULD);
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
									if(hits.length()>0)//存在相同新闻，还需要进行相同新闻合并操作
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
												doc.add(new Field("count",""+format.format(-urlid),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
												indexWriter.addDocument(doc);
												mergecount++;
											}
											else 
											{
												if(Integer.parseInt(doc.get("count"))==0)//可以添加判断内容时候相同的判断
												{
//													System.out.println("&");
													//System.out.println(doc.get("urlid"));
													//System.out.println("TITLE:"+doc.get("title"));
													//System.out.println("CONTENTS:"+doc.get("contents"));
													term=new Term("urlid",doc.get("urlid"));
													indexWriter.deleteDocuments(term);  //首先删除已经存在的相似新闻文档
													
													doc.removeField("count");
													doc.add(new Field("count",""+format.format(-urlid),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
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
														document.add(new Field("count",""+format.format(-urlid),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
														indexWriter.addDocument(document);
														mergecount++;
													}
														
													term=new Term("urlid",doc.get("urlid"));
													indexWriter.deleteDocuments(term);  //首先删除已经存在的相似新闻文档
														
													doc.removeField("count");
													doc.add(new Field("count",""+format.format(-urlid),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
													indexWriter.addDocument(doc);
													mergecount++;
												}
											}
										}
										
									}
									
									Document document = new Document();
									
									document.add(new Field("urlid",String.valueOf(urlid), Field.Store.YES,
											Field.Index.UN_TOKENIZED));
									document.add(new Field("url",rs.getString("url"), Field.Store.YES,
											Field.Index.UN_TOKENIZED));
									if(rs.getDate("create_time")!=null){
										document.add(new Field("date",rs.getDate("create_time").toString(),Field.Store.YES, Field.Index.UN_TOKENIZED));
									}
									if(rs.getString("first_crawler_time")!=null){
										document.add(new Field("path", rs.getString("first_crawler_time").substring(0,10),
												Field.Store.YES, Field.Index.UN_TOKENIZED));
									}
									document.add(new Field("title", title,Field.Store.YES, Field.Index.TOKENIZED));
									if(contents ==null){
										System.out.println("contents is null!!");
										continue;
									}
									document.add(new Field("contents", contents,Field.Store.YES, Field.Index.TOKENIZED));
									document.add(new Field("length", String.valueOf(docsize), Field.Store.YES,Field.Index.NO));
									document.add(new Field("source",rs.getString("site_name"),Field.Store.YES,Field.Index.UN_TOKENIZED));
									document.add(new Field("unit_priority",String.valueOf(rs.getInt("unit_priority")),Field.Store.YES,Field.Index.UN_TOKENIZED));
									
									if(mergecount>0)//之前有查询到相同新闻的，则还需要加入count域
									{
										document.add(new Field("count",""+format.format(mergecount+1),Field.Store.YES,Field.Index.UN_TOKENIZED));
									}
									else
									{
										document.add(new Field("count",""+format.format(0),Field.Store.YES,Field.Index.UN_TOKENIZED));
									}
									
									indexWriter.addDocument(document);
									indexWriter.flush();
									indexSearcher.close();
									
									
									sql="UPDATE innernewsurl SET url_update = 5 WHERE url_id ="+ url_id;
									System.out.println(sql);
									dbc.executeUpdate(sql);
									
									//写进一个文件，防止程序没有运行结束的时候，修改了数据库，但是没有写入索引文件。
									lostPageWriter.write(url_id+"\r\n");
									lostPageWriter.flush();
									
									rs.close();
									read.close();
								}
								else
								{
									System.out.println("数据库记录已经不存在！");
								}
							}
						}
					}
				}
			}
			else{
				
				System.out.println("添加索引");
				int url_id;
				String sql;
				ResultSet rs = null;
				int docsize = 1;
				String path=null;
				int[] a=new int[100000];
				
				sql = "SELECT docsize, url_id , url , create_time , first_crawler_time , innernewsurl.title,innernewssite.site_name,unit_priority " +
					"FROM innernewsurl,innernewssite WHERE innernewsurl.site_id=innernewssite.site_id and url_update !=5  AND docsize >0 ORDER BY last_mod_time";
				
					
			

			rs=dbc.executeQuery(sql);
			while(rs.next())
			{  
				
				if (rs.getString("first_crawler_time") == null) {
					System.out.println("网页时间为空");
					continue;
					
				}
				
				url_id=rs.getInt("url_id");
				path="";
				if(rs.getString("first_crawler_time")!=null && rs.getString("first_crawler_time").length()>10)
				path=analyzerFileDir+"/"+rs.getString("first_crawler_time").substring(0,10)+"/"+url_id+".txt";
				System.out.println(path);
				
				File f = new File(path);
				if(f.exists()==false){
					System.out.println("文件不存在");
					continue;
				}
				
				if (rs.getInt("docsize") > 1024)
					docsize = rs.getInt("docsize") / 1024;	
				int mustcount=0;
			
				
			
				indexSearcher=new IndexSearcher(directory);
				BufferedReader read = new BufferedReader(new FileReader(path));

				scr = read.readLine();
				scr=read.readLine();
				String contents=read.readLine();
				
				String title=rs.getString("title");
				int urlid=rs.getInt("url_id");
				String query=analyzer.segment(title," ");
				String[] words=query.split(" ");
				
				booleanQuery=new BooleanQuery();
				for(int iw=0;iw<words.length;iw++)
				{		
					if(contents.contains(words[iw]))
					{
						booleanQuery.add(new TermQuery(new Term("title",words[iw])),BooleanClause.Occur.MUST);
						mustcount++;
						//System.out.println(words[i]);
					}
					else
					{
						booleanQuery.add(new TermQuery(new Term("title",words[iw])),BooleanClause.Occur.SHOULD);
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
							doc.add(new Field("count",""+format.format(-urlid),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
							indexWriter.addDocument(doc);
							mergecount++;
						}
						else 
						{
							if(Integer.parseInt(doc.get("count"))==0)//可以添加判断内容时候相同的判断
							{
//								System.out.println("&");
								//System.out.println(doc.get("urlid"));
								//System.out.println("TITLE:"+doc.get("title"));
								//System.out.println("CONTENTS:"+doc.get("contents"));
								term=new Term("urlid",doc.get("urlid"));
								indexWriter.deleteDocuments(term);  //首先删除已经存在的相似新闻文档
								
								doc.removeField("count");
								doc.add(new Field("count",""+format.format(-urlid),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
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
									document.add(new Field("count",""+format.format(-urlid),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
									indexWriter.addDocument(document);
									mergecount++;
								}
									
								term=new Term("urlid",doc.get("urlid"));
								indexWriter.deleteDocuments(term);  //首先删除已经存在的相似新闻文档
									
								doc.removeField("count");
								doc.add(new Field("count",""+format.format(-urlid),Field.Store.YES,Field.Index.UN_TOKENIZED));  //将已存在的相似新闻的count设置为-url_id
								indexWriter.addDocument(doc);
								mergecount++;
							}
						}
					}
					
				}
				Document document = new Document();
				
				document.add(new Field("urlid",String.valueOf(urlid), Field.Store.YES,
						Field.Index.UN_TOKENIZED));
				document.add(new Field("url",rs.getString("url"), Field.Store.YES,
						Field.Index.UN_TOKENIZED));
				document.add(new Field("date",rs.getDate("create_time").toString(),Field.Store.YES, Field.Index.UN_TOKENIZED));
				document.add(new Field("path", rs.getString("first_crawler_time").substring(0,10),
						Field.Store.YES, Field.Index.UN_TOKENIZED));
				document.add(new Field("title", title,Field.Store.YES, Field.Index.TOKENIZED));
				document.add(new Field("contents", contents,Field.Store.YES, Field.Index.TOKENIZED));
				document.add(new Field("length", String.valueOf(docsize), Field.Store.YES,Field.Index.NO));
				document.add(new Field("source",rs.getString("site_name"),Field.Store.YES,Field.Index.UN_TOKENIZED));
				document.add(new Field("unit_priority",String.valueOf(rs.getInt("unit_priority")),Field.Store.YES,Field.Index.UN_TOKENIZED));
				if(mergecount>0)
				{
					document.add(new Field("count",""+format.format(mergecount+1),Field.Store.YES,Field.Index.UN_TOKENIZED));
				}
				else
				{
					document.add(new Field("count",""+format.format(0),Field.Store.YES,Field.Index.UN_TOKENIZED));
				}
				
				indexWriter.addDocument(document);
				indexWriter.flush();

				read.close();
				a[m1]=url_id;
				m1++;
				
				
			}  
			
			
			
			System.out.println(m1);
			for(int t=0;t<m1;t++){
			sql="UPDATE innernewsurl SET url_update = 5 WHERE url_id = "+ a[t];
			dbc.executeUpdate(sql);
			
			//写进一个文件，防止程序没有运行结束的时候，修改了数据库，但是没有写入索引文件。		
			lostPageWriter.write(a[t]+"\r\n");
			lostPageWriter.flush();
			
			
			}
			
				  
			rs.close();	
			}

			lostPageWriter.close();
			  //顺利添加索引后，把记录丢失网页编号删除掉。	
			   File lostPageRecord=new File(logFileDir+ "/lostPage.txt");
			  if(lostPageRecord.exists())
			  {
				 lostPageRecord.delete();
			  }
			  
			indexWriter.optimize();

			indexWriter.close();

			directory.close();

			dataFiles = indexData.listFiles();
			for (int d = 0; d < dataFiles.length; d++) {
				if (dataFiles[d].isFile())
					dataFiles[d].delete();

			}

			dataFiles = indexDir.listFiles();

			long l = 0;

			for (int k = 0; k < dataFiles.length; k++) {

				if (dataFiles[k].isFile()) {

					l += dataFiles[k].length();
					System.out.println(dataFiles[k].getAbsolutePath()
							+ "文件的大小为" + dataFiles[k].length() + "Byte");

					FileInputStream input = new FileInputStream(dataFiles[k]
							.getAbsolutePath());
					FileOutputStream output = new FileOutputStream(xpathEngine
							.evaluate(installpath, xmlSource)
							+ xpathEngine.evaluate(indexDataDirectory,
									xmlSource)
							+ "/"
							+ (dataFiles[k].getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
					dataFiles[k].delete();

				}

			}

			

			df1.setGroupingUsed(true);// 或者不写

			long endTime = new Date().getTime();
			long a3 = (endTime - startTime) / 1000;
			long nDay = a3 / (24 * 60 * 60);
			long nHour = (a3 - nDay * 24 * 60 * 60) / (60 * 60);
			long nMinute = (a3 - nDay * 24 * 60 * 60 - nHour * 60 * 60) / 60;
			long nSecond = a3 - nDay * 24 * 60 * 60 - nHour * 60 * 60 - nMinute
					* 60;
            String st=null;
			if(m2==0)
			{st = "索引了" + df1.format(m1) + "文档,共花费" + nDay + "天" + nHour
					+ "小时" + nMinute + "分" + nSecond + "秒时间.创建索引文件大小为"
					+ df2.format(l / 1048576.0) + "MB";
			}else{
				st="索引了" + df1.format(m1-m2) + "文档,更新了"+df1.format(m2)+"文档,共花费" + nDay + "天" + nHour
				+ "小时" + nMinute + "分" + nSecond + "秒时间.创建索引文件大小为"
				+ df2.format(l / 1048576.0) + "MB";
				
			}
            	
			System.out.println(st);
			

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			BufferedWriter writer = new BufferedWriter(new FileWriter(
					xpathEngine.evaluate(installpath, xmlSource)
							+ xpathEngine.evaluate(logpath, xmlSource)
							+ "/index.log", true));

			writer.write(formatter.format(endTime) + st + "\n");
			writer.close();
			
			dbc.closeConnection();

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getLocalizedMessage());
		}

	}
	
	
	public static void main(String[] args)
	{
		Indextask it=new Indextask();
		it.execute();
	}

}
