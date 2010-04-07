/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
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
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.CopyFile;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PreviewPyramidBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewPyramidBeanUtil;

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
        String params = requestUrl.toString().substring( prefix.length() );

        // return xml if requested
        Matcher matcher = Pattern.compile( "/(.*)/xml" ).matcher( params ); //$NON-NLS-1$
        if ( matcher.matches() ) {
            try {
                Resource uri = Resource.uriRef( matcher.group( 1 ) );
                log.debug( "GET PYRAMID " + uri );

                PreviewPyramidBeanUtil ipbu = new PreviewPyramidBeanUtil( TupeloStore.getInstance().getBeanSession() );
                PreviewPyramidBean ipb = ipbu.get( uri );
                if ( ipb == null ) {
                    throw (new NotFoundException( "Could not find the pyramid with given URI." ));
                }
                getXml( ipb, resp );
                return;
            } catch ( IllegalArgumentException x ) {
                die( resp, 404, x );
                return;
            } catch ( OperatorException x ) {
                die( resp, 404, x );
                return;
            }
            // should never get here
        }

        // check if it wants a specific tile
        matcher = Pattern.compile( "/(.*)/xml_files/(\\d+)/(\\d+)_(\\d+)\\.(.*)" ).matcher( params ); //$NON-NLS-1$
        if ( matcher.matches() ) {
            try {
                Resource uri = Resource.uriRef( matcher.group( 1 ) );
                Resource tileUri = getTileUri( uri, matcher.group( 2 ), matcher.group( 3 ), matcher.group( 4 ) );
                resp.setContentType( "image/" + matcher.group( 5 ) ); //$NON-NLS-1$
                CopyFile.copy( TupeloStore.read( tileUri ), resp.getOutputStream() );
                return;
            } catch ( NotFoundException x ) {
                die( resp, 404, x );
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
    private void getXml( PreviewPyramidBean pyramid, HttpServletResponse resp ) throws IOException
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
     * @param col
     *            column of the tile in the level
     * @param row
     *            row of the tile in the level
     * @return the uri of the tile
     * @throws OperatorException
     *             throws operatorexecption if the tile could not be found.
     */
    private Resource getTileUri( Resource uri, String level, String col, String row ) throws OperatorException
    {
        // FIXME should really get ints as input but does not work
        log.debug( "GET TILE " + uri + " level " + level + " (" + row + "," + col + ")" );
        Unifier u = new Unifier();
        u.setColumnNames( "tile" ); //$NON-NLS-1$
        u.addPattern( uri, Rdf.TYPE, PreviewPyramidBeanUtil.PREVIEW_TYPE );
        u.addPattern( uri, PreviewPyramidBeanUtil.PYRAMID_TILES, "tile" ); //$NON-NLS-1$
        u.addPattern( "tile", PreviewPyramidBeanUtil.PYRAMID_TILE_LEVEL, Resource.literal( level ) ); //$NON-NLS-1$
        u.addPattern( "tile", PreviewPyramidBeanUtil.PYRAMID_TILE_ROW, Resource.literal( row ) ); //$NON-NLS-1$
        u.addPattern( "tile", PreviewPyramidBeanUtil.PYRAMID_TILE_COL, Resource.literal( col ) ); //$NON-NLS-1$
        TupeloStore.getInstance().getContext().perform( u );
        List<Resource> hits = u.getFirstColumn();
        if ( hits.size() == 1 ) {
            return hits.get( 0 );
        } else if ( hits.size() > 1 ) {
            log.warn( "more than one tile found for " + uri );
            return hits.get( 0 );
        } else {
            throw new NotFoundException( "no tile found" );
        }
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
