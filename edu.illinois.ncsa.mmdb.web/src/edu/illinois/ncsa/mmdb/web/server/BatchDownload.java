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
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Bulk Download Servlet
 * 
 * @author Luis Mendez
 * 
 */
public class BatchDownload extends AuthenticatedServlet {
    private static final long serialVersionUID = 8540356072366902770L;
    Log                       log              = LogFactory.getLog(BatchDownload.class);

    /**
     * Handle POST request.<br>
     */
    @Override
    public void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String userId = AuthenticatedServlet.getUserUri(request);
        if (userId == null) {
            return;
        }

        String uris = request.getParameter("uri");
        String filename = request.getParameter("filename");

        //log.info("Bulk Download: downloading " + uri);
        byte[] buf = new byte[1024];
        int counter = 1;
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition", "attachment; filename=" + filename + ".zip");

        String[] list = uris.split("&");

        ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        HashSet<String> filenames = new HashSet<String>();

        for (String uri : list ) {
            try {

                String file = dbu.get(uri).getFilename();
                //Check to make sure no two files contain the same name in the zip
                if (filenames.contains(file)) {
                    file = counter + "_" + file;
                    counter++;
                } else {
                    filenames.add(file);
                }
                log.info("Adding file: " + file + " URI: " + uri);
                InputStream in = beanSession.fetchBlob(Resource.uriRef(uri));

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(file));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();

            } catch (IOException e) {
                log.info("failed reading file stream");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                log.info("fetch filename failed");
            }
        }
        // Complete the ZIP file
        out.flush();
        out.close();

    }
}