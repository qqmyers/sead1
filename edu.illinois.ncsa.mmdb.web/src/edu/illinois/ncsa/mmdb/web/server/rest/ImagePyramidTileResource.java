package edu.illinois.ncsa.mmdb.web.server.rest;

import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewPyramidBeanUtil;

public class ImagePyramidTileResource extends ServerResource {
    private static Log log = LogFactory.getLog(ImagePyramidTileResource.class);

    @Get
    public InputRepresentation getPyramidTile() {
        String dataset = URLDecoder.decode(getRequest().getAttributes().get("dataset").toString()); //$NON-NLS-1$
        String level = getRequest().getAttributes().get("level").toString(); //$NON-NLS-1$
        String row = getRequest().getAttributes().get("row").toString(); //$NON-NLS-1$
        String col = getRequest().getAttributes().get("col").toString(); //$NON-NLS-1$
        String ext = getRequest().getAttributes().get("ext").toString(); //$NON-NLS-1$

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
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid pyramid tile provided");
            return null;
        }

        List<Resource> hits = u.getFirstColumn();
        if (hits.size() > 1) {
            log.warn("more than one tile found for " + uri);
        } else if (hits.size() <= 0) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid pyramid tile provided");
            return null;
        }
        try {
            return new InputRepresentation(TupeloStore.read(hits.get(0)), MediaType.valueOf("image/" + ext)); //$NON-NLS-1$
        } catch (OperatorException exc) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid pyramid tile provided");
            return null;
        }
    }
}
