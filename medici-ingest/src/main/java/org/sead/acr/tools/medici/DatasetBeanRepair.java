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
import org.tupeloproject.rdf.terms.Beans;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
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

/*This class scans all existing DataSets and assures they have the minimal metadata inherent in the Dataset bean. These are not necessary 
 * for proper bean function, as they will be generated if/when a bean is read and rewritten. However, external tools and webservices cannot currently 
 * depend on the existence of this metadata since some upload mechanisms have not historically used beans or written this metadata. (Current software does
 * write this metadata upon upload and, historically, any operation that caused a bean read/write silently fixed/upgraded.).
 * 
 *  To scan, this class looks for all datasets and checks to see if they have a dc:identifier property set. If not, one is added along with 
 *  the other bean metadata.
 * 
 * @author myersjd@umich.edu
 *                               
 */

public class DatasetBeanRepair extends MediciToolBase {

    private static long        max         = 9223372036854775807l;
    private static long        skip        = 0l;
    private static long        addCount    = 0l;
    private static long        removeCount = 0l;
    
    public static void main(String[] args) throws Exception {

        init("datasetbean-repair-log-", false); //No beansession needed
        

    repairBeans();
    flushLog();
    }

    private static void repairBeans() throws OperatorException, IOException {

        Unifier uf = new Unifier();
        uf.addPattern("db",  Rdf.TYPE, Cet.DATASET);
        uf.addPattern("db", Dc.IDENTIFIER, "id", true);
        uf.setColumnNames("db", "id");
        try {
        context.perform(uf);
        TripleWriter tw = new TripleWriter();
        for(Tuple<Resource> tu: uf.getResult()) {
            if(tu.get(1) == null) {
                println("Updating: " + tu.get(0).toString());

                tw.add(tu.get(0), Dc.IDENTIFIER, tu.get(0));
                tw.add(tu.get(0), Rdf.TYPE, Beans.STORAGE_TYPE_BEAN_ENTRY);
                tw.add(tu.get(0), Beans.PROPERTY_VALUE_IMPLEMENTATION_CLASSNAME,
                        Resource.literal("edu.uiuc.ncsa.cet.bean.DatasetBean"));
                tw.add(tu.get(0), Beans.PROPERTY_IMPLEMENTATION_MAPPING_SUBJECT,
                        Resource.uriRef("tag:cet.ncsa.uiuc.edu,2009:/mapping/http://cet.ncsa.uiuc.edu/2007/Dataset"));
            }
        }
                context.perform(tw);
            } catch (Exception e) {
                println(e.getMessage());
                println(e.getStackTrace().toString());
                flushLog();
                System.exit(0);
            }
        }
    }

