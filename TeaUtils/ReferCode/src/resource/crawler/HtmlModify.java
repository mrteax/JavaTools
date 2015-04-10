package resource.crawler;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class HtmlModify {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自动生成方法存根

	}

	/*
	 * 修改网页中的链接
	 */	
	public static String urlModifying(String html,String base)
	{
		Parser p=new Parser();
		Page page = new Page();
		page.setBaseUrl(base);
		try {
			p.setInputHTML(html);
			NodeList nl=getNodeList(p.elements());
			attributeModifying(nl,page,"href");
			attributeModifying(nl,page,"src");
			
		//	setCharset(nl);
			html=nl.toHtml();
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		
		return html;
	}
	
	
	public static void urlModifying(NodeList nl,String base)
	{
		Page page = new Page();
		page.setBaseUrl(base);
		attributeModifying(nl,page,"href");
		attributeModifying(nl,page,"src");
	}
	
	
	/*
	 * 修改attribute的值
	 */
	public static void attributeModifying(NodeList nl,Page page,String attribute)
	{
		NodeList al = nl.extractAllNodesThatMatch(new HasAttributeFilter(attribute),true);
		for (int i =0; i < al.size(); i++) {
			TagNode  tn=(TagNode)al.elementAt(i);
			String href=tn.getAttribute(attribute);
		//	System.out.println(page.getAbsoluteURL(href));
			tn.setAttribute(attribute, page.getAbsoluteURL(href));
		}
	}
	/*
	 * 由NodeIterator得到NodeList
	 */
	public static NodeList getNodeList(NodeIterator ni)
	{
		NodeList nl=new NodeList();
		try {
			while(ni.hasMoreNodes())
			{
				nl.add(ni.nextNode());
			}
		} catch (ParserException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		return nl;
	}
	
	
	
	/*
	 * <meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
	 */
	public static void setCharset(NodeList nl)
	{
		NodeFilter nf =new AndFilter(new TagNameFilter("meta"), new HasAttributeFilter("http-equiv", "Content-Type"));
		NodeList mnl=nl.extractAllNodesThatMatch(nf,true);// =getNodeList(p,nf);
		if(mnl==null ||mnl.size()==0)return;
//		System.out.println(mnl.size());
		MetaTag mt=(MetaTag)mnl.elementAt(0);
		mt.setAttribute("content", "text/html; charset=gb2312");
	}
}
