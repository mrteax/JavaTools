package resource.analyzer;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import resource.crawler.FileOperate;
import resource.database.DataBaseConnect;
 
public class Model {
	
	
	//正则表达式（英文：Regular Expression）
	/*
	 * similarurl 
	 * 
	 * 计算相似url
	 * 
	 * 如果这样的url
	 * http://www.sinopecnews.com.cn/news/content/2009-11/06/content_692944.htm
	 * http://www.sinopecnews.com.cn/news/content/2009-10/30/content_689105.htm
	 * 也认为它们是相似的url
	 * 即可以包含多个不同数字
	 * 但最多只能包含一个不同非数字部分
	 * 
	 * 返回其正则表达式
	 */
	public String similarUrl(String url1,String url2)
	{
		String patternStr="";
		
		ArrayList<Integer> difp=new ArrayList<Integer>();
	
		
		String division="[/?]";//分割符-分割线
		String division_no="[^/?]";
		
		String Nop="(\\d|-|_)+";//数字和连接符

		String urla[]=url1.replaceFirst("http://", "").split(division);
		String urlb[]=url2.replaceFirst("http://", "").split(division);
		if(urla.length==urlb.length)
		{
			for(int j=0;j<urla.length;j++)
			{
				if(!urla[j].equals(urlb[j]))
				{
					difp.add(j);
				}
			}
			
			/*
			 * 下面这句是因为“?”是正则表达式中的符号
			 * 
			 * 所以要替换成\?
			 */
			patternStr=url1.replaceAll("[?]", "\\\\?");
			
			int dif=0;
			for(int i=0;i<difp.size();i++)
			{
				if(urla[difp.get(i)].matches(Nop))
				{
					patternStr=patternStr.replaceAll(urla[difp.get(i)],Nop);
				}
				else
				{
					dif++;
					if(dif>1)
					{
						patternStr="";
						break;
					}
					patternStr=patternStr.replaceAll(urla[difp.get(i)],division_no+ "*?");
				}
			}
		}
		System.out.println("patternStr:"+patternStr);
		
		return patternStr; 
	}
	
	//正则表达式（英文：Regular Expression）
	/*
	 * similarurl 
	 * 
	 * 参数url是排好序的
	 * 
	 * 计算相似url
	 * 返回其正则表达式
	 */
	public ArrayList<String> similarUrl(ArrayList<String> url,ArrayList<String> path)
	{
		ArrayList<String> pStr=new ArrayList<String>();
		ArrayList<String> p_al=new ArrayList<String>();
		int sim;//相似url的个数

		String patternStr; 
		Pattern p;
		Matcher m;

		for(int i=1;i<url.size();i++)
		{
		//	System.out.println("url.get(i):"+ url.get(i));
			patternStr=similarUrl(url.get(i-1),url.get(i));
			//System.out.println("patternStr:"+patternStr);
			String modelUrl="";
			String modelpath="";
			
			if(!patternStr.equals(""))//只有一部分不同则认为其为相似的url
			{
				pStr.add(patternStr);
			//	System.out.println(patternStr);
				modelUrl=modelUrl+url.get(i-1)+" ";
				modelUrl=modelUrl+url.get(i)+" ";
				modelpath=modelpath+path.get(i-1)+" ";
				modelpath=modelpath+path.get(i)+" ";
				p_al.add(path.get(i-1));
				p_al.add(path.get(i));
				p = Pattern.compile(patternStr);
				sim=2;i++;
				while(i<url.size()){
					modelUrl=modelUrl+url.get(i)+" ";
					modelpath=modelpath+path.get(i)+" ";
					p_al.add(path.get(i));
					
					m = p.matcher(url.get(i));
					
					if(m.matches())
					{
						sim++;
					}
					else
					{
						break;
					}
					i++;
				}
				i--;
				/*
				 * 保存相似的url
				 * 到指定的路径下
				 */
				FileOperate fo=new FileOperate();
				
				String sp=pStr_path(patternStr);
				fo.newFolder(sp);
				fo.newFile(sp+"modelUrl.txt", modelUrl);
				String model=getSame(p_al);
				fo.newFile(sp+"model.txt",model);
			}
		}
		
		for(int i=0;i<pStr.size();i++)
		{
			System.out.println(pStr.get(i));
			
		}
		return pStr;
	}
	
	/*
	 * 根据正则表达式
	 * 
	 * 返回其对应的路径
	 * 
	 * 例如：http://news.baidu.com/则返回news.baidu.com/
	 * http://www.bpmmp.com.cn/news_list.jsp?pagetype=tpp_content&[^/?&]*?&tree=0
	 * 则返回www.bpmmp.com.cn/news_list.jsp/pagetype=tpp_content/tree=0
	 */
	public String pStr_path(String pStr)
	{
		String path="";
		
		pStr=pStr.replaceAll("http://", "");
		path=pStr.replaceAll("\\[\\^/\\?&\\]\\*\\?[\\s\\S]?", "");
		path=path.replaceAll("[/?&]", "/");
		if(!path.endsWith("/"))
			path=path+"/";
	//	System.out.println(path);
		return path;
	}
	
	
	
	public String getSame(ArrayList<String> path)
	{
		FileOperate fo=new FileOperate();
		analyzer a=new analyzer();
		String html1="",body1="";
		String body2="";
		String same1="",same2="";
		int sameNo=0;
		System.out.println(path.get(0));
		html1=fo.readFileStr(path.get(0));
		if(html1.equals(""))return "";
		body1=a.getBody(html1);

		
		for(int i=1;i<path.size();i++){
			body2=body1;
			html1=fo.readFileStr(path.get(i));
			body1=a.getBody(html1);
			same2=same1;
			same1=getSame(body1,body2);
			if(i!=1)
			{
				if(same1.equals(same2))
				{
					sameNo++;
				}
			}
		}
		
		if(path.size()/2>sameNo)
		{
			same1="";
		}
		return same1;
	}
	
	/*
	 * 找出两个文本相同的前后两部分
	 *
	 * 返回其对应的字符串
	 */
	public String getSame(String txt1,String txt2)
	{
		String same="";
		int f,b;
		f=getSameFront(txt1,txt2);
		b=getSameBack(txt1,txt2);
		if(f<b)
			same=txt1.substring(0,f)+"  "+txt1.substring(b+1);
		return same;
	}
	
	/*
	 * 找出两个文本相同的前面的部分
	 * 
	 * 返回其所在位置
	 */
	public int getSameFront(String txt1,String txt2)
	{
		char []b=txt1.toCharArray();
		char []m=txt2.toCharArray();
		
		int i;
		//找出相同的前面的部分
		for(i=0;i<b.length && i<m.length;i++)
		{
			if(b[i]!=m[i])
			{
				break;
			}
		}
		
		return i;
	}
	
	/*
	 * 找出两个文本相同的后面的部分
	 * 
	 * 返回其所在位置（在txt1中）
	 */
	public int getSameBack(String txt1,String txt2)
	{
		
		
		char []b=txt1.toCharArray();
		char []m=txt2.toCharArray();

		int j;
		//找出相同的前面的部分
		int ml=0;//
		
		ml=m.length-1;
		for(j=b.length-1;j>=0 && ml>=0;j--,ml--)
		{
//			System.out.println(j+":"+ml);
			
			if(b[j]!=m[ml])
			{
//				忽略不相同的数字
//				if(b[j]>='0' && b[j]<='9')
//					j--;
//				else if(m[ml]>='0' && m[ml]<='9')
//					ml--;
//				else
				
				break;
			}
		}
		
		return j;
	}
	
	/*
	 * 去掉body和model共有前后部分
	 */
	public String content(String body,String model)
	{
		String content="";
		
		char []b=body.toCharArray();
		char []m=model.toCharArray();

		int i,j;
		//找出相同的前面的部分
		int ml=0;//
		for(i=0;i<b.length && ml<m.length;i++,ml++)
		{
			if(b[i]!=m[ml])
			{
				//忽略不相同的数字 因为有像“页次:49/57页”这样的内容出现
				if(b[i]>='0' && b[i]<='9')
					i++;
				else if(m[ml]>='0' && m[ml]<='9')
					ml++;
				else
					break;
			}
		}
			
		//找出相同的后面的部分
		ml=m.length-1;
		for(j=b.length-1;j>0 && ml>0;j--,ml--)
		{
			System.out.println(j+":"+ml);
			
			if(b[j]!=m[ml])
			{
				if(b[j]>='0' && b[j]<='9')
					j--;
				else if(m[ml]>='0' && m[ml]<='9')
					ml--;
				else
					break;
			}
		}
		if(i<=j)
			content=body.substring(i, j);
		
		return content;
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
	//	System.out.println(matcher.group());

		return matcher.group();
	}
	
	public static void main(String[] args) {
		
		Model m=new Model();
		System.out.println("2009-11-27".matches("(\\d|-|_)+"));
		m.similarUrl("http://www.sinopecnews.com.cn/news/content/2009-11/06/content_692944.htm", "http://www.sinopecnews.com.cn/news/content/2009-10/30/content_689105.htm");
//		File f=new File("data/html/2009-11-27/34461.html");
//		if(f.exists())
//		{
//			System.out.println("OK");
//		}
		
//		System.out.println(m.getSame("aasdfsafew", "aakwejew"));
//		String str="";
//		str="http://www.bpmmp.com.cn/news_list.jsp?pagetype=tpp_content&[^/?&]*?&tree=0";
//		str="http://www.baidu.com/";
//		m.pStr_path(str);
//		DataBaseConnect dbc;
//		String sql;
//		ResultSet r;
//		
//		dbc=new DataBaseConnect("conf/config.xml");
//
//		sql="select url,FIRST_CRAWLER_TIME,URL_ID,URL_UPDATE from newsurl where URL_UPDATE=1 ORDER BY url";
//		
//		r=dbc.executeQuery(sql);
//		ArrayList<String> url=new ArrayList<String>();
//		ArrayList<String> path=new ArrayList<String>();
//		try {
//			while(r.next())
//			{
//				System.out.println(r.getString("URL_UPDATE")+"::"+r.getString("url"));
//				url.add(r.getString("url"));
//				path.add("data/html/"+r.getString("FIRST_CRAWLER_TIME").split(" ")[0]+"/"+r.getString("URL_ID")+".html");
//				//System.out.println("data/html/"+r.getString("FIRST_CRAWLER_TIME").split(" ")[0]+"/"+r.getString("URL_ID")+".html");
//				//System.out.println(r.getString("url"));
//			}
//		} catch (SQLException e) {
//			// TODO 自动生成 catch 块
//			e.printStackTrace();
//		}
//		dbc.closeConnection();
		
		//m.similarUrl(url,path);
		
	}
	
}
