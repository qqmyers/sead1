package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.NotFoundException;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.CopyFile;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.ImagePyramidBean;
import edu.uiuc.ncsa.cet.bean.ImagePyramidTileBean;
import edu.uiuc.ncsa.cet.bean.tupelo.ImagePyramidBeanUtil;

public class ImagePyramidServlet extends HttpServlet {
	Log log = LogFactory.getLog(ImagePyramidServlet.class);

	/*
	 * >>> I am now calling the code in the image extractor to create the
>>> pyramids, however they are not written to the context yet. Part of me
>>> is thinking to create a new bean that will store the image pyramid
>>> information (PreviewPyramidBean). Following are the fields that would
>>> be in this bean:
>>>
>>> ImagePyramidBean
>>> - int tilesize
>>> - int overlap
>>> - String format
>>> - int width
>>> - int height
>>> - Set< ImagePyramidTileBean> tiles
>>>
>>> ImagePyramidTileBean
>>> - int level
>>> - int col
>>> - int row
>>> - String type
>>> - blob = image
>>>
>>> The endpoint of the service would have the following structure
>>>
>>> /pyramid/{xyz}.xml
>>> returns the xml file that can be generated based from the
>>> ImagePyramidBean
>>>
>>> /pyramid/{xyz}.html
>>> returns the html file that can be generated based from the
>>> ImagePyramidBean
>>>
>>> /pyramid/{xyz}_files/{level}/{col}_{row}.{type}
>>> returns the file that tile that is found in the pyramid
>>>
>>> Please let me know if this makes sense and if there any good
>>> predicates to use.
	 */
	public ImagePyramidServlet() {
		// TODO Auto-generated constructor stub
	}

	void die(HttpServletResponse resp, String message) throws IOException {
    	PrintWriter pw = new PrintWriter(resp.getOutputStream());
    	pw.println("<h1>Not Found</h1>");
    	log.error("404: "+message);
    	pw.println(message);
    	pw.flush();
    	resp.setStatus(404);
	}

	ImagePyramidTileBean getTile(ImagePyramidBean pyramid, int level, int row, int col) {
		for(ImagePyramidTileBean tile : pyramid.getTiles()) {
			if(tile.getLevel()==level &&
					tile.getRow()==row &&
					tile.getCol()==col) {
				return tile;
			}
		}
		return null;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
        String url = req.getRequestURL().toString();
		URL requestUrl = new URL(url);
        String prefix = requestUrl.getProtocol()+"://"+requestUrl.getHost();
        if(requestUrl.getPort() != -1) {
            prefix = prefix + ":" + requestUrl.getPort();
        }
        prefix += req.getContextPath();
        prefix += req.getServletPath();
        if(prefix.length() >= url.length()) {
        	die(resp,"Illegal pyramid URL "+url);
        	return;
        }
        String suffix = requestUrl.toString().substring(prefix.length());
        if(suffix.startsWith("/uri/")) {
        	Resource uri = null;
        	try {
        		uri = Resource.uriRef(suffix.substring(5));
        	} catch(IllegalArgumentException x) {
        		die(resp,"invalid or relative uri "+uri);
        		return;
        	}
        	try {
        		Thing dataset = TupeloStore.fetchThing(uri);
        		Resource pyramidUri = dataset.getResource(ImagePyramidBeanUtil.HAS_PYRAMID);
        		if(pyramidUri == null) {
        			TupeloStore.refetch(uri);
        			dataset = TupeloStore.fetchThing(uri);
            		pyramidUri = dataset.getResource(ImagePyramidBeanUtil.HAS_PYRAMID);
        		}
        		ImagePyramidBean ipb =
        			(ImagePyramidBean) TupeloStore.fetchBean(pyramidUri);
        		log.info("GET PYRAMID "+uri);
        		produceHtml(ipb,resp,prefix+"/");
        		//resp.setStatus(resp.SC_MOVED_PERMANENTLY);
        		//resp.setHeader("Location",prefix+"/"+label+".html");
        	} catch(OperatorException x) {
        		die(resp,"failure "+x);
        		return;
        	}
        } else if(url.matches(".*/[0-9a-f-]+_files/[0-9]+/[0-9]+_[0-9]+\\.jpg")) {
			String label = url.replaceFirst(".*/(.*)_files/.*","$1");
			int level = Integer.parseInt(url.replaceFirst(".*/.*_files/([0-9]+)/.*","$1"));
			int col = Integer.parseInt(url.replaceFirst(".*/.*_files/[0-9]+/([0-9]+)_.*","$1"));
			int row = Integer.parseInt(url.replaceFirst(".*/.*_files/[0-9]+/[0-9]+_([0-9]+).*","$1"));
			try {
				ImagePyramidBean pyramid = getPyramidForLabel(label);
				ImagePyramidTileBean tile = getTile(pyramid,level,row,col);
				if(tile==null) {
					TupeloStore.refetch(pyramid);
					tile = getTile(pyramid,level,row,col);
				}
				if(tile==null) {
					die(resp,"tile not found");
				}
				log.info("GET TILE "+label+" level "+level+", ("+row+","+col+")");
				resp.setContentType("image/jpg");
				CopyFile.copy(TupeloStore.read(tile), resp.getOutputStream());
				return;
			} catch(OperatorException x) {
				log.error("failed",x);
				resp.setStatus(500);
				PrintWriter pw = new PrintWriter(resp.getOutputStream());
				x.printStackTrace(pw);
			}
		} else {
			die(resp,"GET (unrecognized) "+url);
        }
	}
	
	ImagePyramidBean getPyramidForLabel(String label) throws OperatorException {
		Resource ip = null;
		for(Resource i : TupeloStore.match(Rdfs.LABEL, label)) {
			ip = i;
		}
		if(ip == null) {
			throw new NotFoundException("no image pyramid for label "+label);
		}
		return (ImagePyramidBean) TupeloStore.fetchBean(ip);
	}
    /**
     * Create a simple HTML file that will show the pyramid.
     * 
     * @param ipb
     *            the imagepyramidbean that holds all the information
     * @param sizex
     *            the width of the area for the zoomable image in pixels
     * @param sizey
     *            the height of the area for the zoomable image in pixels
     * @throws OperatorException 
     * @throws IOException 
     */
	void produceHtml(String label, HttpServletResponse resp, String base) throws OperatorException, IOException {
		produceHtml(getPyramidForLabel(label),resp,base);
	}
	void produceHtml(ImagePyramidBean ipb, HttpServletResponse resp, String base) throws OperatorException, IOException {
		int sizex = 640;
		int sizey = 480;
		PrintWriter pw = new PrintWriter(resp.getOutputStream());
        pw.println( "<html>" );
        pw.println("<base href='"+base+"'/>");
        pw.println( "<title>" + ipb.getLabel() + "</title>" );
        pw.println( "<script type=\"text/javascript\" src=\"http://seadragon.com/ajax/embed.js\"></script>" );
        pw.println( "<body>" );
        pw.println("<pre>"); // FIXME debug
        pw.println( "<script type=\"text/javascript\">" );
        pw.println( "Seadragon.Config.debug = true;" );
        pw.println( "Seadragon.Config.imagePath = \"img/\";" );
        pw.println( String.format( "Seadragon.embed(\"%dpx\", \"%dpx\", \"%s.xml\", %d, %d, %d, %d, \"%s\");", sizex, sizey, ipb.getLabel(), ipb.getWidth(), ipb.getHeight(), ipb.getTilesize(), ipb.getOverlap(), ipb.getFormat() ) );
        pw.println( "</script>" );
        pw.println("</pre>"); // FIXME debug
        pw.println( String.format( "Image Size : %d x %d", ipb.getWidth(), ipb.getHeight() ) );
        pw.println( "</body>" );
        pw.println( "</html>" );
        pw.flush();
	}
}
