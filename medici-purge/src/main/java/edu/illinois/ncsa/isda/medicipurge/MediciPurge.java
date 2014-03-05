package edu.illinois.ncsa.isda.medicipurge;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.kernel.impl.HashFileContext;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.tupelo.PreviewBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewPyramidBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class MediciPurge {
	private static String USERNAME = "medici";
	private static String PASSWORD = "medici";
	private static String DATABASE = "medici";
	private static String HOSTNAME = "localhost";
	private static String HFCPATH  = "/home/medici/data";

	public static void main(String[] args) throws Exception {
		boolean doit = false;
		long bytes = 0;
		Set<Resource> blobs = new HashSet<Resource>();
		Set<Resource> keep = new HashSet<Resource>();
		Set<File> files = new HashSet<File>();
		TripleWriter tw = new TripleWriter();
		
		int idx = 0;
		if ((args.length > 0) && args[0].equals("--doit")) {
			doit = true;
			idx++;
		}
		if (args.length == idx+1) {
			HFCPATH = args[idx];
		} else if (args.length == idx+3) {
			USERNAME = args[idx];
			PASSWORD = args[idx+1];
			DATABASE = args[idx+2];
		} else if (args.length == idx+4) {
			USERNAME = args[idx];
			PASSWORD = args[idx+1];
			DATABASE = args[idx+2];
			HFCPATH = args[idx+3];
		}
		
		if (doit) {
			System.out.println("Will delete data.");
		}
		System.out.println(String.format("path=%s user=%s pass=%s dbase=%s host=%s", HFCPATH, USERNAME, PASSWORD, DATABASE, HOSTNAME));
		
		
        MysqlContext mc = new MysqlContext();
        mc.setUser(USERNAME);
        mc.setPassword(PASSWORD);
        mc.setSchema(DATABASE);
        mc.setHost(HOSTNAME);
        mc.connect();
        
        
        HashFileContext.HashFileMapping hfm = new HashFileContext.HashFileMapping();
        hfm.setDepth(3);
        hfm.setDirectory(new File(HFCPATH));

        Unifier uf = new Unifier();
        uf.addPattern("s", Rdf.TYPE, Cet.DATASET);
        uf.addPattern("s", DcTerms.IS_REPLACED_BY, "r", true);
        uf.addPattern("s", "p", "o");
        uf.addOrderBy("s");
        uf.setColumnNames("s", "p", "o", "r");
        mc.perform(uf);
        for(Tuple<Resource> row : uf.getResult()) {
        	if (row.get(3) != null) {
        		if (!row.get(3).equals(Rdf.NIL)) {
        			System.out.println(row.get(3));
        		}
	        	blobs.add(row.get(0));
	        	File f = new File(hfm.inverseMap(((UriRef)row.get(0)).getUri()));
	        	if (!files.contains(f)) {
		        	files.add(f);
		        	if (f.exists()) {
		        		bytes += f.length();
		        	}
		        	f = new File(f.getAbsolutePath() + ".uri");
		        	files.add(f);
		        	if (f.exists()) {
		        		bytes += f.length();
		        	}
	        	}
	        	tw.remove(row.get(0), row.get(1), row.get(2));
        	} else {
        		keep.add(row.get(0));
        	}
        }
        Set<Resource> uris = new HashSet<Resource>(blobs);
        System.out.println(String.format("Step 1  : Keeping %,d datasets, Removing %,d datasets, %,d bytes, %,d triples, %,d blobs.", keep.size(), uris.size(), bytes, tw.getToRemove().size(), blobs.size()));

        int i=1;
        for (Resource r : uris) {
        	bytes += stage0(mc, hfm, r, tw, blobs, files);        	
            System.out.println(String.format("%03d/%03d : %,d bytes, %,d triples, %,d blobs.", i++, uris.size(), bytes, tw.getToRemove().size(), blobs.size()));
        }
        
        System.out.println(String.format("Total   : Keeping %,d datasets, Removing %,d datasets, %,d bytes, %,d triples, %,d blobs.", keep.size(), uris.size(), bytes, tw.getToRemove().size(), blobs.size()));
        if (doit) {
        	for (File f : files) {
        		while(f.delete()) {
        			f = f.getParentFile();
        		}
        	}
        	mc.perform(tw);
        }
	}

    public static long stage0(Context c, HashFileContext.HashFileMapping hfm, Resource uri, TripleWriter tw, Set<Resource> blobs, Set<File> files) throws OperatorException {
    	long bytes = 0;
    	
        // remove metadata
        Unifier uf = new Unifier();
        uf.addPattern(uri, "predicate", "value"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern("predicate", Rdf.TYPE, MMDB.METADATA_TYPE); //$NON-NLS-1$
        uf.addPattern("predicate", MMDB.METADATA_EXTRACTOR, "e"); //$NON-NLS-1$
        uf.setColumnNames("predicate", "value"); //$NON-NLS-1$ //$NON-NLS-2$ 
        c.perform(uf);
        for (Tuple<Resource> row : uf.getResult() ) {
            tw.remove(uri, row.get(0), row.get(1));
        }

        // remove pyramid tiles
        uf = new Unifier();
        uf.addPattern(uri, PreviewBeanUtil.HAS_PREVIEW, "preview"); //$NON-NLS-1$
        uf.addPattern("preview", PreviewPyramidBeanUtil.PYRAMID_TILES, "tile"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern("tile", "p", "o"); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
        uf.setColumnNames("tile", "p", "o"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.perform(uf);
        for (Tuple<Resource> row : uf.getResult() ) {
            // FIXME TUP-507 workaround
            if (row.get(0) != null) {
                if ((row.get(1) != null) && (row.get(2) != null)) {
                    tw.remove(row.get(0), row.get(1), row.get(2));
                }
	        	blobs.add(row.get(0));
	        	File f = new File(hfm.inverseMap(((UriRef)row.get(0)).getUri()));
	        	if (!files.contains(f)) {
		        	files.add(f);
		        	if (f.exists()) {
		        		bytes += f.length();
		        	}
		        	f = new File(f.getAbsolutePath() + ".uri");
		        	files.add(f);
		        	if (f.exists()) {
		        		bytes += f.length();
		        	}
	        	}
            }
        }

        // remove previews and data
        uf = new Unifier();
        uf.addPattern(uri, PreviewBeanUtil.HAS_PREVIEW, "preview"); //$NON-NLS-1$
        uf.addPattern("preview", "p", "o"); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
        uf.setColumnNames("preview", "p", "o"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
        c.perform(uf);
        for (Tuple<Resource> row : uf.getResult() ) {
            // FIXME TUP-507 workaround
            if (row.get(0) != null) {
                if ((row.get(1) != null) && (row.get(2) != null)) {
                    tw.remove(row.get(0), row.get(1), row.get(2));
                }
                tw.remove(uri, PreviewBeanUtil.HAS_PREVIEW, row.get(0));
	        	blobs.add(row.get(0));
	        	File f = new File(hfm.inverseMap(((UriRef)row.get(0)).getUri()));
	        	if (!files.contains(f)) {
		        	files.add(f);
		        	if (f.exists()) {
		        		bytes += f.length();
		        	}
		        	f = new File(f.getAbsolutePath() + ".uri");
		        	files.add(f);
		        	if (f.exists()) {
		        		bytes += f.length();
		        	}
	        	}
            }
        }

        // remove sections
        uf = new Unifier();
        uf.addPattern(uri, MMDB.METADATA_HASSECTION, "section"); //$NON-NLS-1$
        uf.addPattern("section", MMDB.METADATA_EXTRACTOR, "e"); //$NON-NLS-1$
        uf.addPattern("section", Rdf.TYPE, MMDB.SECTION_TYPE); //$NON-NLS-1$
        uf.addPattern("section", MMDB.SECTION_TEXT, "text"); //$NON-NLS-1$  //$NON-NLS-2$
        uf.addPattern("section", MMDB.SECTION_LABEL, "label"); //$NON-NLS-1$  //$NON-NLS-2$
        uf.addPattern("section", MMDB.SECTION_PREVIEW, "preview"); //$NON-NLS-1$  //$NON-NLS-2$
        uf.addPattern("section", MMDB.SECTION_MARKER, "marker"); //$NON-NLS-1$  //$NON-NLS-2$
        uf.setColumnNames("section", "text", "label", "preview", "marker"); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        c.perform(uf);
        for (Tuple<Resource> row : uf.getResult() ) {
            tw.remove(uri, MMDB.METADATA_HASSECTION, row.get(0));
            tw.remove(row.get(0), MMDB.METADATA_EXTRACTOR, "e");
            tw.remove(row.get(0), Rdf.TYPE, MMDB.SECTION_TYPE);
            tw.remove(row.get(0), MMDB.SECTION_TEXT, row.get(1));
            tw.remove(row.get(0), MMDB.SECTION_LABEL, row.get(2));
            tw.remove(row.get(0), MMDB.SECTION_PREVIEW, row.get(3));
            tw.remove(row.get(0), MMDB.SECTION_MARKER, row.get(4));
        }
        
        return bytes;
    }

}
