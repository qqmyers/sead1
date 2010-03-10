package edu.illinois.ncsa.mmdb.web.server.search;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.cet.search.TextExtractor;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class SearchableThingTextExtractor implements TextExtractor<String> {

	@Override
	/**
	 * Extract a text representation of an mmdb thing (e.g., a dataset or collection)
	 * for full-text indexing purposes.
	 */
	public String extractText(String uri) {
		assert uri != null;
		try {
			Object bean = TupeloStore.fetchBean(uri);
			if(bean instanceof CETBean) {
				return text((CETBean)bean);
			}
		} catch(Exception x) { }
		// it's either not a bean or not a CETBean
		try {
			return text(uri);
		} catch(Exception x) { // something's wrong
			x.printStackTrace();
			return "";
		}
	}
	
	String text(String uri) throws OperatorException {
		return unsplit(title(uri), tags(uri));
	}

	String text(CETBean bean) {
		Resource uri = Resource.uriRef(bean.getUri());
		return unsplit(title(bean), tags(uri));
	}
	
	String tags(Resource uri) {
		return tags(uri.getString());
	}
	
	String tags(String uri) {
		TagEventBeanUtil tebu = new TagEventBeanUtil(TupeloStore.getInstance().getBeanSession());
		TreeSet<String> tags = new TreeSet<String>();
		try {
			tags.addAll(tebu.getTags(uri));
			return unsplit(tags);
		} catch (OperatorException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	String title(CETBean bean) {
		return bean.getLabel();
	}
	
	String title(String uri) throws OperatorException {
		Thing thing = TupeloStore.fetchThing(uri);
		String dcTitle = thing.getString(Dc.TITLE);
		String rdfsLabel = thing.getString(Rdfs.LABEL);
		return (dcTitle != null ? dcTitle : "") + (rdfsLabel != null ? " " + rdfsLabel : "");
	}

	// why am I writing this.
	String unsplit(Iterable<String> strings) {
		boolean first = true;
		StringWriter sw = new StringWriter();
		for(String s : strings) {
			if(!first) { sw.append(' '); }
			sw.append(s);
			first = false;
		}
		return sw.toString();
	}
	
	String unsplit(String... strings) {
		List<String> s = new ArrayList<String>(strings.length);
		for(int i = 0; i < strings.length; i++) {
			s.add(strings[i]);
		}
		return unsplit(s);
	}
}
