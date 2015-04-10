package resource.manage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import jeasy.analysis.MMAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.InputSource;

import resource.database.DataBaseConnect;

public class InnerNewsDelete 
{
	private DataBaseConnect dbc=null;
	public InnerNewsDelete() 
	{
		dbc=new DataBaseConnect();
	}
	
	public ArrayList<Url> getList(int pageId, int num,String title,String url)
	{
		ArrayList<Url> allurl=new ArrayList<Url>();
		if(!dbc.getConnection())
			return null;

        //得到总长度
		String queryLength="";
		if((url==null || "".equals(url))&&(title==null || "".equals(title)))
		{
			queryLength="select count(*) from newsurl where url_update=5";
		}	
		else if(url==null || "".equals(url))
		{
			queryLength="select count(*) from newsurl where title like '%"+title+"%' and url_update=5";
		}	
		else if(title==null || "".equals(title))
		{
			queryLength="select count(*) from newsurl where url like '%"+url+"%' and url_update=5";
		}
		else
		{
			queryLength="select count(*) from newsurl where title like '%"+title+"%' and url like '%"+url+"%' and url_update=5";
		}

		ResultSet rs0=dbc.executeQuery(queryLength);
		int length=0;
		try
		{
			if(rs0.next())
			{
				length=rs0.getInt(1);
			}
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//最后一页		
		if(pageId==-1)
		{
			pageId=length/num+1;
		}
		
		if(pageId==0)
			pageId=1;
	
		int pagecount=0;
		if(length % num==0)
		{
		   pagecount=length/num;	
		}
		else
		{
		   pagecount=length/num+1;	
		}  //获得总页数  
		int beginIndex=(pageId-1)*num;
		int endIndex=pageId*num;
		
		if(beginIndex>length)
		{
			beginIndex=0;
		    endIndex=num;
		}
		String query;
		if((url==null || "".equals(url))&&(title==null || "".equals(title)))
		{
			query="select * from (select rownum as rowno, tempSite.* from (select * from newsurl where  url_update=5 order by url_id desc) tempSite where rownum<='"+endIndex+"') table_alias where table_alias.rowno>'"+beginIndex+"'";			
		}	
		else if(url==null || "".equals(url))
		{
			query="select * from (select rownum as rowno, tempSite.* from (select * from newsurl where title like '%"+title+"%' and url_update=5 order by url_id desc) tempSite where rownum<='"+endIndex+"') table_alias where table_alias.rowno>'"+beginIndex+"'";			
		}	
		else if(title==null || "".equals(title))
		{
			query="select * from (select rownum as rowno, tempSite.* from (select * from newsurl where SITE_URL like '%"+url+"%' and url_update=5 order by url_id desc) tempSite where rownum<='"+endIndex+"') table_alias where table_alias.rowno>'"+beginIndex+"'";			
		}
		else
		{
			query="select * from (select rownum as rowno, tempSite.* from (select * from newsurl where SITE_URL like '%"+url+"%' and title like '%"+title+"%' and url_update=5 order by url_id desc) tempSite where rownum<='"+endIndex+"') table_alias where table_alias.rowno>'"+beginIndex+"'";			
		}
		
		ResultSet rs=dbc.executeQuery(query);
		System.out.println(query);
		try 
		{
			while(rs.next())
			{
				Url u=new Url();
				u.setId(rs.getInt("url_id"));
				u.setTitle(rs.getString("title"));
				u.setUrl(rs.getString("url"));
			    u.setPagecount(pagecount);
				allurl.add(u);
			}
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		dbc.closeConnection();
		return allurl;
	}
	public String del(String newsid)  
	{		
		String info="";
		DataBaseConnect dbc=new DataBaseConnect();	
		try 
		{
			XPath xpathEngine = XPathFactory.newInstance().newXPath();
			String indexDataDirectory = "/config/InnerNewsindexDataDirectory/text()";						
			InputSource xmlSource = new InputSource(
					"/opt/oilsearch/outer/conf/config.xml"); 		
			File indexDir = new File(xpathEngine.evaluate(indexDataDirectory, xmlSource));       
			Directory directory = FSDirectory.getDirectory(indexDir);
			/*IndexReader reader = IndexReader.open(directory);
			IndexReader.unlock(directory);*/
			Term term = new Term("urlid",newsid);
			MMAnalyzer analyzer = new MMAnalyzer();
			info="not exist url_id="+newsid;
			
			NumberFormat format = NumberFormat.getIntegerInstance();   
//			设置数字的位数 由实际情况的最大数字决定   
			format.setMinimumIntegerDigits(10);   
//			是否按每三位隔开,如:1234567 将被格式化为 1,234,567。在这里选择 否   
			format.setGroupingUsed(false); 
			
			//删除新闻索引，如果有相同标题新闻还需要更新其他新闻的count数目
			IndexSearcher indexSearcher = new IndexSearcher(directory);
			Query termquery = new TermQuery(term);
			Hits termhits = indexSearcher.search(termquery);
			IndexWriter indexWriter = new IndexWriter(directory, analyzer,false);
			if(termhits !=null || termhits.length()>0){
				String title = termhits.doc(0).get("title");
				String contents = termhits.doc(0).get("contents");
				String count = termhits.doc(0).get("count");  //count域
				
				if(count != null){
					info += "count is not null";
					
					Integer countint = Integer.parseInt(count);
					if(countint > 0) {//其他合并新闻的count域是-url_id
						info += "count is " + countint + " 其他合并新闻的count域是-url_id";
						termquery=new TermQuery(new Term("count",format.format(-Integer.parseInt(newsid))));
						Hits hits = indexSearcher.search(termquery);
						if(hits!=null){
							info += "***********************" + "hits:" + hits.length();
							Term tempterm = null;
							Document tempdoc = hits.doc(0);
							int newcountid = Integer.parseInt(tempdoc.get("urlid"));//其他相似新闻的count改为该urlid
							term = new Term("urlid",tempdoc.get("urlid"));
							indexWriter.deleteDocuments(term);//删除当前文档
							tempdoc.removeField("count");
							tempdoc.add(new Field("count",format.format(--countint),Field.Store.YES,Field.Index.UN_TOKENIZED));//规定设置第一个相同新闻为参照，相同新闻的数量-1
							indexWriter.addDocument(tempdoc);//修改后重新添加文档
							
							for(int i=1;i<hits.length();i++){//其余相似新闻的count改为-newcountid
								
								tempdoc = hits.doc(i);
								term = new Term("urlid",tempdoc.get("urlid"));
								indexWriter.deleteDocuments(term);//删除当前文档
								tempdoc.removeField("count");
								tempdoc.add(new Field("count",format.format(-newcountid),Field.Store.YES,Field.Index.UN_TOKENIZED));
								indexWriter.addDocument(tempdoc);//修改后重新添加文档
								info += "***************" + "修改了" + tempdoc.get("urlid");
								
							}
						}
					}else if(countint < 0){//本身是被合并的新闻
						info += "*****************" + "本身是被合并新闻，源头是" +countint; 
						termquery=new TermQuery(new Term("urlid",String.valueOf(-countint)));//查询相似新闻中拥有mergcount的新闻
						Hits hits = indexSearcher.search(termquery);
						if(hits.length()>0){
							Document tempdoc = hits.doc(0);
							int modcount = Integer.parseInt(tempdoc.get("count"));
							term = new Term("urlid",tempdoc.get("urlid"));
							indexWriter.deleteDocuments(term);
							tempdoc.removeField("count");
							tempdoc.add(new Field("count",format.format(--modcount),Field.Store.YES,Field.Index.UN_TOKENIZED));//修改相似新闻的数量
							indexWriter.addDocument(tempdoc);
							info += "***************" + "修改了" + countint	;
						}
					}
				}
				indexWriter.flush();
				indexWriter.close();
			}else{
				return info;
			}
			
			int num;
			IndexReader reader = IndexReader.open(directory);
			IndexReader.unlock(directory);
			term = new Term("urlid",newsid);
			num=reader.deleteDocuments(term);	 //删除此id的网页的索引
			reader.close();
			directory.close(); 		
					
			if(dbc.getConnection() && num>0)
			{
				String query="update newsurl set url_bad=2 where url_id='"+newsid+"'";
				dbc.executeUpdate(query);
				dbc.closeConnection();
				info+="###############################url_id="+newsid+"is deleted, and set url_bad=2";
			}
		}
		catch (Exception e) 
		{
			info += " caught a " + e.getClass() +
			 "\n with message: " + e.getMessage();
		      System.out.println(" caught a " + e.getClass() +
					 "\n with message: " + e.getMessage());
	    }
		return info;
	}
}