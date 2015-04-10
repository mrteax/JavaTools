package resource.index;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;


import resource.database.*;

public class UnitName {


	private DataBaseConnect dbc;
	
	public UnitName()
	{
		dbc=new DataBaseConnect("/opt/oilsearch/inner/conf/config.xml");		
	}
	
	


//通过网址得到域名。	
	public String getDomainName(String url)
	{
		String domainName="";
		
		if(url==null || "".equals(url))
			return domainName;
		
		URL gurl=null;
		try {
			gurl = new URL(url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String hostwww = gurl.getHost();//得到域名
		hostwww = hostwww.replace("/", "");		
		domainName = hostwww.replace("www.", "");
		return domainName;
	}
	
//	通过网址得到主机名。	
	public String getHostName(String url)
	{
		String hostName="";
		
		if(url==null || "".equals(url))
			return hostName;
		
		URL gurl=null;
		try {
			gurl = new URL(url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "";
		}
		hostName = gurl.getHost();//得到主机名
		return hostName;
	}
	
	
//通过域名得到ip地址	
	public String getIpAddress(String host)
	{
		String ipAddress="";
	
		if(host==null || "".equals(host))
			return ipAddress;
		
		InetAddress ts =null;
		try{
			ts = InetAddress.getByName(host);
			 if(ts!=null)
		     {
		        ipAddress=ts.getHostAddress();       	
		      }
		        
		}catch(UnknownHostException e)
		{
			e.printStackTrace();			
		}
       
        System.out.println("ipAddress:"+ipAddress);
		return ipAddress;
	}

//通过网址得到单位名称。	
	public String getUnitName(String ipAddress)
	{
		String unitName="";
	   if(ipAddress!=null && !ipAddress.equals(""))
		{
			String queryCell="select second_unit, belong_unit, from_ip, to_ip from unit_ip ";
			if(dbc.getConnection())
			{
				ResultSet rs=dbc.executeQuery(queryCell);
				try {
					while(rs.next())
					{
						String second_unit=rs.getString(1);
						String belong_unit=rs.getString(2);
						String from_ip=rs.getString(3);
						String to_ip=rs.getString(4);
						
						if(belongRangeIp(ipAddress,from_ip,to_ip ))
						{
							if((second_unit!=null)&&(!second_unit.equals("")))
							{
								unitName+=second_unit;
							}
							//if((belong_unit!=null)&&(!belong_unit.euqals("")))
							//{
							//	unitName+=","+belong_unit;
							//}
							//unitName=second_unit+","+belong_unit;
							
	//						System.out.println("unit name is:"+unitName);
							return unitName;
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
//		System.out.println("unit name is:"+unitName);
		return unitName;
	}

//判断网址的ip地址是否在范围内	
	public boolean belongRangeIp(String ip, String fromIp, String toIp)
	{
		boolean belong=false;
		String[] ipNumber=ip.split("\\.");
		String[] fromIpNumber=fromIp.split("\\.");
		String[] toIpNumber=toIp.split("\\.");
		if(ipNumber.length != fromIpNumber.length || ipNumber.length != toIpNumber.length)
			return false;
		int i=0;
		for(i=0; i<ipNumber.length; i++)
		{
//			System.out.println("ip="+Integer.valueOf(ipNumber[i]));
//			System.out.println("from="+Integer.valueOf(fromIpNumber[i]));
//			System.out.println("to="+Integer.valueOf(toIpNumber[i]));
			if(Integer.valueOf(ipNumber[i])< Integer.valueOf(fromIpNumber[i]) || 
					Integer.valueOf(ipNumber[i]) > Integer.valueOf(toIpNumber[i]))
			{
				return belong;
			}
		}
		belong=true;
		return belong;
	}
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		UnitName ws=new UnitName();
		System.out.println(ws.getIpAddress("220.181.6.81"));
	}
}
