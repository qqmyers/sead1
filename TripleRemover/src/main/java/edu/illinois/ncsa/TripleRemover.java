package edu.illinois.ncsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.tupeloproject.kernel.BlobChecker;
import org.tupeloproject.kernel.BlobRemover;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.impl.HashFileContext;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;

public class TripleRemover {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("TripleRemover subject ...");
			System.exit(0);
		}
		
		MysqlContext mc = new MysqlContext();
		mc.setHost("localhost");
		mc.setUser("medici");
		mc.setPassword("medici");
		mc.setSchema("medici");
		
		HashFileContext hfc = new HashFileContext();
		hfc.setDepth(3);
		hfc.setDirectory(new File("/home/medici/data"));
		
		TripleWriter tw = new TripleWriter();
		Set<UriRef> blobs = new HashSet<UriRef>();
		for(String x : args) {
			UriRef uri = Resource.uriRef(x);
			try {
				TripleMatcher tm = new TripleMatcher();
				tm.setSubject(uri);
				mc.perform(tm);
				tw.removeAll(tm.getResult());
			} catch(OperatorException e) {
				e.printStackTrace();
			}
			
			try {
				TripleMatcher tm = new TripleMatcher();
				tm.setObject(uri);
				mc.perform(tm);
				tw.removeAll(tm.getResult());
			} catch(OperatorException e) {
				e.printStackTrace();
			}

			try {
				BlobChecker bc = new BlobChecker();
				bc.setSubject(uri);
				hfc.perform(bc);
				if (bc.exists()) {
					blobs.add(uri);
				}
			} catch(OperatorException e) {
				e.printStackTrace();
			}
		}
		
		for(Triple t : tw.getToRemove()) {
			System.out.println(t);
		}

		System.out.println("Removing " + tw.getToRemove().size() + " triples.");
		System.out.println("Removing " + blobs.size() + " blobs.");
		
		try {
			System.out.print("Are you sure you want to run this? [y/N] : ");
			String answer = new BufferedReader(new InputStreamReader(System.in)).readLine();
			
			if (answer.toLowerCase().startsWith("y")) {
				mc.perform(tw);
				for(UriRef uri : blobs) {
					hfc.perform(new BlobRemover(uri));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
