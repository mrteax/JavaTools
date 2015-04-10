package resource.query;
import java.io.BufferedReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class pingyin {

	ArrayList<String> pingYin=new ArrayList<String>();
	ArrayList<String> hanZi=new ArrayList<String>();
	

	/*
	 * 把pingyin.txt中拼音和其对应的汉字读入
	 */
	public void init()
	{
		
		try{
			String line;
			String[] linearray;
			String file="/opt/oilsearch/inner/resource/query/pingyin.txt";
			//file="pingyin.txt"; 
			InputStreamReader read = new  InputStreamReader(new FileInputStream(file),"GB2312");
		     BufferedReader reader = new BufferedReader(read);	
		//	BufferedReader read = new BufferedReader(new FileReader("/opt/oilsearch/inner/resource/query/pingyin.txt"));///opt/oilsearch/inner/resource/query/
			line = reader.readLine();
			//System.out.println("start");
			while (line!= null) {

				linearray =line.split("=");
				pingYin.add(linearray[0].trim());
				hanZi.add(linearray[1].substring(linearray[1].indexOf("\"")+1,linearray[1].lastIndexOf("\"") ));;
				
				
				line = reader.readLine();
			//	System.out.println("start");
			}
		}
		catch(Exception e){}
	}
	
	public void addHotword(SplitWord s)
	{
		DataBaseConnect dbc=null;
		dbc=new DataBaseConnect();
		
		String query="select WORDNAME from hotwords where RATINGS>3";
		ResultSet rs=dbc.executeQuery(query);
		System.out.println(query);
		try {
			while(rs.next())
			{
				System.out.println(rs.getString("WORDNAME"));
				s.InsertWord(rs.getString("WORDNAME"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * 输入：拼音字符串
	 * 返回：所有可能的组合
	 */
	public ArrayList<String> splitPingYin(String p)
	{
		ArrayList<String> result=new ArrayList<String>();
		LinkedList<String> currentP=new LinkedList<String>();
		LinkedList<Integer> index=new LinkedList<Integer>();

		String currentPingYin="";
				
		char [] c =p.toCharArray();
		for(int i =0;i <c.length ;i++)
		{
			int j;
			for(j =6;j>=1;j--)
			{
				if(i+j>c.length)
				{
					j=c.length-i;
				}
				
				String s=new String(c,i,j);
				int pos=pingYin.indexOf(s);
				if(pos!=-1)
				{
					
					currentP.add(currentPingYin+" "+s);
			//		System.out.println("cPingYin: "+currentPingYin+" "+s);
					index.add(new Integer(i+j));
				}
			}
		
			while(!index.isEmpty())
			{
				i=(index.removeFirst()).intValue()-1;
				if(i==p.length()-1)
				{
					
					result.add(currentP.removeFirst().trim());//trim()去掉最前面的一个空格
				}
				else
				{					
					currentPingYin=currentP.removeFirst();
					break;
				}
			}
		}
//		for(int i=0;i<result.size();i++)
//			System.out.println(result.get(i));

		return result;
	}

	
	
	/*
	 * 根据拼音返回对应的汉字
	 */
	public String pingYinToHanZi(String pingyin)
	{
		String result="";
	
		SplitWord word=new SplitWord();
		//把数据库中的热门词加入到词典中
		//addHotword(word);
		
		WordNode nextW;//当前字结点
		LinkedList<node> mid=new LinkedList<node>();
		LinkedList<node> WordS=new LinkedList<node>();

		ArrayList<String> sp=splitPingYin(pingyin);
		
		//for(int i=0;i<sp.size();i++)
		for(int i=0;i<sp.size();i++)
		{
	//		System.out.println(sp.get(i));
			String[] p=sp.get(i).split(" ");
			//拼音长度小于1的不处理
			if(p.length<=1)continue;
			//如果对前面的拼音分割能匹配出一定结果刚结束
			if(result!="")break;
			
			mid.clear();
			WordS.clear();
			WordS.addLast(new node("",word.T,0));
			WordS.addLast(null);
			
			for(int j =0;j<p.length;j++)
			{

				while(WordS.getFirst()!=null)
				{
					int index=pingYin.indexOf(p[j]);
				//	System.out.println(hanZi.get(index));
					char zi[]=hanZi.get(index).toCharArray();
					node n=WordS.removeFirst();
					
					for(int k=0;k<zi.length;k++)
					{
						nextW=word.find(n.w.sub, zi[k]);
						if(nextW!=null)
						{
							if(!nextW.isWord)
							{	
								WordS.addLast(new node(n.s+zi[k],nextW,n.l));	
							}
							else
							{
								if(n.s.length()+1==p.length)
									result=result+n.s+zi[k]+" ";
								else
								{
									WordS.addLast(new node(n.s+zi[k],nextW,n.s.length()+1));
									mid.addLast(new node(n.s+zi[k],word.T,n.s.length()+1));

								}
							}
						}
					}
					
				}//whileWordS.getFirst()!=null)
				
				if(result!="")break;
				
				if(mid.isEmpty()||mid.getLast()!=null)
					mid.addLast(null);
				WordS.removeFirst();
				if(WordS.isEmpty())
				{

//					System.out.println("J:"+j);
//					System.out.println(WordS.size());
//					System.out.println(mid.size());
//					System.out.println(i);
//					for(int w=0;w<mid.size();w++)
//					{
//						if(mid.get(w)!=null)
//						System.out.println(mid.get(w).s);
//						else
//							System.out.println(" ");
//					}
					mid.removeLast();
					while(mid.getLast()!=null)
					{
						j=mid.getLast().s.length()-1;					
						WordS.addLast(mid.removeLast());
					}
//					System.out.println("JJJJJJJJ"+j);
				}
				WordS.addLast(null);
				
			}
				
		}
//		System.out.println("result:"+result);
//		System.out.println("GGGGGGGGG");
//		System.out.println(mid.size());
//		for(int i=0;i<mid.size();i++)
//		{
//			System.out.println(mid.get(i).s);
//		}

		return result;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自动生成方法存根
		
		String encoding = System.getProperty("file.encoding");   
		System.out.println(encoding);   
		pingyin p=new pingyin();
		p.init();
		String s="woshizhongguoren";
		s="zhongguozhonghuarenmingongheguo";
		s="shanghai";
		if(args!=null)
			s=args[0];
		System.out.println(p.pingYinToHanZi(s));
		System.out.println("finished");
	}
}

class node
{
	String s;
	WordNode w;
	int l;
	public node(String s, WordNode w, int l) {
		this.s = s;
		this.w = w;
		this.l = l;
	}
	
}
