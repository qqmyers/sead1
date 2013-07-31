package edu.illinois.ncsa.medici.geowebapp.server;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.illinois.ncsa.medici.geowebapp.client.service.WmsProxyService;
import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;

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
	public LayerInfo[] getCapabilities() {
		String WMS_URL = "http://sead.ncsa.illinois.edu/geoserver/wms?request=GetCapabilities";
		String requestUrl = WMS_URL;
		HttpGet httpget = new HttpGet(requestUrl);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		try {
			String responseStr = httpclient.execute(httpget, responseHandler);
			// System.out.println(responseStr);
			return getLayerInfos(responseStr);
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

	public LayerInfo[] getLayerInfos(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		List<LayerInfo> infoList = new ArrayList<LayerInfo>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document dom = docBuilder
				.parse(new ByteArrayInputStream(xml.getBytes()));

		Element docEle = dom.getDocumentElement();
		NodeList layerList = docEle.getElementsByTagName("Layer");
		for (int i = 0; i < layerList.getLength(); i++) {
			Node layer = layerList.item(i);
			LayerInfo layerInfo = getLayerInfo(layer);
			if (layerInfo.getName() == null)
				continue;
			if (layerInfo.getName().startsWith("medici:")) {
				infoList.add(layerInfo);
			}
		}
		return infoList.toArray(new LayerInfo[infoList.size()]);
	}

	public LayerInfo getLayerInfo(Node layer) {
		LayerInfo layerInfo = new LayerInfo();
		NodeList childNodes = layer.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node e = childNodes.item(i);
			if (e.getNodeName().toUpperCase().equals("NAME")) {
				Node text = e.getFirstChild();
				String name = text.getNodeValue();
				layerInfo.setName(name);
			}
			if (e.getNodeName().toUpperCase().equals("BOUNDINGBOX")) {
				NamedNodeMap attributes = e.getAttributes();
				Node namedItem = attributes.getNamedItem("CRS");
				String crsText = namedItem.getTextContent();
				if (crsText.startsWith("EPSG:")) {
					layerInfo.setCrs(crsText);
					Node minx = attributes.getNamedItem("minx");
					layerInfo
							.setMinx(Double.parseDouble(minx.getTextContent()));

					Node miny = attributes.getNamedItem("miny");
					layerInfo
							.setMiny(Double.parseDouble(miny.getTextContent()));

					Node maxx = attributes.getNamedItem("maxx");
					layerInfo
							.setMaxx(Double.parseDouble(maxx.getTextContent()));

					Node maxy = attributes.getNamedItem("maxy");
					layerInfo
							.setMaxy(Double.parseDouble(maxy.getTextContent()));
				}
			}
		}
		System.out.println("getLayerInfo: " + layerInfo);
		return layerInfo;
	}

	@Override
	public LayerInfo[] getLayers(String tag) {
		GeoServerRestUtil.server = getServletContext().getInitParameter(
				"geoserver.host");
		GeoServerRestUtil.geoserverRestUrl = getServletContext()
				.getInitParameter("geoserver.rest.url");
		GeoServerRestUtil.user = getServletContext().getInitParameter(
				"geoserver.user");
		GeoServerRestUtil.pw = getServletContext().getInitParameter(
				"geoserver.pw");

		MediciRestUtil.tagRestUrl = getServletContext().getInitParameter(
				"medici.rest.url");
		MediciRestUtil.user = getServletContext().getInitParameter(
				"medici.user");
		MediciRestUtil.pw = getServletContext().getInitParameter("medici.pw");

		List<LayerInfo> layers = null;
		if (tag == null || tag.trim().equals("")) {
			layers = GeoServerRestUtil.getLayers();
		} else {
			layers = GeoServerRestUtil.getLayersByTag(tag);
		}
		return layers.toArray(new LayerInfo[layers.size()]);
	}

	@Override
	public String[] getUrls() {
		String[] urls = new String[2];
		urls[0] = getServletContext().getInitParameter("geoserver.wms.url");
		urls[1] = getServletContext().getInitParameter("medici.dataset.url");
		
		return urls;
	}

}
