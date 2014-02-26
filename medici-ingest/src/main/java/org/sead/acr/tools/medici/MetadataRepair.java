package org.sead.acr.tools.medici;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.ContentStoreContext;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.kernel.impl.HashFileContext;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.mysql.NewMysqlContext;
import org.tupeloproject.rdf.Literal;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

/*This class allows read/write of individual triples to an ACR/Medici triple store. It accepts triples in csv format, preceded by the action to be taken:
 * <action>,<s>,<p>,<o>
 * where action can be:
 * Remove - remove a triple with a URI object
 * RemoveL - remove a triple with a literal object
 * Add - add a triple with a URI object
 * AddL - add a triple with a literal (string) object
 * AddL<type> - add a triple with a literal object of the XML type <type>
 * 
 * The last option is important for some Medici functionlity, i.e. a file size that is sent as a literal 
 * rather than a literal/long will not show up as a file size in the GUI (although the triple will be there)
 * 
 * The triple elements must be enclosed in quotes if they contain a ',' character (standard csv)
 * 
 * Extra spaces will cause parse errors
 * 
 * Example: 
 * 
 * AddL,"tag:cet.ncsa.uiuc.edu,2008:/bean/Dataset/02b073d1-4241-4465-a737-7d8d77c21144",http://purl.org/vocab/frbr/core#embodimentOf,/path/to/original/file
 * 
 * 
 * The software is invoked with any of three optional parameters and the file(s) to be ingested:
 * 
 * -listOnly - only parse the file and write the results (triples that would be removed/written)
 * -limit<long> - e.g. -limit100 - limits the number of triples processed. Currently, the software will only 
 *                remove limit/2 and write limit/2 triples - helpful if you are replacing triples and have not sorted the input lines
 * -skip<long> - e.g. -skip50 - # of triples to skip before starting to remove/write triples. WHen used with -limit, limit triples 
 *               will still be removed/added, at the offset of skip. Since the counts/limits are maintained separately for remove/add, 
 *               skip should also be even - the first skip/2 removes and adds will be read before new writes are processed.
 *               
 * Example:
 * java -cp mi.jar edu.illinois.ncsa.medici.ingest.MetadataRepair -listOnly -limit100 -skip100 test.csv
 * 
 * Triples parsed are written to a repair-log file as well as to the console along with the parameters used.
 * 
 * @author myersjd@umich.edu
 *                               
 */

public class MetadataRepair extends MediciToolBase {

    private static long        max         = 9223372036854775807l;
    private static long        skip        = 0l;
    private static long        addCount    = 0l;
    private static long        removeCount = 0l;
    
    public static void main(String[] args) throws Exception {

        init("metadata-repair-log-", false); //No beansession needed

        for (String arg : args) {
            println("Arg is : " + arg);
            if (arg.equalsIgnoreCase("-listonly")) {
                listonly = true;
                println("List Only Mode");
            } else if (arg.startsWith("-limit")) {
                max = Long.parseLong(arg.substring(6));
                println("Max triple count: " + max);
            } else if (arg.startsWith("-skip")) {
                skip = Long.parseLong(arg.substring(5));
                println("Skip triple count: " + skip);
            }
        }

        // go through arguments
        for (String arg : args) {
            if (!((arg.equalsIgnoreCase("-listonly")) || (arg.startsWith("-limit")) || (arg.startsWith("-skip")))) {

                File file = new File(arg);
                rwTriples(file);
            }
        }
        flushLog();
    }

    private static void rwTriples(File file) throws OperatorException, IOException {

        BufferedReader br;
        TripleWriter tw = null;
        br = new BufferedReader(new FileReader(file));
        long skipCount = 0l;
        int icr = 0;
        while (br.ready() && (addCount + removeCount < max)) {
            if (icr == 0) {
                tw = new TripleWriter();
            }
            String nextLine = br.readLine();

            String[] cells = new String[4];
            for (int i = 0; i < cells.length; i++) {
                if (i > 0) {
                    nextLine = nextLine.substring(1); // remove ',' after first
                                                      // run through
                }
                if (nextLine.startsWith("\"")) {
                    nextLine = nextLine.substring(1);
                    cells[i] = nextLine.substring(0, nextLine.indexOf("\""));
                    nextLine = nextLine.substring(cells[i].length() + 1); // Skip
                                                                          // token,
                                                                          // following
                                                                          // quote
                } else {
                    int j = nextLine.indexOf(",");
                    if (j != -1) {
                        cells[i] = nextLine.substring(0, j);
                    } else {
                        cells[i] = nextLine; // Last time through
                    }

                    nextLine = nextLine.substring(cells[i].length());
                }

            }
            if (nextLine.length() != 0) {
                println("Error: Unread tokens: " + nextLine);
            }
            if (skipCount < skip) {
                skipCount++;

            } else {
                if (cells[0].equals("Remove")) {
                    if (removeCount < max / 2) {
                        tw.remove(Resource.uriRef(cells[1]), Resource.uriRef(cells[2]), Resource.uriRef(cells[3]));
                        removeCount++;
                    }
                } else if (cells[0].equals("Add")) {
                    if (addCount < max / 2) {
                        tw.add(Resource.uriRef(cells[1]), Resource.uriRef(cells[2]), Resource.uriRef(cells[3]));
                        addCount++;
                    }
                } else if (cells[0].equals("RemoveL")) {
                    if (removeCount < max / 2) {
                        tw.remove(Resource.uriRef(cells[1]), Resource.uriRef(cells[2]), Resource.literal(cells[3]));
                        removeCount++;
                    }
                } else if (cells[0].startsWith("AddL")) {
                    if (addCount < max / 2) {
                        String type = cells[0].substring(4);
                        Literal obj = null;
                        if (type.length() > 0) {
                            obj = Resource.literal(cells[3], Resource.uriRef("http://www.w3.org/2001/XMLSchema#" + type));
                        } else {
                            obj = Resource.literal(cells[3]);
                        }
                        tw.add(Resource.uriRef(cells[1]), Resource.uriRef(cells[2]), obj);
                        addCount++;
                    }
                }
                icr++;
            }
            if (icr == 100) {
                icr = 0;
                println("Current Count: " + (addCount + removeCount));
                writeTriples(tw);
            }
        }
        if (icr != 0) {
            println("Final Count: " + (addCount + removeCount));
            writeTriples(tw);
        }
        flushLog();
    }

    private static void writeTriples(TripleWriter tw) {
        Set<Triple> rem = tw.getToRemove();
        Set<Triple> add = tw.getToAdd();
        println("To Remove:");
        for (Triple t : rem) {
            println(t.toString());
        }
        println("To Add:");
        for (Triple t : add) {
            println(t.toString());
        }
        if (!listonly) {
            try {
                context.perform(tw);
            } catch (Exception e) {
                println(e.getMessage());
                println(e.getStackTrace().toString());
                flushLog();
                System.exit(0);
            }
        }
    }
}
