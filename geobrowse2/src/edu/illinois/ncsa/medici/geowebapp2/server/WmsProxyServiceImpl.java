package edu.illinois.ncsa.medici.geowebapp2.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.illinois.ncsa.medici.geowebapp2.client.Geowebapp2;
import edu.illinois.ncsa.medici.geowebapp2.client.service.WmsProxyService;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

@SuppressWarnings("serial")
public class WmsProxyServiceImpl extends RemoteServiceServlet implements
		WmsProxyService {
	private HttpClient httpclient = new DefaultHttpClient();

	@Override
	public String getCapabilities() {
		String requestUrl = Geowebapp2.URL;
		HttpGet httpget = new HttpGet(requestUrl);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		try {
			String responseStr = httpclient.execute(httpget, responseHandler);
			return getLayerNames(responseStr);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getLayerNames(String xml) throws ParserConfigurationException, SAXException, IOException {
		List<String> nameList = new ArrayList<String>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document dom = docBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
		
		Element docEle = dom.getDocumentElement();
		NodeList layerList = docEle.getElementsByTagName("Layer");
		for(int i=0;i < layerList.getLength(); i++) {
			Node layer = layerList.item(i);
			String layerName = getLayerName(layer);
			if(layerName.startsWith("medici:")) {
				nameList.add(layerName);
			}
		}
		if (!nameList.isEmpty()) {
			String list = "";
			for (String n : nameList) {
				list += n + ",";
			}
			String namelist = list.substring(0, list.length() - 1);
			return namelist;
		}
		
		return null;
	}

	public String getLayerName(Node layer) {
		NodeList childNodes = layer.getChildNodes(); 
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node e = childNodes.item(i);
			if (e.getNodeName().toUpperCase().equals("NAME")) {
				Node text = e.getFirstChild();
				String name = text.getNodeValue();
				System.out.println(name);
				return name;
			}
		}
		return "";
	}
	
}
