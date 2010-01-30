package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.NotFoundException;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.CopyFile;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.ImagePyramidBean;
import edu.uiuc.ncsa.cet.bean.tupelo.ImagePyramidBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.ImagePyramidTileBeanUtil;

/**
 * Servlet responsible for handling the image pyramid requests. The requests
 * will have first the url encoded of the pyramid followed by the request, for
 * example <code>/&lt;URI&gt;/xml</code> will return the xml needed by seadragon
 * for the pyramid given by &lt;URI&gt;. To get a tile use
 * <code>/&lt;URI&gt;/xml_files/&lt;level&gt;/&ltcol&gt;_&lt;row&gt;.&lt;format&gt;</code>
 * 
 * @see https://wiki.ncsa.illinois.edu/display/cet/Image+Pyramid
 * @author Rob Kooper
 * @author Joe Futrelle
 * 
 */
@SuppressWarnings( { "serial" })
public class ImagePyramidServlet extends AuthenticatedServlet
{
    private Log log = LogFactory.getLog( ImagePyramidServlet.class );

    public ImagePyramidServlet()
    {
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
    {
        if ( !authenticate( req, resp ) ) {
            return;
        }
        String url = req.getRequestURL().toString();
        URL requestUrl = new URL( url );
        String prefix = requestUrl.getProtocol() + "://" + requestUrl.getHost(); //$NON-NLS-1$
        if ( requestUrl.getPort() != -1 ) {
            prefix = prefix + ":" + requestUrl.getPort(); //$NON-NLS-1$
        }
        prefix += req.getContextPath();
        prefix += req.getServletPath();
        if ( prefix.length() >= url.length() ) {
            die( resp, 404, new Throwable( "Illegal pyramid URL " + url ) );
            return;
        }

        // parse the request
        String params = requestUrl.toString().substring( prefix.length() + 1 );
        String request = params.substring( params.indexOf( '/' ) );
        Resource uri = null;
        try {
            uri = Resource.uriRef( URLDecoder.decode( params.substring( 0, params.indexOf( '/' ) ) ) );
        } catch ( IllegalArgumentException x ) {
            die( resp, 404, x );
            return;
        }

        // return xml if requested
        if ( request.equals( "/xml" ) ) { //$NON-NLS-1$
            try {
                log.debug( "GET PYRAMID " + uri );

                ImagePyramidBeanUtil ipbu = new ImagePyramidBeanUtil( TupeloStore.getInstance().getBeanSession() );
                ImagePyramidBean ipb = ipbu.get( uri );
                if ( ipb == null ) {
                    throw (new NotFoundException( "Could not find the pyramid with given URI." ));
                }
                getXml( ipb, resp );
                return;
            } catch ( OperatorException x ) {
                die( resp, 404, x );
                return;
            }
            // should never get here
        }

        // check if it wants a specific tile
        Matcher matcher = Pattern.compile( "/xml_files/(\\d+)/(\\d+)_(\\d+)\\.(.*)" ).matcher( request ); //$NON-NLS-1$
        if ( matcher.matches() ) {
            int level = Integer.parseInt( matcher.group( 1 ) );
            int col = Integer.parseInt( matcher.group( 2 ) );
            int row = Integer.parseInt( matcher.group( 3 ) );
            String type = matcher.group( 4 );
            try {
                try {
                    log.debug( "GET TILE " + uri + " level " + level + " (" + row + "," + col + ")" );
                    Resource tileUri = getTileUri( uri, level, row, col );
                    resp.setContentType( "image/" + type ); //$NON-NLS-1$
                    CopyFile.copy( TupeloStore.read( tileUri ), resp.getOutputStream() );
                } catch ( NotFoundException x ) {
                    die( resp, 404, x );
                }
                return;
            } catch ( OperatorException x ) {
                die( resp, 500, x );
                return;
            }

        }

        // unknown request
        die( resp, 404, new Throwable( "GET (unrecognized) " ) );
    }

    /**
     * Return the XML description of the deepzoom image. The tiles that are part
     * of this image will be fetched based on this URL, i.e. if the request is
     * for the xml file called /pyramid/foo.xml, the images should be in
     * /pyramid/foo_files/0/0_0.<format>
     */
    private void getXml( ImagePyramidBean pyramid, HttpServletResponse resp ) throws IOException
    {
        PrintStream ps = new PrintStream( resp.getOutputStream() );
        ps.println( "<?xml version=\"1.0\" encoding=\"utf-8\"?>" ); //$NON-NLS-1$
        ps.print( String.format( "<Image TileSize=\"%d\" Overlap=\"%d\" Format=\"%s\" ", pyramid.getTilesize(), pyramid.getOverlap(), pyramid.getFormat() ) ); //$NON-NLS-1$
        ps.println( "ServerFormat=\"Default\" xmlns=\"http://schemas.microsoft.com/deepzoom/2009\">" ); //$NON-NLS-1$
        ps.println( String.format( "<Size Width=\"%d\" Height=\"%d\" />", pyramid.getWidth(), pyramid.getHeight() ) ); //$NON-NLS-1$
        ps.println( "</Image>" ); //$NON-NLS-1$
        ps.flush();
    }

    /**
     * Get the uri of the requested tile.
     * 
     * @param uri
     *            the pyramid
     * @param level
     *            level in the pyramid
     * @param row
     *            row of the tile in the level
     * @param col
     *            column of the tile in the level
     * @return the uri of the tile
     * @throws OperatorException
     *             throws operatorexecption if the tile could not be found.
     */
    private Resource getTileUri( Resource uri, int level, int row, int col ) throws OperatorException
    {
        // FIXME MMDB-382
        Resource rl = Resource.literal( level );
        Resource rr = Resource.literal( row );
        Resource rc = Resource.literal( col );
        Unifier u = new Unifier();
        u.setColumnNames( "tile", "level", "row", "col" ); //$NON-NLS-1$
        u.addPattern( uri, ImagePyramidTileBeanUtil.PYRAMID_TILES, "tile" ); //$NON-NLS-1$
        u.addPattern( "tile", ImagePyramidTileBeanUtil.PYRAMIDTILE_LEVEL, "level" ); //$NON-NLS-1$
        u.addPattern( "tile", ImagePyramidTileBeanUtil.PYRAMIDTILE_ROW, "row" ); //$NON-NLS-1$
        u.addPattern( "tile", ImagePyramidTileBeanUtil.PYRAMIDTILE_COL, "col" ); //$NON-NLS-1$
        TupeloStore.getInstance().getContext().perform( u );
        for ( Tuple<Resource> r : u.getResult() ) {
            if (rl.equals( r.get(1)) && rr.equals( r.get(2))  && rc.equals( r.get(3) ) ) {
            return r.get( 0 );
            }
        }
        throw new NotFoundException( "no tile found" );
    }

    /**
     * Helper function to create a reason why the system could not handle the
     * request.
     * 
     * @param resp
     * @param message
     * @throws IOException
     */
    private void die( HttpServletResponse resp, int code, Throwable thr ) throws IOException
    {
        log.error( String.format( "%d : %s", code, thr.getMessage() ), thr ); //$NON-NLS-1$

        resp.setStatus( code );
        PrintWriter pw = new PrintWriter( resp.getOutputStream() );
        pw.println( String.format( "<h1>Error : %s</h1>", thr.getMessage() ) ); //$NON-NLS-1$
        thr.printStackTrace( pw );
        pw.flush();
    }

}
