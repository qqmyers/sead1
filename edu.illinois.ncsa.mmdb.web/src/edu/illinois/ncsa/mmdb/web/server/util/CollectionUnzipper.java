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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.Calendar;

import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class CollectionUnzipper {

    /*
    List Contents Of Zip File Example
    This Java example shows how to list contents of zip file
    using entries method of Java ZipFile class.
    */

    /** Commons logging **/
    private static Log log = LogFactory.getLog(CollectionUnzipper.class);

    public static void main(String args[])
    {
        try {
            /*
                 * To Open a zip file, use
                 *
                 * ZipFile(String fileName)
                 * constructor of the ZipFile class.
                 *
                 * This constructor throws IOException for any I/O error.
                 */
            ZipInputStream zis;

            zis = new ZipInputStream(new FileInputStream("c:/resteasy.zip"));

            /*
             * To get list of entries in the zip file, use
             *
             * Enumeration entries()
             * method of ZipFile class.
             */
            System.out.println("Reading c:/resteasy.zip\n" + listContents(zis));

            zis = new ZipInputStream(new FileInputStream("c:/yellvpf.zip"));

            /*
             * To get list of entries in the zip file, use
             *
             * Enumeration entries()
             * method of ZipFile class.
             */

            System.out.println("Reading c:/yellvpf.zip\n" + listContents(zis));

            zis = new ZipInputStream(new FileInputStream("c:/SEADUploader.zip"));

            /*
             * To get list of entries in the zip file, use
             *
             * Enumeration entries()
             * method of ZipFile class.
             */
            System.out.println("Reading c:/SEADUplaoder.zip\n" + listContents(zis));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String listContents(ZipInputStream zis) {
        return listContents(zis, false);
    }

    public static String listContents(ZipInputStream zis, boolean writeFiles) {
        StringBuffer sBuffer = new StringBuffer();

        Stack<String> s = new Stack<String>();
        ZipEntry entry = null;
        try {
            while ((entry = zis.getNextEntry()) != null)
            {
                String name = entry.getName();
                sBuffer.append("Name: " + name + "\n");
                if (entry.isDirectory()) {
                    String shortname = name.substring(name.substring(0, name.length() - 1).lastIndexOf('/') + 1, name.length() - 1);
                    sBuffer.append("Create Collection: " + shortname + "\n");
                    while (!s.empty()) {
                        if (name.contains(s.peek())) {
                            sBuffer.append(shortname + " is in " + s.peek() + "\n");
                            break;
                        } else {
                            sBuffer.append(s.peek() + " is done\n");
                            s.pop();
                        }
                    }
                    s.push(name);
                } else {
                    String shortname = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                    sBuffer.append(shortname + "\n");
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(entry.getTime());
                    sBuffer.append(DateFormat.getInstance().format(c.getTime()) + "\n");
                    if (writeFiles) {
                        File newFile = new File(File.separator + "temp" + File.separator + shortname);

                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        new File(newFile.getParent()).mkdirs();

                        FileOutputStream fos;
                        try {
                            fos = new FileOutputStream(newFile);

                            int len;
                            byte[] buffer = new byte[1024];

                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }

                            fos.close();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                    while (!s.empty()) {
                        if (name.contains(s.peek())) {
                            sBuffer.append(shortname + " is in " + s.peek() + "\n");
                            break;
                        } else {
                            sBuffer.append(s.peek() + " is done\n");
                            s.pop();
                        }
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sBuffer.toString();

    }

    public static String unpackContents(ZipInputStream zis, String parentCollectionUri, UriRef creator) {

        Context c = TupeloStore.getInstance().getContext();
        ThingSession ts = c.getThingSession();
        //Zip paths mapping to identifiers
        HashMap<String, Thing> collections = new HashMap<String, Thing>();

        //A stack of collection paths to track current depth
        Stack<String> s = new Stack<String>();

        ZipEntry entry = null;
        try {
            while ((entry = zis.getNextEntry()) != null)
            {
                String name = entry.getName();
                log.debug("Found ZipEntry Name: " + name + "\n");
                if (entry.isDirectory()) {
                    String shortname = name.substring(name.substring(0, name.length() - 1).lastIndexOf('/') + 1, name.length() - 1);

                    // Create collection uri
                    Map<Resource, Object> md = new LinkedHashMap<Resource, Object>();
                    md.put(Rdf.TYPE, RestService.COLLECTION_TYPE);
                    UriRef uri = Resource.uriRef(RestUriMinter.getInstance().mintUri(md));
                    log.debug("Creating Collection: " + shortname + ": " + uri.toString() + "\n");

                    Thing t = ts.newThing(uri);
                    BeanFiller.fillCollectionBean(t, shortname, creator, getDate(entry));
                    collections.put(name, t);

                    //Make sub-collection relationship
                    if (s.isEmpty()) {
                        //Did not find a parent collection, this must be directly below the parentCollectionUri
                        ts.addValue(Resource.uriRef(parentCollectionUri), DcTerms.HAS_PART, uri);
                        log.debug("Adding collection " + shortname + " to top-level collection \n");

                    }
                    while (!s.empty()) {
                        if (name.contains(s.peek())) {
                            log.debug("Adding collection " + shortname + " to " + s.peek() + "\n");
                            collections.get(s.peek()).addValue(DcTerms.HAS_PART, uri);
                            break;
                        } else {
                            String collString = s.pop();
                            log.debug(collString + " collection is complete");
                            if (s.isEmpty()) {
                                //Did not find a parent collection, this must be directly below the parentCollectionUri
                                ts.addValue(Resource.uriRef(parentCollectionUri), DcTerms.HAS_PART, uri);
                                log.debug("Adding collection " + shortname + " to top-level collection \n");
                            }
                        }
                    }
                    s.push(name);
                } else { //is File
                    String shortname = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);

                    // Create dataset uri
                    UriRef uri = Resource.uriRef(RestUriMinter.getInstance().mintUri(null));

                    log.debug("Creating dataset: " + shortname + " as " + uri.toString() + "\n");
                    Thing t = ts.newThing(uri);
                    String contentType = TupeloStore.getInstance().getMimeMap().getContentTypeFor(shortname);
                    BeanFiller.fillDataBean(c, t, shortname, contentType, creator, getDate(entry), zis);
                    if (s.isEmpty()) {
                        //Did not find a parent collection, this must be directly below the parentCollectionUri
                        ts.addValue(Resource.uriRef(parentCollectionUri), DcTerms.HAS_PART, uri);
                        log.debug("Adding dataset " + shortname + " to top-level collection \n");
                    }
                    while (!s.empty()) {
                        if (name.contains(s.peek())) {
                            log.debug("Adding dataset " + shortname + " to " + s.peek() + "\n");
                            collections.get(s.peek()).addValue(DcTerms.HAS_PART, uri);
                            break;
                        } else {
                            String collString = s.pop();
                            log.debug(collString + " collection is complete");
                        }
                        if (s.isEmpty()) {
                            //Did not find a parent collection, this must be directly below the parentCollectionUri
                            ts.addValue(Resource.uriRef(parentCollectionUri), DcTerms.HAS_PART, uri);
                            log.debug("Adding dataset " + shortname + " to top-level collection \n");
                        }
                    }
                }
            }
            //Make final hasPart connections, save, and clean up
            while (!s.isEmpty()) {
                log.debug(s.pop() + " collection is complete");
            }
            ts.save();
            ts.close();
            log.debug("Unzipped content successfully saved");
        } catch (IOException e) {
            log.error(e);
            return null;
        } catch (OperatorException e) {
            log.error(e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            log.warn(e);
            return null;
        }
        return parentCollectionUri;
    }

    private static Date getDate(ZipEntry entry) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(entry.getTime());
        return c.getTime();
    }

}
