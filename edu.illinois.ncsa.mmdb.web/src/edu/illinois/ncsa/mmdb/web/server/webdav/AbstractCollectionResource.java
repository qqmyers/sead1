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
package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MiltonServlet;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;

/**
 * Helper class to easily create a collection (folder). This will take care of
 * handling the getChildren and child functions and makes sure there is no name
 * collision (by prepending the file/folder with a number).
 * 
 * @author Rob Kooper
 * 
 */
public abstract class AbstractCollectionResource extends AbstractResource implements CollectionResource, GetableResource, PropFindableResource, LockableResource, LockingCollectionResource
{
    /** how long to keep children list between consecutive calls in ms */
    public static int                   CACHE_TIME  = 1000;

    private final Map<String, Resource> resourcemap = new HashMap<String, Resource>();
    private DatasetBeanResource         folder      = null;
    private long                        last        = 0;

    public AbstractCollectionResource(String name, Context context, SecurityManager security)
    {
        super(name, null, null, context, security);
    }

    public AbstractCollectionResource(String name, org.tupeloproject.rdf.UriRef uri, Context context, SecurityManager security)
    {
        super(name, uri, null, context, security);
    }

    public AbstractCollectionResource(String name, org.tupeloproject.rdf.UriRef uri, Date created, Context context, SecurityManager security)
    {
        super(name, uri, created, context, security);
    }

    public AbstractCollectionResource(String name, org.tupeloproject.rdf.UriRef uri, Date created, Date modified, Context context, SecurityManager security)
    {
        super(name, uri, created, modified, context, security);
    }

    /**
     * DatasetResource that is returned when windows asks for the folder.jpg
     * 
     * @param folder
     *            the resource representing the image to show for this
     *            collection.
     */
    public void setFolder(DatasetBeanResource folder)
    {
        this.folder = folder;
    }

    protected Resource getFolder()
    {
        Resource r = resourcemap.get("folder.jpg"); //$NON-NLS-1$
        if (r != null) {
            return r;
        }
        if (folder != null) {
            return folder;
        }
        if (!resourcemap.isEmpty()) {
            return resourcemap.values().iterator().next();
        }
        return null;
    }

    protected Resource getDesktopINI()
    {
        Resource r = resourcemap.get("desktop.ini"); //$NON-NLS-1$
        if (r != null) {
            return r;
        }
        String text = "[ViewState]\nMode=\nVid=\nFolderType=Pictures\nLogo=\n"; //$NON-NLS-1$
        return new TextResource("desktop.ini", text, getContext(), getSecurity()); //$NON-NLS-1$
    }

    // ----------------------------------------------------------------------
    // GetableResource
    // ----------------------------------------------------------------------

    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException
    {
        String path = MiltonServlet.request().getRequestURL().toString();
        if (!path.endsWith("/")) { //$NON-NLS-1$
            path = path + "/"; //$NON-NLS-1$
        }
        //<table><tr><th><img src="/icons/blank.gif" alt="[ICO]"></th><th><a href="?C=N;O=D">Name</a></th><th><a href="?C=M;O=A">Last modified</a></th><th><a href="?C=S;O=A">Size</a></th><th><a href="?C=D;O=A">Description</a></th></tr><tr><th colspan="5"><hr></th></tr>
        XmlWriter w = new XmlWriter(out);
        w.open("html"); //$NON-NLS-1$
        w.open("body"); //$NON-NLS-1$
        w.begin("h1").open().writeText(this.getName()).close(); //$NON-NLS-1$
        w.open("table"); //$NON-NLS-1$
        for (Resource r : getChildren() ) {
            w.open("tr"); //$NON-NLS-1$

            w.open("td"); //$NON-NLS-1$
            w.begin("a").writeAtt("href", path + r.getName()).open().writeText(r.getName()).close(); //$NON-NLS-1$ //$NON-NLS-2$
            w.close("td"); //$NON-NLS-1$

            w.begin("td").open(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (r instanceof GetableResource) {
                GetableResource get = (GetableResource) r;
                if (get.getContentLength() != null) {
                    w.writeText(get.getContentLength() + ""); //$NON-NLS-1$
                }
            }
            w.close("td"); //$NON-NLS-1$

            w.begin("td").open(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (r.getModifiedDate() != null) {
                w.writeText(r.getModifiedDate() + ""); //$NON-NLS-1$                
            }
            w.close("td"); //$NON-NLS-1$

            w.close("tr"); //$NON-NLS-1$
        }
        w.close("table"); //$NON-NLS-1$
        w.close("body"); //$NON-NLS-1$
        w.close("html"); //$NON-NLS-1$
        w.flush();
    }

    public Long getMaxAgeSeconds(Auth auth)
    {
        return null;
    }

    public String getContentType(String accepts)
    {
        return "text/html"; //$NON-NLS-1$
    }

    public Long getContentLength()
    {
        return null;
    }

    // ----------------------------------------------------------------------
    // CollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Resource child(String childName)
    {
        if (resourcemap.size() == 0) {
            getChildren();
        }
        if (childName.equals("folder.jpg")) { //$NON-NLS-1$
            return getFolder();
        }
        if (childName.equals("desktop.ini")) { //$NON-NLS-1$
            return getDesktopINI();
        }
        return resourcemap.get(childName);
    }

    @Override
    public synchronized List<? extends Resource> getChildren()
    {
        if (System.currentTimeMillis() - last < CACHE_TIME) {
            return new ArrayList<Resource>(resourcemap.values());
        }

        List<String> dups = new ArrayList<String>();
        Set<String> done = new HashSet<String>();

        resourcemap.clear();
        Map<String, AbstractResource> map = getResourceList();

        // look for dups
        for (Entry<String, AbstractResource> entry1 : map.entrySet() ) {
            // skip if already processed (duplicate)
            if (done.contains(entry1.getKey())) {
                continue;
            }

            // search for duplicate names
            dups.clear();
            for (Entry<String, AbstractResource> entry2 : map.entrySet() ) {
                if (!entry1.getKey().equals(entry2.getKey()) && entry1.getValue().getName().equals(entry2.getValue().getName())) {
                    if (!dups.contains(entry1.getKey())) {
                        dups.add(entry1.getKey());
                    }
                    if (!dups.contains(entry2.getKey())) {
                        dups.add(entry2.getKey());
                    }
                }
            }

            // make sure all names are unique
            if (dups.size() > 0) {
                Collections.sort(dups);
                int c = 1;
                for (String d1 : dups ) {
                    AbstractResource r = map.get(d1);
                    r.setName(String.format("%03d %s", c++, r.getName())); //$NON-NLS-1$
                    resourcemap.put(r.getName(), r);
                    done.add(d1);
                }
            } else {
                resourcemap.put(entry1.getValue().getName(), entry1.getValue());
                done.add(entry1.getKey());
            }
        }

        last = System.currentTimeMillis();
        return new ArrayList<Resource>(resourcemap.values());
    }

    /**
     * Return a collection of resources that should be shown to the user. This
     * is used by getChildren to get the initial list of resources after which
     * the names of resources will be made unique. The key in the map is used to
     * sort the collection of resources to generate names in case of collision
     * of names. This key has to be consistent between different calls (for
     * example use the URI of the bean).
     * 
     * @return collection of resources to show to the user.
     */
    public abstract Map<String, AbstractResource> getResourceList();

    // ----------------------------------------------------------------------
    // PutableResource
    // ----------------------------------------------------------------------
    public Resource createNew(String newName, InputStream stream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        // create the bean
        DatasetBean db = new DatasetBean();
        db.setFilename(newName);
        db.setTitle(newName);
        db.setMimeType(contentType);
        db.setDate(new Date());
        db.setSize(length);

        DatasetBeanResource result = new DatasetBeanResource(db, getContext(), getSecurity());

        if (length == 0) {
            return null;
        }

        if (!(getSecurity() instanceof MediciSecurityManager)) {
            throw (new NotAuthorizedException(result));
        }
        db.setCreator(((MediciSecurityManager) getSecurity()).getUser());

        BeanSession bs;
        try {
            bs = CETBeans.createBeanSession(getContext());
        } catch (OperatorException e) {
            throw (new IOException("Could not create beansession.", e));
        } catch (ClassNotFoundException e) {
            throw (new IOException("Could not create beansession.", e));
        }

        // write the data
        try {
            bs.writeBlob(org.tupeloproject.rdf.Resource.uriRef(db.getUri()), stream);
        } catch (OperatorException e) {
            throw (new IOException("Could not write data.", e));
        }

        // write the bean
        try {
            bs.save(db);
        } catch (OperatorException e) {
            throw (new IOException("Could not write bean.", e));
        }

        // start the extractor
        TupeloStore.getInstance().extractPreviews(db.getUri());

        // create the resource
        return result;
    }

    // ----------------------------------------------------------------------
    // LockingCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public LockToken createAndLock(String arg0, LockTimeout arg1, LockInfo arg2) throws NotAuthorizedException {
        return createNullLock();
    }

    // ----------------------------------------------------------------------
    // LockableResource
    // ----------------------------------------------------------------------

    /**
     * Returns a LockToken with a zero lifetime. Mac OS X insists on
     * lock support before it allows writing to a WebDAV
     * store.
     */
    protected LockToken createNullLock()
    {
        return new LockToken(UUID.randomUUID().toString(),
                new LockInfo(LockInfo.LockScope.SHARED, LockInfo.LockType.WRITE, "", LockInfo.LockDepth.ZERO),
                new LockTimeout(0L));
    }

    @Override
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo)
    {
        return LockResult.success(createNullLock());
    }

    @Override
    public LockResult refreshLock(String token)
    {
        return LockResult.success(createNullLock());
    }

    @Override
    public void unlock(String tokenId)
    {
    }

    @Override
    public LockToken getCurrentLock()
    {
        return null;
    }
}
