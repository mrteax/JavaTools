package resource.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormat {
	/*
	 * 给定的日期字符串(可以是包含日期的字符串)
	 * //2009-08-29//2009-11-4 15:48:44//2009-10-22 9:12:23//2008年3月21日
	 * 
	 * 不能解析则返回当前时间
	 * 
	 * 返回统一格式的日期字符串"yyyy-MM-dd"
	 */
	public String dateformat(String date)
	{
		date=date.replaceAll("年", "-");
		date=date.replaceAll("月", "-");
	//	System.out.println("date:"+date);
		SimpleDateFormat normalf =new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat f ;//= new SimpleDateFormat("yy-MM-dd");
		
		String patternStr; 
		Pattern p;
		Matcher m;

		try {
			patternStr="\\d{4}-\\d{1,2}-\\d{1,2}"; 
			p = Pattern.compile(patternStr);
			m = p.matcher(date);
			
			if(m.find())
			{
				date=date.substring(m.start(), m.end());
				date=normalf.format(normalf.parse(date));
				return date;
			}

			patternStr="\\d{2}-\\d{1,2}-\\d{1,2}"; 
			p = Pattern.compile(patternStr);
			m = p.matcher(date);
			if(m.find())
			{
				date=date.substring(m.start(), m.end());
				f = new SimpleDateFormat("yy-MM-dd");
				date=normalf.format(f.parse(date));
				return date;
			}
			patternStr="\\d{8}"; 
			p = Pattern.compile(patternStr);
			m = p.matcher(date);
			if(m.find())
			{
				date=date.substring(m.start(), m.end());
				f = new SimpleDateFormat("yyMMdd");
				date=normalf.format(f.parse(date));
				return date;
			}
			date =GetDate();
			
		}catch (ParseException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		
		//SimpleDateFormat f = new SimpleDateFormat("yyyyMMddhhmmss");
		
		return date;
	}
	
	/*
	 * 返回系统时间
	 * 格式为：yyyy-MM-dd
	 */
	public String GetDate()
	{
		Date   currentTime   =   new  Date();   
        String   datestr="";   
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");   
        datestr=formatter.format(currentTime);
		return datestr;
	}
	
	/*
	 * 返回系统时间
	 * 格式为：yyyy-MM-dd hh:mm:ss
	 */
	public String GetTime()
	{
		Date   currentTime   =   new  Date();   
        String   timestr="";   
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");   
        timestr=formatter.format(currentTime);
		return timestr;
	}
	 /**
	   * LST
	   * num为正:当前日期后num天是返回值
	   * num为负:当前日期前num天是返回值
	   * 返回的日期的格式:yyyy-MM-dd
	   */
	 public String getTheDay(int num){
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    GregorianCalendar gc =   new GregorianCalendar();
		    gc.add(GregorianCalendar.DATE, num);
		    Date theday =gc.getTime();
		    return sdf.format(theday);
		 }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自动生成方法存根
		DateFormat df= new DateFormat();
		System.out.println(df.dateformat("2008年3月21日"));
		
		System.out.println(df.getTheDay(-1));
	}

}
