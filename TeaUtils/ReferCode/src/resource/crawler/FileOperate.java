package resource.crawler;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.Vector;

public class FileOperate
{
	public FileOperate()
	{
	}

	/**
	 * 创建多级文件夹
	 * 
	 * @param folderPath
	 *            String �� c:/fqf
	 * @return boolean
	 * @throws Exception
	 */
	public void newFolder(String folderPath)
	{
		try
		{
			File myFilePath = new File(folderPath);
			if (!myFilePath.exists())
			{
				myFilePath.mkdirs();
			}
		}
		catch (Exception e)
		{
			System.out.println("�½�Ŀ¼������");
			e.printStackTrace();
		}
	}

	/**
	 * �½��ļ�
	 * 
	 * @param filePathAndName
	 *            String �ļ�·������� ��c:/fqf.txt
	 * @param fileContent
	 *            String �ļ�����
	 * @return boolean
	 */
	public void newFile(String filePathAndName, String fileContent)
	{

		try
		{
			File myFilePath = new File(filePathAndName);
			if (!myFilePath.exists())
			{
				myFilePath.createNewFile();
			}
			FileWriter resultFile = new FileWriter(myFilePath);
			PrintWriter myFile = new PrintWriter(resultFile);
			String strContent = fileContent;
			myFile.println(strContent);
			resultFile.close();

		}
		catch (Exception e)
		{
			System.out.println("�½�Ŀ¼������");
			e.printStackTrace();

		}

	}

	public void newFileEncoding(String filePath,String fileContent,String charset){
		FileOutputStream fos;
		Writer writer ;
		try{
			String content = fileContent;
			//byte[] bytes = content.getBytes("GB2312");
			fos = new FileOutputStream(new File(filePath));
			writer = new BufferedWriter(new OutputStreamWriter(fos,charset));
			//OutputStreamWriter osw = new OutputStreamWriter(fos,charset);
			writer.write(content);
			writer.close();
			fos.close();
		}catch(Exception e){
			System.out.println("创建失败");
			System.out.println(e.getMessage());
			e.printStackTrace();
		} 
		
		
	}
	
	/**
	 * ɾ���ļ�
	 * 
	 * @param filePathAndName
	 *            String �ļ�·������� ��c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public void delFile(String filePathAndName)
	{
		try
		{
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myDelFile = new File(filePath);
			myDelFile.delete();

		}
		catch (Exception e)
		{
			System.out.println("ɾ���ļ�������");
			e.printStackTrace();

		}

	}

	/**
	 * ɾ���ļ���
	 * 
	 * @param filePathAndName
	 *            String �ļ���·������� ��c:/fqf
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public void delFolder(String folderPath)
	{
		try
		{
			delAllFile(folderPath); // ɾ����������������
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // ɾ����ļ���

		}
		catch (Exception e)
		{
			System.out.println("ɾ���ļ��в�����");
			e.printStackTrace();

		}

	}

	/**
	 *深度删除 ɾ���ļ�������������ļ�
	 * 
	 * @param path
	 *            String �ļ���·�� �� c:/fqf
	 */
	public void delAllFile(String path)
	{
		File file = new File(path);
		if (!file.exists())
		{
			return;
		}
		if (!file.isDirectory())
		{
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++)
		{
			if (path.endsWith(File.separator))
			{
				temp = new File(path + tempList[i]);
			}
			else
			{
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile())
			{
				temp.delete();
			}
			if (temp.isDirectory())
			{
				delAllFile(path + "/" + tempList[i]);// ��ɾ���ļ���������ļ�
				delFolder(path + "/" + tempList[i]);// ��ɾ����ļ���
			}
		}
	}
/*
 * 
 */
	public void delAllFiles(String path)
	{
		File file = new File(path);
		if (!file.exists())
		{
			return;
		}
		if (!file.isDirectory())
		{
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++)
		{
			if (path.endsWith(File.separator))
			{
				temp = new File(path + tempList[i]);
			}
			else
			{
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile())
			{
				temp.delete();
			}
//			if (temp.isDirectory())
//			{
//				delAllFile(path + "/" + tempList[i]);// ��ɾ���ļ���������ļ�
//				delFolder(path + "/" + tempList[i]);// ��ɾ����ļ���
//			}
		}
	}
	/**
	 * ���Ƶ����ļ�
	 * 
	 * @param oldPath
	 *            String ԭ�ļ�·�� �磺c:/fqf.txt
	 * @param newPath
	 *            String ���ƺ�·�� �磺f:/fqf.txt
	 * @return boolean
	 */
	public void copyFile(String oldPath, String newPath)
	{
		try
		{
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists())
			{ // �ļ�����ʱ
				InputStream inStream = new FileInputStream(oldPath); // ����ԭ�ļ�
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1)
				{
					bytesum += byteread; // �ֽ��� �ļ���С
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		}
		catch (Exception e)
		{
			System.out.println("���Ƶ����ļ�������");
			e.printStackTrace();

		}

	}

	/**
	 * ��������ļ�������
	 * 拷贝下面的文件
	 * @param oldPath
	 *            String ԭ�ļ�·�� �磺c:/fqf
	 * @param newPath
	 *            String ���ƺ�·�� �磺f:/fqf/ff
	 * @return boolean
	 */
	public void copyFolder(String oldPath, String newPath)
	{
		try
		{
			(new File(newPath)).mkdirs(); // ����ļ��в����� ��b���ļ���
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++)
			{
				if (oldPath.endsWith(File.separator))
				{
					temp = new File(oldPath + file[i]);
				}
				else
				{
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile())
				{
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1)
					{
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("��������ļ������ݲ�����");
			e.printStackTrace();
		}
	}
	/*
	 * 拷贝下面的文件和文件夹
	 */
	public void deepCopyFolder(String oldPath, String newPath)
	{
		try
		{
			(new File(newPath)).mkdirs(); // ����ļ��в����� ��b���ļ���
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++)
			{
				if (oldPath.endsWith(File.separator))
				{
					temp = new File(oldPath + file[i]);
				}
				else
				{
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile())
				{
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1)
					{
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory())
				{// ��������ļ���
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("��������ļ������ݲ�����");
			e.printStackTrace();
		}
	}

	/**
	 * �ƶ��ļ���ָ��Ŀ¼
	 * 
	 * @param oldPath
	 *            String �磺c:/fqf.txt
	 * @param newPath
	 *            String �磺d:/fqf.txt
	 */
	public void moveFile(String oldPath, String newPath)
	{
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	/**
	 * �ƶ��ļ���ָ��Ŀ¼
	 * 
	 * @param oldPath
	 *            String �磺c:/fqf.txt
	 * @param newPath
	 *            String �磺d:/fqf.txt
	 */
	public void moveFolder(String oldPath, String newPath)
	{
		copyFolder(oldPath, newPath);
		delFolder(oldPath);
	}

	/**
	 * ɾ���ļ�
	 * 
	 * @param filePathAndName
	 *            String �ļ�·������� ��c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	// String filePath,String fileName
	public Vector<String> readFile(String filePathAndName) throws IOException
	{
		String line = new String();
		Vector<String> v = new Vector<String>();
		try
		{
			FileReader fr = new FileReader(filePathAndName);
			BufferedReader br = new BufferedReader(fr);
			line = br.readLine();

			while (line != null)
			{
				v.add(line);
				line = br.readLine();
			}
			br.close();
			fr.close();
		}
		catch (Exception e)
		{
			System.out.print(e);
			return null;
		}
		return v;
	}
	/*
	 * 参数文件绝对路径+文件名
	 * 
	 * 返回：文件内容
	 */
	public static String readFileStr(String filePathAndName)
	{
		String line = new String();
		String content="";
		try
		{
			FileReader fr = new FileReader(filePathAndName);
			BufferedReader br = new BufferedReader(fr);
			line = br.readLine();

			while (line != null)
			{
				content+=line+ System.getProperty("line.separator"); 
				line = br.readLine();
			}
			br.close();
			fr.close();
		}
		catch (Exception e)
		{
			System.out.print(e);
			return null;
		}
		return content;
	}
	
	/*
	 * 把content加入到path文件中
	 * 
	 * 如果文件不存在，新建一个
	 */
	
	public void appendfile(String path,String content)
	{
        try {   
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件   
             FileWriter writer = new FileWriter(path, true);   
             writer.write(content);   
             writer.close();   
         } catch (IOException e) {   
             e.printStackTrace();   
         }   
	}
	
	public void a()
	{
		String path="D:\\";
		RandomAccessFile rf;
		try {
			rf = new RandomAccessFile(path + "\\test.txt","rw");
			//定义一个类RandomAccessFile的对象，并实例化
			rf.seek(rf.length());//将指针移动到文件末尾
			rf.writeBytes("Append a line to the file!");
			rf.close();//关闭文件流
			System.out.println("写入文件内容为：<br>");
			FileReader fr=new FileReader(path + "\\test.txt");
			BufferedReader br=new BufferedReader(fr);//读取文件的BufferedRead对象
			String Line=br.readLine();
			while(Line!=null){
			System.out.println(Line + "<br>");
			Line=br.readLine();
			}
			fr.close();//关闭文件
		} catch (FileNotFoundException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}

	}
	 /*
	  * 返回路径下的
	  * 符合filter的文件列表
	  */
	 public String[] getfileList(String path,String filter)
	 {
		 File f=new File(path);
		 FileAccept acceptCondition=new FileAccept(filter);
		 return f.list(acceptCondition);
	 }
	
	 public void test()
	 {
		  File dir=new File("D:\\");
		  File deletedFile=new File(dir,"E.java"); 
		  FileAccept acceptCondition=new FileAccept("java");      //?有点不懂
		  File fileName[]=dir.listFiles(acceptCondition);
		  for(int i=0;i<fileName.length;i++)
		  {
		   System.out.println("文件名称："+fileName[i].getName());
		  }
		  boolean boo=deletedFile.delete();
		  if(boo)
		  {
		   System.out.println("文件:"+deletedFile.getName()+"被删除");
		  }
	 }
	//public class dierge
	
	 public static void main(String args[]) throws Exception
	 {
		long s=System.currentTimeMillis();
		FileOperate fo =new FileOperate();
//		System.out.println(System.currentTimeMillis()-s);
//		FileOperate.readFileStr("1.html");
//		System.out.println(System.currentTimeMillis()-s);
		//fo.delAllFile("c:/1");
		//fo.copyFolder("c:/inner", "c:/2");
		//fo.copyFile("C:/inner/output0706-0707/网页总字数.txt", "c:/2/网页总字数.txt");
		//fo.deepCopyFolder("c:/inner", "c:/2");
		//fo.delAllFile("c:/2");
		//fo.delAllFiles("c:/2");
		//fo.delFile("c:/2");
		//fo.delFolder("c:/2");
		//String[] files=fo.getfileList("c:/inner", "txt");
		//for(int i=0;i<files.length;i++)
			//System.out.println(files[i]);
		//fo.moveFile("c:/2/TextSocketServer.java", "c:/1/1.txt");
		//fo.appendfile("c:/1/1.txt","wwwj");
		//fo.moveFolder("c:/1", "c:/3");
		//fo.newFile("c:/3/3.txt", "nihaoma");
		//fo.newFolder("c:/1/1/1/1/1/1");
		//Vector<String> ss=fo.readFile("c:/3/1.txt");
		//System.out.println(ss.size());
		//for(int i=0;i<ss.size();i++)
		//	System.out.println(ss.get(i));
		//System.out.println(fo.readFileStr("c:/3/1.txt"));
		
		//System.out.println(fo.getCnAscii('u'));
//		fo.appendfile("E:\\test.txt", "追加到文件的末尾");
//		System.out.println("http://www.hqcec.com/cn/news/pressreleases/20061011135849_963909.htm");
//		fo.copyFile("E:\\test.txt", "D:\\test.txt");
	 }
	 /**
	  * 获得单个汉字的Ascii，并用"-"连接成一个字符串
	  * 
	  * @param cn char 汉字字符
	  * @return string 错误返回 空字符串,否则返回ascii
	  */
	 public String getCnAscii(char cn) {
	     byte[] bytes = (String.valueOf(cn)).getBytes();
	     if (bytes == null || bytes.length > 2 || bytes.length <= 0) { // 错误
	         return "";
	     }
	     if (bytes.length == 1) { // 英文字符
	    	 System.out.println(bytes[0]);
	         return new String(bytes);
	     }
	     if (bytes.length == 2) { // 中文字符
	         int hightByte = 256 + bytes[0];
	         int lowByte = 256 + bytes[1];

	         String ascii = hightByte + "-" + lowByte;

	         System.out.println("ASCII=" + ascii);

	         return ascii;
	     }

	     return ""; // 错误
	 }

}




class FileAccept implements FilenameFilter
{
	 String str =null;
	 FileAccept(String s)
	 {
	  str="."+s;
	 }
	 public boolean accept(File dir,String name)      //name被实例化目录中的一个文件名，dir为调用List的当前目录对象
	 {
		// System.out.println("over"+name);
	  return name.endsWith(str);            //测试此字符串是否以指定的后缀结束,boolean 
	 }
}	
