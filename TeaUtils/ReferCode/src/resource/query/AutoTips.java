package resource.query;

import java.io.*;
import java.sql.*;

public class AutoTips{

	
	public String autotip(String input)
	{
		DataBaseConnect dbc=new DataBaseConnect("/opt/oilsearch/newssearch/conf/config.xml");
		
		
		String sql="select wordname from newshotwords where wordname like ? order by ratings desc";
		
		
		String tips="";
		try{
			PreparedStatement ps=dbc.prepareStatement(sql);
			ps.setString(1,input+"%");
			int count=0;
			ResultSet rs=ps.executeQuery();
			while(rs.next())
			{
				tips+=(rs.getString("wordname")+",");
				count++;
				if(count>14)
					break;
			}
			dbc.closeConnection();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		if(tips.indexOf(",")!=-1)
		{
			tips=tips.substring(0,tips.lastIndexOf(","));
		}
	    return tips;
	}
	public static void main(String args[])
	{
		AutoTips autotips=new AutoTips();
		String tips=autotips.autotip("胜利");
		System.out.println(tips);
	}

}
