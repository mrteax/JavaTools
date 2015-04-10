package resource.query;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;


public class DataBaseConnect {
	
	/**
	   * 类私有成员
	   * */
	private Connection conn;    //连接对像
	private String configXmlPath="/opt/oilsearch/newssearch/conf/config.xml";
	private Statement stmtQuery=null;
//	private Statement stmtUpdate=null;
	private boolean connectOkay=false;
	
	public DataBaseConnect()
	{
		this.startConnection();
	}
	
	/**
	   * 构造时即获取了一个连接
	   * */
	public DataBaseConnect(String configXmlPath)
	{
	   this.configXmlPath=configXmlPath;
	   this.startConnection();	   
	}
	
	/**
	   * 获取连接
	   * */
	public boolean getConnection()
	{
	   return connectOkay;
	}
	
	
	public void startConnection()
	{
		   //oracle(需要加载驱动程序)	  
		   String classname="oracle.jdbc.driver.OracleDriver";
		   String url = "jdbc:oracle:thin:@so.shu.edu.cn:1521:oradb";//test为数据源名称
		   String user ="oilsearch";
		   String password = "shu216";

		 
		   ConfigXmlRead cxr=new ConfigXmlRead(configXmlPath);
		   String userPath= "/config/DataBaseUser/text()";
		   String passwordPath= "/config/DataBasePassword/text()";
		   String hostPath= "/config/DataBaseHost/text()";
		   String databasePath= "/config/DataBaseName/text()";
		   
		   String host=cxr.getAttribute(hostPath);
		   String databse=cxr.getAttribute(databasePath);
		   user=cxr.getAttribute(userPath);
		   password=cxr.getAttribute(passwordPath);
		   url = "jdbc:oracle:thin:@" + host + ":1521:" + databse; 
		  
		   try
		   {
		    Class.forName(classname);
		    conn=DriverManager.getConnection(url,user,password);
		    stmtQuery=conn.createStatement();
//		    stmtUpdate=conn.createStatement();
		    
		    connectOkay=true;
		   }
		   catch(Exception ex)
		   {
		    System.out.println("exception:connect database wrong  "+ex.getMessage());
		   }
	}
	
//	
//	public boolean startConnection()
//	{
//		return connectOkay;
//	}
//	
	/**
	   * 关闭数据库连接
	   * 由于Java中没有析造
	   * 故此一定要手工关闭数据库
	   * */
	public void closeConnection()
	{
	   try
	   {
	    if(conn!=null || !conn.isClosed())
	    {
	    	stmtQuery.close();	
//	    	stmtUpdate.close();	
	     conn.close();
	    }
	   }
	   catch(Exception ex)
	   {
	    System.out.println("DateBase::CloseConnection()"+ex.getMessage());
	   }
	}
	/**
	   * 执行查询操作,返回ResultSet
	   * 注意:关闭数据连接
	   * */
	public ResultSet executeQuery(String sql)
	{
	   ResultSet rs=null;
	   if(sql==null || sql.equals(""))
	   {
	    return null;
	   }
	   try
	   {
	     rs=stmtQuery.executeQuery(sql);
	   }
	   catch(Exception ex)
	   {
	    this.closeConnection();
	    System.out.println("DateBase::ExecuteQuery(String sql)"+ex.getMessage());
	   }
	   return rs;
	}
	
	/**
	   * 执行插入操作
	   * 
	   * */
	public boolean insert(String sql)
	{
		boolean b=true;
	   if(sql==null || sql.equals(""))
	   {
	    return false;
	   }
	   try
	   {
		   b=stmtQuery.execute(sql);
	   }
	   catch(Exception ex)
	   {
		   System.out.println("QQQQQQQQQQQQ");
		   System.out.println("DateBase::ExecuteQuery(String sql)"+ex.getMessage());
		   b=false;
	   }
	   return b;
	}
	
	/**
	 * 返回预编译的PreparedStatement变量
	 */
	public PreparedStatement prepareStatement(String sql)
	{
		try
		{
			return conn.prepareStatement(sql);
		}catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *插入数据库操作，返回插入的数据库项的自动id编号,如果失败返回-1
	 */
	public int callexecuteUpdate(String sql1)   
	{
		String sql="BEGIN "+sql1+";END;";
		System.out.println(sql);
		CallableStatement sta;
		try
		{
			sta=conn.prepareCall(sql);
			sta.registerOutParameter(1,Types.NUMERIC);
			sta.executeUpdate();
			return sta.getInt(1);
		}catch(SQLException e){
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	   * 执行更删改操作,返回boolean值
	   * 注意:方法执行完自动关闭连接
	   * */
	public boolean executeUpdate(String sql)
	{
 
	   if(sql==null || sql.equals(""))
	   {
	    return false;
	   }
	 
	    int rows;
		try {
			rows = stmtQuery.executeUpdate(sql);
			 if(rows>0)
			 {
			    return true;
			  }	  
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("update the database wrong-----------");
			e.printStackTrace();
		}	    
	     
	   
	  
	   return false;
	}
	
	/**
	   * 事务操作
	   * 说明:这个针对事务操作的方法只允许select,delete,insert,update操作
	   * ----------简单示例-----------
	   * String []sql=new String[4];
	   * sql[0]="select * from test";
	   * sql[1]="delete from test";
	   * sql[2]="select * from test";
	   * //即便没有id=30这个值,但这个操作仍然返回真,因为这条语句是合法的
	   * sql[3]="update test set name='zhang' where id=30";
	   * if(db.TransAction(sql))
	   * {
	   *    System.out.println("事务成功");
	   * }
	   * else
	   * {
	   *   System.out.println("事务失败");
	   * }
	   * */
	public boolean TransAction(String sql[])
	throws SQLException
	{
	   //参数检查
	   if(sql==null || sql.length==0)
	   {
	    return false;
	   }
	   try
	   {
	    conn.setAutoCommit(true);
	    for(int i=0;i<sql.length;i++)
	    {
	     if(sql[i].indexOf("select")==0 || sql[i].indexOf("SELECT")==0)
	     {
	      this.executeQuery(sql[i]);
	     }
	     else if(!this.executeUpdate(sql[i]))
	     {
	      return false;
	     }
	    }
	    conn.commit();
	   }
	   catch(SQLException ex)
	   {
	    conn.rollback();
	    System.out.println("DataBase:TransAction(String sql[])"+ex.getMessage());
	    return false;
	   }
	   return true;
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		DataBaseConnect dbc=new DataBaseConnect("/opt/newssearch/conf/config.xml");
		ResultSet r=dbc.executeQuery("select * from newssite");
		try {
			while (r.next()) {
			System.out.println(":"+r.getString("SITE_URL"));
			}
			} catch (Exception e) {
			e.printStackTrace();
			}
//		dbc.executeUpdate("insert into student values(STUDENT_ID.nextval,'测试中文')");	
			dbc.closeConnection();

	}

}
