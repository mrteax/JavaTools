package resource.analyzer;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.xml.sax.InputSource;


import resource.crawler.CheckCharset;
import resource.crawler.FileOperate;
import resource.crawler.siteBean;
import resource.crawler.urlBean;
import resource.database.DataBaseConnect;



public class analyzer {
//	private String configpath="\\conf\\config.xml";
//	private String urltxtlpath="\\data\\txt";
//	private String modelpath="\\data\\model";
	
	private String configpath="conf/config.xml";
	private String urltxtlpath="data/txt";
	private String innerurltxtpath="/opt/oilsearch/inner/analyzer/endtxt/wordstxt";
	private String modelpath="data/model";
	
	private boolean down=false;
	private String firstpage="首页|上一页|\\|&lt;|&lt;&lt;";
	private String nextpage="下一页|下页|&gt;&gt;";
	private String lastpage="尾页|最后一页|末页|最后页|&gt;\\|";
	private String listpage="\\[\\d+\\]|\\d+|\\d{4,4}年|<\\d{4,4}年公司新闻>";
	private String listpaget="\\[\\d+\\]|\\d{4,4}年|<\\d{4,4}年公司新闻";
	private String all=firstpage+"|"+nextpage+"|"+lastpage+"|"+listpage;
	private String allt=firstpage+"|"+nextpage+"|"+lastpage;
	public analyzer()
	{
		XPath xpathEngine=XPathFactory.newInstance().newXPath();
		String urltxtpath_conf="/config/UrlTxtPath/text()";
		String innerurltxtpath_conf="config/InnerUrlTxtPath/text()";
		InputSource xmlSource=new InputSource(configpath);//假设XML文件路径为
		
		try {
			urltxtlpath=xpathEngine.evaluate(urltxtpath_conf,xmlSource);
			innerurltxtpath=xpathEngine.evaluate(innerurltxtpath_conf,xmlSource);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
	}
	
	public void test()
	{
		try {
			Parser parser=new Parser("http://yjsgzdw.shu.edu.cn/Default.aspx?tabid=16020&ctl=Detail&mid=28666&Id=44959&SkinSrc=%5BL%5DSkins/yjsdj_1fen/yjsdj_1fen");
			System.out.println(parser.getEncoding());
			parser.setEncoding("gb2312");
			ArrayList<String> u;
			ArrayList<String> title=new ArrayList<String>();
			/*u=get_url(parser,title);
			for(int i=0;i<title.size();i++)
			{
				System.out.println(title.get(i));
				System.out.println(u.get(i));
			}*/

			NodeFilter aFilter =new TagNameFilter("a");
			NodeList aList = (NodeList) parser.parse(aFilter);
			int startp=aList.elementAt(0).getStartPosition();
			int endp=aList.elementAt(aList.size()-1).getStartPosition()+aList.elementAt(aList.size()-1).toHtml().length();
			//提取url
			parser.reset();
			for (int i = 0; i < aList.size(); i++) {
				LinkTag n = (LinkTag) aList.elementAt(i);

				System.out.println("link:"+n.extractLink());	
				System.out.println("link:"+n.getLinkText().replaceAll("\\s+", ""));	
			}
			
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	}
	
	
	/*
	 * 分析strUrl并提取其中的新闻Url
	 * 并保存到数据库中
	 */
	public void parserUrl(String html,siteBean sb)
	{
		//	System.out.println("#######################################################");
		//	System.out.println("分析strUrl并提取其中的新闻Url:"+sb.getSite_url());
		ArrayList<String> url=new ArrayList<String>();
		ArrayList<String> title= new ArrayList<String>();
		ArrayList<String> date= new ArrayList<String>();
		
		String sql="";
		boolean notitleflag = false;//默认有title,false
		DataBaseConnect dbc;
		dbc=new DataBaseConnect(configpath);
		try
		{	
	//		System.out.println(html);
			Parser parser=new Parser();
			parser.setInputHTML(html);
			
			PrototypicalNodeFactory p = new PrototypicalNodeFactory();
			p.registerTag(new TagFont());
			parser.setNodeFactory(p);
			
//			parser.setURL(sb.getSite_url());
//			parser.setEncoding("utf-8");
			int startp=0,endp=0;
			/*
			 * sitebranch表示在普通新闻种子网站的种子网站查找种子网站的标签（如专题，其中每一个链接都是一个种子网站）
			 * sitebranch有三个元素
			 * 第一个为要提取的html标签
			 * 第二个为要提取的html标签的属性
			 * 第二个为要提取的html标签的属性的值
			 */
			String sitebranch=sb.getSitebranch();
			if(!sitebranch.equals("@@"))
			{
				System.out.println("提取种子网站开始！");
				System.out.println(sitebranch);
				String branchs[]=sitebranch.split("@");
				if(branchs.length>1)
				{
					NodeFilter andFilter=new AndFilter(new TagNameFilter(branchs[0]),new HasAttributeFilter(branchs[1],branchs[2]));
					NodeList seeds=parser.parse(andFilter);
					if(seeds==null)
					{
						System.out.println("种子网站提取为空！");
						return;
					}
					System.out.println("种子网站大小："+seeds.size());
					startp=seeds.elementAt(0).getStartPosition();
					endp=seeds.elementAt(seeds.size()-1).getStartPosition()+seeds.elementAt(seeds.size()-1).toHtml().length();
					parser.reset();
					System.out.println("中间");
					url=get_url(parser,startp,endp,title,notitleflag,"");//暂时性处理****************************还没有判断过！！！
					System.out.println("url的大小:"+url.size());
					if(url.size()==0&&html!=null&&!html.equals(""))
					{
						/*DataBaseConnect dbcon=new DataBaseConnect();
						dbcon.executeStatusUpdate(sb.getSite_id());
						dbcon.closeConnection();*/
						BufferedReader br=new BufferedReader(new FileReader(new File("exception.log")));
					  String bwcontent="";
					  String aLine=br.readLine();
					  while(aLine!=null)
					  {
						  bwcontent+=aLine;
						  aLine=br.readLine();
					  }
					  br.close();
					  if(!bwcontent.contains(sb.getSite_url()))
					  {
					  	BufferedWriter bw=new BufferedWriter(new FileWriter(new File("exception.log"),true));
						  bw.write(sb.getSite_url()+"可能已经改版.\r\n");
						  bw.flush();
						  bw.close();
						}
					}
					for(int i=0;i<url.size();i++)
					{
						System.out.println(url.get(i));
						System.out.println(title.get(i));
					}
					url=linktToUrl(url,sb.getSite_url());
					System.out.println("*");
					for(int i=0;i<url.size();i++)
						System.out.println(url.get(i));
					for(int i=0;i<url.size();i++)
					{
						sql="insert into innernewssite values(innernewssite_id.nextval,'"+url.get(i)+"','','"+title.get(i)+"',0,'@@','"+sb.getNewslist()+"','"+sb.getTitle()+"','"+sb.getDate()+"','"+sb.getContent()+"')";
						while(!dbc.getConnection())
						{
							dbc.startConnection();
						}
						System.out.println("专题链接:"+dbc.insert(sql));
					}
				}
				return;
			}
			/*
			 * newslist有三个元素
			 * 第一个为要提取的html标签
			 * 第二个为要提取的html标签的属性
			 * 第二个为要提取的html标签的属性的值
			 */
			//System.out.println(sb.getNewslist());
			
			String newslistpath = sb.getNewslist();
			if(newslistpath.startsWith("notitle")){//无直接标题的种子网站(当然也指那些链接字存在异常的网站，不进行提取)***************************
				notitleflag = true;
				newslistpath = newslistpath.substring(8);
				System.out.println("newslistpath:"+newslistpath);
			}
			String newslist[]=newslistpath.split("@");
			for(int w=0;w<newslist.length;w++)
				System.out.println(newslist[w]);
			System.out.println("*");
			NodeFilter tableFilter =new AndFilter(new TagNameFilter(newslist[0]), new HasAttributeFilter(newslist[1], newslist[2]));
			
			NodeList newsurlList = parser.parse(tableFilter);
			System.out.println("**");
			
			if((newsurlList==null||newsurlList.size()==0)&&html!=null&&!html.equals(""))
			{
				BufferedReader br=new BufferedReader(new FileReader(new File("exception.log")));
				String bwcontent="";
				String aLine=br.readLine();
				while(aLine!=null)
				{
					bwcontent+=aLine;
					aLine=br.readLine();
				}
				br.close();
				if(!bwcontent.contains(sb.getSite_url()))
				{
				  BufferedWriter bw=new BufferedWriter(new FileWriter(new File("exception.log"),true));
				  bw.write(sb.getSite_url()+"可能已经改版.\r\n");
				  bw.flush();
				  bw.close();
				}
				System.out.println("链接提取为空！");
				return ;
			}
			System.out.println(newsurlList.size());
		    
			startp=newsurlList.elementAt(0).getStartPosition();
			
			endp=newsurlList.elementAt(newsurlList.size()-1).getStartPosition()+newsurlList.elementAt(newsurlList.size()-1).toHtml().length();
			//提取url
			parser.reset();
			System.out.println("&");
			String host = sb.getSite_url();
			URL tempurl = new URL(host);
			System.out.println(tempurl.getHost());
			if((!tempurl.getHost().equals("10.67.53.20")) && (!tempurl.getHost().equals("10.66.5.106"))){
				System.out.println("非环保处/劳资处");
				url=get_url(parser,startp,endp,title,notitleflag,"");
			}else{//对于“环保处”/“劳资处”网站特殊处理**************************
				System.out.println("环保处/劳资处");
				if(tempurl.getPort()==-1){//无端口
					url=get_url(parser,startp,endp,title,notitleflag,tempurl.getHost());
				}else{
					url=get_url(parser,startp,endp,title,notitleflag,tempurl.getHost() + ":" + tempurl.getPort());	
				}
			}
			
			System.out.println("url的大小:"+url.size());
			if((url.size()==0)&&html!=null&&!html.equals(""))
			{
				BufferedReader br=new BufferedReader(new FileReader(new File("exception.log")));
				String bwcontent="";
				String aLine=br.readLine();
				while(aLine!=null)
				{
					bwcontent+=aLine;
					aLine=br.readLine();
				}
				br.close();
				if(!bwcontent.contains(sb.getSite_url()))
				{
			  	BufferedWriter bw=new BufferedWriter(new FileWriter(new File("exception.log"),true));
				  bw.write(sb.getSite_url()+"可能已经改版.\r\n");
				  bw.flush();
				  bw.close();
				}
			}
			if(title!=null && title.size()>0){
				System.out.println("title的大小：" + title.size());
				for(int i=0;i<url.size();i++)
				{
					System.out.println(url.get(i));
					System.out.println(title.get(i));
				}
			}else{
				System.out.println("该种子网站在新闻列表不提取链接字");
			}
			
			//先把一些js链接过滤掉！！！！
			if(url!=null && title!=null && url.size()==title.size()){
				System.out.println("url size和title size一致,大小为" + url.size());//******************************修改为“一致”
				for(int i=0;i<url.size();i++){
					if(url.get(i).contains("javascript")){
						String a = url.remove(i);
						String b = title.remove(i);
						System.out.println("remove js link:");
						System.out.println(a + "*****************" + b);
						i--;
						System.out.println("temp urlsize:" + url.size());
					}
				}
				
				//对于销售事业部-新闻动态，“http://10.66.22.98/xiaoshou/news/newswin.asp?id=20756&typename1=鍏徃鍔ㄦ？ target=”
				//类型的URL进行缩减处理
				if(new URL(sb.getSite_url()).getHost().equals("10.66.22.98")){
					ArrayList<String> tempurllist = new ArrayList<String>();
					for(int i=0;i<url.size();i++){
						if(url.get(i).contains("typename1")){
							url.set(i, url.get(i).replaceAll("&typename1.*", ""));
						}
					}
				}
			}
			
			url=linktToUrl(url,sb.getSite_url());
			parser.reset();
			System.out.println("***************最终获得的URL************");
			for(int i=0;i<url.size();i++)
				System.out.println(url.get(i));
			

			String d =sb.getDate();
			String []filter =d.split("@");
			//提取新闻的日期
			
			if( filter.length-1>0 && filter[filter.length-1].equals("site"))
			{
				date=getall_al(d,parser,startp,endp);
			}
			int n=url.size();
			System.out.println("先期提取的日期："+date.size());
			
			DateFormat df = new DateFormat();
			
			//过滤得到正确格式的日期，如果在前期得到的date无效则删除
			String tempdate = null;
			for(int k=0;k<date.size();k++){
				tempdate = df.dateformat(date.get(k),true);
				if(tempdate == null){
					date.remove(k);
					k--;
				}else{
					date.set(k, tempdate);
				}
				//date.set(k, df.dateformat(date.get(k),true));
			}
			
			System.out.println("过滤后的日期数：" + date.size());
			for(int i=0;i<date.size();i++)
			{
				System.out.println(date.get(i));
			}
			
			//分析出的url和日期插入数据库
			if(url.size()==date.size())
			{
				for(int i=0;i<n;i++)
				{
					String date1=date.get(i);
					//DateFormat df= new DateFormat();
					//date1=df.dateformat(date.get(i),false);
					if(title!=null){
						if(title.size()!=url.size()){//title数量与URL数量不一致，则不插入title，默认设置title为空串
							sql="insert into innernewsurl (URL_ID,URL,TITLE,SITE_ID,URL_UPDATE,FIRST_CRAWLER_TIME,CREATE_TIME) values(innernewsurl_id.nextval,'"+url.get(i)+"','',"+sb.getSite_id()+",0,sysdate,to_date('"+date1 + "','yyyy-mm-dd'))";
						}else{
							sql="insert into innernewsurl (URL_ID,URL,TITLE,SITE_ID,URL_UPDATE,FIRST_CRAWLER_TIME,CREATE_TIME) values(innernewsurl_id.nextval,'"+url.get(i)+"','"+title.get(i)+"',"+sb.getSite_id()+",0,sysdate,to_date('"+date1 + "','yyyy-mm-dd'))";
						}
					}
					while(!dbc.getConnection())
					{
						dbc.startConnection();
					}
					System.out.println("有date:"+dbc.insert(sql));
				}
			}else if(url.size()!=date.size()){
				for(int i=0;i<n;i++)
				{
					if(title!=null){
						if(title.size()!=url.size()){//title数量与URL数量不一致，则不插入title，默认设置title为空串
							sql="insert into innernewsurl (URL_ID,URL,TITLE,SITE_ID,URL_UPDATE,FIRST_CRAWLER_TIME) values(innernewsurl_id.nextval,'"+url.get(i)+"','',"+sb.getSite_id()+",0,sysdate)";
						}else{
							sql="insert into innernewsurl (URL_ID,URL,TITLE,SITE_ID,URL_UPDATE,FIRST_CRAWLER_TIME) values(innernewsurl_id.nextval,'"+url.get(i)+"','"+title.get(i)+"',"+sb.getSite_id()+",0,sysdate)";
						}
					}
					while(!dbc.getConnection())
					{
						dbc.startConnection();
					}
					System.out.println("无date："+dbc.insert(sql));
				}
			}
			dbc.closeConnection();
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	/*
	 * 根据link返回url
	 * 
	 * 1.本身就是url返回：本身
	 * 2./oil/html/oil-0933093385426887.html返回：域名+/oil/html/oil-0933093385426887.html
	 * 3.NewsShow.Asp?Id=1538返回：当前目录上级+NewsShow.Asp?Id=1538
	 */
	public String linktToUrl(String link,String mainurl)
	{
		String url=new String();
		
		try
		{
			URI base=new URI(mainurl);
			URI abs=base.resolve(link.replaceAll("&amp;","&"));
			URL absURL=abs.toURL();
			url=absURL.toString();
			System.out.println(url);
		}catch(Exception e){
				//e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
		}	
		
		return url;
		/*String u=link;
		if(u.startsWith("http"))
		{	
			url=link;
		}
		else if(u.startsWith("/"))
		{
			URL gurl;
			try {
				gurl = new URL(mainurl);
				String hostname= "http://"+gurl.getHost();//得到域名
				url=(hostname+link);
			} catch (MalformedURLException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			} 
		}
		else
		{
			String hostname=mainurl.substring(0, mainurl.lastIndexOf("/")+1);
			url=(hostname+link);
		}
		
		return url;*/
		
	}
	/*
	 * 根据link返回url
	 * 
	 * 1.本身就是url返回：本身
	 * 2./oil/html/oil-0933093385426887.html返回：域名+/oil/html/oil-0933093385426887.html
	 * 3.NewsShow.Asp?Id=1538返回：当前目录上级+NewsShow.Asp?Id=1538
	 */
	public ArrayList<String> linktToUrl(ArrayList<String> link,String mainurl)
	{
		ArrayList<String> url=new ArrayList<String>();
		
		try
		{
			URI base=new URI(mainurl);
			System.out.println("解析URL中");
			for(int i=0;i<link.size();i++)
			{
				//System.out.println(link.get(i));
				URI abs=base.resolve(link.get(i).replaceAll("&amp;","&"));
				URL absURL=abs.toURL();
				url.add(absURL.toString());
				//System.out.println(absURL.toString());
			}
		}catch(Exception e){
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
			if(link!=null && link.size()>0){//解析失败则原样返回（实际是针对“销售事业部-新闻动态”，不过后来这个也没用，把后面乱码去掉即可，此处暂时保留）
				return link;
			}
		}
		return url;
	}
	
	/*
	 * 参数s中有三个值
	 * 第一个为要提取的html标签
	 * 第二个为要提取的html标签的属性
	 * 第三个为要提取的html标签的属性的值
	 * 
	 * 返回pFilter下的全部内容
	 * 返回类型ArrayList<String>
	 */
	private ArrayList<String> getall_al(String s, Parser parser) {
		ArrayList<String> al =new ArrayList<String>();
	//	System.out.println("parser format:"+s);
	//	System.out.println("parser format:"+s.length());
		String filter[]=new String[3];
		NodeFilter pFilter=null;

		//即s=="@@"时，则返回空
		if(s==null||filter.length<3)
		{
			return null;
		}

		filter =s.split("@");
		pFilter =new AndFilter(new TagNameFilter(filter[0]), new HasAttributeFilter(filter[1], filter[2]));
		try {
			NodeList titleNodeList = (NodeList) parser.parse(pFilter);
			for (int i = 0; i < titleNodeList.size(); i++) {
				titleNodeList.elementAt(i);
				al.add(titleNodeList.elementAt(i).toPlainTextString());
			}
		} catch (ParserException ex) {
			System.out.println(ex.getMessage());
		}
		return al;
	}
	
	/*
	 * 参数s中有三个值
	 * 第一个为要提取的html标签
	 * 第二个为要提取的html标签的属性
	 * 第三个为要提取的html标签的属性的值
	 * 
	 * startp和endp为开始和结束提取的位置
	 * 
	 * 返回pFilter下的全部内容
	 * 返回类型ArrayList<String>
	 */
	private ArrayList<String> getall_al(String s, Parser parser,int startp,int endp) 
	{
		ArrayList<String> al =new ArrayList<String>();
		String filter[]=new String[4];
		NodeFilter pFilter=null;
		filter =s.split("@");
		//即s=="@@"时，则返回空
		if(s==null||filter.length<3)
		{
			return null;
		}
		
		filter =s.split("@");
		
		pFilter =new AndFilter(new TagNameFilter(filter[0]), new HasAttributeFilter(filter[1], filter[2]));
		try {
			NodeList aList = (NodeList) parser.parse(pFilter);
			for (int i = 0; i < aList.size(); i++) {
				if(aList.elementAt(i).getStartPosition()<startp)continue;
				if(aList.elementAt(i).getStartPosition()>endp)break;				
				al.add(aList.elementAt(i).toPlainTextString());
			}
		} catch (ParserException ex) {
			System.out.println(ex.getMessage());
		}
	
		return al;
	}
	/*
	 * 判断是不是跳转网页
	 * 
	 * 如果是则做出相应的处理
	 * 
	 * 标记该url为跳转网页
	 * 提取url并插入数据库
	 */
	
	public boolean isRefresh(String html,urlBean ub)
	{
		DataBaseConnect dbc;
		dbc=new DataBaseConnect(configpath);
		if(html.length()<50)
		{
			String sql="update innernewsurl set URL_UPDATE =3 where URL_ID="+ub.getUrl_id();
			while(!dbc.getConnection())
			{
				dbc.startConnection();
			}
			dbc.executeUpdate(sql);
			dbc.closeConnection();

			return true;
		}
		else if(html.length()<1000)
		{
			if(html.indexOf("refresh")!=-1)
			{
				String url="";
				String patternStr; 
				Pattern p;
				Matcher m;
//<meta http-equiv="refresh" content="0; url=http://events12.broadcastone.net/0857/20060823/s_default.htm">
				patternStr="http://[^\"]+"; 
				p = Pattern.compile(patternStr);
				m = p.matcher(html);

				if(m.find())
				{
					url=html.substring(m.start(), m.end());
					
//					String sql="insert into newsurl (URL_ID,URL,SITE_ID,URL_UPDATE,FIRST_CRAWLER_TIME) values(newssite_id.nextval,'"+url+"',"+ub.getSite_id()+",0,sysdate)";			
//					dbc.executeUpdate(sql);
				}
				String sql="update innernewsurl set URL_UPDATE =2 where URL_ID="+ub.getUrl_id();
				while(!dbc.getConnection())
				{
					dbc.startConnection();
				}
				dbc.executeUpdate(sql);
				dbc.closeConnection();

				return true;
			}
		}
		return false;
				
	}

	/*
	 * 分析新闻
	 * 
	 * 提取标题、日期和正文
	 * 
	 * 如果是跳转网页无法解析则返回0
	 * 如果下载的网页解析正常返回1
	 * 如果下载的网页按标签提取不到返回2
	 * 如果网页需要间址并且下载失败返回3
	 * 
	 */
	
	public int parserNews(String html,urlBean ub)
	{
		boolean tagfindflag=true;
		try
		{
			//System.out.println("分析新闻");
			Parser parser=new Parser();
			
			parser.setInputHTML(pretreatment(html));
			
			//注册FONT结点************************************************************
			PrototypicalNodeFactory p = new PrototypicalNodeFactory();
			p.registerTag(new TagFont());
			parser.setNodeFactory(p);
			
			String title=ub.getTitle();
			String titleflag=ub.getTitleflag();
			String date=ub.getDate();
			System.out.println("前期的date："+date);
			String content="";
			String s="";

			/*
			 * 把得到的新闻标题、日期和正文写回urlBean ub中
			 * 用它来建立索引
			 */
//			分析提取新闻日期
			parser.reset();
			if(date==null)
			{
				s=ub.getTime();
				DateFormat df= new DateFormat();
				if(s.split("@").length==3)
				{
					date=getallText(s,parser);
					System.out.println("date中间:"+date);
					date=df.dateformat(date,false);
				}
				else
				{
					date=df.GetDate();
				}
				ub.setDate(date);
			}
			
			System.out.println("date:"+s);
			System.out.println("TITLE:"+title);
			System.out.println("DATE:"+date);
			
			//分析提取新闻标题
			
			System.out.println("**********************titleflag******************" + titleflag);
			if(!titleflag.equals(""))
			{
				parser.reset();
				String str=getallText(titleflag,parser);
				System.out.println("**********************title******************" + str);
				//if((str!=null)&&!str.equals("")&&str.length()<title.length()*1.5)//此处修改，不限定一定要比链接字长
				//0.8的系数主要是有个异常现象，因为在链接字有些后面用"..."表示，但是正好又只是省略了1-2个字符，导致出现标题是...
				if((str!=null)&&!str.equals("")&&str.length()>title.length()*0.8 && str.length()<title.length()*1.5)
				{
					title=str;
				}else if((str!=null)&&!str.equals("") && (title==null || title.equals(""))){//对于那些从新闻列表中无法提取链接字的标题的处理
					title=str;
				}
			}
			ub.setTitle(title);//*************更新title
			
//			分析提取新闻正文
			parser.reset();
			parser.setInputHTML(html);
			s=ub.getContent();
			String[] indirectlabels=s.split("!");
			int indirectlength=indirectlabels.length;
			for(int i=0;i<indirectlength-1;i++)
			{
				down=false;
				String newslist[]=indirectlabels[i].split("@");
				
				System.out.println(indirectlabels[i]);
				NodeFilter tableFilter =new AndFilter(new TagNameFilter(newslist[0]), new HasAttributeFilter(newslist[1], newslist[2]));
		    	
				NodeList aList = (NodeList) parser.parse(tableFilter);
				System.out.println(aList.size());
				int startp=aList.elementAt(0).getStartPosition();
				int endp=aList.elementAt(aList.size()-1).getStartPosition()+aList.elementAt(aList.size()-1).toHtml().length();
				System.out.println("startp:"+startp);
				System.out.println("endp:"+endp);
				System.out.println(aList.toHtml());
				NodeFilter aFilter =new TagNameFilter("script");
				parser.reset();
			    aList = (NodeList) parser.parse(aFilter);
			    System.out.println(aList.size());
			    
				//提取url，间接二次寻址的网页，网页内容是嵌套另一个网页。
				String indirecturl="";
				for (int j= 0; j < aList.size(); j++)     //原则上只能有一个元素存在，否则是提取出错
				{
					
					if(aList.elementAt(j).getStartPosition()<startp) continue;
					if(aList.elementAt(j).getEndPosition()>endp) break;
					System.out.println(aList.elementAt(j).toHtml());
					ScriptTag st=(ScriptTag)aList.elementAt(j);
					indirecturl=st.getAttribute("src");
				}
				indirecturl=linktToUrl(indirecturl,ub.getUrl());
				System.out.println("中间提取的url："+indirecturl);
				html=geturlcontent(indirecturl);
				System.out.println("中间网页内容："+html);
				if(down)
				{
					parser.setInputHTML(html);
				}
				else
				{
					System.out.println("download error finished.");
					return 3;
				}
				
			}
			
			parser.setInputHTML(pretreatment(html));
			System.out.println(indirectlabels[indirectlength-1]);
			System.out.println(indirectlabels[indirectlength-1].indexOf("site"));
			if(indirectlabels[indirectlength-1].indexOf("site")!=-1)
			{
				String[] ss=indirectlabels[indirectlength-1].split("@");
				if(ss.length==3)
				{
					content=getallText(ss[0],parser);
					if(content.trim().equals(""))
					{
						content=getallText(ss[1],parser);
					}
				}
			}
			else
			{
				content=getallText(indirectlabels[indirectlength-1],parser);
			}
			
			//在此处判断能否提取成功，若不，设置status=1；对于要用替换的要一直提示，查看能否查找到提取的标签
			content=processcontent(content).trim();
			
			System.out.println("content1:"+content.length()+":"+content);
			
			if(content.equals(""))
			{
				DataBaseConnect dbc=new DataBaseConnect();
				System.out.println(dbc.getConnection());
				tagfindflag=false;
				String sqls="select count(*) from innernewsurl where site_id="+ub.getSite_id()+" and url_update=4 and to_char(first_crawler_time,'yyyymmdd')=to_char(sysdate-1,'yyyymmdd')";
				while(!dbc.getConnection())
				{
					dbc.startConnection();
				}
				ResultSet rs=dbc.executeQuery(sqls);
				rs.next();
				int nofind=rs.getInt(1);
				sqls="select count(*) from innernewsurl where site_id="+ub.getSite_id()+" and to_char(first_crawler_time,'yyyymmdd')=to_char(sysdate-1,'yyyymmdd')";
				while(!dbc.getConnection())
				{
					dbc.startConnection();
				}
				rs=dbc.executeQuery(sqls);
				rs.next();
				int total=rs.getInt(1);
				if(nofind>0.9*total)
				{
					BufferedReader br=new BufferedReader(new FileReader(new File("exception.log")));
					String bwcontent="";
					String aLine=br.readLine();
					while(aLine!=null)
					{
						bwcontent+=aLine;
						aLine=br.readLine();
					}
					br.close();
					if(bwcontent.contains(ub.getUrl()))
					{
						BufferedWriter bw=new BufferedWriter(new FileWriter(new File("exception.log"),true));
						bw.write(ub.getUrl()+"可能已经改版，来自于site_id:"+ub.getSite_id()+".\r\n");
						bw.flush();
					  bw.close();
					}
				}
				dbc.closeConnection();
				System.out.println("判定结束！");
				
				//这就是为什么url_update=4的网页虽然有内容，确实全文内容；这里如果没有获取内容，就把所有body内容都拿过来做content
				content=getallText("body",parser);
				content=processcontent(content).trim();
				System.out.println("content2:"+content.length()+":"+content);
			}
//			去除残留的html标签
			
		
			String uburl=ub.getUrl();
			if(content.equals("")&&!(uburl.endsWith("pdf")||uburl.endsWith("wmv")||uburl.endsWith("swf")||uburl.endsWith("avi")))
			{
				html="<body>"+html+"</body>";
				parser.setInputHTML(html);
				content=getallText("body",parser);
				content=content.replace(indirectlabels[indirectlength-1],"");
				content=processcontent(content).trim();
				System.out.println("content3:"+content.length()+":"+content);
			}
			
			
		
			
			ub.setContent(content);
			if(content.trim().equals("")||content.trim().length()<30)
			{
				return 0;
			}
			
			
			s=title+ System.getProperty("line.separator")+date+ System.getProperty("line.separator")+content;

			try{
				FileOperate fo=new FileOperate();
				String path=urltxtlpath+"/"+ub.getFirst_crwaler_time()+"/";
				fo.newFolder(path);
				String Abspath=path+ub.getUrl_id()+ ".txt";
				fo.newFile(Abspath,s);
				/*
				 * 此处同时完成把txt写入endtxt中*******************************************************************************
				 * 同时如果已经有的，则会进行覆盖（替换）
				 */
				/**
				 * 先把内网注释测试
				 */
				/*if(ub.getInner_url_id()!=-1)
				{
					path=innerurltxtpath + ub.getInner_first_crawler_time()+"/";
					fo.newFolder(path);
					Abspath=path+ub.getInner_url_id()+ ".txt";
					System.out.println("替换内网的txt路径:" +Abspath);
					//覆盖原先内网的TXT
					String contents=title+"*********"+content;
					fo.newFile(Abspath,contents);
				}*/
				
			}catch (Exception e){
				System.out.println(e.getMessage());
			}
		}catch (Exception e){	
			System.out.println(e.getMessage());
		}
		if(tagfindflag)
			return 1;
		else
			return 2;
	}
	
	/*
	 * 参数s中有三个值
	 * 第一个为要提取的html标签
	 * 第二个为要提取的html标签的属性
	 * 第二个为要提取的html标签的属性的值
	 * 
	 * 返回pFilter下的全部内容
	 */
	private String getallText(String s, Parser parser) {
		String text = "";
	//	System.out.println("parser format:"+s);
	//	System.out.println("parser format:"+s.length());
		String filter[];
		NodeFilter pFilter=null;

		//即s=="@@"时，则返回空
		filter =s.split("@");
		if(s==null||filter.length<3)
		{
			if(filter.length==0)
				return "";
			pFilter=new TagNameFilter(filter[0]);
		}else
		{
			pFilter =new AndFilter(new TagNameFilter(filter[0]), new HasAttributeFilter(filter[1], filter[2]));
		}
		parser.reset();
		try {
			NodeList titleNodeList = (NodeList) parser.parse(pFilter);
			if(titleNodeList==null)return "";
			for (int i = 0; i < titleNodeList.size(); i++) {
				titleNodeList.elementAt(i);
				text += (titleNodeList.elementAt(i).toPlainTextString()+" ");
			}
		} catch (ParserException ex) {
			
			System.out.println(ex.getMessage());
			return "";
		}
		return text;
	}
	/*
	 * 去除htmlparser返回的正文中
	 * 的残留的html标签和多余的空格
	 */
	public String  processcontent(String content)
	{
		content=content.replaceAll("\u3000"," ");
		content=content.replaceAll("(?u)&nbsp;|&ldquo;|&gt;|&rdquo;|&mdash;", "");
		//替换所有空白字符
		content=content.replaceAll("\\s+", "  ");
		//针对新闻网页主页  除去其中的日期
		content=content.replaceAll("(\\s+?\\p{Punct}?\\d{2,4}-\\d{1,2}-\\d{1,2}\\p{Punct}?){2,}", "");
//		针对新闻网页主页  除去其中的第几页
		content=content.replaceAll("(\\s+?第\\s?\\d{1,4}\\s?页){2,}", "");
		content=filtersymbol(content);
		return content;
	}
	
	/*
	 * 去除一些无用的且有规律的html标签如&nbsp;等
	 */
	public static String filtersymbol(String str)
	{
		int semicolon=str.indexOf(";");
		while(semicolon>=0)
		{
			int start=semicolon;
			boolean stop=false;
			for(int i=semicolon;i>=0&&!stop;i--)
			{
				int code=str.codePointAt(i);
				if(code<1000)
				{
					start=i;
					if(code==38)
						stop=true;
				}
					
			}
			if(stop)
			{
				//System.out.println(start+"*"+semicolon);
				String replaced=str.substring(start,semicolon+1);
				str=str.replace(replaced,"");
				semicolon=str.indexOf(";");
			}
			else
			{
				semicolon=str.indexOf(";",semicolon+1);
			}
		}
		
		return str;
	}
	/*
	 * 参数网页内容 
	 * 
	 * 返回去除<script>标签和注释后的内容
	 */
	public String pretreatment(String html)
	{
		// <!-- .STYLE1 {color: #FFFFFF} -->
		html=html.replaceAll("(?u)<!--[\\s\\S]*?-->", "");//去除注释
		html=html.replaceAll("(?u)<script[\\s\\S]*?</script>", "");//去除script
		html=html.replaceAll("(?u)<style[\\s\\S]*?</style>", "");//去除style
		
		//<?xml:namespace prefix="st1" ns="urn:schemas-microsoft-com:office:smarttags" /> 
		html=html.replaceAll("(?u)<\\?\\s?xml[\\s\\S]*?/>", "");
		
//		html=html.replaceAll("<(?u)\\s*/?tbody\\s*>", "");//去掉htmlparser不能解析的标签tbody
		return html;
	}
	/*
	 * 参数网页内容 
	 * 
	 * 返回去除<a>标签后的内容
	 */
	public String remove_a(String html)
	{
		return html.replaceAll("(?u)<a [\\s\\S]*?</a>", "");
	}
	

	/*
	 * 提取网页中所有url
	 */
	public ArrayList<String> get_url(Parser parser)
	{
		 ArrayList<String> url=new ArrayList<String>();
		 

		try {
			parser.reset();
			NodeFilter aFilter =new TagNameFilter("a");
			NodeList aList = (NodeList) parser.parse(aFilter);
			for (int i = 0; i < aList.size(); i++) {
				LinkTag n = (LinkTag) aList.elementAt(i);
				url.add(n.extractLink());
				System.out.println("link:"+n.extractLink());	
			}
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		 return url;
	}
	/*
	 * 提取网页中所有url
	 */
	public ArrayList<String> get_url(Parser parser,ArrayList<String> title)
	{
		ArrayList<String> url=new ArrayList<String>();
		
		try {
			parser.reset();
			NodeFilter aFilter =new TagNameFilter("a");
			NodeList aList = (NodeList) parser.parse(aFilter);
			for (int i = 0; i < aList.size(); i++) {
				LinkTag n = (LinkTag) aList.elementAt(i);
				url.add(n.extractLink());
				title.add(n.getLinkText());
				//System.out.println("link:"+n.extractLink());	
			}
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		 return url;
	}
	/*
	 * 提取网页中
	 * int startp和int endp之间的url
	 */
	public ArrayList<String> get_url(Parser parser,int startp,int endp,ArrayList<String> title,boolean notitleflag,String host)
	{

		ArrayList<String> url=new ArrayList<String>();
		try {
			parser.reset();
			NodeFilter aFilter =new TagNameFilter("a");
			NodeList aList = (NodeList) parser.parse(aFilter);
			for (int i = 0; i < aList.size(); i++) {
				if(aList.elementAt(i).getStartPosition()<startp)continue;
				if(aList.elementAt(i).getEndPosition()>endp)break;
				LinkTag n = (LinkTag) aList.elementAt(i);
				
				String titles=n.getLinkText();
				System.out.println("过滤前TITLE："+titles);
				titles=titles.replaceAll("(?u)&nbsp;|&ldquo;|&gt;|&rdquo;|&mdash;|&rsaquo;", "");
				titles=titles.replaceAll("\\s+", "");
				//针对新闻网页主页  除去其中的日期
				titles=titles.replaceAll("(\\s+?\\p{Punct}?\\d{2,4}-\\d{1,2}-\\d{1,2}\\p{Punct}?){2,}", "");
				titles=titles.replaceAll("\\[[^]]+\\]", "");
				titles=titles.replaceAll("\\d{2,4}.\\d{1,2}.\\d{1,2}", "");
				titles=titles.replaceAll(allt,"").replaceAll("Go","");
				if(titles.contains("废油资讯-"))                        //针对”废油资讯-新闻中心“不规范网页的过滤
				{
					titles="";
				}
				else if(!titles.equals("")&&titles.charAt(0)=='·')
				{
					titles=titles.substring(1);
				}
				/**
				 * 过滤纯粹数字的连接字（主要是针对提取到数字页链接，其他很少有用数字做链接字）
				 */
				Pattern numberPattern = Pattern.compile("\\d{1,6}");//扩大到5位数
				Matcher numberMatcher = numberPattern.matcher(titles);
				if(numberMatcher.matches()){
					titles = "";
				}
				//对于title太短的情况，直接删除该url，因为很可能能是一个符号，新闻标题不会是这样
				if(titles.length()<=1){
					titles = "";
				}
				System.out.println("过滤后TITLE："+titles);
				
				if(!titles.trim().equals(""))
				{
					String strurl=n.extractLink();
					if (strurl != null && !strurl.equals("")) {
						System.out.println("host:" + host);
						if(host!=null && host.equals("10.67.53.20:81")){//如果包含host（默认需要进行JS链接拼接为URL）
							String onclick = n.getAttribute("onclick");
							Pattern p = Pattern.compile("/HSE/EFileInfo.aspx.*FromType=0");
							Matcher matcher = p.matcher(onclick);
							if(matcher.find()){
								String path = onclick.substring(matcher.start(),matcher.end());
								System.out.println(path);
								strurl = "http://" + host + path;
							}
						}else if(host!=null && host.equals("10.66.5.106")){
							Pattern p = Pattern.compile("/labour.*opendocument");
							Matcher matcher = p.matcher(strurl);
							if(matcher.find()){
								String path = strurl.substring(matcher.start(),matcher.end());
								strurl = "http://" + host + path;
							}
						}
					}
					url.add(strurl);
					if(notitleflag){//如果是有notitle标记的，说明此处的标题因为一些异常，舍弃不要！
						continue;
					}
					title.add(titles);			
				}else{
					if((notitleflag)){//对于不提取或者提取不到链接字的URL的提取
						String strurl = n.extractLink();
						if (strurl != null && !strurl.equals("")) {
							url.add(strurl);
						}
					}
				}
				
			}
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}

		return url;
	}
	

	public String geturlcontent(String strUrl)
	{
		//strUrl="http://www.blogjava.net/sinoly/archive/2007/12/27/148046.html";
		//System.out.println(strUrl);
		String charset="";
		String html="";

			
		URL url;
		try {
			url = new URL(strUrl);
			
			URLConnection uc=url.openConnection();
			uc.setReadTimeout(40000);
			uc.setConnectTimeout(6000);
			uc.connect();
			// 调用CheckCharset类判断网页字符集

			BufferedInputStream bis = new BufferedInputStream(uc.getInputStream());
			
			  
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			byte [] content = new byte [1000];
			int size = 0;
			while((size = bis.read(content)) != -1){
				//System.out.println(size);
				bo.write(content,0,size);
			}

			byte[] lBytes = bo.toByteArray();

			/*
			 * 下载中不知道为什么会出现这样的
			 * 乱码，不加下面这个循环会出现
			 * 不能解析的字符而变成多余的问号
			 */
//			for(int k=3;k<lBytes.length;k++)
//			{
//				if(lBytes[k]==-62 && lBytes[k+1]==-96)
//				{
//					lBytes[k]=32;
//					lBytes[k+1]=32;
//				}
//			}
			
			if (CheckCharset.isValidUtf8(lBytes, lBytes.length))
			{
				charset = "utf-8";
			}
			else
			{
				charset = "gbk";
			}
			html=new String(lBytes,charset);	
			bo.close();
//			System.out.println(charset);
			charset = "gb2312";
			html = html.replaceAll("(?u)utf-8", charset).replaceAll("(?u)utf8",
					charset);
			down=true;
		} catch (MalformedURLException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}   


			
//		System.out.println(charset);
//		System.out.println(html);
		return html;
	}
	
	
	/*
	 * 返回html对应的body去掉a的内容
	 */
	public String getBody_a(String html)
	{
		String body="";
		
		NodeFilter htmlFilter =new TagNameFilter("body");
		
		try {
			Parser parser = new Parser();
			html=pretreatment(html);
			html=html.replaceAll("(?u)<a [\\s\\S]*?</a>", "");
		//	System.out.println(html);
			parser.setInputHTML(html);
			NodeList nodeList = parser.parse(htmlFilter);
			
			body=nodeList.elementAt(0).toPlainTextString();

			System.out.println(processcontent(nodeList.elementAt(0).toPlainTextString()));
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			System.out.println(e.getMessage());
		}
		body=processcontent(body);
		return body;
	}
	
	/*
	 * 返回html对应的body的内容
	 */
	public String getBody(String html)
	{
		String body="";
		
		NodeFilter htmlFilter =new TagNameFilter("body");
	//	System.out.println(html);
		try {
			Parser parser = new Parser();
			html=pretreatment(html);
		//	System.out.println(html);
			parser.setInputHTML(html);
			NodeList nodeList = parser.parse(htmlFilter);
			
			body=nodeList.elementAt(0).toPlainTextString();

			System.out.println(processcontent(nodeList.elementAt(0).toPlainTextString()));
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			System.out.println(e.getMessage());
		}
		body=processcontent(body);
		return body;
	}

	/*
	 * 根据url获得主域名
	 * 
	 */
	public String getmainhost(String url)
	{
		Pattern p = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)(\\.[^/]*)?",Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(url);
		matcher.find();
		System.out.println(matcher.group());

		return matcher.group();
	}
	
	public void p()
	{
		
		try {
			Parser parser=new Parser("http://www.petrochina.com.cn/PetroChina/xwygg/gsxw/中国石油与哈萨克斯坦国家石油公司完成购买曼格什套油气公司100％普通股股份的交易.htm");
			Parser model=new Parser("http://www.petrochina.com.cn/PetroChina/xwygg/gsxw/20080528.htm");
			parserNews(parser,model);
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	
	}
	
	public String parserNews(Parser parser,Parser model)
	{
		NodeFilter Filter =new TagNameFilter("body");
		
		try {
			NodeList nodeList = parser.parse(Filter);
			NodeList mnodeList = model.parse(Filter);
			for(int j=0;nodeList!=null;j++)
			{
				for(int i=0;nodeList!=null && i<nodeList.size();i++)
				{
					System.out.println("startstartstartstartstartstartstartstartstartstartstart");
					if(nodeList.elementAt(i).toPlainTextString().replaceAll("\\s+?", "").equals(mnodeList.elementAt(i).toPlainTextString().replaceAll("\\s+?", "")))
					{
//						nodeList.remove(i);
//						mnodeList.remove(i);
						
					}else
					{
						if(i>nodeList.size())break;
						if(nodeList.elementAt(i).getText().equals(nodeList.elementAt(i).toPlainTextString()))break;
						System.out.println(nodeList.elementAt(i).getText().equals(nodeList.elementAt(i).toPlainTextString()));
						System.out.println(nodeList.elementAt(i).toPlainTextString().replaceAll("\\s+?", ""));
						System.out.println("--------------------------------------------------");
						System.out.println(mnodeList.elementAt(i).toPlainTextString().replaceAll("\\s+?", ""));
						
						
						System.out.println("+++++++++++++++++++++++++++++++++++++++");
						
						break;
					}
				}
				System.out.println(nodeList.size());
				for(int i=0;i<nodeList.size();i++)
				{
					if(nodeList.elementAt(0).toPlainTextString().replaceAll("\\s+?", "").startsWith("<!--"))
						continue;
					System.out.println("i:"+i);
					nodeList=nodeList.elementAt(0).getChildren();
					mnodeList=mnodeList.elementAt(0).getChildren();
				}
				
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return"";
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		analyzer a= new analyzer();
		//a.test();
		a.p();
	}
		
}
