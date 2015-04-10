package resource.analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Testcookie {

	/**
	 * able to succeed
	 * @param args
	 */
	public static void test(){
		
	}
	
	public static void get(URL url,URL cookieurl){
		HttpURLConnection connection;
		String cookieVal = null;
		String sessionId = "";
		String key=null;
		if(cookieurl!=null){			
			try{
				connection = (HttpURLConnection)cookieurl.openConnection();
				String redirecturl="";
				connection.setInstanceFollowRedirects(false);
				
				boolean flag = false;
				while(connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || 
						connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM){
					System.out.println(connection.getResponseCode());
					
					System.out.println(connection.getResponseMessage());
					String location = connection.getHeaderField("Location");
					System.out.println("Location:  " +location);
					if(!location.startsWith("http")){//相对地址
						String temp = cookieurl.toString();
						while(location.startsWith("../")){
							location = location.substring(3);
							//System.out.println(location);
							//System.out.println(url.lastIndexOf("/"));
							//System.out.println(url.length()-1);
							
							temp = temp.substring(0,temp.lastIndexOf("/"));
							temp = temp.substring(0,temp.lastIndexOf("/"))+"/";
							//System.out.println(temp);
							/*if(url.lastIndexOf("/") != (url.length()-1)){
								
							}else{
								url = url.substring(0, url.length()-2);
							}*/
						}
						if(temp.lastIndexOf("/") != (temp.length()-1)){
							temp = temp.substring(0,temp.lastIndexOf("/")) +"/";
						}
						redirecturl = temp + location;
						System.out.println("***" +redirecturl);
					}else{//绝对地址
						redirecturl = location;
					}
					if(!redirecturl.equals("")){
						System.out.println("开始重定向");
						cookieurl = new URL(redirecturl);
						connection = (HttpURLConnection)cookieurl.openConnection();
						connection.setInstanceFollowRedirects(false);
						
						for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++ ) {
							if (key.equalsIgnoreCase("set-cookie")) {
								cookieVal = connection.getHeaderField(i);
								System.out.println("get cookie:"+cookieVal);
							}
						}
						continue;
					}
					/*if(connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP){
						location = connection.getHeaderField("Location");
						System.out.println("Location:  " +location);
						if(!location.startsWith("http")){
							redirecturl = cookieurl.toString() + location;
							System.out.println("***" +redirecturl);
						}
						if(!redirecturl.equals("")){
							System.out.println("开始重定向");
							cookieurl = new URL(redirecturl);
							connection = (HttpURLConnection)cookieurl.openConnection();
							connection.setInstanceFollowRedirects(false);
						}
					}*/
					//Map<String,List<String>> map = connection.getHeaderFields();
					/*for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++ ) {
						if (key.equalsIgnoreCase("set-cookie")) {
							cookieVal = connection.getHeaderField(i);
							System.out.println("get cookie:"+cookieVal);
							cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));
							sessionId = sessionId+cookieVal+";";
							System.out.println("sessionid:"+sessionId);
							flag = true;
						}
					}*/
					/*if(flag){
						break;
					}*/
				}
				cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));
				sessionId = sessionId+cookieVal+";";
				System.out.println("sessionid:"+sessionId);
				/*for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++ ) {
					if (key.equalsIgnoreCase("set-cookie")) {
						cookieVal = connection.getHeaderField(i);
						System.out.println("get cookie:"+cookieVal);
						cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));
						sessionId = sessionId+cookieVal+";";
						System.out.println("sessionid:"+sessionId);
						flag = true;
					}
				}*/
				System.out.println(sessionId);
			}catch(MalformedURLException e){
				System.out.println("url can't connection");
				//return null;
			}catch(IOException e){
				System.out.println(e.getMessage());
				//return null;
			}
		}
		
		try {
			connection = (HttpURLConnection)url.openConnection();
			//connection.setRequestProperty("Cookie", "ASPSESSIONIDSQTBBAAT=PDDCCOODOCHMBPFHNMOIAGOG");
			connection.setRequestProperty("Cookie", sessionId);
			
			
			//connection.setRequestProperty("Referer", "http://10.67.12.202/zzbdj/index.asp");
			connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Foxy/1; .NET CLR 2.0.50727; MEGAUPLOAD 1.0)");
			//connection.setInstanceFollowRedirects(false);
			InputStream ips=connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(ips,"gb2312"));
			String lineStr = br.readLine();
			while(lineStr!=null){
				System.out.println(lineStr);  
				lineStr=br.readLine();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		URL url1=null;
		try {
			url1 = new URL("http://10.67.12.202/zzbdj/content_show.asp?id=130782");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		URL url2=null;
		try {
			//url2 = new URL("http://10.67.12.202/zzbdj/index.asp");
			url2 = new URL("http://10.67.12.202/zzbdj"); //无条件发送此请求 http://10.67.12.202/zzbdj/netoffice/login.asp?username=guest
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		get(url1,url2);
	}

}
