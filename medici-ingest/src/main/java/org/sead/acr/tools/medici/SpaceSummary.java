/*
 *
 * Copyright 2015 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * @author myersjd@umich.edu
 */

package org.sead.acr.tools.medici;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

/* This tool records summary information about a space including:
 * config options,Content stats, Users, views/download stats, metadata and relationship terms/labels/definitions, tags
 */

public class SpaceSummary extends MediciToolBase {

    private static PrintWriter sumPw     = null;
    private static boolean     anonymize = false;

    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            if (args[0].equals("-anon")) {

                anonymize = true;
                println("Removing user names");
            }
        }

        init("summary-log-", false); // No beansession needed

        Map<String, String> configOptionsMap = getConfigOptions();
        String projectName = configOptionsMap.get("http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/ProjectName");
        println("Summarizing the \"" + projectName + "\" Project Space");

        String projectFileName = projectName.replace(" ", "_");
        File summaryFile = null;
        if (anonymize) {
            summaryFile = new File("Summary." + projectFileName + ".txt");
        } else {
            summaryFile = new File("SummaryWithAccountNames." + projectFileName + ".txt");
        }
        try {
            sumPw = new PrintWriter(new FileWriter(summaryFile));
        } catch (Exception e) {
            println(e.getMessage());
        }
        sumPw.println("Summary for the \"" + projectName + "\" Project Space");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        sumPw.println(dateFormat.format(date));
        sumPw.println();

        println("Retrieving Config Options");
        printConfigOptions(configOptionsMap);

        sumPw.println();
        println("Retrieving Content Stats");
        printStats();
        sumPw.println();
        println("Retrieving User List");
        printUsers();
        sumPw.println();
        println("Retrieving View Statistics");
        printViewStats();
        sumPw.println();
        println("Retrieving Download Statistics");
        printDownloadStats();

        sumPw.println();
        println("Retrieving Metadata Terms");
        printMetadata();

        sumPw.println();
        println("Retrieving Relationship Terms");
        printRelationships();

        sumPw.println();
        println("Retrieving Tags");
        printTags();

        println("Summarization Complete");

        flushLog();
        sumPw.flush();
        sumPw.close();
    }

    private static void printUsers() {
        sumPw.println("Users:");

        HashMap<String, String> roles = new HashMap<String, String>();
        Unifier roleUf = new Unifier();
        roleUf.addPattern("role", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/role/Role"));
        roleUf.addPattern("role", Rdfs.LABEL, "name");
        roleUf.setColumnNames("role", "name");
        sumPw.println("Defined Roles:");
        try {
            context.perform(roleUf);

            for (Tuple t : roleUf.getResult()) {
                roles.put(t.get(0).toString(), t.get(1).toString());
                sumPw.println(t.get(1).toString());
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sumPw.println();
        HashMap<String, String> users = new HashMap<String, String>();
        Unifier uf = new Unifier();
        uf.addPattern("person", Rdf.TYPE, Resource.uriRef("http://xmlns.com/foaf/0.1/Person"));
        uf.addPattern("person", Resource.uriRef("http://xmlns.com/foaf/0.1/name"), "name");
        uf.addPattern("person", Resource.uriRef("http://xmlns.com/foaf/0.1/mbox"), "mail");
        uf.addPattern("person", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/role/hasRole"), "role");
        uf.addPattern("person", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/lastLogin"), "last", true);
        uf.addPattern("person", Resource.uriRef("http://purl.org/dc/terms/alternative"), "altid", true);
        uf.addPattern("person", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/retired"), "ret", true);

        uf.setColumnNames("person", "name", "mail", "role", "last", "altid", "ret");

        try {
            context.perform(uf);
            int numUsers = 1;
            for (Tuple t : uf.getResult()) {
                if (anonymize) {
                    sumPw.println("User " + numUsers + ":");
                } else {
                    sumPw.println(t.get(1).toString() + ":");
                }
                sumPw.println("\tEmail: " + anonymizeIfNeeded(t.get(2).toString()));
                String role = t.get(3).toString();
                role = roles.get(role);
                sumPw.println("\tRole: " + role);
                if (t.get(4) != null) {
                    sumPw.println("\tLastLogin: " + t.get(4).toString());
                }
                if (t.get(5) != null) {
                    sumPw.println("\tPID: " + t.get(5).toString());
                }
                if (t.get(6) != null) {
                    sumPw.println("\tAccessEnded: " + t.get(6).toString());
                }
            }

        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void printStats() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        sumPw.println("Content Statistics:");

        // HttpGet httpget = new
        // HttpGet("https://localhost/acr/resteasy/sys/info");
        HttpGet httpget = new HttpGet("http://localhost:8080/medici/resteasy/sys/info");

        CloseableHttpResponse getResponse;
        try {
            getResponse = httpclient.execute(httpget);
            if (getResponse.getStatusLine().getStatusCode() == 200) {
                String stats = EntityUtils.toString(getResponse.getEntity());
                stats = stats.substring(1, stats.length() - 1); // strip json {}
                stats = stats.replace("\"", ""); // strip quotes
                stats = stats.replace(":", ": "); // add space
                stats = stats.replace(",", "\r\n"); // separate lines
                sumPw.println(stats);
            } else {
                println("Unable to retrieve stats");
                sumPw.println("Unable to retrieve stats");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            sumPw.println("Unable to retrieve stats");

        } catch (IOException e) {
            e.printStackTrace();
            sumPw.println("Unable to retrieve stats");

        }
    }

    private static void printConfigOptions(Map<String, String> options) {
        String key = "http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/ProjectName";
        sumPw.println("ProjectName: " + options.get(key));
        options.remove(key);

        key = "http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/ProjectDescription";
        sumPw.println("ProjectDescription: " + options.get(key));
        options.remove(key);

        key = "http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/ProjectHeaderLogo";
        sumPw.println("ProjectHeaderLogo: " + options.get(key));
        options.remove(key);

        key = "http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/ProjectURL";
        sumPw.println("ProjectURL: " + options.get(key));
        options.remove(key);

        sumPw.println();
        sumPw.println("Additional Configuration Options:");
        for (String key1 : options.keySet()) {
            sumPw.println(key1.substring("http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/".length()) + ": " + options.get(key1));
        }

    }

    private static void printViewStats() {
        Unifier uf = new Unifier();
        uf.addPattern("data", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/isViewedBy"), "event");
        uf.addPattern("data", Rdfs.LABEL, "name");
        uf.addPattern("event", Resource.uriRef("http://purl.org/dc/elements/1.1/creator"), "person");

        uf.setColumnNames("data", "person", "name");
        Map<String, Integer> dataaccessesHashMap = new HashMap<String, Integer>();
        Map<String, String> dsnamesHashMap = new HashMap<String, String>();

        Map<String, Integer> useraccessesHashMap = new HashMap<String, Integer>();
        sumPw.println("View Statistics:");

        try {
            context.perform(uf);
            for (Tuple t : uf.getResult()) {
                String dataset = t.get(0).toString();
                String user = t.get(1).toString();
                dsnamesHashMap.put(dataset, t.get(2).toString());

                if (dataaccessesHashMap.containsKey(dataset)) {
                    dataaccessesHashMap.put(dataset, dataaccessesHashMap.get(dataset) + 1);
                } else {
                    dataaccessesHashMap.put(dataset, 1);
                }
                if (useraccessesHashMap.containsKey(user)) {
                    useraccessesHashMap.put(user, useraccessesHashMap.get(user) + 1);
                } else {
                    useraccessesHashMap.put(user, 1);
                }
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sumPw.println();
        sumPw.println("By Dataset: ");
        int totalViews = 0;
        dataaccessesHashMap = sortByValue(dataaccessesHashMap);
        for (String dataset : dataaccessesHashMap.keySet()) {
            sumPw.println(dataaccessesHashMap.get(dataset) + "," + csvEscapeString(dsnamesHashMap.get(dataset)) + "," + csvEscapeString(dataset));
            totalViews += dataaccessesHashMap.get(dataset);
        }
        sumPw.println("\r\nBy User: ");
        useraccessesHashMap = sortByValue(useraccessesHashMap);
        for (String user : useraccessesHashMap.keySet()) {
            sumPw.println(useraccessesHashMap.get(user) + "," + csvEscapeString(anonymizeIfNeeded(user)));
        }
        sumPw.println("\r\nTotal Views: " + totalViews);
    }

    private static void printDownloadStats() {
        Unifier uf = new Unifier();
        uf.addPattern("data", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/isDownloadedBy"), "event");
        uf.addPattern("data", Rdfs.LABEL, "name");

        uf.addPattern("event", Resource.uriRef("http://purl.org/dc/elements/1.1/creator"), "person");

        uf.setColumnNames("data", "person", "name");
        Map<String, Integer> dataaccessesHashMap = new HashMap<String, Integer>();
        Map<String, String> dsnamesHashMap = new HashMap<String, String>();
        Map<String, Integer> useraccessesHashMap = new HashMap<String, Integer>();
        sumPw.println("Download Statistics:");

        try {
            context.perform(uf);
            for (Tuple t : uf.getResult()) {
                String dataset = t.get(0).toString();
                String user = t.get(1).toString();
                dsnamesHashMap.put(dataset, t.get(2).toString());
                if (dataaccessesHashMap.containsKey(dataset)) {
                    dataaccessesHashMap.put(dataset, dataaccessesHashMap.get(dataset) + 1);
                } else {
                    dataaccessesHashMap.put(dataset, 1);
                }
                if (useraccessesHashMap.containsKey(user)) {
                    useraccessesHashMap.put(user, useraccessesHashMap.get(user) + 1);
                } else {
                    useraccessesHashMap.put(user, 1);
                }
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sumPw.println();
        sumPw.println("By Dataset: ");
        int totalDownloads = 0;
        dataaccessesHashMap = sortByValue(dataaccessesHashMap);
        for (String dataset : dataaccessesHashMap.keySet()) {
            sumPw.println(dataaccessesHashMap.get(dataset) + "," + csvEscapeString(dsnamesHashMap.get(dataset)) + "," + csvEscapeString(dataset));
            totalDownloads += dataaccessesHashMap.get(dataset);
        }
        sumPw.println("\r\nBy User: ");
        useraccessesHashMap = sortByValue(useraccessesHashMap);
        for (String user : useraccessesHashMap.keySet()) {
            sumPw.println(useraccessesHashMap.get(user) + "," + csvEscapeString(anonymizeIfNeeded(user)));
        }
        sumPw.println("\r\nTotal Downloads: " + totalDownloads);
    }

    private static void printMetadata() {

        Unifier uf = new Unifier();
        uf.addPattern("md", Rdf.TYPE, Resource.uriRef("http://sead-data.net/terms/acr/Viewable_Metadata"));
        uf.addPattern("md", Rdfs.LABEL, "name");
        uf.addPattern("md", Resource.uriRef("http://www.w3.org/2000/01/rdf-schema#comment"), "desc", true);

        uf.addPattern("md", "type", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/userMetadataField"), true);

        uf.setColumnNames("md", "name", "desc", "type");

        sumPw.println("Metadata Terms:");

        try {
            context.perform(uf);
            for (Tuple t : uf.getResult()) {
                String term = t.get(0).toString();
                String name = t.get(1).toString();
                String desc = "";
                String gui = "View Only";
                if (t.get(2) != null) {
                    desc = "," + csvEscapeString(t.get(2).toString());
                }
                if (t.get(3) != null) {
                    gui = "In Add List";
                }
                sumPw.println();
                sumPw.println(csvEscapeString(name) + "," + csvEscapeString(term) + desc + "," + csvEscapeString(gui));
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void printRelationships() {

        Unifier uf = new Unifier();
        uf.addPattern("rel", Rdf.TYPE, Resource.uriRef("http://sead-data.net/terms/acr/Viewable_Relationship"));
        uf.addPattern("rel", Rdfs.LABEL, "name");
        uf.addPattern("rel", Resource.uriRef("http://www.w3.org/2002/07/owl#inverseOf"), "inv", true);
        uf.setColumnNames("rel", "name", "inv");

        sumPw.println("Relationship Terms:");

        try {
            context.perform(uf);
            for (Tuple t : uf.getResult()) {
                String term = t.get(0).toString();
                String name = t.get(1).toString();
                String inv = "";
                if (t.get(2) != null) {
                    inv = "InverseOf : " + t.get(2).toString();
                }
                sumPw.println();
                sumPw.println(csvEscapeString(name) + "," + csvEscapeString(term) + "," + csvEscapeString(inv));
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void printTags() {
        Map<String, String> tagsMap = new HashMap<String, String>();
        Unifier uf = new Unifier();
        uf.addPattern("tag", Resource.uriRef("http://www.holygoat.co.uk/owl/redwood/0.1/tags/name"), "name");

        uf.setColumnNames("name");

        sumPw.println("Tags:");

        try {
            context.perform(uf);
            for (Tuple t : uf.getResult()) {
                String name = t.get(0).toString();
                sumPw.println(name);
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static String csvEscapeString(String s) {
        if (s.contains(",") || s.contains("\n") || s.contains("\"")) {
            s = s.replace("\"", "\"\"");
            s = "\"" + s + "\"";
        }
        return s;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    static String anonymizeIfNeeded(String user) {
        if (anonymize) {

            String privateUser = user.substring(user.lastIndexOf("/") + 1);

            if (privateUser.contains("@")) {
                privateUser = "****" + privateUser.substring(privateUser.indexOf("@"));
            }
            return privateUser;
        } else {
            return user;
        }
    }
}
