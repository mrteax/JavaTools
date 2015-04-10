package resource.query;


import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

public class ConfigXmlRead {
	
	private String configXmlPath=null;
	private XPath xpathEngine=null;
	private InputSource xmlSource=null;
	
	public ConfigXmlRead(String configXmlPath)
	{
		this.configXmlPath=configXmlPath;
		xpathEngine = XPathFactory.newInstance().newXPath();
		xmlSource = new InputSource(configXmlPath);
	}
	
	public String getAttribute(String attrPathName)
	{
		String result="";
		try {
			result = xpathEngine.evaluate(attrPathName, xmlSource);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
