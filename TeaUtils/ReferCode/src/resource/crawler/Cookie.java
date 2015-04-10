package resource.crawler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import resource.crawler.CheckCharset;

public class Cookie {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		// TODO 自动生成方法存根
		Cookie c=new Cookie();
		try {
			URL u=new URL("http://www.jhpa.com.cn/show.php?boardid=3");
			HttpURLConnection huc=getReFreshHttpURLConnection(u);
			out(huc);
			String charset="";
			String html="";
			
			BufferedInputStream bis = new BufferedInputStream(huc.getInputStream());
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
			System.out.println(html);
		} catch (MalformedURLException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	}


	/*
	 * 获取url链接
	 */
	public static HttpURLConnection getHttpURLConnection(URL u)
	{
		HttpURLConnection huc=null;

		try {
			huc=(HttpURLConnection)u.openConnection();
			huc.setConnectTimeout(6000);
			huc.setReadTimeout(18000);
			/*
			 * 设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向（响应代码为 3xx 的请求）。 
			 * 默认值来自 followRedirects，其默认情况下为 true。 
			 */
			huc.setInstanceFollowRedirects(false);
			
		//	huc.connect();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		
		return huc;
	}
	
	/*
	 * 获取刷新的url链接
	 */
	public static HttpURLConnection getReFreshHttpURLConnection(URL u)
	{
		
		HttpURLConnection huc=null;
		
		try {
			huc=(HttpURLConnection)u.openConnection();
			huc.setConnectTimeout(6000);
			huc.setReadTimeout(18000);
			String cookie=getCookie(huc);
			System.out.println("cookie:"+cookie);
			
			if((cookie!=null)&&(!cookie.equals("")))
			{
				
				huc=getHttpURLConnection(u);
				huc.setRequestProperty("Cookie",cookie);
			}
			
		} catch (IOException e1) {
			// TODO 自动生成 catch 块
			e1.printStackTrace();
		}

		return huc;
	}
	
//	已有这样的函数
//	/*
//	 * 返回指定关键字的值
//	 */
//	public static String getHeaderField(HttpURLConnection huc,String key)
//	{
//		String headField="";
//		if (huc != null) {
//			for (int i = 1; (key = huc.getHeaderFieldKey(i)) != null; i++) {
//				if (key.equalsIgnoreCase(key)) {
//					headField= huc.getHeaderField(key);
//				}
//			} 
//		}
//		return headField;
//	}
	
	/*
	 * 
	 */
	public static String getCookie(HttpURLConnection huc)
	{
		String cookie="";
		if (huc != null) {
			cookie=huc.getHeaderField("set-cookie");
			if(cookie!=null)
			{
				int index=cookie.indexOf(";");
				if(index!=-1)
					cookie = cookie.substring(0, index);
			}
			
		}
		return cookie;
	}

	
	public static void out(HttpURLConnection connection)
	{
		if (connection != null) {
			String key;
			for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++) {
				System.out.println(key);
				System.out.println("-->"+connection.getHeaderField(key));
			} 
		}
	}
}
