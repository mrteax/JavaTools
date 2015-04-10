package resource.crawler;

import resource.database.DataBaseConnect;

public class Clear {

	/*
	 * 清空数据库
	 */
	void clear()
	{
		DataBaseConnect dbc=new DataBaseConnect("conf/config.xml");
		String sql[];
		sql=new String[2];
		sql[0]="delete from newssite where SITE_ID<>10";
		sql[1]="delete from newsurl where URL_ID<>0";
		try{
			dbc.TransAction(sql);
		}catch (Exception e){	
			System.out.println(e.getMessage());
		}
		dbc.closeConnection();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自动生成方法存根
		Clear c =new Clear();
		c.clear();
	}

}
