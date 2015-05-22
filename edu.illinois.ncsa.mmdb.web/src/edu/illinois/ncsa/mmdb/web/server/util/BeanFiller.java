/*******************************************************************************
 * Copyright 2014 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package edu.illinois.ncsa.mmdb.web.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Beans;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

public class BeanFiller {

    /** Commons logging **/
    private static Log   log         = LogFactory.getLog(BeanFiller.class);

    public static UriRef SHA1_DIGEST = Resource.uriRef("http://sead-data.net/terms/hasSHA1Digest");

    /**
     * Fill required fields to use a Thing as a CollectionBean. Does not save
     * Thing.
     *
     * @param t
     * @param dirName
     * @param creator
     * @param d
     * @throws OperatorException
     */
    public static void fillCollectionBean(Thing t, String dirName, UriRef creator, Date d) throws OperatorException {

        t.addType(CollectionBeanUtil.COLLECTION_TYPE);
        t.addValue(Rdfs.LABEL, dirName);

        //Next 4 are Bean specific
        t.addType(Beans.STORAGE_TYPE_BEAN_ENTRY);
        t.setValue(Beans.PROPERTY_VALUE_IMPLEMENTATION_CLASSNAME,
                Resource.literal("edu.uiuc.ncsa.cet.bean.CollectionBean"));
        t.setValue(Dc.IDENTIFIER, Resource.literal(t.getSubject().toString()));
        t.setValue(Beans.PROPERTY_IMPLEMENTATION_MAPPING_SUBJECT,
                Resource.uriRef("tag:cet.ncsa.uiuc.edu,2009:/mapping/" + CollectionBeanUtil.COLLECTION_TYPE));

        t.setValue(Dc.TITLE, dirName);
        t.addValue(DcTerms.DATE_CREATED, new Date());
        t.setValue(Dc.CREATOR, creator);
    }

    /**
     * Adds the blob and basic file metadata to a Thing to create a basic
     * DataBean. Bean is not saved here so further metadata may be added.
     *
     * @param t
     * @param fileName
     * @param contentType
     * @param creator
     * @param date
     * @param is
     * @throws OperatorException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static void fillDataBean(Context c, Thing t, String fileName, String contentType, UriRef creator, Date date, InputStream is) throws OperatorException, NoSuchAlgorithmException, IOException {
        byte[] digest = null;

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        DigestInputStream dis = new DigestInputStream(is, sha1);

        BlobWriter bw = new BlobWriter();
        String uri = t.getSubject().toString();
        bw.setUri(URI.create(uri));
        bw.setInputStream(dis);
        c.perform(bw);
        //dis.close();
        //Handle zip streams as a special case
        if (is instanceof ZipInputStream) {
            ((ZipInputStream) is).closeEntry();
        } else {
            is.close();
        }
        log.debug("Created " + fileName + " (" + bw.getSize() + " bytes), uri=" + uri);

        digest = sha1.digest();
        t.addType(Cet.DATASET);
        //Next 3 are Bean related
        t.addType(Beans.STORAGE_TYPE_BEAN_ENTRY);
        t.setValue(Beans.PROPERTY_VALUE_IMPLEMENTATION_CLASSNAME,
                Resource.literal("edu.uiuc.ncsa.cet.bean.DatasetBean"));
        t.setValue(Dc.IDENTIFIER, Resource.literal(uri));
        t.setValue(Beans.PROPERTY_IMPLEMENTATION_MAPPING_SUBJECT,
                Resource.uriRef("tag:cet.ncsa.uiuc.edu,2009:/mapping/http://cet.ncsa.uiuc.edu/2007/Dataset"));

        t.addValue(Rdfs.LABEL, fileName);
        t.addValue(SHA1_DIGEST, asHex(digest));

        t.setValue(RestService.FILENAME_PROPERTY, fileName);
        t.setValue(Dc.TITLE, fileName);
        t.addValue(Dc.DATE, date);
        t.setValue(Dc.CREATOR, creator);
        t.addValue(Files.LENGTH, bw.getSize());
        t.setValue(RestService.FORMAT_PROPERTY, contentType);

    }

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String asHex(byte[] buf)
    {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i )
        {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

}
