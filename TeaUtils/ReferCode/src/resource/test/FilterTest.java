package resource.test;

import java.io.IOException;

import jeasy.analysis.MMAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;

import resource.query.DateFormat;

public class FilterTest {
	public static void main(String[] args) {
		try {
			// StringBuffer sb = new StringBuffer();
			// String indexFileDir = "data/collections";

			RAMDirectory ramDir = new RAMDirectory();
			IndexWriter writer = new IndexWriter(ramDir, new MMAnalyzer());


			Document doc1 = new Document();
			doc1.add(new Field("url_id","1",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc1.add(new Field("content", "石油美景", Field.Store.YES,
					Field.Index.TOKENIZED));
			doc1.add(new Field("site_name","党办",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc1.add(new Field("create_date","2012-05-15",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc1.add(new Field("priority","1",Field.Store.YES,Field.Index.UN_TOKENIZED));

			Document doc2 = new Document();
			doc2.add(new Field("url_id","2",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc2.add(new Field("content", "石油炼化 ", Field.Store.YES,
					Field.Index.TOKENIZED));
			doc2.add(new Field("site_name","局办",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc2.add(new Field("create_date","2012-05-17",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc2.add(new Field("priority","1",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc3 = new Document();
			doc3.add(new Field("url_id","3",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc3.add(new Field("content", "石油钻杆接头螺纹 ", Field.Store.YES,
					Field.Index.TOKENIZED));
			doc3.add(new Field("site_name","局办",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc3.add(new Field("create_date","2012-05-17",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc3.add(new Field("priority","1",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc4 = new Document();
			doc4.add(new Field("url_id","4",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc4.add(new Field("content", "石油库设计规范 ", Field.Store.YES,
					Field.Index.TOKENIZED));
			doc4.add(new Field("site_name","党办",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc4.add(new Field("create_date","2012-05-17",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc4.add(new Field("priority","1",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc5 = new Document();
			doc5.add(new Field("url_id","5",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc5.add(new Field("content", "谁有最新石油修井机底盘的标准 ", Field.Store.YES,
					Field.Index.TOKENIZED));
			doc5.add(new Field("site_name","宣传处",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc5.add(new Field("create_date","2012-05-06",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc5.add(new Field("priority","1",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc6 = new Document();
			doc6.add(new Field("url_id","6",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc6.add(new Field("content", "石油歌曲《石油红包》 ", Field.Store.YES,
					Field.Index.TOKENIZED));
			doc6.add(new Field("site_name","管理",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc6.add(new Field("create_date","2012-05-17",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc6.add(new Field("priority","999",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc7 = new Document();
			doc7.add(new Field("url_id","7",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc7.add(new Field(
							"content",
							"石油事业如今是如火如荼，蒸蒸日上，但是不可再生能源与有限储量问题限制了石油的可持续发展啊，也不知道石油还能火多少年啊！",
							Field.Store.YES, Field.Index.TOKENIZED));
			doc7.add(new Field("site_name","检查",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc7.add(new Field("create_date","2012-05-17",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc7.add(new Field("priority","999",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc8 = new Document();
			doc8.add(new Field("url_id","8",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc8.add(new Field("content", "我石油馆演绎188天精彩完美谢幕，孙焕泉前来参观", Field.Store.YES,
					Field.Index.TOKENIZED));
			doc8.add(new Field("site_name","管理",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc8.add(new Field("create_date","2012-05-17",Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc8.add(new Field("priority","999",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			
			/*
			Document doc9 = new Document();
			doc9.add(new Field("content", "有谁有石油金融方面的资料?", Field.Store.YES,
					Field.Index.TOKENIZED));
			doc9.add(new Field("site_name","",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc10 = new Document();
			doc10.add(new Field("content", "谁有关于石油或者储运方面的参考文献 急用 ",
					Field.Store.YES, Field.Index.TOKENIZED));
			doc10.add(new Field("site_name","",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc11 = new Document();
			doc11.add(new Field("content", "石油地震勘探解释图件(石油行业标准) ",
					Field.Store.YES, Field.Index.TOKENIZED));
			doc11.add(new Field("site_name","",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc12 = new Document();
			doc12.add(new Field("content", "热爱,石油软件｜地球物理｜石油软件开发 ",
					Field.Store.YES, Field.Index.TOKENIZED));
			doc12.add(new Field("site_name","",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc13 = new Document();
			doc13.add(new Field("content", "大家学习学习  附件：石油构造分析.",
					Field.Store.YES, Field.Index.TOKENIZED));
			doc13.add(new Field("site_name","",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc14 = new Document();
			doc14.add(new Field("content", "博研石油论坛-石油资料交流-石油标准",
					Field.Store.YES, Field.Index.TOKENIZED));
			doc14.add(new Field("site_name","",Field.Store.YES,Field.Index.UN_TOKENIZED));
			
			Document doc15 = new Document();
			doc15.add(new Field("content", "海洋石油钻井。   石油   石油钻井  ",
					Field.Store.YES, Field.Index.TOKENIZED));
			doc15.add(new Field("site_name","",Field.Store.YES,Field.Index.UN_TOKENIZED));*/
			
			writer.addDocument(doc1);
			writer.addDocument(doc2);
			writer.addDocument(doc3);
			writer.addDocument(doc4);
			writer.addDocument(doc5);
			writer.addDocument(doc6);
			writer.addDocument(doc7);
			writer.addDocument(doc8);
			/*
			
			writer.addDocument(doc9);
			writer.addDocument(doc10);
			writer.addDocument(doc11);
			writer.addDocument(doc12);
			writer.addDocument(doc13);
			writer.addDocument(doc14);
			writer.addDocument(doc15);*/

			writer.optimize();
			writer.close();

			IndexSearcher searcher = new IndexSearcher(ramDir);

			//RangeQuery currentBooks=new RangeQuery(new Term("date",theday),new Term("date",curday),false);
			/*Term term = new Term("content", "石油");

			Query query = new TermQuery(term);
			Hits hits = searcher.search(query);
			int num = hits.length();
			for (int i = 0; i < num; i++) {
				System.out.println("序号:" + i);
				System.out.println("得分：" + hits.score(i));
				System.out.println("内容：" + hits.doc(i).get("content"));
				System.out.println("分析："
						+ searcher.explain(query, hits.id(i)).toString());
			}*/
			
			//进行过滤器检索
			/*Term term = new Term("content", "石油");
			Query query = new TermQuery(term);*/
			
			//QueryFilter fq = new QueryFilter(QueryParser.);
			
			Sort sort = new Sort();
			SortField f1 = new SortField("create_date",true);
			//SortField f2 = new SortField("crawler_time", false);
			//SortField f3 = new SortField("url_id",SortField.INT, false);
			//Sort sort=new Sort("create_date",true);
			sort.setSort(new SortField[]{f1,new SortField("priority",SortField.INT,false),new SortField(null,SortField.DOC,true)});
			
			int n=10;
			DateFormat df=new DateFormat();

			//这里theday和curday指的是时间范围内，不包括theday和curday，为一个开区间
			String curday = df.getTheDay(1);
			String theday=df.getTheDay(-n);
			System.out.println(theday);
			System.out.println(curday);
			RangeQuery recentDayQuery =new RangeQuery(new Term("create_date",theday),new Term("create_date",curday),false);
			Hits hits = searcher.search(recentDayQuery, new SiteNameFilter(),sort);
			int num = hits.length();
			for (int i = 0; i < num; i++) {
				System.out.println("序号:" + i);
				System.out.println("得分：" + hits.score(i));
				System.out.println("优先级：" + hits.doc(i).get("priority"));
				System.out.println("文档序号：" + hits.id(i));
				System.out.println("来源：" + hits.doc(i).get("site_name"));
				System.out.println("时间：" + hits.doc(i).get("create_date"));
				System.out.println("内容：" + hits.doc(i).get("content") +"\n");
			}
			searcher.close();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
