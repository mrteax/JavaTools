package resource.query;

import java.sql.ResultSet;

import jeasy.analysis.*;

public class updateHotWord {

	/**
	 * @param args
	 */
	public void update(String word)
	{
		int inspecialword=0;
		String flag="";
		DataBaseConnect dbc=new DataBaseConnect("/opt/oilsearch/newssearch/conf/config.xml");
		
		String wordSql= "select * from newshotwords where wordname = '"+word+"' ";
		ResultSet r=dbc.executeQuery(wordSql);
		try{
			
			if(r.next())
			{
				String updateSql = "update newshotwords set ratings=ratings+1 where wordname='"+ word+"'";
				dbc.executeUpdate(updateSql);
				
			}
			else
			{
				String isspecialword="select * from  specialword where ci_name ='"+word+"'";
				ResultSet is= dbc.executeQuery(isspecialword);
				if(is.next())
				{
					
					inspecialword=1;
				}
				System.out.println();
				String[] insertSql=new String[1];
				insertSql[0]="insert into newshotwords(hotwords_id,wordname,ratings,inspwords) values(newshotwords_id.nextval,'"+word+"',1,"+inspecialword+")";
				System.out.println(insertSql[0]);
				dbc.TransAction(insertSql);
			}
		dbc.closeConnection();
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		// TODO �Զ���ɷ������
		MMAnalyzer analyzer=new MMAnalyzer();
		String word="";
		try{
			word=analyzer.segment("石油", " ");
		}catch(Exception e){
			e.printStackTrace();
		}
		String words[]=word.split(" ");
		for(int i=0;i<words.length;i++)
		{
			updateHotWord u= new updateHotWord();
			u.update(words[i]);
			System.out.println(words[i]);
		}
		
		
	}

}
