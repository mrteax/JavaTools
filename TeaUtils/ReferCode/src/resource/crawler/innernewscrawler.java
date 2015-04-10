package resource.crawler;

import resource.analyzer.DateFormat;
import resource.analyzer.analyzer;
import resource.database.DataBaseConnect;
import resource.index.index;

import java.io.*;
import java.net.*;
import java.sql.*;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.xml.sax.InputSource;

public class innernewscrawler implements Runnable {
	private String configpath = "conf/config.xml";
	private String sitehtmlpath = "data/sitehtml/";
	private String urlhtmlpath = "data/html/";
	private String innerurlhtmlpath = "/opt/oilsearch/inner/analyzer/endhtml/";
	private String fronthtmlpath = "/opt/oilsearch/inner/analyzer/readfile/addfronthtml.txt";

	private String urltxtlpath = "data/txt";
	private String logpath = "data/log";
	private boolean down = false;
	private ArrayList<siteBean> siteurl = new ArrayList<siteBean>();
	private ArrayList<urlBean> urlb = new ArrayList<urlBean>();

	public innernewscrawler() {
		XPath xpathEngine = XPathFactory.newInstance().newXPath();
		String urltxtpath_conf = "/config/UrlTxtPath/text()";
		String sitehtmlpath_conf = "/config/SiteHtmlPath/text()";
		String urlhtmlpath_conf = "/config/UrlHtmlPath/text()";
		String innerurlhtmlpath_conf = "/config/InnerUrlHtmlPath/text()";
		String fronthtmlpath_conf = "/config/FrontHtmlPath/text()";
		String logpath_conf = "/config/LogPath/text()";
		InputSource xmlSource = new InputSource(configpath);// 假设XML文件路径为

		try {
			urltxtlpath = xpathEngine.evaluate(urltxtpath_conf, xmlSource);
			sitehtmlpath = xpathEngine.evaluate(sitehtmlpath_conf, xmlSource);
			urlhtmlpath = xpathEngine.evaluate(urlhtmlpath_conf, xmlSource);
			innerurlhtmlpath = xpathEngine.evaluate(innerurlhtmlpath_conf,
					xmlSource);
			fronthtmlpath = xpathEngine.evaluate(fronthtmlpath_conf, xmlSource);
			logpath = xpathEngine.evaluate(logpath_conf, xmlSource);

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public void run() {

	}

	/*
	 * 初始化数据库
	 */
	public void init(String filpath) {
		String line = new String();
		String[] la = new String[20];
		String sql;
		ResultSet r;
		DataBaseConnect dbc;
		dbc = new DataBaseConnect(configpath);
		while (!dbc.getConnection()) {
			dbc.startConnection();
		}
		r = dbc.executeQuery("select * from innernewssite where STATUS=2");

		try {

			System.out.println("start");
			if (r.next())
				return;
			System.out.println("end");
			// 文件中读入数据，插入数据库中
			FileReader fr = new FileReader(filpath);
			BufferedReader br = new BufferedReader(fr);
			line = br.readLine();
			while (!dbc.getConnection()) {
				dbc.startConnection();
			}
			int count=0;
			while (line != null) {
				la = line.split("\t");
				sql = "insert into innernewssite values(innernewssite_id.nextval,'"
						+ la[1] + "','" + la[2] + "','" + la[3]+ "','"
						+ la[4] + "','" + la[5] + "','" + la[6] + "','" + la[7]
						+ "','" + la[8] + "','" + la[9]+ "')";
				System.out.println(sql);
				dbc.insert(sql);
				line = br.readLine();
				count++;
			}
			System.out.println("一共插入了" + count + "条记录");
			br.close();
			fr.close();
			dbc.closeConnection();

			// System.out.println("把种子网站中的种子网站分支找到，并插入数据库中");
			//			
			//			
			// //把种子网站中的种子网站分支找到，并插入数据库中
			// Sitebranch();
			//			
			// dbc=new DataBaseConnect(configpath);
			// sql="select * from newssite where STATUS=2";
			// r=dbc.executeQuery(sql);
			// setsiteBean(r);
			// System.out.println(siteurl.size());
			// dbc.closeConnection();
		} catch (Exception e) {
			System.out.print(e.getLocalizedMessage());
		}

	}

	public void Sitebranch() {
		// 把种子网站中的种子网站分支找到，并插入数据库中
		String sql;
		DataBaseConnect dbc;
		dbc = new DataBaseConnect(configpath);
		while (!dbc.getConnection()) {
			dbc.startConnection();
		}

		sql = "select * from innernewssite where STATUS=1";
		ResultSet r = dbc.executeQuery(sql);
		setsiteBean(r);

		String[] sitebantch = new String[3];

		while (!siteurl.isEmpty()) {
			siteBean sn = new siteBean();
			sn = siteurl.remove(siteurl.size() - 1);
			// System.out.println("sn.getSitebranch():"+sn.getSitebranch());
			// 如果是第一个参数等于null或""，说明种子网站下无分支
			// 则继续
			if (sn.getSitebranch() == null || sn.getSitebranch().length() < 3)
				continue;
			sitebantch = sn.getSitebranch().split("@");

			// System.out.println("getbean end，并插入数据库中");

			// 格式化输出字符串
			int l = sitebantch[1].length();
			String f = "%1$0" + String.valueOf(l) + "d";

			for (int i = Integer.parseInt(sitebantch[1]); i <= Integer
					.parseInt(sitebantch[2]); i++) {

				String url = sitebantch[0].replaceAll("\\(\\*\\)", String
						.format(f, i));
				System.out.println(url);
				sql = "insert into innernewssite values(innernewssite_id.nextval,'" + url
						+ "','" + sn.getSite_url() + "',2,'" + "" + "','"
						+ sn.getNewslist() + "','" + sn.getTitle() + "','"
						+ sn.getDate() + "','" + sn.getContent() + "')";
				// System.out.println(sql);
				while (!dbc.getConnection()) {
					dbc.startConnection();
				}
				dbc.insert(sql);
			}
		}
		dbc.closeConnection();
	}

	/*
	 * 使用数据库结果 ，设置sitebean;
	 */
	public void setsiteBean(ResultSet r) {
		try {
			if (r != null) {
				while (r.next()) {
					siteBean sn = new siteBean();
					sn.setSite_id(r.getInt("site_id"));
					sn.setSite_url(r.getString("SITE_URL"));
					sn.setSite_name(r.getString("SITE_NAME"));
					sn.setStatus(r.getInt("STATUS"));
					sn.setSitebranch(r.getString("SITEBRANCH"));
					sn.setNewslist(r.getString("NEWSLIST"));
					sn.setTitle(r.getString("TITLE"));
					sn.setDate(r.getString("TIME"));
					sn.setContent(r.getString("CONTENT"));
					sn.setCookieSite(r.getString("cookie_site"));
					siteurl.add(sn);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * 使用数据库结果 设置urlBean;
	 */
	public void seturlBean(ResultSet r) {
		try {
			if (r != null) {
				while (r.next()) {
					urlBean ub = new urlBean();
					// sql="select URL_ID,URL,SITE_ID,TITLE,DATE,CONTENT from newsurl,newssite where newsurl.site_id=newssite.site_id";
					ub.setUrl_id(r.getInt("URL_ID"));
					ub.setUrl(r.getString("URL"));
					ub.setSite_id(r.getInt("site_id"));
					ub.setCookie_site(r.getString("cookie_site"));
					if (r.getString(6) != null)
						ub.setTitle(r.getString(6));//innernewsurl.title
					else
						ub.setTitle("");
					if (r.getString(5) != null)
						ub.setTitleflag(r.getString(5));//innernewssite.title
					else
						ub.setTitleflag("");
					ub.setTime(r.getString("TIME"));
					ub.setContent(r.getString("CONTENT"));
					ub.setFirst_crwaler_time(r.getString("FIRST_CRAWLER_TIME")
							.split(" ")[0]);
					if (r.getDate("create_time") != null) {
						ub.setDate(r.getDate("create_time").toString());
					}
					ub.setSite_name(r.getString("site_name"));
					ub.setUnit_priority(r.getInt("unit_priority"));

					// System.out.println(r.getString("FIRST_CRAWLER_TIME").split(" ")[0]);
					urlb.add(ub);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * 
	 */
	public urlBean geturlBean() {
		urlBean ub = new urlBean();

		ub = urlb.remove(urlb.size() - 1);
		return ub;
	}

	/*
	 * 下载并 保存网页
	 */
	public String saveHtml(siteBean sn, String html) {
		// System.out.println("stsrt savehtml");

		// 获取url对应的见面内容
		// html=geturlcontent(sn.getSite_url());
		FileOperate fo = new FileOperate();

		String Abspath = sitehtmlpath + sn.getSite_id() + ".html";
		fo.newFile(Abspath, html);
		return html;
	}

	/*
	 * 下载并 保存网页
	 */
	public String saveurlHtml(urlBean ub) {
		// System.out.println("stsrt savehtml:");
		// System.out.println(ub.getUrl());
		String html = "";
		FileOperate fo = new FileOperate();
		try {
			// 获取url对应的见面内容
			html = geturlcontent(ub.getUrl(),ub.getCookie_site());
			if (html == null || html.trim().equals(""))
				return null;
			DateFormat df = new DateFormat();
			/**
			 * 对于文件头先注释掉****************************************
			 */
			/*BufferedReader br = new BufferedReader(
					new FileReader(fronthtmlpath));
			String aLine = br.readLine();
			String front = "";
			while (aLine != null) {
				front += aLine;
				aLine = br.readLine();
			}*/
			String front = "";
			ub.setCrawler_date(df.GetDate());
			String path = urlhtmlpath + df.GetDate() + "/";//这里怎么是随便用一个获取日期来作为路径，怎么不是first_crawler_time
			fo.newFolder(path);
			String Abspath = path + ub.getUrl_id() + ".html";
			fo.newFile(Abspath, front + html);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "";
		}
		ub.setDocsize(html.length());
		ub.setUrl_update(3);//***********************************************************************
		// ub.setUrl_update(url_update(html));
		// System.out.println(ub.getSite_id()+":url_update:"+url_update(html));
		/*
		 * 更新数据库
		 */
		DataBaseConnect dbc = new DataBaseConnect(configpath);
		while (!dbc.getConnection()) {
			dbc.startConnection();
		}

		String sql = "update innernewsurl set URL_UPDATE =" + ub.getUrl_update()
				+ ",Docsize=" + ub.getDocsize() + " where URL_ID="
				+ ub.getUrl_id();
		dbc.executeUpdate(sql);
		
		/**
		 * 此处完成向inner_url数据库中插入数据项,同时写入endhtml***************************************************
		 */
		/*sql = "select * from inner_url where url='" + ub.getUrl() + "'";
		System.out.println(ub.getUrl());
		while (!dbc.getConnection()) {
			dbc.startConnection();
		}
		ResultSet rs = dbc.executeQuery(sql);
		try {
			if (!rs.next()) {//如果内网没有记录，则插入
				System.out.println("内网中没有记录");
				sql = "select * from innernewsurl where url_id=" + ub.getUrl_id();
				while (!dbc.getConnection()) {
					dbc.startConnection();
				}
				rs = dbc.executeQuery(sql);
				if (rs.next()) {
					URL url = new URL(rs.getString("url"));
					if (rs.getDate("create_time") != null) {//在内网新闻中有记录，则直接读取createtime为last_mod_time，如果还没有，则默认createtime为sysdate
						sql = "insert into inner_url(url_id,status,docsize,last_crawler_time,first_crawler_time,last_mod_time,site_id,url_linked,"
								+ "url,url_value,page_content_value,url_bad,url_update,class_id,unit_name,cycle,first_extract_time,"
								+ "url_extract,domain,pr) values(inner_url_id.nextval,"
								+ 0
								+ ","
								+ rs.getInt("docsize")
								+ ",to_timestamp('"
								+ rs.getString("first_crawler_time")
								+ "','yyyy-mm-dd hh24:mi:ssxff'),to_timestamp('"
								+ rs.getString("first_crawler_time")
								+ "','yyyy-mm-dd hh24:mi:ssxff'),to_timestamp('"
								+ rs.getString("create_time")
								+ "','yyyy-mm-dd hh24:mi:ssxff'),"
								+ rs.getInt("site_id")
								+ ","
								+ rs.getInt("url_linked")
								+ ",'"
								+ rs.getString("url")
								+ "',"
								+ rs.getInt("url_value")
								+ ","
								+ 100
								+ ","
								+ rs.getInt("url_bad")
								+ ","
								+ 0
								+ ","
								+ 0
								+ ","
								+ "''"
								+ ","
								+ 10000
								+ ",sysdate,''"
								+ ",'"
								+ url.getHost()
								+ "',"
								+ 0.15
								+ ") returning url_id into ?";
					} else {
						sql = "insert into inner_url(url_id,status,docsize,last_crawler_time,first_crawler_time,last_mod_time,site_id,url_linked,"
								+ "url,url_value,page_content_value,url_bad,url_update,class_id,unit_name,cycle,first_extract_time,"
								+ "url_extract,domain,pr) values(inner_url_id.nextval,"
								+ 0
								+ ","
								+ rs.getInt("docsize")
								+ ",to_timestamp('"
								+ rs.getString("first_crawler_time")
								+ "','yyyy-mm-dd hh24:mi:ssxff'),to_timestamp('"
								+ rs.getString("first_crawler_time")
								+ "','yyyy-mm-dd hh24:mi:ssxff'),sysdate,"
								+ rs.getInt("site_id")
								+ ","
								+ rs.getInt("url_linked")
								+ ",'"
								+ rs.getString("url")
								+ "',"
								+ rs.getInt("url_value")
								+ ","
								+ 100
								+ ","
								+ rs.getInt("url_bad")
								+ ","
								+ 0
								+ ","
								+ 0
								+ ","
								+ "''"
								+ ","
								+ 10000
								+ ",sysdate,''"
								+ ",'"
								+ url.getHost()
								+ "',"
								+ 0.15
								+ ") returning url_id into ?";
					}
					while (!dbc.getConnection()) {
						dbc.startConnection();
					}
					int id = dbc.callexecuteUpdate(sql);
					if (id == -1)
						System.out.println("新闻插入外网不成功！");
					ub.setInner_url_id(id);
					if (ub.getInner_url_id() != -1) {
						System.out.println("INNER_ID:" + ub.getInner_url_id());
						System.out.println("URL:" + ub.getUrl());
						System.out.println("NEWS_ID:" + rs.getInt("url_id"));
						String path = innerurlhtmlpath
								+ ub.getFirst_crwaler_time() + "/";
						fo.newFolder(path);
						BufferedReader br = new BufferedReader(new FileReader(
								fronthtmlpath));
						String aLine = br.readLine();
						String front = "";
						while (aLine != null) {
							front += aLine;
							aLine = br.readLine();
						}
						String Abspath = path + ub.getInner_url_id() + ".html";
						System.out.println(Abspath);
						fo.newFile(Abspath, front + html);
					}
				}
			}else{//如果外网已经有此记录，则进行相关信息的替换
				int url_id = rs.getInt("url_id");
				String inner_first_crawler_time = rs.getString("first_crawler_time").substring(0,10);
				ub.setInner_url_id(url_id);
				ub.setInner_first_crawler_time(inner_first_crawler_time);
				System.out.println(url_id + "在内网中已经存在，开始进行替换");
				//先替换HTML文件******************************************************************
				if(url_id != -1 && url_id!=0){
					System.out.println("INNER_ID:" + url_id);
					System.out.println("URL:" + ub.getUrl());
					String path = innerurlhtmlpath
					+ inner_first_crawler_time + "/";
					fo.newFolder(path);
					BufferedReader br = new BufferedReader(new FileReader(
							fronthtmlpath));
					String aLine = br.readLine();
					String front = "";
					while (aLine != null) {
						front += aLine;
						aLine = br.readLine();
					}
					String Abspath = path + url_id + ".html";
					System.out.println(Abspath);
					fo.newFile(Abspath, front + html);
				}
				
				sql = "select * from innernewsurl where url_id=" + ub.getUrl_id();
				rs = dbc.executeQuery(sql);
				String update_sql = "";
				if (rs.next()) {//如果新闻有这条url
					//URL url = new URL(rs.getString("url"));
					if (rs.getDate("create_time") != null) {
						update_sql = "update inner_url set last_mod_time = to_timestamp('"
								+ rs.getString("create_time")+ "','yyyy-mm-dd hh24:mi:ssxff'),cycle=10000" + " where url_id=" + url_id;
					}
					//一般来说这里create_time是没有的，需要等到新闻解析之后才有，所以一些更新操作放在了解析之后
					if(dbc.executeUpdate(update_sql)){
						System.out.println(url_id + "内网中更新成功！");
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		/***
		 * 截止
		 */

		dbc.closeConnection();

		return html;
	}


	/**
	 * URL中文字符编码
	 * @param url
	 * @return
	 */
	public static String EncodeUrl(String url) {
		if(url==null || url.equals("")){
			return null;
		}
		String regex = "[\u4e00-\u9fa5]";
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(url);
		StringBuffer sb = new StringBuffer();
		int start = 0;
		int end = 0;
		String chinesestr = "";
		while (matcher.find()) {
			try {
				chinesestr = url.substring(matcher.start(), matcher.end());
				end = matcher.start();
				sb.append(url.substring(start, end));
				sb.append(URLEncoder.encode(chinesestr,"GBK"));
				start = end+1;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if(start<url.length()){
			sb.append(url.substring(start, url.length()));
		}
		url = sb.toString();
		System.out.println("编码后的URL:" + url);
		return url;
	}
	
	/*
	 * 参数url 返回url对应的网页的内容
	 */
	public String geturlcontent(String strUrl,String cookieUrlStr) {
		//对于中文URL进行UTF-8编码
		strUrl = EncodeUrl(strUrl);
		cookieUrlStr = EncodeUrl(cookieUrlStr);
		
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
			url = new URL(strUrl);
			String cookieVal = null;
			String sessionId = "";
			String key=null;

			HttpURLConnection huc=null,cookieHuc=null;
			SinoDetect detector = new SinoDetect();

			if (cookieUrlStr != null && !cookieUrlStr.equals("")) {//如果需要从登录网页中获取cookie中的session信息
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

				huc.setInstanceFollowRedirects(false);// *******增加的**********

			} else {
				//对于胜利信息网-油田快讯进行特殊处理（因为URL会跳转，临时解决办法，这里用HOST,运行起来后用site_id）
				if(url.getHost().equals("10.67.12.228")){
					huc = (HttpURLConnection) url.openConnection();
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

			System.out.println("charset:" + charset);
			if (charset.equalsIgnoreCase("other"))
				charset = "GB2312";

			html = new String(lBytes, charset);

			bo.close();
			
			charset = "gb2312";
			html = html.replaceAll("(?u)utf-8", charset).replaceAll("(?u)utf8",
					charset).replaceAll("(?u)unicode", charset);

			html = HtmlModify.urlModifying(html, strUrl);

			down = true;
		} catch (MalformedURLException e) {
			// TODO 自动生成 catch 块
			// 写入exception.log日志，url格式错误
			DataBaseConnect dbc = new DataBaseConnect();
			while (!dbc.getConnection()) {
				dbc.startConnection();
			}
			String fromUrl = dbc.executeErrorQuery(strUrl);
			dbc.closeConnection();
			try {
				if (!bwcontent.contains(strUrl)) {
					if (!fromUrl.equals("")) {
						bw.write(strUrl + "的格式不正确，来自于" + fromUrl + "\r\n");
						bw.flush();
					} else {
						bw.write(strUrl + "的格式不正确.\r\n");
						bw.flush();
					}
				}
				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// 写入exception.log日志，url已经废弃
			DataBaseConnect dbc = new DataBaseConnect();
			while (!dbc.getConnection()) {
				dbc.startConnection();
			}
			String fromUrl = dbc.executeErrorQuery(strUrl);
			dbc.closeConnection();
			try {
				if (!bwcontent.contains(strUrl)) {
					if (!fromUrl.equals("")) {
						bw.write(strUrl + "网站已经被取消，来自于" + fromUrl + "\r\n");
						bw.flush();
						return null; // 如果有种子网站，一定没有被取消
					} else {
						bw.write(strUrl + "网站已经被取消.\r\n");
						bw.flush();
					}
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} catch (ConnectException e) {
			// 连接超时，返回null，url_update=0
			return null;
		} catch (SocketTimeoutException e) {
			// 读取超时，返回null，url_update=0
			return null;
		} catch (UnsupportedEncodingException e) {
			// 编码不支持,to do nothing,url_update=3
		} catch (FileNotFoundException e) {
			// 写入exception.log日志，url已经废弃
			DataBaseConnect dbc = new DataBaseConnect();
			while (!dbc.getConnection()) {
				dbc.startConnection();
			}
			String fromUrl = dbc.executeErrorQuery(strUrl);
			dbc.closeConnection();
			try {
				if (!bwcontent.contains(strUrl)) {
					if (!fromUrl.equals("")) {
						bw.write(strUrl + "新闻网址已经被废弃，来自于" + fromUrl + "\r\n");
						bw.flush();
					} else {
						bw.write(strUrl + "新闻网址已经被废弃.\r\n");
						bw.flush();
					}
					bw.close();
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
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
		return html;
	}

	/*
	 * 根据抓取到的html网页内容 分析是哪种网页
	 * 
	 * 1--抓取网页失败，内容为空 2--抓取网页为非标准的html网页 3--抓取网页为标准的html网页
	 */
	// public int url_update(String html)
	// {
	// int url_update=0;
	// String patternStr;
	// Pattern p;
	// Matcher m;
	// // <!--html 格式-->
	// // <html xmlns="http://www.w3.org/1999/xhtml">
	// // <head>
	// // <meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
	// // <title>无标题文档</title>
	// // </head>
	// //
	// // <body>
	// // </body>
	// // </html>
	// patternStr="(?u)<\\s?html[\\s\\S]*?>"+"[\\s\\S]*?"+
	// "<\\s?head\\s?>"+"[\\s\\S]*?"+
	// "<\\s?title\\s?>[\\s\\S]*?<\\s?/title\\s?>"+"[\\s\\S]*?"+
	// "<\\s?/\\s?head\\s?>"+"[\\s\\S]*?"+
	// "<\\s?body[\\s\\S]*?>"+"[\\s\\S]*?"+
	// "<\\s?/\\s?body\\s?>"+"[\\s\\S]*?"+
	// "<\\s?/\\s?html\\s?>";
	// p = Pattern.compile(patternStr);
	// m = p.matcher(html);
	// if(html.length()<20)
	// {
	// url_update=1;
	// }else if(m.find())
	// {
	// url_update=3;
	// }else
	// {
	// url_update=2;
	// }
	//
	// return url_update;
	// }
	/*
	 * 清空数据库
	 */
	void clear() {
		DataBaseConnect dbc = new DataBaseConnect(System
				.getProperty("user.dir")
				+ "/conf/config.xml");
		while (!dbc.getConnection()) {
			dbc.startConnection();
		}
		String sql[];
		sql = new String[2];
		sql[0] = "delete from innernewssite where SITE_ID<>10";
		sql[1] = "delete from innernewsurl where URL_ID<>0";
		try {
			dbc.TransAction(sql);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		dbc.closeConnection();
	}

	public void search() {

		analyzer a = new analyzer();
		DataBaseConnect dbc;
		String sql;
		ResultSet r = null;
		ResultSet rs;
		long starttime = System.currentTimeMillis();

		System.out.println("setsiteBean(r)");
		dbc = new DataBaseConnect(configpath);
		/*
		 * 首先处理status=0的正常网页 在newslist,time,content字段中如果存在分号就是间址提取
		 */
		sql = "select * from innernewssite where STATUS=0";
		while (!dbc.getConnection()) {
			dbc.startConnection();
		}
		r = dbc.executeQuery(sql);
		setsiteBean(r);

		System.out.println(siteurl.size());

		System.out.println("download parser siteurl start:+++++++++++++");
		while (!siteurl.isEmpty()) {
			down = false;
			siteBean sb = siteurl.remove(0);
			System.out.println("parser siteurl:" + sb.getSite_url());
			String html = geturlcontent(sb.getSite_url(),sb.getCookieSite());//这里对于种子网站也无妨用cookiesite设置一下，看看有没有问题，因为看到有部分列表也要登陆（待验证）
			if (down) {//如果下载成功
				saveHtml(sb, html);

				a.parserUrl(html, sb);
				System.out.println("finished.");
			} else {
				System.out.println("download error finished.");
			}
		}

		/*
		 * 下面处理status=1的网页，需要进行日期替换 如YYYYMMDD用当前日期替换为20100714 2010-07-14修改
		 */
		while (!dbc.getConnection()) {
			dbc.startConnection();
		}
		sql = "select * from innernewssite where STATUS=1";
		r = dbc.executeQuery(sql);
		setsiteBean(r);
		DateFormat dateformat = new DateFormat();
		String currentDate = dateformat.GetDate();
		String[] ymd = currentDate.split("-");

		int sitesize = siteurl.size();

		for (int i = 0; i < sitesize; i++) {
			siteBean sb = siteurl.remove(0);
			String url = sb.getSite_url();
			url = url.replaceAll("\\(YYYY\\)", ymd[0]);
			url = url.replaceAll("\\(MM\\)", ymd[1]);
			url = url.replaceAll("\\(DD\\)", ymd[2]);
			sb.setSite_url(url);
			siteurl.add(sb);
		}

		System.out.println(siteurl.size());

		System.out.println("download parser siteurl start:+++++++++++++");
		while (!siteurl.isEmpty()) {
			down = false;
			siteBean sb = siteurl.remove(0);
			String html = geturlcontent(sb.getSite_url(),sb.getCookieSite());
			if (down) {
				saveHtml(sb, html);
				System.out.println("parser siteurl:" + sb.getSite_url());
				a.parserUrl(html, sb);
				System.out.println("finished.");
			} else {
				System.out.println("download error finished.");
			}
		}

		System.out.println("seturlBean(r)");

		if (urlb.isEmpty()) {
			while (!dbc.getConnection()) {
				dbc.startConnection();
			}
			sql = "dbms_random.seed(1234567891)";
			System.out.println(dbc.execute(sql));

			//unit_priority
			sql = "select url_id,url,cookie_site,innernewsurl.site_id,innernewssite.title,innernewsurl.title,time,content,create_time," +
					"FIRST_CRAWLER_TIME,innernewssite.site_name,unit_priority," +
					"dbms_random.value(1,round(dbms_random.value(1,(select count(*) from innernewsurl,innernewssite " +
					"where innernewsurl.site_id=innernewssite.site_id and URL_UPDATE=0)))) a from innernewsurl,innernewssite " +
					"where innernewsurl.site_id=innernewssite.site_id and URL_UPDATE=0 order by a";
			r = dbc.executeQuery(sql);
			
			seturlBean(r);
		}

		//开始下载与解析新闻内容网页
		System.out.println("urlb:" + urlb.size());
		System.out.println("parser news start:+++++++++++++");
		index i = new index();
		;
		int n = 0;
		while (!urlb.isEmpty()) {
			while (!dbc.getConnection()) {
				dbc.startConnection();
			}
			down = false;
			urlBean ub = geturlBean();
			if (ub == null)
				break;
			System.out.println("parser:第" + urlb.size() + "个" + ub.getUrl()
					+ "site_id:" + ub.getSite_id());

			String html = saveurlHtml(ub);
			if (down) {
				if (ub.getUrl_update() == 3) {

					int flag = a.parserNews(html, ub); // flag标志位，0表示网页内容太少，1表示成功解析，2表示提取标签失效，3表示间址情况没有下载成功
					if ((flag == 1) || (flag == 2)) {
						n++;
						i.indexNews(ub);
						i.searcherclose();
						/**
						 * 更新内网cycle以及date,并置url_update为3（表示重新建立索引）*************************************
						 */
						/*if(flag==1){
							int inner_url_id = ub.getInner_url_id();
							String update_sql = "update inner_url set last_mod_time = to_timestamp('"
								+ ub.getDate() + "','yyyy-mm-dd hh24:mi:ssxff'),cycle=10000, url_update=3 where url_id=" + inner_url_id;
							if(dbc.executeUpdate(update_sql)){
								System.out.println(inner_url_id + "在内网中更新成功！");
							}
						}*/
						/**
						 * 截止
						 */
						if (flag == 2) {
							/*while (!dbc.getConnection()) {
								dbc.startConnection();
							}*/
							sql = "update innernewsurl set URL_UPDATE =4 where URL_ID="
									+ ub.getUrl_id();
							dbc.executeUpdate(sql);
						}
					} else if (flag == 0) {
						while (!dbc.getConnection()) {
							dbc.startConnection();
						}
						sql = "update innernewsurl set URL_UPDATE =6 where URL_ID="
								+ ub.getUrl_id();
						dbc.executeUpdate(sql);
					}

				} else {
					System.out.println("getUrl_update<>3");
					DateFormat df = new DateFormat();
					if (!ub.getFirst_crwaler_time().equals(df.GetDate())) {
						while (!dbc.getConnection()) {
							dbc.startConnection();
						}
						sql = "update innernewsurl set URL_UPDATE =6 where URL_ID="
								+ ub.getUrl_id();
						dbc.executeUpdate(sql);
					}
					System.out.println("处理跳转网页或内容为空");
				}

			}

			// break;
		}
		i.indexclose();
		dbc.closeConnection();

		long endtime = System.currentTimeMillis();
		DateFormat df = new DateFormat();
		FileOperate fo = new FileOperate();
		String content = "";
		content += df.GetTime() + "：遍历了一次site和处理了" + n + "个新闻网页,共用了："
				+ String.valueOf((endtime - starttime) / 1000) + "秒"
				+ System.getProperty("line.separator");

		System.out.println(content);
		fo.appendfile(logpath + "/" + df.GetDate() + ".txt", content);

	}

	public static void main(String[] args) {
		innernewscrawler nc = new innernewscrawler();
		nc.init("data/newssite/new0511.txt");
		//while (true) {
			nc.search();
			/*try {
				Thread.sleep(120 * 60 * 1000);// 60*1000表示1分钟
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
	}
}
