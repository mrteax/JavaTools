package resource.crawler;


public class siteBean {
	private int site_id;
	private String site_url;
	private String site_name;
	private int status;
	private String sitebranch=null;
	private String newslist;
	private String title;
	private String date;
	private String content;
	private String cookieSite;
	
	public String getCookieSite() {
		return cookieSite;
	}
	public void setCookieSite(String cookieSite) {
		this.cookieSite = cookieSite;
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
	public int getSite_id() {
		return site_id;
	}
	public void setSite_id(int site_id) {
		this.site_id = site_id;
	}
	public String getSite_name() {
		return site_name;
	}
	public void setSite_name(String site_name) {
		this.site_name = site_name;
	}
	public String getSite_url() {
		return site_url;
	}
	public void setSite_url(String site_url) {
		this.site_url = site_url;
	}
	public String getSitebranch() {
		return sitebranch;
	}
	public void setSitebranch(String sitebranch) {
		this.sitebranch = sitebranch;
	}
	public String getNewslist() {
		return newslist;
	}
	public void setNewslist(String newslist) {
		this.newslist = newslist;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}
