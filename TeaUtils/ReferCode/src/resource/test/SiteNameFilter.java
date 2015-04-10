package resource.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;

public class SiteNameFilter extends Filter{

	static final String[] names = new String[]{
		"党办","局办","检查","管理"
	};
	
	static final String[] querynames = new String[]{
		"石油","孙焕泉"
	};
	
	
	//返回过滤信息，true表示留下，false表示舍去
	@Override
	public BitSet bits(IndexReader reader) throws IOException {
		//下面代码需要在多线程下需要加对象锁
		/*if(siteNameList == null){
			siteNameList = new ArrayList<String>();
			init();
		}*/
		BitSet bits = new BitSet(reader.maxDoc());
		BitSet otherbits = new BitSet(reader.maxDoc());
		BitSet tempbits = new BitSet(reader.maxDoc());
		bits.set(0, bits.size()-1, false);
		otherbits.set(0, bits.size()-1, false);//针对非50个网站
		tempbits.set(0, bits.size()-1, true);
		
		Term term = null;
		TermDocs termDocs = null;
		int length = names.length;
		for(int i=0;i<length;i++){
			term = new Term("site_name",names[i]);
			termDocs = reader.termDocs(term);
			while(termDocs.next()){
				bits.set(termDocs.doc(), true);
				tempbits.set(termDocs.doc(), false);
			}
		}
		
		
		for(int i=0;i<querynames.length;i++){
			term = new Term("content",querynames[i]);
			termDocs = reader.termDocs(term);
			while(termDocs.next()){
				otherbits.set(termDocs.doc(),true);
			}
		}
		
		
		otherbits.and(tempbits);//and操作，得到最终除去50网站的剩余符合要求的doc
		
		bits.or(otherbits);//or操作合并最终符合条件的网站
		
		return bits;
	}
	
	/*@Override
	public BitSet bits(IndexReader reader) throws IOException {
		final BitSet bits = new BitSet(reader.maxDoc());
		bits.set(0, bits.size()-1, false);
		
		Term term = null;
		TermDocs termDocs = null;
		int length = names.length;
		for(int i=0;i<length;i++){
			term = new Term("site_name",names[i]);
			termDocs = reader.termDocs(term);
			
			//termDocs.seek(term);
			while(termDocs.next()){
				bits.set(termDocs.doc(), true);
			}
		}
		
		return bits;
	}*/
	
	public static void main(String[] args){
		ArrayList<String> siteNameList = new ArrayList<String>();
		File file = new File("data/newssite/sitenamelist.txt");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"gb2312"));
			String line = reader.readLine();
			while(line != null){
				siteNameList.add(line);
				System.out.println(line);
				line = reader.readLine();
			}
			System.out.println(siteNameList.size());
			/*String[] names = (String[])siteNameList.toArray();
			for(int i=0;i<names.length;i++){
				System.out.println(names[i]);
			}*/
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
