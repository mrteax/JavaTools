package resource.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;

public class SiteNameFilter extends Filter{

	/*static final String[] names = new String[]{
		"局办","党办"
	};*/
	
	//private ArrayList<String> siteNameList = null;
	static final String[] names = new String[]{
		"党委组织部-组工快讯",
		"党委组织部-他山之石",
		"宣传部-工作动态",
		"宣传部-特别推荐",
		"宣传部-信息动态",
		"局纪委-工作动态",
		"统战部-要闻",
		"局工会-动态信息",
		"局团委-热点新闻",
		"机关党委-机关动态",
		"综合治理办-工作动态",
		"勘探处-内部动态",
		"局办-工作动态",
		"开发处-生产快讯",
		"采油工程处-工程动态",
		"设备管理处-工作动态",
		"基建处-基建快讯",
		"油地工作处-图片新闻",
		"科技处-科技信息",
		"经营管理部-工作动态",
		"财务资产处-工作动态",
		"规划计划部-工作动态",
		"劳资处-工作信息",
		"财务资产部-工作动态",
		"审计处-工作动态",
		"法律事务部-工作动态",
		"安全环保处",
		"勘探项目部-工作动态",
		"勘探处-内部动态",
		"西部勘探项目部-工作动态",
		"新疆勘探项目部-工作动态",
		"销售事业部-新闻动态",
		"财务监控中心-工作动态",
		"定额价格管理中心",
		"卫生管理中心",
		"公共事业中心-工作动态",
		"局信息中心",
		"社保中心-工作动态",
		"老年中心-工作信息",
		"海检中心-图文快讯",
		"疾控中心-工作动态",
		"实体部-工作动态",
		"川东北工委-中心动态",
		"石油工程处-工作动态",
		"石油工程处-工程信息",
		"胜利新闻网-油田要闻",
		"胜利新闻网-油区动态",
		"胜利新闻网-油田政工",
		"油田外部网-胜利新闻"
	};
	
	static final String[] querynames = new String[]{
		"孙焕泉",
		"席秀海",
		"杨昌江",
		"赵金洲",
		"李中树",
		"张煜",
		"张善文",
		"宋振国",
		"许卫华",
		"毕义泉",
		"张洪山",
		"局领导",
		"管理局局长",
		"管理局副局长",
		"局党委书记",
		"局党委副书记",
		"分公司总经理",
		"分公司副总经理",
		"局经理办公室",
		"局经理办",
		"政策研究室",
		"勘探处",
		"开发处",
		"采油工程处",
		"采油处",
		"生产管理处",
		"生产处",
		"基建处",
		"设备管理处",
		"设备处",
		"技术发展处",
		"规划计划部",
		"规划部",
		"计划部",
		"财务资产处",
		"财务处",
		"资产处",
		"人力资源处",
		"人事处",
		"经营管理部",
		"法律事务处",
		"法务处",
		"审计处",
		"纪委监察处",
		"监察处",
		"安全环保处",
		"安保处",
		"质量技术监督处",
		"质量监督处",
		"油地工作处",
		"局党委办公室",
		"局党委办",
		"党委组织部",
		"党委统战部",
		"局团委",
		"企业文化处",
		"文化处",
		"局工会",
		"社会治安综合治理办公室",
		"局综合治理办",
		"机关党委",
		"卫生管理中心",
		"公共事业管理部",
		"公共事业部",
		"劳动就业服务中心",
		"劳动就业中心",
		"社会保险管理中心",
		"社保中心",
		"离退休职工管理中心",
		"勘探项目管理部",
		"勘探项目部",
		"西部勘探项目部",
		"西部项目部",
		"新疆勘探项目管理部",
		"新疆项目部",
		"勘探开发监督管理部",
		"监理部",
		"销售事业部",
		"局信息中心",
		"财务监控中心",
		"审计中心",
		"定额价格管理中心",
		"油区稽查支队",
		"科技展览中心",
		"局文工团",
		"文联工作办公室",
		"局文联"
	};
	/*//初始化单位名列表
	public void init(){
		//读取siteName信息
		File file = new File("data/readfile/sitenamelist.txt");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"gb2312"));
			String line = reader.readLine();
			while(line != null){
				siteNameList.add(line);
				System.out.println(line);
				line = reader.readLine();
			}
			System.out.println(siteNameList.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	//返回过滤信息，true表示留下，false表示舍去
	@Override
	public BitSet bits(IndexReader reader) throws IOException {
		
		//下面代码需要在多线程下需要加对象锁
		/*if(siteNameList == null){
			siteNameList = new ArrayList<String>();
			init();
		}*/
		
		/**
		 * 修改前版本
		 * final BitSet bits = new BitSet(reader.maxDoc());
		final BitSet otherbits = new BitSet(reader.maxDoc());
		final BitSet tempbits = new BitSet(reader.maxDoc());
		bits.set(0, bits.size()-1, false);
		otherbits.set(0, bits.size()-1, false);//针对非50个网站
		tempbits.set(0, bits.size()-1, true);
		
		Term term = null;
		TermDocs termDocs = null;
		int length = names.length;
		for(int i=0;i<length;i++){
			term = new Term("source",names[i]);
			termDocs = reader.termDocs(term);
			while(termDocs.next()){
				bits.set(termDocs.doc(), true);
				tempbits.set(termDocs.doc(), false);
			}
		}
		
		
		for(int i=0;i<querynames.length;i++){
			term = new Term("contents",querynames[i]);
			termDocs = reader.termDocs(term);
			while(termDocs.next()){
				otherbits.set(termDocs.doc(),true);
			}
		}
		
		
		otherbits.and(tempbits);//and操作，得到最终除去50网站的剩余符合要求的doc
		
		bits.or(otherbits);//or操作合并最终符合条件的网站
*/		
		
		/**
		 * 新版本，针对50个网站也进行过滤，也就是对所有种子网站设置正文内容过滤
		 */
		final BitSet bits = new BitSet(reader.maxDoc());
		bits.set(0, bits.size()-1, false);
		
		Term term = null;
		TermDocs termDocs = null;
		
		
		for(int i=0;i<querynames.length;i++){
			term = new Term("contents",querynames[i]);
			termDocs = reader.termDocs(term);
			while(termDocs.next()){
				bits.set(termDocs.doc(),true);
			}
		}
		
		return bits;
	}

	public static void main(String[] args){
		File file = new File("data/newssite/words.txt");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"gb2312"));
			String line = reader.readLine().trim();
			while(line != null){
				System.out.println("\"" +line + "\",");
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
