package resource.query;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

public class FilteredNewsQuery {
	
	int maxpage=10;//返回页面的最大个数
	int summarylength=120;//摘要的最大长度
	
	private int length=0;//共找到多少个记录
	private int pagelength=0;//返回多少个记录
	private double time=0;//查询所用的时间
	
	private IndexSearcher searcher =null;
	
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
		
		return summary;
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
