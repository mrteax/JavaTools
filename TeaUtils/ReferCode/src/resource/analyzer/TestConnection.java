package resource.analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class TestConnection {
	public static void get(URL url,URL cookieurl){
		HttpURLConnection connection;
		String cookieVal = null;
		String sessionId = "";
		String key=null;
		if(cookieurl!=null){			
			try{
				connection = (HttpURLConnection) cookieurl.openConnection();
				//String redirecturl = "";
				connection.setInstanceFollowRedirects(false);
				// Map<String,List<String>> map = connection.getHeaderFields();
				for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++) {
					if (key.equalsIgnoreCase("set-cookie")) {
						cookieVal = connection.getHeaderField(i);
						System.out.println("get cookie:" + cookieVal);
						cookieVal = cookieVal.substring(0, cookieVal
								.indexOf(";"));
						sessionId = sessionId + cookieVal + ";";
						System.out.println("sessionid:" + sessionId);
					}
				}

				/*
				 * cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));
				 * sessionId = sessionId+cookieVal+";";
				 * System.out.println("sessionid:"+sessionId);
				 */
				/*
				 * for (int i = 1; (key = connection.getHeaderFieldKey(i)) !=
				 * null; i++ ) { if (key.equalsIgnoreCase("set-cookie")) {
				 * cookieVal = connection.getHeaderField(i);
				 * System.out.println("get cookie:"+cookieVal); cookieVal =
				 * cookieVal.substring(0, cookieVal.indexOf(";")); sessionId =
				 * sessionId+cookieVal+";";
				 * System.out.println("sessionid:"+sessionId); flag = true; } }
				 */
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
			
			//**试验***
			/*connection.setInstanceFollowRedirects(true);
			
			if(connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP){
				System.out.println(connection.getResponseCode());
				String location = connection.getHeaderField("Location");
				System.out.println("Location:  " +location);
				URI baseURI =  url.toURI();
				URI absoluteURI = baseURI.resolve(location);
				URL absoluteURL = absoluteURI.toURL();
				System.out.println(absoluteURL);
				//String absolutelocation = 
				
				connection = (HttpURLConnection) absoluteURL.openConnection();
				
				connection.disconnect();
				

				return;
			}*/
			//***试验***
			//connection.setRequestProperty("Cookie", "ASPSESSIONIDSQTBBAAT=PDDCCOODOCHMBPFHNMOIAGOG");
			connection.setRequestProperty("Cookie", sessionId);
			
			
			//connection.setRequestProperty("Referer", "http://10.67.12.202/zzbdj/index.asp");
			connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Foxy/1; .NET CLR 2.0.50727; MEGAUPLOAD 1.0)");
			//connection.setInstanceFollowRedirects(false);
			InputStream ips=connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(ips,"utf-8"));
			String lineStr = br.readLine();
			while(lineStr!=null){
				System.out.println(lineStr);  
				lineStr=br.readLine();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		URL url1=null;
		try {
			url1 = new URL("http://10.66.19.187/jyglbTYWD.vs?cmd=getManageListOfOthers&ywfl=JYGLBTYWD&tyfl=0&tyid=JYGLBGZDT&wsbg=j&flag=ym");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		URL url2=null;//cookie url
		try {
			//url2 = new URL("http://10.67.12.202/zzbdj/index.asp");//http://10.67.12.194/
			//url2 = new URL("http://10.67.53.179/xbxmb/netoffice/login.asp?username=guest"); //无条件发送此请求 http://10.67.12.202/zzbdj/netoffice/login.asp?username=guest
			url2 = new URL("http://10.66.19.187/JYGLB/index.jsp?noFilter=true");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		get(url1,url2);
	}
}
