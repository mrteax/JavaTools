package resource.analyzer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import resource.crawler.Cookie;
import resource.crawler.FileOperate;
import resource.crawler.HtmlModify;
import resource.crawler.SinoDetect;
import resource.database.DataBaseConnect;

public class TestExtract {
	private String firstpage = "首页|上一页|\\|&lt;|&lt;&lt;";
	private String nextpage = "下一页|下页|&gt;&gt;";
	private String lastpage = "尾页|最后一页|末页|最后页|&gt;\\|";
	private String listpage = "\\[\\d+\\]|\\d+|\\d{4,4}年|<\\d{4,4}年公司新闻>";
	private String listpaget = "\\[\\d+\\]|\\d{4,4}年|<\\d{4,4}年公司新闻";
	private String all = firstpage + "|" + nextpage + "|" + lastpage + "|"
			+ listpage;
	private String allt = firstpage + "|" + nextpage + "|" + lastpage;

	private FileOperate fo = new FileOperate();

	/*
	 * 参数url 返回url对应的网页的内容
	 */
	public String[] geturlcontent(String strUrl, String cookieUrlStr) {
		String htmlcontent[] = new String[2];
		// strUrl="http://www.blogjava.net/sinoly/archive/2007/12/27/148046.html";
		// System.out.println(strUrl);
		String charset = "GB2312";
		String html = "";
		BufferedWriter bw = null;
		BufferedReader br = null;
		String bwcontent = "";
		try {
			br = new BufferedReader(new FileReader(new File("exception.log")));
			String aLine = br.readLine();
			while (aLine != null) {
				bwcontent += aLine;
				aLine = br.readLine();
			}
			br.close();
			bw = new BufferedWriter(new FileWriter(new File("exception.log"),
					true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		URL url;
		try {
			/*
			 * URL mainurl = new URL("http://10.67.12.202/zzbdj/index.asp");
			 * HttpURLConnection
			 * mainhuc=cookie.getReFreshHttpURLConnection(mainurl);
			 * 
			 * //HttpURLConnection mainhuc =
			 * (HttpURLConnection)mainurl.openConnection(); String maincookie =
			 * mainhuc.getRequestProperty("Cookie");
			 * System.out.println(maincookie); mainhuc.disconnect(); mainhuc =
			 * null;
			 */

			url = new URL(strUrl);
			String cookieVal = null;
			String sessionId = "";
			String key = null;

			SinoDetect detector = new SinoDetect();
			HttpURLConnection huc = null, cookieHuc = null;
			if (cookieUrlStr != null && !cookieUrlStr.equals("")) {
				URL cookieUrl = new URL(cookieUrlStr);
				cookieHuc = (HttpURLConnection) cookieUrl.openConnection();
				cookieHuc.setInstanceFollowRedirects(false);
				for (int i = 1; (key = cookieHuc.getHeaderFieldKey(i)) != null; i++) {
					if (key.equalsIgnoreCase("set-cookie")) {
						cookieVal = cookieHuc.getHeaderField(i);
						System.out.println("cookie:" + cookieVal);
						cookieVal = cookieVal.substring(0, cookieVal
								.indexOf(";"));
						sessionId = sessionId + cookieVal + ";";
					}
				}
				// 释放连接
				cookieHuc.disconnect();
				cookieHuc = null;

				System.out.println("sessionid:" + sessionId);

				
				huc = (HttpURLConnection) url.openConnection();
				huc.setConnectTimeout(8000);
				huc.setReadTimeout(40000);
				// connection.setRequestProperty("Cookie",
				// "ASPSESSIONIDSQTBBAAT=PDDCCOODOCHMBPFHNMOIAGOG");
				huc.setRequestProperty("Cookie", sessionId);
				huc.setRequestProperty(
								"User-Agent",
								"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Foxy/1; .NET CLR 2.0.50727; MEGAUPLOAD 1.0)");

				huc.setInstanceFollowRedirects(false);// 增加的

			} else {
				//对于胜利信息网-油田快讯进行特殊处理（因为URL会跳转，临时解决办法，这里用HOST,运行起来后用site_id）
				if(url.getHost().equals("10.67.12.228")){
					huc = (HttpURLConnection) url.openConnection();
					String redirecturl="";
					huc.setInstanceFollowRedirects(false);
					if(huc.getResponseCode()==HttpURLConnection.HTTP_MOVED_TEMP || 
							huc.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM){
						System.out.println(huc.getResponseCode());
						String location = huc.getHeaderField("Location");
						System.out.println("Location:  " +location);
						
						if(location.startsWith("http")){//如果是绝对地址，则进行跳转，否则还是按照原来去抓避免错误（暂时处理）
							url = null;
							huc.disconnect();
							huc = null;
							url = new URL(location);
						}
					}
				}
				huc = Cookie.getReFreshHttpURLConnection(url);
			}
			BufferedInputStream bis = new BufferedInputStream(huc
					.getInputStream());
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			byte[] content = new byte[1024];
			int size = 0;

			while ((size = bis.read(content)) != -1) {
				bo.write(content, 0, size);
			}
			byte[] lBytes = bo.toByteArray();
			if (lBytes.length != 0) {
				charset = detector.detectEncodingStr(lBytes);
			}

			if (charset.equalsIgnoreCase("other"))
				charset = "GB2312";

			System.out.println("****charset:" + charset);

			htmlcontent[0] = charset;

			html = new String(lBytes, charset);

			bo.close();
			// System.out.println(charset);
			// charset = "gb2312";
			html = html.replaceAll("(?u)utf-8", charset).replaceAll("(?u)utf8",
					charset).replaceAll("(?u)unicode", charset);
			HtmlModify hm = new HtmlModify();

			html = hm.urlModifying(html, strUrl);

			htmlcontent[1] = html;

			// System.out.println(html);

			// down=true;
		} catch (MalformedURLException e) {
			// TODO 自动生成 catch 块
			// 写入exception.log日志，url格式错误

			e.printStackTrace();
		} catch (UnknownHostException e) {
			// 写入exception.log日志，url已经废弃

			e.printStackTrace();
		} catch (ConnectException e) {
			// 连接超时，返回null，url_update=0
			e.printStackTrace();
			return null;
		} catch (SocketTimeoutException e) {
			// 读取超时，返回null，url_update=0
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			// 编码不支持,to do nothing,url_update=3
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// 写入exception.log日志，url已经废弃
			e.printStackTrace();
		} catch (Exception e) {
			// 写入exception.log日志，e.getLocalizedMessage();
			try {
				if (!bwcontent.contains(strUrl)) {
					bw.write(strUrl + "发生错误：" + e.getLocalizedMessage()
							+ "\r\n");
					bw.flush();

				}
				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return null; // 其它错误，返回null，url_update=0

		}
		return htmlcontent;
	}

	public String EncodeUrl(String url) {
		String regex = "[\u4e00-\u9fa5]";
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(url);
		StringBuffer sb = new StringBuffer();
		/*try {
			URL tempurl = new URL(url);
			System.out.println(tempurl.getFile());
			System.out.println(tempurl.getAuthority());
			System.out.println(tempurl.getHost());
			System.out.println(tempurl.getPath());
			System.out.println(tempurl.getProtocol());
			System.out.println(tempurl.getQuery());
			System.out.println(tempurl.getRef());
			System.out.println(tempurl.getUserInfo());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}*/
		int start = 0;
		int end = 0;
		String chinesestr = "";
		while (matcher.find()) {
			try {
				chinesestr = url.substring(matcher.start(), matcher.end());
				end = matcher.start();
				sb.append(url.substring(start, end));
				sb.append(URLEncoder.encode(chinesestr,"gbk"));
				start = end+1;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if(start<url.length()){
			sb.append(url.substring(start, url.length()));
		}
		url = sb.toString();
		//
		// System.out.println(sb.toString());
		/*try {
			url = url.replace(sb.toString(), URLEncoder.encode(sb.toString(),
					"utf-8"));
			//url = url.replace(sb.toString(), URLEncoder.encode(sb.toString(),"gb2312"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
		System.out.println("编码后的URL:" + url);
		return url;
	}

	/**
	 * 下载网页
	 * 
	 * @param url
	 * @param fileName
	 */
	public boolean getnewsHtml(String url, String cookieSite,String fileName) {

		url = EncodeUrl(url);
		String[] htmlcontent = geturlcontent(url, cookieSite);

		String filepath = "data/html/" + fileName;
		// fo.newFile(filepath, html);
		if (htmlcontent == null || htmlcontent[1] == null) {
			System.out.println(url + "下载失败！");
			return false;
		}
		fo.newFileEncoding(filepath, htmlcontent[1], htmlcontent[0]);
		return true;
	}

	public void extractNewsList(String html, String newslistpath,
			String datepath, String site_url) {

		ArrayList<String> url = new ArrayList<String>();
		ArrayList<String> title = new ArrayList<String>();
		ArrayList<String> date = new ArrayList<String>();

		Parser parser = new Parser();
		boolean notitleflag = false;//默认有title,false
		try {
			parser.setInputHTML(html);

			parser.setEncoding("GB2312");
			int startp = 0, endp = 0;
			
			if(newslistpath.startsWith("notitle")){
				notitleflag = true;
				newslistpath = newslistpath.substring(8);
				System.out.println(newslistpath);
			}
			String newslist[] = newslistpath.split("@");
			
			NodeFilter tableFilter = new AndFilter(new TagNameFilter(
					newslist[0]), new HasAttributeFilter(newslist[1],
					newslist[2]));

			NodeList newsurlList = parser.parse(tableFilter);

			System.out.println(newsurlList.size());

			// 确定urllist在html中的开始与结束位置，在这个区域提取A标签
			startp = newsurlList.elementAt(0).getStartPosition();
			endp = newsurlList.elementAt(newsurlList.size() - 1)
					.getStartPosition()
					+ newsurlList.elementAt(newsurlList.size() - 1).toHtml()
							.length();
			System.out.println(startp + ":" + endp);
			parser.reset();
			/*String tempurlstr="";
			try {
				URL tempurl = new URL("http://10.67.53.20:81/HSE/economy/economylist.aspx?OneDept=FA9B7CEF-B784-4AF2-80BB-D9FF00A542AE&TwoDept=4486CAA5-4D00-4356-B64D-BA984D61D312");
				tempurlstr = "http://" + tempurl.getHost() + ":" + tempurl.getPort();
				System.out.println(tempurlstr);
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}*/
			url = get_url(parser, startp, endp, title,notitleflag,"");// 获取URLs
			/*ArrayList<String> newlist = new ArrayList<String>();
			if(url!=null && url.size()!=0 && title!=null && title.size()!=url.size()){
				String item="";
				for(int i=0;i<url.size();i++){
					item = url.get(i);
					if(newlist.contains(item)){
						continue;
					}else{
						newlist.add(item);
					}
				}
			}*/
			
			System.out.println("url个数：" + url.size());
			System.out.println("title个数：" + title.size());
			//System.out.println("new url个数：" + newlist.size());
			
			if (url != null && url.size() != 0) {
				for (int i = 0; i < url.size(); i++) {
					System.out.println(url.get(i));
					if (title != null && title.size() != 0 && i<title.size()) {
						System.out.println(title.get(i));
					}
				}
			}

			//先把一些js链接过滤掉！！！！
			if(url!=null && title!=null && url.size()==title.size()){
				System.out.println("url size和title size一致");
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
			}
			
			
			
			// 转换URL为绝对路径
			if(site_url!=null){
				url = linktToUrl(url, site_url);
			}
			System.out.println("*****"+url.size());
			System.out.println("*****" + title.size());
			parser.reset();
			
			/*
			 * for(int i=0;i<url.size();i++){ System.out.println(url.get(i)); }
			 */

			String d = datepath;
			String[] filter = d.split("@");
			// 提取新闻的日期

			if (filter.length - 1 > 0
					&& filter[filter.length - 1].equals("site")) {
				System.out.println("日期先期提取中...");
				date = getall_al(d, parser, startp, endp);
			}
			System.out.println("先期提取的日期：" + date.size());
			DateFormat df = new DateFormat();
			
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
			 for(int i=0;i<date.size();i++) {
				 System.out.println(date.get(i));
			 }
			 

		} catch (ParserException e) {
			e.printStackTrace();
		}

	}

	/*
	 * 参数s中有三个值 第一个为要提取的html标签 第二个为要提取的html标签的属性 第三个为要提取的html标签的属性的值
	 * 
	 * startp和endp为开始和结束提取的位置
	 * 
	 * 返回pFilter下的全部内容 返回类型ArrayList<String>
	 */
	private ArrayList<String> getall_al(String s, Parser parser, int startp,
			int endp) {
		ArrayList<String> al = new ArrayList<String>();
		String filter[] = new String[4];
		NodeFilter pFilter = null;
		filter = s.split("@");
		// 即s=="@@"时，则返回空
		if (s == null || filter.length < 3) {
			return null;
		}

		filter = s.split("@");

		pFilter = new AndFilter(new TagNameFilter(filter[0]),
				new HasAttributeFilter(filter[1], filter[2]));
		try {
			NodeList aList = (NodeList) parser.parse(pFilter);
			for (int i = 0; i < aList.size(); i++) {
				if (aList.elementAt(i).getStartPosition() < startp)
					continue;
				if (aList.elementAt(i).getStartPosition() > endp)
					break;
				al.add(aList.elementAt(i).toPlainTextString());
			}
		} catch (ParserException ex) {
			System.out.println(ex.getMessage());
		}

		return al;
	}

	/*
	 * 根据link返回url
	 * 
	 * 1.本身就是url返回：本身
	 * 2./oil/html/oil-0933093385426887.html返回：域名+/oil/html/oil-0933093385426887.
	 * html 3.NewsShow.Asp?Id=1538返回：当前目录上级+NewsShow.Asp?Id=1538
	 */
	public ArrayList<String> linktToUrl(ArrayList<String> link, String mainurl) {
		ArrayList<String> url = new ArrayList<String>();

		try {
			URI base = new URI(mainurl);
			for (int i = 0; i < link.size(); i++) {
				URI abs = base.resolve(link.get(i).replaceAll("&amp;", "&"));
				URL absURL = abs.toURL();

				url.add(absURL.toString());
				System.out.println(absURL.toString());
			}
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}

		return url;
	}

	/*
	 * 参数网页内容
	 * 
	 * 返回去除<script>标签和注释后的内容
	 */
	public String pretreatment(String html) {
		// <!-- .STYLE1 {color: #FFFFFF} -->
		html = html.replaceAll("(?u)<!--[\\s\\S]*?-->", "");// 去除注释
		html = html.replaceAll("(?u)<script[\\s\\S]*?</script>", "");// 去除script
		html = html.replaceAll("(?u)<style[\\s\\S]*?</style>", "");// 去除style

		// <?xml:namespace prefix="st1"
		// ns="urn:schemas-microsoft-com:office:smarttags" />
		html = html.replaceAll("(?u)<\\?\\s?xml[\\s\\S]*?/>", "");

		// html=html.replaceAll("<(?u)\\s*/?tbody\\s*>",
		// "");//去掉htmlparser不能解析的标签tbody
		return html;
	}

	/**
	 * 提取日期
	 * 
	 * @param html
	 * @param datepath
	 */
	public void extractDate(String html, String datepath) {
		Parser parser = new Parser();
		DateFormat df = new DateFormat();
		String date = "";
		try {
			parser.setInputHTML(pretreatment(html));
			PrototypicalNodeFactory p = new PrototypicalNodeFactory();
			p.registerTag(new TagFont());
			parser.setNodeFactory(p);
			// 提取日期
			if (datepath.split("@").length == 3) {
				date = getallText(datepath, parser);
				System.out.println("date中间:" + date);
				date = df.dateformat(date,false);
			} else {
				date = df.GetDate();
			}

			System.out.println("DATE:" + date);
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 提取标题
	 * 
	 * @param html
	 * @param titlepath
	 */
	public void extractTitle(String html, String titlepath) {
		Parser parser = new Parser();
		try {
			parser.setInputHTML(pretreatment(html));

			PrototypicalNodeFactory p = new PrototypicalNodeFactory();
			p.registerTag(new TagFont());
			parser.setNodeFactory(p);

			String title = "";
			// 提取标题
			parser.reset();
			title = getallText(titlepath, parser);

			System.out.println("TITLE:" + title);

		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 提取正文内容
	 * 
	 * @param html
	 * @param url
	 * @param contentpath
	 */
	public void extractContent(String html, String url, String contentpath) {
		Parser parser = new Parser();
		try {
			parser.setInputHTML(pretreatment(html));
			PrototypicalNodeFactory p = new PrototypicalNodeFactory();
			p.registerTag(new TagFont());
			parser.setNodeFactory(p);

			String content = "";

			// 提取正文内容
			parser.reset();
			parser.setInputHTML(html);

			String[] indirectlabels = contentpath.split("!");
			int indirectlength = indirectlabels.length;
			for (int i = 0; i < indirectlength - 1; i++) {
				String newslist[] = indirectlabels[i].split("@");

				// System.out.println(indirectlabels[i]);
				NodeFilter tableFilter = new AndFilter(new TagNameFilter(
						newslist[0]), new HasAttributeFilter(newslist[1],
						newslist[2]));

				NodeList aList = (NodeList) parser.parse(tableFilter);
				System.out.println(aList.size());
				int startp = aList.elementAt(0).getStartPosition();
				int endp = aList.elementAt(aList.size() - 1).getStartPosition()
						+ aList.elementAt(aList.size() - 1).toHtml().length();
				System.out.println("startp:" + startp);
				System.out.println("endp:" + endp);
				System.out.println(aList.toHtml());
				NodeFilter aFilter = new TagNameFilter("script");
				parser.reset();
				aList = (NodeList) parser.parse(aFilter);
				System.out.println(aList.size());

				// 提取url，间接二次寻址的网页，网页内容是嵌套另一个网页。
				String indirecturl = "";
				for (int j = 0; j < aList.size(); j++) // 原则上只能有一个元素存在，否则是提取出错
				{

					if (aList.elementAt(j).getStartPosition() < startp)
						continue;
					if (aList.elementAt(j).getEndPosition() > endp)
						break;
					System.out.println(aList.elementAt(j).toHtml());
					ScriptTag st = (ScriptTag) aList.elementAt(j);
					indirecturl = st.getAttribute("src");
				}
				indirecturl = linktToUrl(indirecturl, url);
				System.out.println("中间提取的url：" + indirecturl);
				html = geturlcontent(indirecturl, null)[1];
				System.out.println("中间网页内容：" + html);

			}

			parser.setInputHTML(pretreatment(html));
			// System.out.println(indirectlabels[indirectlength-1]);
			// System.out.println(indirectlabels[indirectlength-1].indexOf("site"));
			if (indirectlabels[indirectlength - 1].indexOf("site") != -1) {
				String[] ss = indirectlabels[indirectlength - 1].split("@");
				if (ss.length == 3) {
					content = getallText(ss[0], parser);
					if (content.trim().equals("")) {
						content = getallText(ss[1], parser);
					}
				}
			} else {
				content = getallText(indirectlabels[indirectlength - 1], parser);
			}

			System.out.println("CONTENT:" + content);
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 根据link返回url
	 * 
	 * 1.本身就是url返回：本身
	 * 2./oil/html/oil-0933093385426887.html返回：域名+/oil/html/oil-0933093385426887.
	 * html 3.NewsShow.Asp?Id=1538返回：当前目录上级+NewsShow.Asp?Id=1538
	 */
	public String linktToUrl(String link, String mainurl) {
		String url = new String();

		try {
			URI base = new URI(mainurl);
			URI abs = base.resolve(link.replaceAll("&amp;", "&"));
			URL absURL = abs.toURL();
			url = absURL.toString();
			System.out.println(url);
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
		}

		return url;

	}

	/*
	 * 参数s中有三个值 第一个为要提取的html标签 第二个为要提取的html标签的属性 第二个为要提取的html标签的属性的值
	 * 
	 * 返回pFilter下的全部内容
	 */
	private String getallText(String s, Parser parser) {
		String text = "";
		// System.out.println("parser format:"+s);
		// System.out.println("parser format:"+s.length());
		String filter[];
		NodeFilter pFilter = null;

		// 即s=="@@"时，则返回空
		filter = s.split("@");
		if (s == null || filter.length < 3) {
			if (filter.length == 0)
				return "";
			pFilter = new TagNameFilter(filter[0]);
		} else {
			pFilter = new AndFilter(new TagNameFilter(filter[0]),
					new HasAttributeFilter(filter[1], filter[2]));
		}
		parser.reset();
		try {
			NodeList titleNodeList = (NodeList) parser.parse(pFilter);
			if (titleNodeList == null)
				return "";
			for (int i = 0; i < titleNodeList.size(); i++) {
				titleNodeList.elementAt(i);
				text += (titleNodeList.elementAt(i).toPlainTextString() + " ");
			}
		} catch (ParserException ex) {
			System.out.println(ex.getMessage());
			return "";
		}
		return text;
	}

	/*
	 * 提取网页中 int startp和int endp之间的url
	 */
	public ArrayList<String> get_url(Parser parser, int startp, int endp,
			ArrayList<String> title,boolean notitleflag,String host) {

		ArrayList<String> url = new ArrayList<String>();
		try {
			parser.reset();
			NodeFilter aFilter = new TagNameFilter("a");
			NodeList aList = (NodeList) parser.parse(aFilter);
			System.out.println(aList.size() + "***");
			for (int i = 0; i < aList.size(); i++) {
				if (aList.elementAt(i).getStartPosition() < startp)
					continue;
				if (aList.elementAt(i).getEndPosition() > endp)
					break;
				LinkTag n = (LinkTag) aList.elementAt(i);

				String titles = n.getLinkText();
				System.out.println("过滤前TITLE：" + titles);
				titles = titles.replaceAll(
						"(?u)&nbsp;|&ldquo;|&gt;|&rdquo;|&mdash;|&rsaquo;", "");
				titles = titles.replaceAll("\\s+", "");
				// 针对新闻网页主页 除去其中的日期
				titles = titles
						.replaceAll(
								"(\\s+?\\p{Punct}?\\d{2,4}-\\d{1,2}-\\d{1,2}\\p{Punct}?){2,}",
								"");
				titles = titles.replaceAll("\\[[^]]+\\]", "");
				titles = titles.replaceAll("\\d{2,4}.\\d{1,2}.\\d{1,2}", "");
				titles = titles.replaceAll(allt, "").replaceAll("Go", "");
				if (titles.contains("废油资讯-")) // 针对”废油资讯-新闻中心“不规范网页的过滤
				{
					titles = "";
				} else if (!titles.equals("") && titles.charAt(0) == '·') {
					titles = titles.substring(1);
				}
				/**
				 * 过滤纯粹数字的连接字（主要是针对提取到数字页链接，其他很少有用数字做链接字）
				 */
				Pattern numberPattern = Pattern.compile("\\d{1,6}");
				Matcher numberMatcher = numberPattern.matcher(titles);
				if(numberMatcher.matches()){
					titles = "";
				}
				//对于title太短的情况，直接删除该url，因为很可能能是一个符号，新闻标题不会是这样
				if(titles.length()<=1){
					titles = "";
				}
				System.out.println("过滤后TITLE：" + titles);

				if (!titles.trim().equals("")) {
					String strurl = "";
					//正常提取情况
					strurl = n.extractLink();
						
					//System.out.println("onclick:" + n.getAttribute("onclick"));
					if (strurl != null && !strurl.equals("")) {
						if(host!=null && !host.equals("")){//如果包含host（默认需要进行JS链接拼接为URL）
							String onclick = n.getAttribute("onclick");
							Pattern p = Pattern.compile("/HSE/EFileInfo.aspx.*FromType=0");
							Matcher matcher = p.matcher(onclick);
							if(matcher.find()){
								String path = onclick.substring(matcher.start(),matcher.end());
								System.out.println(path);
								strurl = host + path;
							}
						}
						url.add(strurl);
						//System.out.println("add:::::::::::::::" + strurl);
					}
					title.add(titles);
				} else{
					if((notitleflag)){
						String strurl = n.extractLink();
						if (strurl != null && !strurl.equals("")) {
							url.add(strurl);
						}
					}
				}

			}
		} catch (ParserException e) {
			e.printStackTrace();
		}

		return url;
	}

	public String readFile(String filename) {
		// File file = new File("data/html/2/3/151.html");
		File file = new File(filename);
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (null != line) {
				sb.append(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String html = sb.toString();
		return html;
	}

	public String readFile(String filename, String charset) {
		// File file = new File("data/html/2/3/151.html");
		File file = new File(filename);
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), charset));
			String line = br.readLine();
			while (null != line) {
				sb.append(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String html = sb.toString();
		return html;
	}

	public void getAllPages(String filepath) {
		String[] site = new String[2];
		File newssite = new File(filepath);
		try {
			FileReader fr = new FileReader(newssite);
			BufferedReader bfr = new BufferedReader(fr);
			String line = bfr.readLine();
			String site_id = "";
			String site_url = "";
			while (line != null) {
				site = line.split("\t");
				if (site != null && site.length == 3) {
					site_id = site[0];
					site_url = site[1];
					if(getnewsHtml(site_url, null,site_id + "-1.html")){
						System.out.println(site_id + "下载成功！");
					}
				}
				line = bfr.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getNewsContent(String html,String titlepath,String datepath,String contentpath){
		extractTitle(html, titlepath);
		extractDate(html, datepath);
		extractContent(html, "", contentpath);
	}
	
	public void getNewsURLList(String html,String newsListPath){
		extractNewsList(html, newsListPath,"", "");
	}
	
	public static void main(String[] args) {
		TestExtract te = new TestExtract();

		//te.getnewsHtml("http://scdd.slof.com/scdd/userfile/moreBulletin.jsp","","16.html");
		/*String html = te.readFile("data/html/temp0511/55.html","gb2312");
		String html2 = te.readFile("data/html/temp0511/55-1.html","gb2312");
		
		String newsListPath = "table@width@99%";
		String titlepath = "td@class@f18";
		String datepath = "font@color@#666666";
		String contentpath = "table@width@97%";
		
		//获取新闻列表
		te.getNewsURLList(html, newsListPath);
		//获取新闻标题、日期、正文
		te.getNewsContent(html2, titlepath, datepath, contentpath);*/
		
		 //String html = te.readFile("data/html/newtemp/271.html","gb2312");
		 String html = te.readFile("data/html/falv-temp.html","utf-8");
		 //System.out.println(html);
		 //te.extractNewsList(html,"div@class@title", "", "http://slrb.slof.com/list.aspx?cid=6");
		 //String url = "http://10.67.9.137/office/content_list.aspx?wz=胜利宣传网&td=首页信息&tx=工作动态"; 
		 String newslistpath = "table@class@box";
		 String site_url = "http://10.67.12.80/oa/content_list1.asp?id=1&types=types";
		 //String datepath = "span@id@HRCMS_ctr908_ArtDetail_lblDatePosted";
		 //String html = te.geturlcontent(url)[1]; //System.out.println(html);
		 //te.extractNewsList(html, newslistpath,"td@align@right@site", site_url);
		 String titlepath = "font@style@COLOR:#000000;FONT-FAMILY:宋体;FONT-SIZE:18px; LINE-HEIGHT: 15pt";//td@width@550
		 String datepath = "font@color@DAE7FC";
		 String contentpath = "td@style@line-height:20px; font-size:16px";
		 
		 //te.extractTitle(html, titlepath);0
		 //te.extractDate(html, datepath);
		 te.extractContent(html, "", contentpath);
		 //te.getnewsHtml("http://10.67.12.126:5020/DocLib3/%E4%BF%A1%E6%81%AF%E8%AF%BB%E5%8F%96.aspx?newsId=1990","","falv-temp.html");
		/*te.getnewsHtml(
						"http://10.67.9.137/office/Content_show_new.aspx?id=78758&typename=工作动态",
						"00-1.html");*/
		//te.getnewsHtml("http://10.67.12.80/oa/content_show1.asp?id=50436","","11112.html");
		//te.getAllPages("data/html/temp0511/newssite0511new.txt");
		//te.getnewsHtml("http://10.67.12.145/content_show.asp?id=47214", "http://10.67.12.145/netoffice/login.asp?username=guest", "45-1.html");
	}
}
