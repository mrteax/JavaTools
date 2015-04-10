package resource.crawler;

public class urlBean {
	private int url_id;
	private String url;
	private int site_id;
	private String first_crwaler_time;
	private String inner_first_crawler_time;
	private String title="";
	private String titleflag="";
	private String content;
	private String date=null;//create date
	private String crawler_date=null;
	private String time=null;
	private int docsize;
	private int url_update;
	private int inner_url_id=-1;
	private String site_name="";
	private String cookie_site;
	private int unit_priority;
	
	
	public int getUnit_priority() {
		return unit_priority;
	}
	public void setUnit_priority(int unitPriority) {
		unit_priority = unitPriority;
	}
	public String getInner_first_crawler_time() {
		return inner_first_crawler_time;
	}
	public void setInner_first_crawler_time(String innerFirstCrawlerTime) {
		inner_first_crawler_time = innerFirstCrawlerTime;
	}
	public String getCookie_site() {
		return cookie_site;
	}
	public void setCookie_site(String cookieSite) {
		cookie_site = cookieSite;
	}
	public String getTime()
	{
		return time;
	}
	public void setTime(String time)
	{
		this.time=time;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getFirst_crwaler_time() {
		return first_crwaler_time;
	}
	public void setFirst_crwaler_time(String first_crwaler_time) {
		this.first_crwaler_time = first_crwaler_time;
	}
	public int getSite_id() {
		return site_id;
	}
	public void setSite_id(int site_id) {
		this.site_id = site_id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitleflag() {
		return titleflag;
	}
	public void setTitleflag(String titleflag) {
		this.titleflag = titleflag;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getUrl_id() {
		return url_id;
	}
	public void setUrl_id(int url_id) {
		this.url_id = url_id;
	}
	public int getDocsize() {
		return docsize;
	}
	public void setDocsize(int docsize) {
		this.docsize = docsize;
	}
	public int getUrl_update() {
		return url_update;
	}
	public void setUrl_update(int url_update) {
		this.url_update = url_update;
	}
	
	
	public int getInner_url_id() {
		return inner_url_id;
	}
	public void setInner_url_id(int innerUrlId) {
		inner_url_id = innerUrlId;
	}
	public void setSite_name(String site_name)
	{
		this.site_name=site_name;
	}
	
	public String getSite_name()
	{
		return site_name;
	}
	public void setCrawler_date(String date)
	{
		this.crawler_date=date;
	}
	public String getCrawler_date()
	{
		return this.crawler_date;
	}
}
