package resource.query;



import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeFilter;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.xml.sax.InputSource;


import jeasy.analysis.*;

//import resourse.analyzer.DateFormat;


public class BoolQueryBACKUP {
	private String INDEX_PATH = "/opt/oilsearch/newssearch/data/colletions";
	private String indexDataDirectory ="/config/IndexFileDir/text()";
	private XPath xpathEngine = XPathFactory.newInstance().newXPath();
	InputSource xmlSource = new InputSource("/opt/oilsearch/newssearch/conf/config.xml");
	int maxpage=10;//返回页面的最大个数
	int summarylength=160;//摘要的最大长度
	
	private int length=0;//共找到多少个记录
	private int pagelength=0;//返回多少个记录
	private double time=0;//查询所用的时间
	
	Date start = new Date();

	private IndexSearcher searcher =null;
	/*
	 * 
	 */
	public BoolQueryBACKUP()
	{
		
		if(System.getProperty("user.dir").indexOf(":")!=-1)
		{
			System.out.println("very good:"+System.getProperty("user.dir"));
			INDEX_PATH = "data/colletions";
		}
		try {
			File indexDir = new File(xpathEngine.evaluate(indexDataDirectory, xmlSource));
			INDEX_PATH = indexDir.getAbsolutePath();
		} catch (XPathExpressionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//INDEX_PATH=System.getProperty("user.dir")+"/"+INDEX_PATH;
		System.out.println(INDEX_PATH);
		 try {
			searcher = new IndexSearcher(INDEX_PATH);
		} catch (CorruptIndexException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	}
	/*
	 * 返回新闻标题搜索的结果
	 * 
	 * 用于页面显示
	 */
	public resultBean[] titlequery(String word,int page,int type,int sortbytime,String queryid) //type为0表示正常查询；1表示相同新闻查询
	{                                                                                  //sortbytime为0表示按照相关性排序；1表示按照时间排序
		QueryAnalyzer qa =new QueryAnalyzer();                                         //tcflag为1表示按全文搜索；2表示标题搜索
		String[] words_reverse=word.split(" ");
		String words="";
		for(int i=0;i<words_reverse.length;i++)                                   //反转搜索词
		{
			String[] words_temp=(qa.split(words_reverse[i])).split(" ");
			
			for(int j=words_temp.length-1;j>=0;j--)
			{
				words+=words_temp[j]+" ";
			}
		}
		Hits hits=Search(words,type,2,sortbytime,queryid);
		setLength(hits.length());//总的记录条数
		int start=0;
		int end=0;
		
		start=(page-1)*maxpage*2;
		end=start+maxpage*2;//得到记录的条数
		
		if(end>hits.length())
		{
			end=hits.length();
		}
		System.out.println(start);
		System.out.println("end:"+end);
		resultBean []rs=new resultBean [end-start];
		setPagelength(end-start);
		for(int i=start;i<end;i++)
		{
			System.out.println(i);
			Document doc;
			try {
				doc = hits.doc(i);
				resultBean rb=new resultBean();
				String title=doc.get("title");
		        if(title.length()>28)
		        	title=title.substring(0, 28);
				title=colorShow(title,words);
				rb.setTitle(title);
				rb.setUrl(doc.get("url"));
				rb.setDate(doc.get("date"));
				//String s=summary(doc.get("contents"),words);
				
				//s=colorShow(s,words);
				String source=doc.get("source");
				if(source.length()>10)
				{
					source=source.substring(0,10);
				}
				rb.setSource(source);
				rb.setUrl_id(doc.get("urlid"));
				rb.setCrawler_date(doc.get("crawler_date"));
				rb.setSize(doc.get("length")+"K");
				rb.setCount(doc.get("count"));
				
				if((doc.get("count")!=null)&&(Integer.parseInt(doc.get("count"))>0))
				{
					rb.setContent(doc.get("contents"));
				}
				rs[i-start]=rb;
			} catch (CorruptIndexException e) {
				System.out.println("JJJJJJJJJ");
				// TODO 自动生成 catch 块
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IIIIIIII");
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		}
		
		return rs;
	}
	
	/*
	 * 返回最近几天的新闻
	 * 
	 * 用于页面显示
	 */
	public resultBean[] query(String word,int page,int type,int sortbytime,String queryid) //type为0表示正常查询；1表示相同新闻查询
	{                                                                                  //sortbytime为0表示按照相关性排序；1表示按照时间排序
		QueryAnalyzer qa =new QueryAnalyzer();                                         //tcflag为1表示按全文搜索；2表示标题搜索
		String[] words_reverse=word.split(" ");                             //反转搜索词
		String words="";
		for(int i=0;i<words_reverse.length;i++)
		{
			String[] words_temp=(qa.split(words_reverse[i])).split(" ");
			
			for(int j=words_temp.length-1;j>=0;j--)
			{
				words+=(words_temp[j]+" ");
			}
		}
	
		Hits hits=Search(words,type,1,sortbytime,queryid);
		setLength(hits.length());//总的记录条数
		int start=0;
		int end=0;
		
		start=(page-1)*maxpage;
		end=start+maxpage;//得到记录的条数
		
		
		
		if(end>hits.length()||type==1)
		{
			end=hits.length();
		}
		System.out.println(start);
		System.out.println("end:"+end);
		resultBean []rs=new resultBean [end-start];
		setPagelength(end-start);
		for(int i=start;i<end;i++)
		{
			System.out.println(i);
			Document doc;
			try {
				doc = hits.doc(i);
				resultBean rb=new resultBean();
				String title=doc.get("title");
				if(title.length()>28)
					title=title.substring(0, 28);
				title=colorShow(title,words);
				rb.setTitle(title);
				rb.setUrl(doc.get("url"));
				rb.setDate(doc.get("date"));
				String s=getSummary(doc.get("contents"),words,"[ ；。？！!\\?]");
				
				s=colorShow(s,words);
				
				rb.setUrl_id(doc.get("urlid"));
				rb.setCrawler_date(doc.get("crawler_date"));
				rb.setContent(s);
				rb.setSize(doc.get("length")+"K");
				rb.setCount(doc.get("count"));
				rb.setDocid(hits.id(i));
				String source=doc.get("source");
				if(source.length()>10)
				{
					source=source.substring(0,10);
				}
				rb.setSource(source);
				rs[i-start]=rb;
			} catch (CorruptIndexException e) {
				System.out.println("JJJJJJJJJ");
				// TODO 自动生成 catch 块
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IIIIIIII");
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		}
		
		return rs;
	}
//	/*
//	 * 返含有word关键字的结果
//	 * 
//	 * 用于页面显示
//	 */
//	public ArrayList<resultBean> query(String word,int page)
//	{
//		QueryAnalyzer qa =new QueryAnalyzer();
//		String words=qa.split(word);
//		
//		ArrayList<resultBean>rs=new ArrayList<resultBean>();
//		Hits hits=Search(words);
//		setLength(hits.length());//总的记录条数
//		int start=(page-1)*maxpage;
//		int end=start+maxpage;//得到记录的条数
//		if(end>hits.length())
//		{
//			end=hits.length();
//		}
//		
//		
//		for(int i=start;i<end;i++)
//		{
//			Document doc;
//			try {
//				doc = hits.doc(i);
//				resultBean rb=new resultBean();
//				String title=doc.get("title");
//				title=colorShow(title,words);
//				rb.setTitle(doc.get("title"));
//				rb.setUrl(doc.get("url"));
//				rb.setDate(doc.get("date"));
//				String s=summary(doc.get("contents"),words);
//				s=colorShow(s,words);
//				rb.setContent(s);
//				rb.setSize(doc.get("length")+"K");
//				
//				rs.add(rb);
//			} catch (CorruptIndexException e) {
//				// TODO 自动生成 catch 块
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO 自动生成 catch 块
//				e.printStackTrace();
//			}
//		}
//	
//		return rs;
//	}
	/*
	 * 返含有word关键字的结果
	 */
	public Hits Search(String words,int type,int tcflag,int sortbytime,String queryid)  //type为0表示正常查询；1表示相同新闻查询
	{                                                         //sortbytime为0表示按照相关性排序；1表示按照时间排序
		long start=System.currentTimeMillis();                //tcflag为1表示按全文搜索；2表示标题搜索
		Hits hits=null;
		int k=0,length=0;
		BooleanQuery boolRangeQuery = null;
		
		NumberFormat format = NumberFormat.getIntegerInstance();   
//		设置数字的位数 由实际情况的最大数字决定   
		format.setMinimumIntegerDigits(10);   
//		是否按每三位隔开,如:1234567 将被格式化为 1,234,567。在这里选择 否   
		format.setGroupingUsed(false); 
		
		String ws[]=words.split(" ");
		
	//	System.out.println(ws.length);
		TermQuery[] termQuery = new TermQuery[ws.length];
		if(tcflag==1)
		{
			for(int i =0;i <ws.length;i++)
			{
				termQuery[i] = new TermQuery(new Term("contents", ws[i].trim()));
			}
		}
		else if(tcflag==2)
		{
			for(int i =0;i <ws.length;i++)
			{
				termQuery[i] = new TermQuery(new Term("title", ws[i].trim()));
			}
		}
		else   //为其它用途保留余地，tcflag为其它值时候，默认为contents检索
		{
			for(int i =0;i <ws.length;i++)
			{
				termQuery[i] = new TermQuery(new Term("contents", ws[i].trim()));
			}
		}
		Term startc=new Term("count",format.format(0));
		Term endc=new Term("count",format.format(1000000000));
		RangeQuery rangeQuery=new RangeQuery(startc,endc,true);
		Sort sort;
		if(sortbytime==1)
		{
			sort=new Sort("date",true);
		}
		else
		{
			sort=Sort.RELEVANCE;
		}
		
		if(type==1)
		{
			
			String urlid=queryid;
			 
			
			boolRangeQuery =new BooleanQuery();
			BooleanQuery bool1=new BooleanQuery();
			BooleanQuery bool2=new BooleanQuery();
			bool1.add(new TermQuery(new Term("count",""+format.format(-Integer.parseInt(urlid)))),BooleanClause.Occur.MUST);
			bool2.add(new TermQuery(new Term("urlid",""+Integer.parseInt(urlid))),BooleanClause.Occur.MUST);
			boolRangeQuery.add(bool1,BooleanClause.Occur.SHOULD);
			boolRangeQuery.add(bool2,BooleanClause.Occur.SHOULD);
			System.out.println(boolRangeQuery.toString());
			try {
				hits=searcher.search(boolRangeQuery,sort);
		System.out.println(hits.length());
			} catch (IOException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		}
		else
		{
			for(k=0;k<ws.length&&length<1;k++)
			{
				System.out.println("K:"+k);
				boolRangeQuery =new BooleanQuery();
			for(int j=0;j<ws.length;j++)
			{
				if(j==k)
				{
					boolRangeQuery.add(termQuery[j],BooleanClause.Occur.MUST);
				}
				else
				{
					boolRangeQuery.add(termQuery[j],BooleanClause.Occur.SHOULD);
				}
				
			/*if(termQuery.length>0)
			{
				boolRangeQuery.add(termQuery[0],BooleanClause.Occur.MUST); 
			}
		    for(int i=1;i<termQuery.length;i++)
		    {  
			  boolRangeQuery.add(termQuery[i],BooleanClause.Occur.SHOULD); 				
		    }*/
			}
			boolRangeQuery.add(rangeQuery,BooleanClause.Occur.MUST);
			try {
				hits=searcher.search(boolRangeQuery,sort);
		
			} catch (IOException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
			length=hits.length();
			}
		}
		
		long end=System.currentTimeMillis();
		double time =(double)(end - start)/1000;
		setTime(time);
		return hits;	
	}
	/*
	 * 关闭索引文件，否则会造成apache一直使用的假象
	 */
	public void Close_searcher()
	{
		try {
			searcher.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	/*
	 * 根据传入的title和contents内容获得检索相同新闻的检索词
	 */
	public String getWords(String title,String contents)
	{
		title=title.replaceAll("<font color=\"#c60a00\">","");
		title=title.replaceAll("</font>","");
		String word="";
		int mustcount=0;
		MMAnalyzer analyzer=new MMAnalyzer();
		try
		{
			word=analyzer.segment(title," ");
		}catch(IOException e){
			e.getLocalizedMessage();
		}
		String[] words=word.split(" ");
		word="";
		for(int i=0;i<words.length;i++)
		{
			if(contents.contains(words[i]))
			{
				word+=(words[i]+" ");
				mustcount++;
			}
		}
		int i=0;
		while(mustcount<4&&i<words.length&&!contents.contains(words[i]))
		{
			word+=(words[i]+" ");
			mustcount++;
		}
		return word;
	}
	
	/*
	 * 取txt中取前summarylength个字符作为摘要
	 */
	public String summary(String txt)
	{
		String summary="";
		
		int start=0;//摘要的起始位置
		/*
		 * 摘要的结束位置
		 */
		int end=start+summarylength;
		if(end>txt.length())
		{
			end=txt.length()-1;
		}
		summary=txt.substring(start, end);
		
		return summary+"......";
	}
	
	/*
	 *根据word在txt中生成摘要
	 *
	 * 摘要长度固定返回结果中尽可能多的包含word中的词
	 */
	public String summary(String txt,String words)
	{
		String summary="";
		
		QueryAnalyzer qa =new QueryAnalyzer();
		String txta[]=qa.split(txt).split(" ");
		String worda[]=words.split(" ");
		
		int start=0;//摘要的起始位置
		int maxwordnum=0;//摘要包含词的最大个数
		int l=0;//当前txt的长度
		int ws=0;//当前摘要包含词的个数
		
		ArrayList<Integer> pos=new ArrayList<Integer>();
		ArrayList<Integer> index=new ArrayList<Integer>();
		
		
		//记录word中的词在txt中出现的位置
		for(int i=0;i<txta.length;i++)
		{
			l=l+txta[i].length();
			
			for(int j=0;j<worda.length;j++)
			{
				if(txta[i].equals(worda[j]))
				{
				//	System.out.println(txta[i]+"::经济效果asdfasdf");
					pos.add(l);
					index.add(i);
				}
			}
		}
		/*
		 * 利用word中的词在txt中出现的位置
		 * 
		 * 找出包含word中词的个数最多的长度等于summarylength的摘要
		 */
		
		for(int i=0;i<pos.size();i++)
		{
			ws=1;
			for(int j =i+1;j<pos.size();j++)
			{
				if(pos.get(j)-pos.get(i)<summarylength)
				{
					ws++;
				}
			}
			if(ws>maxwordnum)
			{
				maxwordnum=ws;
				start=index.get(i);
			}
		}
		
		/*
		 * 找出以 ；。？！!.?这些中一个符号开始的
		 * 
		 * 作为摘要的开始
		 */
		
		String patternStr; 
		Pattern p;
		Matcher m;
		
		if(start!=0)
		{	
			patternStr="[ ；。？！!\\.\\?][^ ；。？！!\\.\\?]{0,"+summarylength+"}?"; 
			//System.out.println(patternStr);
			for(int i=start;i<start+4 && i<txta.length;i++)
			{
				patternStr=patternStr+txta[i];
				if(i<start+3)
					patternStr=patternStr+"[\\s\\S]{0,10}?";
			}
			//System.out.println(patternStr);
			p = Pattern.compile(patternStr);
			m = p.matcher(txt);
			
			if(m.find())
			{
				start=m.start()+1;
			}
		}
		
		
		/*
		 * 摘要的结束位置
		 */
		int end=start+summarylength;
		if(end>txt.length())
		{
			end=txt.length()-1;
		}
//		System.out.println("txt:"+txt.length());
//		System.out.println(maxwordnum);
//		System.out.println("start:"+start);
//		System.out.println("end:"+end);
		
		return txt.substring(start, end)+"......";
	}
	
	class sdata
	{
		public String data;
		public int num;
		public int id;
	}
	public String getSummary(String txt,String data,String patternStr) throws IOException
	{
		
	String summary="";
		Vector<sdata> v=new Vector<sdata>();
		
		 QueryAnalyzer a=new QueryAnalyzer();
	  String[] words=a.split(data).split(" ");
		//String[] tt=a.compose(txt).split(" ");
		
		String content=txt;
//		String content="";
//		for(int i=0;i<tt.length;i++)
//		 {
//		 	 content +=tt[i];
//		 	}
//	     content=content.replace(" ","");

	  //System.out.println(content);
	  int id=0;

	 // String patternStr="[������!\\?][^ ������!\\?]"; 
	 // System.out.println(patternStr);
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(content);
		int n=0;
		if(matcher.find())
		{
		  n=matcher.start()+1;
		}
	 //System.out.println("---"+n);
	 if(n==0)
	 {
	 		sdata dd=new sdata();
				
				dd.data=content;
				dd.id=id;
				dd.num=1;
			 v.addElement(dd);
			// return n+"==="+content;
	 	}
	 	else
	 	{
		while(n>0&& n<content.length())
	  {
			
//		  int[] index={a0,b,c,d,e,f};
//		  for(int k=0;k<index.length-1;k++)
//		  {
//			  for(int l=k+1;l<index.length;l++)
//			  {
//				  if(index[k]>index[l])
//				  {
//					  int t=index[k];
//					  index[k]=index[l];
//					  index[l]=t;
//				  }
//			  }
//		  }
//		  int n=0;
//		  for(int p=0;p<index.length;p++)
//		  {
//			  if(index[p]>0)
//			  {
//				  n=index[p];
//				  break;
//			  }
//		  }
		 
		
				sdata dd=new sdata();
				
				dd.data=content.substring(0,n);
				dd.id=id;
				dd.num=0;
			 v.addElement(dd);
			  id++;
			  content=content.substring(n);
				

			  pattern = Pattern.compile(patternStr);
			  matcher = pattern.matcher(content);
			  if(matcher.find())
				{
				   n=matcher.start()+1;
				}
	  }
	}
	 
	  for(int i=0;i<v.size();i++)
	  {
		  //System.out.println("===="+v.elementAt(i).data);
	  }
	
	
	  for(int i=0;i<v.size();i++)
	  {
		  for(int j=0;j<words.length;j++)
		  {
			  String ds=v.get(i).data;
			  while(ds.contains(words[j]))
			  {
				  v.get(i).num++;
				  ds=ds.substring(ds.indexOf(words[j])+1);
			  }
		  }
		 
	  }
   
	  for(int x=0;x<v.size()-1;x++)
	  {
		  for(int y=x+1;y<v.size();y++)
		  {
			  if(v.get(x).num<v.get(y).num)
			  {
				  int middle=v.get(x).num;
				  String m=v.get(x).data;
				  v.get(x).num=v.get(y).num;
				  v.get(x).data=v.get(y).data;
				 v.get(y).num=middle;
				  v.get(y).data=m;
			  }
		  }
	  }
//	Vector<String> result=new Vector<String>();
	  for(int i=0;i<v.size();i++)
	  {
		  if(v.get(i).num==0)
			  v.remove(i);
	  }
    
	  
	  String subs="";
	  for(int i=0;i<v.size();i++)
	  {
		 subs +=v.get(i).data;
		 
		 if(subs.length()>160)
		 {
			 summary=subs.substring(0,160)+"......";
			 break;
		 }
	  }
	  if(subs.length()<=160 && subs.length()>0)
	  {
		 //summary=subs.substring(0,subs.lastIndexOf("��"))+"......"; 
		 summary=subs;
	  }
	  if(summary.length()==0)
	  {
	  	if(txt.length()>160)
	  	{
	       summary=txt.substring(0,160)+"......";	
	    }
	    else
	    {
	       summary=txt;	
	    }
	  }
	  if(summary.startsWith(",")||summary.startsWith(".")||summary.startsWith(":")||summary.startsWith("��")||summary.startsWith("��"))
		  summary=summary.substring(1);
	 // return n+"==="+summary;
	 return summary;
	}
	
	public String htmlHostIP() throws Exception{
    	String hostIP =null;
    	XPath xpathEngine = XPathFactory.newInstance().newXPath();  
    //	InputSource xmlSource = new InputSource("/opt/firstfzsearch/conf/config.xml");
    	String htmlDataServer ="/config/HtmlHostIP/text()";
    	hostIP =xpathEngine.evaluate(htmlDataServer, xmlSource);
    	return hostIP;
    }
	
/*
	 * 把content中的words加上颜色呈示
	 * 
	 * 例如content="fontfasasnndfsaefontggggg",words= "font n"
	 */
	/*刘彬写，但是在翻转搜索词后出现bug。
	public String colorShow(String content,String words)
	{
		String result="";
		String [] ws=words.split(" ");
		ArrayList<String> al=new ArrayList<String>();
		
		String patternStr="<font [\\s\\S]*?</font>"; //此正则表达式是判断什么？
		//防止把最后一个词漏掉
		al.add(content+" ");
		for(int i=0;i<ws.length;i++)
		{
		//	System.out.println("IIIIIIIIII:"+i);
			for(int j=0;j<al.size();j++)
			{
			//	System.out.println("JJJJJJJ:"+j);
				if(al.get(j).matches(patternStr))
				{
					//break;
					continue;
				}
				else if(al.get(j).indexOf(ws[i])!=-1)
				{
					
					String []ts=al.remove(j).split(ws[i]);
					if(ts.length>0)
					{
					int k;
			//		System.out.println(ts.length);
					for(k=0;k<ts.length-1;k++)
					{
				//		System.out.println(ts[k]);
						al.add(j+2*k, ts[k]);
						al.add(j+2*k+1,"<font color=\"#c60a00\">"+ws[i]+"</font>");
					}
					al.add(j+2*k, ts[k]);
					}
				}	
			}
		}
		
		for(int i=0;i<al.size();i++)
		{
			result=result+al.get(i);
		}
		
		return result;
	}*/
	public String colorShow(String content,String words)
	{
		String result=content;
		result=result.replaceAll("#","");
		result=result.replaceAll("\\*","");
		String[] words_array=words.split(" ");
		for(int i=0;i<words_array.length;i++)
		{
			String color_text="#"+words_array[i]+"*";
			result=result.replaceAll(words_array[i], color_text);
		}
		result=result.replaceAll("#","<font color=#c60a00>");
		result=result.replaceAll("\\*","</font>");
		return result;
	}
	
	
	public resultBean[] result(Hits hits,String word,int page)
	{		
		int webpage_num=10;
		int start=(page-1)*maxpage;
		
		if((start>=hits.length()-10) &&(start<hits.length()))
		{
			webpage_num=hits.length()-start;
		}
		int end=start+webpage_num;
		
		resultBean[] rb=new resultBean[webpage_num];
		for(int i=start;i<end;i++)
		{
			Document doc;
			try {
				doc = hits.doc(i);
				
				rb[i].setTitle(doc.get("title"));
				rb[i].setUrl(doc.get("url"));
				rb[i].setDate(doc.get("date"));
				rb[i].setContent(summary(doc.get("contents"),word));
				rb[i].setSize(doc.get("length")+"K");
			} catch (CorruptIndexException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		}
		return rb;
	}
//	/*
//	 * 返回最近几天的新闻
//	 * 
//	 * 用于页面显示
//	 */
//	public ArrayList<resultBean> query(int page)
//	{
//		
//		ArrayList<resultBean>rs=new ArrayList<resultBean>();
//		Hits hits=Search();
//		setLength(hits.length());//总的记录条数
//		int start=(page-1)*maxpage;
//		int end=start+maxpage;//得到记录的条数
//		if(end>hits.length())
//		{
//			end=hits.length();
//		}
//		System.out.println(start);
//		System.out.println("end:"+end);
//		for(int i=start;i<end;i++)
//		{
//			System.out.println(i);
//			Document doc;
//			try {
//				doc = hits.doc(i);
//				resultBean rb=new resultBean();
//				String title=doc.get("title");
//				rb.setTitle(title);
//				rb.setUrl(doc.get("url"));
//				rb.setDate(doc.get("date"));
//				String s=summary(doc.get("contents"));
//				rb.setContent(s);
//				rb.setSize(doc.get("length")+"K");
//				rs.add(rb);
//			} catch (CorruptIndexException e) {
//				System.out.println("JJJJJJJJJ");
//				// TODO 自动生成 catch 块
//				e.printStackTrace();
//			} catch (IOException e) {
//				System.out.println("IIIIIIII");
//				// TODO 自动生成 catch 块
//				e.printStackTrace();
//			}
//		}
//		
//		return rs;
//	}
//	
//	
	
	/*
	 * 返回最近几天的新闻
	 * 
	 * 用于页面显示
	 */
	public resultBean[] query(int page)
	{

		Hits hits=Search();
		setLength(hits.length());//总的记录条数
		int start=(page-1)*maxpage;
		int end=start+maxpage;//得到记录的条数
		if(end>hits.length())
		{
			end=hits.length();
		}
		System.out.println(start);
		System.out.println("end:"+end);
		resultBean []rs=new resultBean [end-start];
		setPagelength(end-start);
		for(int i=start;i<end;i++)
		{
			Document doc;
			try {
				doc = hits.doc(i);
				resultBean rb=new resultBean();
				String title=doc.get("title");
				title=title.replaceAll("\\s+", "");
//				System.out.println("title.length():"+title.length());
//				System.out.println("title.length():"+title);
				if(title.length()>28)
					title=title.substring(0, 28);
				rb.setTitle(title);
				rb.setUrl(doc.get("url"));
				rb.setDate(doc.get("date"));
				rb.setUrl_id(doc.get("urlid"));
				String crawler_date=doc.get("crawler_date");
				if(crawler_date==null)
					crawler_date="";
				rb.setCrawler_date(crawler_date);
				
				String s=summary(doc.get("contents"));
				rb.setContent(s);
				rb.setSize(doc.get("length")+"K");
				rb.setDocid(hits.id(i));
				rs[i-start]=rb;
			} catch (CorruptIndexException e) {
				System.out.println("JJJJJJJJJ");
				// TODO 自动生成 catch 块
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IIIIIIII");
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		}
		
		return rs;
	}
	/*
	 * 返回最近几天的新闻
	 */
	public Hits Search() {
		long start=System.currentTimeMillis();
		int n=360;
		
		Hits hits=null;

		DateFormat df=new DateFormat();

		String curday=df.GetDate();
		curday=df.getTheDay(1);
		String theday=df.getTheDay(-n);
//		System.out.println(theday);
		try{
			/*SortField sf2 = new SortField("date",SortField.AUTO,true);
			Sort sort=new Sort();
			sort.setSort(new SortField[]{sf2});*/
			System.out.println("theday:"+theday);
			System.out.println("theday:"+curday);
			Sort sort=new Sort(new SortField(null,SortField.DOC,true));
			
			RangeQuery currentBooks=new RangeQuery(new Term("date",theday),new Term("date",curday),false);
		    hits = searcher.search(currentBooks,sort);
			
		   
		}catch(Exception e)
		{
		 e.getMessage();
		}
		long end=System.currentTimeMillis();
		double time =(double)(end - start)/1000;
		setTime(time);
		return hits;	
	}   

	public resultBean[] result(Hits hits,int page)
	{		
		int webpage_num=10;
		int start=(page-1)*maxpage;
		

		if((start>=hits.length()-10) &&(start<hits.length()))
		{
			webpage_num=hits.length()-start;
		}
		int end=start+webpage_num;
		resultBean[] rb=new resultBean[webpage_num];
		for(int i=start;i<end;i++)
		{
			Document doc;
			try {
				doc = hits.doc(i);
				
				rb[i].setTitle(doc.get("title"));
				rb[i].setUrl(doc.get("url"));
				rb[i].setDate(doc.get("date"));
				rb[i].setContent(summary(doc.get("contents")));
				rb[i].setSize(doc.get("length")+"K");
			} catch (CorruptIndexException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}

		}
		
		return rb;
	}
	
	public  void printResult(Hits hits) throws Exception{
        if(hits !=null)
       {
         System.out.println("hits ：");	
          if(hits.length()==0)
         {
          System.out.println("Sorry");
         }
         else
         {
         	for(int i=0;i<hits.length();i++)
       	   {
       		  Document doc = hits.doc(i);
       		//  String t = doc.get("title");
       		  String u= doc.get("url");
       		System.out.println(u);
       		//  String d= doc.get("date");
       		  String c =doc.get("contents");
       		  System.out.println(c+"   "+hits.score(i));
       	    }
         }
       }
//        else System.out.println("hits 涓虹┖锛侊紒锛侊紒锛?);
     }
	
	public static void main(String[]args)throws Exception{
		
		BoolQuery4 boolQuery = new BoolQuery4();
		System.out.println("start");
		System.out.println(boolQuery.htmlHostIP());
		
		System.out.println("end");
		/*String keywords = "石油";
    	String date = "2009-08-13";
    	
    	String sp="<P><FONT color=#0000ff>载等神摸</FONT></P><FONT color=#0000ff>载等神摸</FONT>";
    	System.out.println(sp.split("载等神摸")[0]);
    	
    	
        Hits hits = null;
   		Hits orHits = null;

   		resultBean[] rb;
   		
    	try{

    		rb= boolQuery.query(1);
    		//hits= boolQuery.Search(keywords);
        	System.out.println("boolQuery.getLength(::"+boolQuery.getLength());
        	for(int i=0;i<rb.length;i++)
        	{
        		System.out.println(rb[i].getTitle().replaceAll("\\s+", ""));
        		System.out.println("Content::::---"+(rb[i].getContent()));
        		System.out.println("Date::::---"+(rb[i].getDate()));
        	}
    	}catch(Exception e)
    	{
    	  e.getMessage();
    	}
    	System.out.println("getPagelength"+boolQuery.getTime());
    	System.out.println("getPagelength"+boolQuery.getPagelength());
    	//boolQuery.printResult(hits);*/

    	String s=boolQuery.summary("中国石油天然气股份有限公司（简称“中国石油”）今天发布2007年度《社会责任报告》，向社会各界报告中国石油2007年在履行社会责任方面所做的努力和探索。 ??? 中国石油2007年度《社会责任报告》主要分为“积极有效的能源开发”、“持续稳定的油气供应”、“安全清洁的生产运营”、“以人为本的员工发展”、“回报社会的积极行动”五个部分，共3.2万余字。主要内容是： ??? --中国石油视努力增加油气产量，持续稳定供应市场，最大程度地满足经济社会发展对油气的需求，保障国家能源安全为必须持续应对的最大挑战和首要社会责任。2007年中国石油立足科技创新，加大资本投入，取得发现冀东南堡油田等重大突破，新增原油储量、天然气储量均创历史新高，油气产量、原油加工量、成品油产量和销售量再创历年新水平，原油、成品油和天然气销量国内市场份额稳中有增，油气资源保障和供给能力进一步提升。尤其是2007年第四季度国内部分地区柴油供应紧张时，中国石油在满负荷生产情况下，进一步优化调整检维修安排，努力增加炼量，同时加强运输协调和资源衔接，积极组织柴油进口，加大资源收购力度，努力保障供应、稳定市场，得到有关地方政府的肯定和认可。 ??? --中国石油高度关注能源短缺、全球气候变化和生态环境恶化问题，积极建设资源节约型和环境友好型企业，保护和恢复生态环境、实现人与自然和谐发展。支持和贯彻落实《中国应对气候变化国家方案》，与中国石油天然气集团公司、国家林业局和中国绿化基金会一道，积极参与设立中国绿色碳基金，支持开展以吸收固定二氧化碳为目的的植树造林和能源林基地建设；启动十大节能工程和十大减排工程，加大技术改造力度，强力推进节能减排，在生产规模扩大的同时，废水中石油类等主要污染物排放量比上年都有明显下降；努力向社会提供清洁能源，天然气产量持续快速增长，生产的国ⅳ标准柴油部分投放北京市场，制定了新能源“十一五”发展规划，着力提升清洁能源供应能力。 ??? --中国石油积极应对高负荷生产和经营规模扩大给安全生产带来的压力和挑战。继续深入开展“安全环保基础年”活动，加大隐患治理工作力度；从健全制度、改进培训、完善业绩考核三个方面，全面推进hse管理体系建设；层层签订安全环保责任书，落实安全环保责任制，事故总起数大幅度下降，重特大事故得到有效遏制，安全环保形势稳定好转。??? --中国石油在促进业务所在地经济发展和和谐社会建设方面，负有义不容辞的责任。不断加大扶贫帮困、捐资助学、赈灾捐赠、服务奥运和志愿者行动等回报社会的公益活动力度，实施的“中国石油全国农产品深加工招商项目免费广告”获得良好的社会经济效果；在哈萨克斯坦、印尼等海外社区，坚持真诚合作、互利双赢，在发展自身业务的同时，努力促进当地经济发展，参与社会公益、支持社区建设、造福当地民众，得到所在国政府和居民的广泛好评。中国石油视员工为最宝贵的资源和财富，坚持以人为本，尊重员工权益，支持员工发展，保护员工健康，让员工分享企业发展创新成果，努力实现公司价值和员工价值的有机统一。 ??? 特别是中国石油在2007年7月申请加入了联合国全球契约，确认支持联合国全球契约倡导的人权、劳工权益、环境保护和反腐败四个领域的十项原则，并将致力于将这十项原则纳入公司的发展战略、企业文化和日常工作之中。中国石油愿与国际上负责任的企业一道，做优秀的“世界公民”，为推动人类社会的进步与和谐世界的建设作出贡献。 ??? 从2006年开始，中国石油建立社会责任报告制度，并于2007年发布首份《社会责任报告》。与上年度报告相比，2007年度《社会责任报告》更加全面系统地阐述了中国石油的发展目标和发展战略；从创新与持续发展的角度，描述了科技、管理和制度建设等方面取得的成果；着重回顾并首先摘要报告了本年度履行社会责任实现的业绩；引入了履行社会责任改进机制，公布了下年度履行社会责任的目标和行动计划；初步建立了有中国石油特色的社会责任业绩指标体系，并附录了业绩数据；参照全球报告倡议组织（gri）《可持续发展报告指南》和国际石油行业环境保护协会（ipieca）/美国石油学会（api）《油气行业可持续发展报告指南》，列出了内容指标索引。 ??? 中国石油希望通过持续发布《社会责任报告》，向关心和支持中国石油事业发展的社会各界报告公司在履行社会责任方面所做的努力。中国石油将始终秉承“奉献能源、创造和谐”的宗旨，进一步提升社会责任理念，更好地履行社会责任，追求公司与各利益相关者整体利益的最大化，为全面建设小康社会和人类的可持续发展作出新的贡献。","贡献");
    	//System.out.println("*"+s);
    	s="胜利油田一项超低浓度瓦斯（0.2%-0.5%）利用技术，近日在陕西一家煤矿通过权威技术鉴定。这标志着我国在破解超低浓度瓦斯利用技术难题方面取得突破。　　国家能源局专家近日在咸阳对这一应用技术成果鉴定认为，由胜利油田胜利动力机械集团有限公司提...";
    	System.out.println(boolQuery.colorShow(s,"胜利 油田"));
      	s=boolQuery.summary("10月31日，正在缅甸访问的寰球公司总经理汪世宏拜会了缅甸能源部部长伦迪准将。伦迪部长对汪总再一次来到缅甸表示欢迎，并对寰球公司在缅甸第四、第五化肥厂建设过程中各项工作的开展情况表示满意并提出了希望。汪总表示寰球公司将认真落实伦迪部长提出的要求。 会谈期间，双方还就未来的深入合作进行了沟通和探讨。缅甸能源部副部长、能源部计划局局长、缅能源公司计划处处长以及寰球公司驻缅甸总代表李宇鹏陪同出席了会见。","出席");

    //	System.out.println("the andHits length is:"+hits.length());
   	
    }

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	public int getPagelength() {
		return pagelength;
	}
	public void setPagelength(int pagelength) {
		this.pagelength = pagelength;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

}
