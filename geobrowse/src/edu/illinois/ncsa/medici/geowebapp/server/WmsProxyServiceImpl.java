package edu.illinois.ncsa.medici.geowebapp.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.acr.common.MediciProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.illinois.ncsa.medici.geowebapp.client.service.WmsProxyService;
import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

@SuppressWarnings("serial")
public class WmsProxyServiceImpl extends ProxiedRemoteServiceServlet implements
		WmsProxyService {
	
	protected static Log log = LogFactory.getLog(WmsProxyServiceImpl.class);

	
	@Override
	public LayerInfo[] getCapabilities() {
		dontCache();
		
		try {
		MediciProxy mp = getProxy();
		String responseStr = mp.executeAuthenticatedGeoGet("/wms", "request=GetCapabilities");
			return getLayerInfos(responseStr);
		} catch (Exception e) {
			invalidateSession();
			log.warn("Error getting capabilities: " + e);
		}
		return null;
	}

	public LayerInfo[] getLayerInfos(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		dontCache();
		
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
		
		dontCache();
		
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
					layerInfo.setSrs(crsText);
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
		log.debug("getLayerInfo: " + layerInfo);
		return layerInfo;
	}


}
