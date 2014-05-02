package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PreviewPyramidBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewPyramidBeanUtil;

@Path("/pyramid/{dataset}/")
public class ImagePyramidRestService {
    private static Log log = LogFactory.getLog(ImagePyramidRestService.class);

    @GET
    @Path("xml")
    public Response getPyramid(@PathParam("dataset") String dataset) {
        log.debug("GET PYRAMID " + dataset);

        PreviewPyramidBean pyramid;
        try {
            UriRef uri = Resource.uriRef(dataset);
            PreviewPyramidBeanUtil ipbu = new PreviewPyramidBeanUtil(TupeloStore.getInstance().getBeanSession());
            pyramid = ipbu.get(uri);
        } catch (OperatorException exc) {
            log.warn("Could not get bean.", exc);
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pyramid uri provided").build();
        }

        if (pyramid == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pyramid uri provided").build();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"); //$NON-NLS-1$
        sb.append(String.format("<Image TileSize=\"%d\" Overlap=\"%d\" Format=\"%s\" ", pyramid.getTilesize(), pyramid.getOverlap(), pyramid.getFormat())); //$NON-NLS-1$
        sb.append("ServerFormat=\"Default\" xmlns=\"http://schemas.microsoft.com/deepzoom/2009\">"); //$NON-NLS-1$
        sb.append(String.format("<Size Width=\"%d\" Height=\"%d\" />", pyramid.getWidth(), pyramid.getHeight())); //$NON-NLS-1$
        sb.append("</Image>"); //$NON-NLS-1$

        return Response.status(Response.Status.OK).entity(sb.toString()).build();
    }

    @GET
    @Path("xml_files/{level}/{col}_{row}.{ext}")
    public Response getPyramidTile(@PathParam("dataset") String dataset, @PathParam("level") String level, @PathParam("row") String row, @PathParam("col") String col, @PathParam("ext") String ext) {
        log.debug("GET TILE " + dataset + " level " + level + " (" + row + "," + col + ")");

        // find the tile
        Resource uri = Resource.uriRef(dataset);
        Unifier u = new Unifier();
        u.setColumnNames("tile"); //$NON-NLS-1$
        u.addPattern(uri, Rdf.TYPE, PreviewPyramidBeanUtil.PREVIEW_TYPE);
        u.addPattern(uri, PreviewPyramidBeanUtil.PYRAMID_TILES, "tile"); //$NON-NLS-1$
        u.addPattern("tile", PreviewPyramidBeanUtil.PYRAMID_TILE_LEVEL, Resource.literal(level)); //$NON-NLS-1$
        u.addPattern("tile", PreviewPyramidBeanUtil.PYRAMID_TILE_ROW, Resource.literal(row)); //$NON-NLS-1$
        u.addPattern("tile", PreviewPyramidBeanUtil.PYRAMID_TILE_COL, Resource.literal(col)); //$NON-NLS-1$
        try {
            TupeloStore.getInstance().getContext().perform(u);
        } catch (OperatorException exc) {
            log.warn("Could not get bean.", exc);
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pyramid tile provided").build();
        }

        List<Resource> hits = u.getFirstColumn();
        if (hits.size() > 1) {
            log.warn("more than one tile found for " + uri);
        } else if (hits.size() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pyramid tile provided").build();
        }
        try {
            InputStream is = TupeloStore.read(hits.get(0));
            return Response.status(Response.Status.OK).type(MediaType.valueOf("image/" + ext)).entity(is).build();
        } catch (OperatorException exc) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pyramid tile provided").build();
        }
    }
}
