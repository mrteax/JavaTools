package resource.query;
public class resultBean {
	private String title="";
	private String url="";
	private String date="";
	private String crawler_date=null;
	private String size="";
	private String content="";
	private String count="0";
	private String urlid="0";
	private int docid=0;
	private String source="";
	private double score=0.0;
	
	public double getScore() {
		return score;
	}	
	public void setScore(double score) {
		this.score = score;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl_id()
	{
		return urlid;
	}
	public void setUrl_id(String id){
		this.urlid=id;
	}
	public int getCount()
	{
		return Integer.parseInt(count);
	}
	public void setCount(String count)
	{
		if(count!=null)
		{
			this.count=count;
		}
	}
	public void setDocid(int docid)
	{
		this.docid=docid;
	}
	public int getDocid()
	{
		return docid;
	}
	
	public void setSource(String source)
	{
		this.source=source;
	}
	
	public String getSource()
	{
		return source;
	}
	public void setCrawler_date(String crawler_date)
	{
		this.crawler_date=crawler_date;
	}
	public String getCrawler_date()
	{
		return this.crawler_date;
	}
}
