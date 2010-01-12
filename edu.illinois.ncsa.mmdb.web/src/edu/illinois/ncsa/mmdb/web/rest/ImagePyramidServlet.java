package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.rdf.terms.Xsd;
import org.tupeloproject.util.CopyFile;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.ImagePyramidBean;
import edu.uiuc.ncsa.cet.bean.ImagePyramidTileBean;
import edu.uiuc.ncsa.cet.bean.tupelo.ImagePyramidBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.ImagePyramidTileBeanUtil;

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
		int n = 0;
		log.info("pyramid "+pyramid.getLabel()+" [tilesize="+pyramid.getTilesize()+", height="+pyramid.getHeight()+", ntiles="+pyramid.getTiles().size()+"]");
		for(ImagePyramidTileBean tile : pyramid.getTiles()) {
			n++;
			if(tile.getLevel()==level &&
					tile.getRow()==row &&
					tile.getCol()==col) {
				return tile;
			}
		}
		log.error("examined "+n+" tiles for pyramid "+pyramid.getLabel()+" and did not find level "+level+" ("+row+","+col+")");
		return null;
	}
	
	public static final String IMAGE_PYRAMID_INFIX = "/uri=";
	
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
        if(suffix.startsWith("/uri=")) {
        	Resource uri = null;
        	try {
        		uri = Resource.uriRef(suffix.substring(5));
        	} catch(IllegalArgumentException x) {
        		die(resp,"invalid or relative uri "+uri);
        		return;
        	}
        	try {
        		ImagePyramidBean ipb = partialFetchPyramidFor(uri);
        		if(ipb == null) {
        			die(resp,"No image pyramid available for dataset "+uri);
        			return;
        		}
        		log.info("GET PYRAMID "+uri);
        		produceHtml(ipb,resp,prefix+"/");
        	} catch(NotFoundException x) {
        		die(resp,"pyramid not found "+x);
        		return;
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
				try {
					Resource tileUri = getTileUri(label,level,row,col);
					log.info("GET TILE "+label+" level "+level+" ("+row+","+col+")");
					resp.setContentType("image/jpg");
					CopyFile.copy(TupeloStore.read(tileUri), resp.getOutputStream());
				} catch(NotFoundException x) {
					die(resp,"tile not found");
				}
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
	
	Resource getTileUri(String label, int level, int row, int col) throws OperatorException {
		Unifier u = new Unifier();
		u.setColumnNames("tile");
		u.addPattern("pyramid",Rdfs.LABEL,Resource.literal(label));
		u.addPattern("pyramid",ImagePyramidBeanUtil.PYRAMID_TILES,"tile");
		u.addPattern("tile",ImagePyramidTileBeanUtil.PYRAMIDTILE_LEVEL,Resource.literal(level));
		u.addPattern("tile",ImagePyramidTileBeanUtil.PYRAMIDTILE_ROW,Resource.literal(row));
		u.addPattern("tile",ImagePyramidTileBeanUtil.PYRAMIDTILE_COL,Resource.literal(col));
		TupeloStore.getInstance().getContext().perform(u);
		for(Tuple<Resource> r : u.getResult()) {
			return r.get(0);
		}
		throw new NotFoundException("no tile found");
	}

	ImagePyramidBean getPyramidForLabel(String label) throws OperatorException {
		Resource ip = null;
		for(Resource i : TupeloStore.match(Rdfs.LABEL, label)) {
			ip = i;
		}
		if(ip == null) {
			throw new NotFoundException("no image pyramid for label "+label);
		}
		log.debug("fetching pyramid "+label+" @ "+ip);
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
		PrintWriter pw = new PrintWriter(resp.getOutputStream());
		String html = generateHtml(ipb,base); 
		pw.println(html);
		pw.flush();
	}
	public static String generateHtml(String datasetUri, String base) throws OperatorException, IOException {
		Thing dataset = TupeloStore.fetchThing(datasetUri);
		Resource pyramidUri = dataset.getResource(ImagePyramidBeanUtil.HAS_PYRAMID);
		if(pyramidUri == null) {
			TupeloStore.refetch(datasetUri);
			dataset = TupeloStore.fetchThing(datasetUri);
    		pyramidUri = dataset.getResource(ImagePyramidBeanUtil.HAS_PYRAMID);
		}
		if(pyramidUri == null) {
			return "<div class='error'>No image pyramid available for dataset "+datasetUri+"</div>";
		}
		ImagePyramidBean ipb =
			(ImagePyramidBean) TupeloStore.fetchBean(pyramidUri);
		return generateHtml(ipb,base);
	}
	public static String generateHtml(ImagePyramidBean ipb, String base) throws OperatorException, IOException {
		return generateHtml(ipb,base,640,480);
	}
	ImagePyramidBean partialFetchPyramidFor(Resource uri) throws OperatorException {
		Unifier u = new Unifier();
		// fetch only the attributes we care about for generating the seadragon manifest
		u.addPattern(uri, ImagePyramidBeanUtil.HAS_PYRAMID, "p");
		u.addPattern("p", Rdfs.LABEL, "label");
		u.addPattern("p", ImagePyramidBeanUtil.PYRAMID_WIDTH, "width");
		u.addPattern("p", ImagePyramidBeanUtil.PYRAMID_HEIGHT, "height");
		u.addPattern("p", ImagePyramidBeanUtil.PYRAMID_FORMAT, "format");
		u.addPattern("p", ImagePyramidBeanUtil.PYRAMID_OVERLAP, "overlap");
		u.addPattern("p", ImagePyramidBeanUtil.PYRAMID_SIZE, "tilesize");
		u.setColumnNames("label","width","height","format","overlap","tilesize");
		TupeloStore.getInstance().getContext().perform(u);
		for(Tuple<Resource> row : u.getResult()) {
			ImagePyramidBean p = new ImagePyramidBean();
			int pi = 0;
			p.setUri(uri.getString());
			p.setLabel(row.get(pi++).getString());
			p.setWidth((Integer)row.get(pi++).asObject());
			p.setHeight((Integer)row.get(pi++).asObject());
			p.setFormat(row.get(pi++).getString());
			p.setOverlap((Integer)row.get(pi++).asObject());
			p.setTilesize((Integer)row.get(pi++).asObject());
			return p;
		}
		return null;
	}
	public static String generateHtml(ImagePyramidBean ipb, String base, int x, int y) throws OperatorException, IOException {
		int sizex = x;
		int sizey = y;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
        pw.println( "<html>" );
        //pw.println("<base href='"+base+"'/>");
        pw.println( "<title>" + ipb.getLabel() + "</title>" );
        pw.println( "<script type=\"text/javascript\" src=\"http://seadragon.com/ajax/embed.js\"></script>" );
        pw.println( "<body>" );
        pw.println( "<script type=\"text/javascript\">" );
        pw.println( "Seadragon.Config.debug = true;" );
        pw.println( "Seadragon.Config.imagePath = \"http://www.seadragon.com/images/seajax/\";" );
        pw.println( String.format( "Seadragon.embed(\"%dpx\", \"%dpx\", \"%s.xml\", %d, %d, %d, %d, \"%s\");", sizex, sizey, ipb.getLabel(), ipb.getWidth(), ipb.getHeight(), ipb.getTilesize(), ipb.getOverlap(), ipb.getFormat() ) );
        pw.println( "</script>" );
        //pw.println( String.format( "Image Size : %d x %d", ipb.getWidth(), ipb.getHeight() ) );
        pw.println( "</body>" );
        pw.println( "</html>" );
        pw.flush();
        return sw.toString();
	}
}
