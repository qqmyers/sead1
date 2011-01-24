package edu.illinois.ncsa.mmdb.web.server.rest;

import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PreviewPyramidBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewPyramidBeanUtil;

public class ImagePyramidResource extends ServerResource {
    private static Log log = LogFactory.getLog(ImagePyramidResource.class);

    @Get
    public String getPyramid() {
        String dataset = URLDecoder.decode(getRequest().getAttributes().get("dataset").toString()); //$NON-NLS-1$
        log.debug("GET PYRAMID " + dataset);

        PreviewPyramidBean pyramid;
        try {
            Resource uri = Resource.uriRef(dataset);
            PreviewPyramidBeanUtil ipbu = new PreviewPyramidBeanUtil(TupeloStore.getInstance().getBeanSession());
            pyramid = ipbu.get(uri);
        } catch (OperatorException exc) {
            log.warn("Could not get bean.", exc);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid pyramid uri provided");
            return null;
        }

        if (pyramid == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid pyramid uri provided");
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"); //$NON-NLS-1$
        sb.append(String.format("<Image TileSize=\"%d\" Overlap=\"%d\" Format=\"%s\" ", pyramid.getTilesize(), pyramid.getOverlap(), pyramid.getFormat())); //$NON-NLS-1$
        sb.append("ServerFormat=\"Default\" xmlns=\"http://schemas.microsoft.com/deepzoom/2009\">"); //$NON-NLS-1$
        sb.append(String.format("<Size Width=\"%d\" Height=\"%d\" />", pyramid.getWidth(), pyramid.getHeight())); //$NON-NLS-1$
        sb.append("</Image>"); //$NON-NLS-1$
        return sb.toString();
    }
}
