package resource.query;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.Vector;
import org.xml.sax.InputSource;
import java.sql.*;

public class Context { 

	private File endTextDir = null;
	private String SITE_STORE_PATH = null;
	public String host = null;

	private XPath xpathEngine = XPathFactory.newInstance().newXPath();
	private String installpath = "/config/installPath/text()";
	private String analyzerPath = "/config/analyzerPath/text()";
	InputSource xmlSource = new InputSource(
			"/opt/oilsearch/outer/conf/queryconfig.xml"); 

	public Context() 
	{
		try 
		{
			endTextDir = new File(xpathEngine.evaluate(installpath, xmlSource)
					+ xpathEngine.evaluate(analyzerPath, xmlSource));
			SITE_STORE_PATH = endTextDir.getAbsolutePath();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	class sdata
	{
		public String data;
		public int num;
		public int id;
	}
	
	//��������
	public Vector<sdata> quickSort(Vector<sdata> v,int low,int high)
	{
		int i,j;
		i=low;
		j=high;
		if(low<high)
		{
			int mid=v.get(low).num;  //���ܴ�a[0]��a[low]Ӧ���Ǹ���ֵ,ÿ�ζ���һ���
			String middata=v.get(low).data;
			while(i<j)
			{
				while(i<j && v.get(j).num>mid)  //���ұ���С��
				{
					j--;
				}
				if(i<j)
				{
					v.get(i).num=v.get(j).num;
					v.get(i).data=v.get(j).data;
					i++;
				}
				
				while(i<j && v.get(i).num<mid)   //������Ҵ��
				{
					i++;
				}
				if(i<j)
				{
					v.get(j).num=v.get(i).num;
					v.get(j).data=v.get(i).data;
					j--;
				}
			}
			v.get(i).num=mid;   //һ���������
			v.get(i).data=middata;
			quickSort(v,low,i-1);  //�ݹ�����ߵ�
			quickSort(v,i+1,high);  //�ݹ����ұߵ�
		}
		return v;		
	}
	
	public String getSummary(String txt,String data,String patternStr) throws IOException
	{
		String summary="";
		Vector<sdata> v=new Vector<sdata>();
		 QueryAnalyzer a=new QueryAnalyzer();
	  String[] words=a.split(data).split(" ");
	  Vector<String> vword=new Vector<String>();
	  for(int i=0;i<words.length;i++)
	  {
		  System.out.println("fenciwei��"+words[i]);
		  vword.addElement(words[i]);
	  }
	  for(int i=0;i<words.length;i++)
	  {
			 BufferedReader read=new BufferedReader(new InputStreamReader(
					 new FileInputStream("/opt/oilsearch/outer/resource/query/stopword.txt"),"utf8"));
			 String in=read.readLine();
			 System.out.println("ci:"+words[i]);
			 while(in!=null && in!="")
			 {
				 //System.out.println("in:"+in.toString());
				 if(words[i].equalsIgnoreCase(in.toString()))
				 {
					 vword.removeElement(words[i]);
				 }
				 in=read.readLine();
			 }
			 read.close();
	  }
	  for(int i=0;i<vword.size();i++)
	  {
		  System.out.println(vword.elementAt(i));
	  }
		String content=txt;
	  int id=0;
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(content);
		int n=0;
		if(matcher.find())
		{
		  n=matcher.start()+1;
		}
   if(n==0)
	{
		sdata dd=new sdata();		
		dd.data=content;
		dd.id=id;
		dd.num=1;
	  v.addElement(dd);
		// return n+"==="+content;
	}
	else
	{
		while(n>0&& n<content.length())
	  {
				sdata dd=new sdata();
				dd.data=content.substring(0,n); //0 =>n-1
				dd.id=id;
				dd.num=0;
			  v.addElement(dd);
			  id++;
			  content=content.substring(n);
				
			  pattern = Pattern.compile(patternStr);
			  matcher = pattern.matcher(content);
			  if(matcher.find())
				{
				   n=matcher.start()+1;
				}
	  }
	}
	  for(int i=0;i<v.size();i++)
	  {
		  System.out.println("===="+v.elementAt(i).id+"==="+v.elementAt(i).data);
	  }
	
	  for(int i=0;i<v.size();i++)
	  {
		  for(int j=0;j<vword.size();j++)
		  {
			  String ds=v.get(i).data;
			  while(ds.contains(vword.elementAt(j)))
			  {
				  v.get(i).num++;
				  ds=ds.substring(ds.indexOf(vword.elementAt(j))+1);
			  }
		  }
	  }
	  for(int i=0;i<v.size();i++)
	  {
		  if(v.get(i).num==0)
			  v.remove(i);
	  }
   if(v.size()>1)
   {
   	 /*
		  for(int x=0;x<v.size()-1;x++)
		  {
			  for(int y=x+1;y<v.size();y++)
			  {
				  if(v.get(x).num<v.get(y).num)
				  {
					  int middle=v.get(x).num;
					  String m=v.get(x).data;
					  v.get(x).num=v.get(y).num;
					  v.get(x).data=v.get(y).data;
					  v.get(y).num=middle;
					  v.get(y).data=m;
				  }
			  }
		  }
		 */ 
		  v=quickSort(v,0,v.size()-1);
  }
	  String subs="";
	  for(int i=v.size()-1;i>=0;i--)
	  {
		 System.out.println("===="+v.elementAt(i).id);
		 subs +=v.get(i).data;
		 if(subs.length()>160)
		 {
			 summary=subs.substring(0,160)+"......";
			 break;
		 }
	  }
	  if(subs.length()<=160 && subs.length()>0)
	  {
		 //summary=subs.substring(0,subs.lastIndexOf("��"))+"......"; 
		  summary=subs;
	  }
	  if(summary.length()==0)
	  {
	  	if(txt.length()>160)
	  	{
	       summary=txt.substring(0,160)+"......";	
	    }
	    else
	    {
	       summary=txt;	
	    }
	  }
	  if(summary.startsWith(",")||summary.startsWith(".")||summary.startsWith("?"))
		  summary=summary.substring(1);
	  return summary;
	}
	
/* public String getSummary(String txt,String data,String patternStr) throws IOException
	{
String summary="";
		Vector<sdata> v=new Vector<sdata>();
		
		 QueryAnalyzer a=new QueryAnalyzer();
	  String[] words=a.compose(data).split(" ");
		//String[] tt=a.compose(txt).split(" ");
		
		String content=txt;
//		String content="";
//		for(int i=0;i<tt.length;i++)
//		 {
//		 	 content +=tt[i];
//		 	}
//	     content=content.replace(" ","");

	  System.out.println(content);
	  int id=0;

		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(content);
		int n=0;
		if(matcher.find())
		{
		  n=matcher.start()+1;
		}
      if(n==0)
	{
		sdata dd=new sdata();
					
		dd.data=content;
		dd.id=id;
		dd.num=1;
	    v.addElement(dd);
		// return n+"==="+content;
    	 // summary=content.substring(0,120);
	}
	else
	{
		while(n>0&& n<content.length())
	    {
				sdata dd=new sdata();
				dd.data=content.substring(0,n); //0 =>n-1
				dd.id=id;
				dd.num=0;
			 v.addElement(dd);
			  id++;
			  content=content.substring(n);
				
			  pattern = Pattern.compile(patternStr);
			  matcher = pattern.matcher(content);
			    if(matcher.find())
				{
				   n=matcher.start()+1;
				}
	     }
	}
	  for(int i=0;i<v.size();i++)
	  {
		  System.out.println("===="+v.elementAt(i).id+"==="+v.elementAt(i).data);
	  }
	
	  for(int i=0;i<v.size();i++)
	  {
		  for(int j=0;j<words.length;j++)
		  {
			  String ds=v.get(i).data;
			  while(ds.contains(words[j]))
			  {
				  v.get(i).num++;
				  ds=ds.substring(ds.indexOf(words[j])+1);
			  }
		  }
	  }
   if(v.size()>1)
   {
	  for(int x=0;x<v.size()-1;x++)
	  {
		  for(int y=x+1;y<v.size();y++)
		  {
			  if(v.get(x).num<v.get(y).num)
			  {
				  int middle=v.get(x).num;
				  String m=v.get(x).data;
				  v.get(x).num=v.get(y).num;
				  v.get(x).data=v.get(y).data;
				  v.get(y).num=middle;
				  v.get(y).data=m;
			  }
		  }
	  }
//	Vector<String> result=new Vector<String>();
	  for(int i=0;i<v.size();i++)
	  {
		  if(v.get(i).num==0)
			  v.remove(i);
	  }
   }
	  String subs="";
	  for(int i=0;i<v.size();i++)
	  {
		 System.out.println("===="+v.elementAt(i).id);
		  
		 subs +=v.get(i).data;
		 
		 if(subs.length()>120)
		 {
			 summary=subs.substring(0,120)+"......";
			 break;
		 }
	  }
	  if(subs.length()<=120 && subs.length()>0)
	  {
		 //summary=subs.substring(0,subs.lastIndexOf("��"))+"......"; 
		  summary=subs;
	  }
	
	  if(summary.startsWith(",")||summary.startsWith(".")||summary.startsWith("?"))
		  summary=summary.substring(1);
	  return summary;
	}
	*/
	public void getStart(String s)
	{
		String [] p={"?","��","��","��"};
		for(int i=0;i<p.length;i++)
		{
		  if(s.indexOf(p[i])>=0)
		  {
			  s=s.substring(s.indexOf(p[i]));
			  break;
		  }
		}
	}

	public void startTrim(String s)
	{
		String [] p={"��","��","��","��","��",",",":",";","��","��","��","..."};
		for(int i=0;i<p.length;i++)
		{
		  if(s.startsWith(p[i]))
		  {
			  s=s.substring(s.indexOf(p[i])+1);
			  break;
		  }
		}
	}
	public String getWenzhai(String txt,String words)
	{
		
		String summary="";
		QueryAnalyzer qa =new QueryAnalyzer();
		String txta[]=qa.split(txt).split(" ");
		String worda[]=qa.split(words).split(" ");
		ArrayList<Integer> index=new ArrayList<Integer>();
		
	
		   String subTxt=txt;
			for(int j=0;j<worda.length;j++)
			{
				System.out.println(worda[j]);
				int position=-1;
				while(subTxt.contains(worda[j])) 
				{
					position +=subTxt.indexOf(worda[j])+1;
					index.add(position);
					subTxt=subTxt.substring(subTxt.indexOf(worda[j])+1);
				}
				subTxt=txt;
			}
		
		
		if(index.size()>=3)
		{
			String s1=txt.substring(0, index.get(1));
			if(s1.length()>40)
			{
			 s1=s1.substring(0, 40);
			}
			startTrim(s1);
			String s2=txt.substring(index.get(1)+s1.length(), index.get(2));
			getStart(s2);
            if(s2.length()>40)
			{
			 s2=s2.substring(0, 40);
			}
      startTrim(s2);
			String s3=txt.substring(index.get(2)+s1.length()+s2.length());
			getStart(s3);
			if(s3.length()>40)
			{
			 s3=s3.substring(0, 40);
			}
			startTrim(s3);
			summary=s1+"..."+s2+"..."+s3+".....";
		}
		else if(index.size()==2)
		{
			String s1=txt.substring(0, index.get(1));
		//	getStart(s1);
			if(s1.length()>60)
			{
			 s1=s1.substring(0, 60);
			}
			startTrim(s1);
			String s2=txt.substring(index.get(1)+s1.length());
			getStart(s2);
			if(s2.length()>60)
			{
			 s2=s2.substring(0, 60);
			}
			startTrim(s2);
			summary=s1+"..."+s2+"......";
		}
		else
		{
			
			String s1=txt.substring(0);
		//	getStart(s1);
			if(s1.length()>120)
			{
				s1=s1.substring(0,120);
			}
			startTrim(s1);
			summary=s1+"......";
			
		}
		startTrim(summary);
		return summary;
	}

	public String getTxtFile(String date, String urlid) 
	{
		String path = "�ļ�·����";

		String p= SITE_STORE_PATH+"/"+date+"/"+urlid+".txt";
		File dir= new File(p);
		if(dir.exists())
			return p;
		else
			return path;
	
	}
	
    public String getTxtContext(String path,String keyword){

    	String cont="";
    	String allLine="";
    	int position = 0;
    	try{
    //BufferedReader br = new BufferedReader(new FileReader(path));
    BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(path),"gb2312"));
		String line = br.readLine();
		while (line != null) {
			allLine += line;
			line = br.readLine();
		}
		br.close();
	//	fr.close();
	  
		position = allLine.indexOf(keyword);
		System.out.println(position);
		
		cont += allLine;
		//cont += allLine.substring(position,position+5);
    	return cont;
    	}catch(Exception e)
    	{
    		e.getMessage();
    		return cont;
    	}
    }
   
   public String summary(String txt,String words)
	{
		
		String summary="";
		int summarylength=120;
		System.out.println(txt);
		QueryAnalyzer qa =new QueryAnalyzer();
		String txta[]=qa.split(txt).split(" ");
		String worda[]=qa.split(words).split(" ");
		for(int i=0;i<worda.length;i++)
			System.out.print(worda[i]+"--");
		int start=0;
		int maxwordnum=0;
		int l=0;
		int ws=0;
		
		ArrayList<Integer> pos=new ArrayList<Integer>();
		ArrayList<Integer> index=new ArrayList<Integer>();
		
		
		for(int i=0;i<txta.length;i++)
		{
			l=l+txta[i].length();
			
			for(int j=0;j<worda.length;j++)
			{
				if(txta[i].equals(worda[j]))
				{
				
					pos.add(l);
					index.add(i);
				}
			}
		}

		
		for(int i=0;i<pos.size();i++)
		{
			ws=1;
			for(int j =i+1;j<pos.size();j++)
			{
				if(pos.get(j)-pos.get(i)<summarylength)
				{
					ws++;
				}
			}
			
			if(ws>maxwordnum)
			{
				maxwordnum=ws;
				start=index.get(i);
			}
			System.out.println("start1:"+start);
		}


		
		String patternStr; 
		Pattern p;
		Matcher m;
		
		if(start!=0)
		{	
			//patternStr="[ ��������!\\.\\?][^ ��������!\\.\\?]{0,"+summarylength+"}?"; 
			patternStr="[ ��������!\\.\\?][^ ��������!\\.\\?]";
			System.out.println("---"+patternStr);
			for(int i=start;i<start+4 && i<txta.length;i++)
			{
				patternStr=patternStr+txta[i];
				if(i<start+3)
					//patternStr=patternStr+"[\\s\\S]{0,10}?";
					patternStr=patternStr+"[\\s\\S]?";
			}
			//System.out.println(patternStr);
			p = Pattern.compile(patternStr);
			m = p.matcher(txt);
			
			if(m.find())
			{
				start=m.start()+1;
			}
		}
		
		int end=start+summarylength;
		if(end>txt.length())
		{
			end=txt.length()-1;
		}
		System.out.println("txt:"+txt.length());
		System.out.println(maxwordnum);
		System.out.println("start:"+start);
		System.out.println("end:"+end);
		
		summary= txt.substring(start, end)+"......";
		return summary;
	}
	
	

	public static void main(String[] args) throws IOException
	{
		String path = "";
		String result ="";
		//String url = "http://www.efp8.com";
		String date = "2010-08-26";
		String urlid = "1757339";
		String keyword = "ʤ������";
		Context context = new Context();
	    //path = context.getTxtFile(url, urlid);
		path = context.getTxtFile(date, urlid);
		//System.out.println(path);
		String result1 = context.getTxtContext(path, keyword);
		System.out.println("--"+result1);
		String[] o=result1.split("\\*\\*\\*");
		System.err.println("size "+o.length);
		result=o[o.length-1];
		//System.out.println(result);
		//String ss="[ ������!?][^ ������!?]";
		  String ss="[ ������!?][^ ������!?]";
		String s=context.getSummary(result,keyword,ss);
		//String s=context.summary(result,keyword);
	//String s=context.getWenzhai(result,keyword);
		System.out.println("wenzhai-------------"+s);
	}
}
