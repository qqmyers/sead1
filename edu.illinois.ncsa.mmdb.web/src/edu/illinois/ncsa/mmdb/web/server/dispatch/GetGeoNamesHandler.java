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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Properties;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoNames;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoNamesResult;
import edu.illinois.ncsa.mmdb.web.common.GeoName;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Retrieve geonames from google place search api
 *
 * @author jonglee
 *
 */
public class GetGeoNamesHandler implements ActionHandler<GetGeoNames, GetGeoNamesResult> {

    /** Commons logging **/
    private static Log        log = LogFactory.getLog(GetGeoNamesHandler.class);

    /** server property to get google api key **/
    private static Properties props;

    @Override
    public GetGeoNamesResult execute(GetGeoNames arg0, ExecutionContext arg1) throws ActionException {

        // reading the server.properties file for google api key
        if (props == null) {
            props = new Properties();
            // property file location
            String path = "/server.properties"; //$NON-NLS-1$
            log.debug("Loading server property file: " + path);

            // load properties
            InputStream input = null;
            try {
                input = TupeloStore.findFile(path).openStream();
                props.load(input);
            } catch (IOException exc) {
                log.warn("Could not load server.properties.", exc);
            } finally {
                try {
                    input.close();
                } catch (IOException exc) {
                    log.warn("Could not close server.properties.", exc);
                }
            }

        }

        String googleKey = props.getProperty("google.api_key");

        // query string
        String query = arg0.getPlaceQuery();

        HashSet<GeoName> geoNames = new HashSet<GeoName>();

        try {
            // need to url encode the query string for URL
            String encode = URLEncoder.encode(query, "UTF-8");

            // generate the url with query string
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + encode + "&key=" + googleKey;

            // GET request
            URL u = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) u.openConnection();
            uc.setRequestProperty("Content-Type", "application/json");

            // add header with referer to support referer restriction
            uc.setRequestProperty("Referer", "sead.ncsa.illinois.edu");

            uc.setRequestMethod("GET");
            uc.setDoOutput(false);
            int status = uc.getResponseCode();
            if (status != 200) {
                log.error("Invalid HTTP response status code " + status + " from web service server.");
            } else {
                InputStream in = uc.getInputStream();
                BufferedReader d = new BufferedReader(new InputStreamReader(in));

                String buffer = "";

                String inputLine;
                while ((inputLine = d.readLine()) != null) {
                    buffer += inputLine;
                }
                d.close();

                // load the json string into json object by using jackson library
                ObjectMapper mapper = new ObjectMapper();

                JsonNode jsObject = mapper.readTree(buffer);
                JsonNode results = jsObject.get("results");

                // retrive the data from json and create GeoName bean
                for (JsonNode r : results ) {
                    GeoName g = new GeoName();
                    String name = r.get("formatted_address").getTextValue();
                    double lat = r.get("geometry").get("location").get("lat").getValueAsDouble();
                    double lng = r.get("geometry").get("location").get("lng").getValueAsDouble();

                    g.setName(name);
                    g.setLat(lat);
                    g.setLng(lng);

                    geoNames.add(g);
                }
            }

        } catch (MalformedURLException e) {
            log.error("URL is invalid: ", e);
        } catch (IOException e) {
            log.error("IO error: ", e);
        }

        return new GetGeoNamesResult(geoNames);
    }

    @Override
    public Class<GetGeoNames> getActionType() {
        return GetGeoNames.class;
    }

    @Override
    public void rollback(GetGeoNames arg0, GetGeoNamesResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) throws ActionException {
        GetGeoNamesHandler g = new GetGeoNamesHandler();
        GetGeoNames gg = new GetGeoNames("chicago");
        GetGeoNamesResult e = g.execute(gg, null);
        HashSet<GeoName> geoNames = e.getGeoNames();
        for (GeoName gn : geoNames ) {
            System.out.println(gn.getName());
        }
    }

}
