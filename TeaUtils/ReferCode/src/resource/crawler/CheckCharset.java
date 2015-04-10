package resource.crawler;

public class CheckCharset
{

	/**
	 * 编码是否有效
	 * 
	 * @param text
	 * @return
	 */
	public static boolean Utf8codeCheck(String text)
	{
		String sign = "";
		if (text.startsWith("%e"))
			for (int i = 0, p = 0; p != -1; i++)
			{
				p = text.indexOf("%", p);
				if (p != -1)
					p++;
				sign += p;
			}
		return sign.equals("147-1");
	}

	/**
	 * 是否Utf8Url编码
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isUtf8Url(String text)
	{
		text = text.toLowerCase();
		int p = text.indexOf("%");
		if (p != -1 && text.length() - p > 9)
		{
			text = text.substring(p, p + 9);
		}
		return Utf8codeCheck(text);
	}
	
	public static String getEncoding(String text)
	{
		String encoding="";
		if(isUtf8Url(text))
		{
			encoding="UTF-8";
		}else
		{
			encoding="GBK";
		}
		return encoding;
	}
	
	public static boolean isValidUtf8(byte[] b,int aMaxCount){

	       int lLen=b.length,lCharCount=0;

	       for(int i=0;i<lLen && lCharCount<aMaxCount;++lCharCount){

	              byte lByte=b[i++];//to fast operation, ++ now, ready for the following for(;;)

	              if(lByte>=0) continue;//>=0 is normal ascii

	              if(lByte<(byte)0xc0 || lByte>(byte)0xfd) return false;

	              int lCount=lByte>(byte)0xfc?5:lByte>(byte)0xf8?4

	                     :lByte>(byte)0xf0?3:lByte>(byte)0xe0?2:1;

	              if(i+lCount>lLen) return false;

	              for(int j=0;j<lCount;++j,++i) if(b[i]>=(byte)0xc0) return false;

	       }
	       return true;
	}
}
