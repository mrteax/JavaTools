package resource.analyzer;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;


public class SplitWord {
	private WordNode T=new WordNode();
	
	/*
	 * 把dicpath文件中的词添加到初始化词库中
	 */
	public SplitWord(String dicpath) 
	{
		try{
			
			String scrwords;
			InputStreamReader read = new  InputStreamReader(new FileInputStream(dicpath));
		     BufferedReader reader = new BufferedReader(read);	
		//	BufferedReader read = new BufferedReader(new FileReader("/opt/oilsearch/inner/resource/query/sDict.txt"));
			scrwords = reader.readLine();
			while (scrwords != null) {
				andWord(scrwords);
				scrwords = reader.readLine().trim();
			}
			read.close();
		}catch(Exception e){}
	}
	
	
	public SplitWord() {
		try{
			
			String scrwords;
			String path=System.getProperty("user.dir");
			String file=path+"/readfile/sDict.txt";
			InputStreamReader read = new  InputStreamReader(new FileInputStream(file));
		    BufferedReader reader = new BufferedReader(read);	
			scrwords = reader.readLine();
			while (scrwords != null) {
				andWord(scrwords);
				scrwords = reader.readLine().trim();
			}
			read.close();
		}catch(Exception e){}
	}
	
	/*
	 * insertChar是andWord的子程序
	 * 是为了把一个字符插入
	 */
	private WordNode insertChar(WordNode wn,char c,boolean isWord)
	{
		if(wn.sub==null)
		{
			wn.sub=new ArrayList<WordNode>();
			wn.sub.add(0,new WordNode(c,isWord));	
			return wn.sub.get(0);
		}
		ArrayList<WordNode> n=wn.sub;
		int left=0,right=n.size()-1,mid=0;
		while(left<=right)
		{
			mid=(left+right)/2;
			if(c>n.get(mid).data)
				left=mid +1;
			else if(c<n.get(mid).data)
				right=mid -1;
			else 
				break;
		}
		if(c!=n.get(mid).data)
		{
			if(c>n.get(mid).data)
				mid++;
			n.add(mid,new WordNode(c,isWord));
		}
		return n.get(mid);
	}
	
	/*
	 *把词 word插入到分词词典中
	 */
	public void andWord(String word)
	{
		int i=0;
		WordNode currentNode=T;
		boolean isword=false;
		char words[]=word.toCharArray();
		for(i=0;i<words.length-1;i++)
		{
			currentNode=insertChar(currentNode,words[i],isword);
		}
		currentNode=insertChar(currentNode,words[i],true);
	}

	void PrintToTxt(String fileName)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			LinkedList<WordNode> visited=new LinkedList<WordNode>();//ջ
			int visitedNo[]=new int[30];
			for(int i=0;i<30;i++)
			{
				visitedNo[i]=0;
			}
			WordNode current=null;
			int level=0;
			int count=0;
			char words[]=new char[30];
			visited.add(T);
			while(!visited.isEmpty())
			{
				if(visited.getFirst().sub.size()==visitedNo[level])
				{
					visited.removeFirst();
					visitedNo[level]=0;
					
					level--;
				}
				else
				{
					count++;
					current=visited.getFirst().sub.get(visitedNo[level]);

					words[level]=current.data;
					if(current.isWord)
					{
						String s=new String(words,0,level+1);
						writer.write(s);
						writer.write("\r\n");
					}
					visitedNo[level]++;
					if(current.sub!=null)
					{
						visited.addFirst(current);
						level++;
					}
	
				}

			}
			writer.close();
			System.out.println(count+":count");
		}catch(Exception e){}
	}
	
	public String segmenter(String txt)
	{
		String s1;
		String dics="";//分词结果
		WordNode currentNode=T;
		boolean isWords=false;
		int wordsLength=1;
		int j=0;
		//加一个空格是为了处理到分词串的最后一个词或单字也输出
		char c[]=(txt+" ").toCharArray();
		int i=0;
		for(i=0;i<c.length;i++)
		{
			//System.out.println(c[i]);
			currentNode=find(currentNode.sub,c[i]);

			if(currentNode!=null)
			{
				j++;
				if(currentNode.isWord)
				{
					isWords=true;
					wordsLength=j;
				}
			}
			else
			{
				if(isWords)
				{
					s1=new String(c,i-j,wordsLength);
					//System.out.println(s1);
					dics=dics+s1+" ";
				}
				else if(j==0||j==1)//把单个字符也加入分词串中
				{
					s1=new String(c,i,1);
					if(i>0)
					{
					dics=dics+c[i-j]+" ";
					//System.out.print(c[i-1]+" p"+i);
					}
				}
				i=i-j+wordsLength-1;
				j=0;
				currentNode=T;
				isWords=false;
				wordsLength=1;

				
			}
		}
		return dics.trim();
	}
	public WordNode find(ArrayList<WordNode> n,char c)
	{

		if(n==null)
		{
			return null;
		}
		int left=0,right=n.size()-1,mid=0;
		while(left<=right)
		{
			mid=(left+right)/2;
			if(c>n.get(mid).data)
				left=mid +1;
			else if(c<n.get(mid).data)
				right=mid -1;
			else 
				break;
		}
		if(c==n.get(mid).data)
			return n.get(mid);
		else
			return null;
	}
	
	/*
	 * 词频统计
	 */
	public HashMap<String,Integer> wordsfre(String txt)
	{
		
		HashMap<String,Integer> words=new HashMap<String,Integer>();
		if("".equals(txt) || txt==null)
			return words;
		String s1;
		WordNode currentNode=T;
		int wordsLength=0;
		
		char c[]=txt.toCharArray();
		//int i=0;
		for(int i=0;i<c.length;i++)
		{
			currentNode=find(currentNode.sub,c[i]);

			if(currentNode!=null)
			{
				wordsLength++;
				if(currentNode.isWord)
				{
					s1=new String(c,i-wordsLength+1,wordsLength);
					if(words.containsKey(s1))
					{
						int fre=words.get(s1).intValue();
						words.put(s1,fre+1);
					}
					else
					{
						words.put(s1,1);
					}
				}
			}
			else
			{
				i=i-wordsLength;
				wordsLength=0;
				currentNode=T;
			}
		}
		return words;
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try{
			
			//System.out.println("Insert Start");
			Date beginTime1 = new Date();
			SplitWord sw=new SplitWord();

			String scrwords,textcontext="";
			BufferedReader read ;

			Date endTime1 = new Date();
			long Stime = endTime1.getTime() - beginTime1.getTime();
			//System.out.println(Stime);
			//System.out.println("Insert Finished");
			
			read = new BufferedReader(new FileReader("1.txt"));
			
			System.out.println("待分词的字符串：");
			scrwords = read.readLine();
			while (scrwords != null) {
				System.out.println(scrwords);
				textcontext = textcontext+scrwords;
				scrwords = read.readLine();
				
			}
			
			//System.out.println(textcontext);
			read.close();
			
			//System.out.println("split Start");
			beginTime1 = new Date();
			System.out.println("分词的结果为：");
			
			System.out.println(sw.segmenter(textcontext).substring(0, 47));
			System.out.println(sw.segmenter(textcontext).substring(47,99));
			System.out.println(sw.segmenter(textcontext).substring(99,sw.segmenter(textcontext).length()));
			
			HashMap<String,Integer> h=sw.wordsfre(textcontext);
			System.out.print(h.size());
			System.out.println("h.size");
			System.out.println(h.toString());
			endTime1 = new Date();
			Stime = endTime1.getTime() - beginTime1.getTime();
			System.out.println(Stime);
			System.out.println("split Finished");
			sw.PrintToTxt("out2.txt");

		}
		catch(Exception e){}

	}

}

class WordNode
{
	char data;
	boolean isWord;
	ArrayList<WordNode> sub;
	public WordNode(){this.sub=null;}
	public WordNode(char data, boolean isWord) {
		this.data = data;
		this.isWord = isWord;
		this.sub=null;
	}
}