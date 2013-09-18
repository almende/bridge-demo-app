package com.almende.bridge.edxl;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class EDXLParser {
	private static SAXBuilder builder = new SAXBuilder();
	static final Logger log = Logger.getLogger("EDXLParser");
	
	public static String getStringByPath(Element from, String[] path){
		Element elem= getElementByPath(from,path);
		if (elem != null) return elem.getText();
		return "";
	}

	public static String getStringByPath(Element from, String path){
		return getStringByPath(from,new String[]{path});
	}
	@SuppressWarnings("rawtypes")
	public static Element getElementByPath(Element from, String[] path){
		try {
			Element elem = from;
			for (String tag : path){
				List children = elem.getChildren();
				elem = null;
				for (int i=0; i<children.size(); i++){
					if (((Element)children.get(i)).getName().equalsIgnoreCase(tag)){
						elem = (Element)children.get(i);
						break;
					}
				}
				if (elem == null){
					return null;
				}
			}
			return elem;
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public static List<Element> getElementsByType(Element from, String type){
		List<Element> result = new ArrayList<Element>();
		for (Object chld : from.getChildren()){
			Element elem = (Element) chld;
			if (type.equalsIgnoreCase(elem.getName())) result.add(elem);
		}
		return result;
	}

	public static EDXLRet parseXML(String xml) {
		try {
			StringReader reader = new StringReader(xml);
			char[] cbuf = new char[1];
			if (reader.read(cbuf, 0, 1) == 1){
				if (cbuf[0] != '\uFEFF'){
					reader.reset();
				} else {
					System.err.println("Skipped BOM.");
				}
			};
			
		    Document document = builder.build(new InputSource(reader));
		    Element rootElement = document.getRootElement();
		    String msgType = rootElement.getName();
		    if ("EDXLDistribution".equalsIgnoreCase(msgType)){
		    	rootElement = (Element) getElementByPath(rootElement,new String[]{
		    		"contentObject",
		    		"xmlContent",
		    		"embeddedXMLContent"
		    	}).getChildren().get(0);
		    	msgType = rootElement.getName();
		    }
//		    if ("RequestResource".equalsIgnoreCase(msgType)) return parseRequestResource(rootElement);
//		    if ("ReleaseResource".equalsIgnoreCase(msgType)) return parseReleaseResource(rootElement);
		    return new EDXLParser().new EDXLRet(document,rootElement,msgType);
		} catch (Exception e) {
			e.printStackTrace();
			log.warning("Exception parsing EDXL:"+e.getMessage());
		} 
		return null;
	}

	public class EDXLRet{
		Document doc;
		String msgType;
		Element root;
		EDXLRet(Document doc, Element root, String msgType){
			this.doc=doc;
			this.msgType=msgType;
			this.root=root;
		}
		public Document getDoc() {
			return doc;
		}
		public void setDoc(Document doc) {
			this.doc = doc;
		}
		public String getMsgType() {
			return msgType;
		}
		public void setMsgType(String msgType) {
			this.msgType = msgType;
		}
		public Element getRoot() {
			return root;
		}
		public void setRoot(Element root) {
			this.root = root;
		}
	}
}
