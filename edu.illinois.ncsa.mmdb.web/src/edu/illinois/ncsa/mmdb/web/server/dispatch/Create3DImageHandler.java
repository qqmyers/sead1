package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.NotFoundException;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.Create3DImage;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class Create3DImageHandler implements ActionHandler<Create3DImage, EmptyResult> {

    private static Log            log            = LogFactory.getLog(Create3DImageHandler.class);
    private static final Resource CLASS_RESOURCE = Resource.literal("edu.illinois.ncsa.mmdb.extractor.image.ImageExtractor");

    @Override
    public EmptyResult execute(Create3DImage action, ExecutionContext arg1) throws ActionException {

        try {

            final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

            //delete older Image if it exists
            deleteImage(action, beanSession);

            File tempfile = null;
            tempfile = File.createTempFile("extract", "unk"); //$NON-NLS-1$ //$NON-NLS-2$

            //Remove "data:image/png;base64," Header
            String withoutHeader = action.getImageData().substring(22);

            //Decode Base64 data
            byte[] data = Base64.decodeBase64(withoutHeader.getBytes());

            OutputStream os = new FileOutputStream(tempfile);
            os.write(data);
            os.close();

            final BufferedImage img = loadImage(tempfile);

            String small = "100x100";
            String medium = "800x600";

            //Image size
            int realw = 480;
            int realh = 360;

            try {
                TupeloStore.getInstance().removeCachedPreview(action.getDataset().getUri(), GetPreviews.SMALL);
                new PreviewImageBeanUtil(beanSession).thumbnail(action.getDataset(), img, 100, 100, false, small, CLASS_RESOURCE);
                new PreviewImageBeanUtil(beanSession).thumbnail(action.getDataset(), img, realw, realh, false, medium, CLASS_RESOURCE);
            } catch (Throwable thr) {
                log.info("Could not create 3D Image Thumbnails");
            }

            log.info("Finished Creating 3D Image");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static BufferedImage loadImage(File input) throws IOException {
        ImageInputStream iis = null;
        try {
            iis = new FileImageInputStream(input);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis);
                return reader.read(0);
            }

            throw (new IOException("Could not load image."));
        } finally {
            if (iis != null) {
                iis.close();
            }
        }
    }

    private void deleteImage(Create3DImage action, BeanSession beanSession) {

        //simulate Extractor
        TripleWriter tw = new TripleWriter();
        Set<Resource> blobs = new HashSet<Resource>();
        Resource uri = Resource.uriRef(action.getDataset().getUri());

        Unifier uf = new Unifier();
        uf.addPattern(uri, PreviewBeanUtil.HAS_PREVIEW, "preview"); //$NON-NLS-1$
        uf.addPattern("preview", MMDB.METADATA_EXTRACTOR, CLASS_RESOURCE); //$NON-NLS-1$
        uf.addPattern("preview", "p", "o"); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
        uf.setColumnNames("preview", "p", "o"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
        try {
            beanSession.getContext().perform(uf);
        } catch (OperatorException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        for (Tuple<Resource> row : uf.getResult() ) {
            // FIXME TUP-507 workaround
            if (row.get(0) != null) {
                if ((row.get(1) != null) && (row.get(2) != null)) {
                    tw.remove(row.get(0), row.get(1), row.get(2));
                    log.info("REMOVE X TIMES");
                }
                tw.remove(uri, PreviewBeanUtil.HAS_PREVIEW, row.get(0));
                blobs.add(row.get(0));
            }
        }

        for (Resource x : blobs ) {
            try {
                beanSession.removeBlob(x);
            } catch (NotFoundException e) {
                log.warn("Could not find blob");
            } catch (OperatorException e) {
                log.warn("Could not remove Blob");
            }
        }
        try {
            beanSession.getContext().perform(tw);
        } catch (OperatorException e) {
            log.warn("Could not remove metadata");
        }
    }

    @Override
    public Class<Create3DImage> getActionType() {
        return Create3DImage.class;
    }

    @Override
    public void rollback(Create3DImage arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
    }
}
