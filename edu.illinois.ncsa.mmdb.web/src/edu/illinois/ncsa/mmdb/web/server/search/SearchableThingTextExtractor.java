package edu.illinois.ncsa.mmdb.web.server.search;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.cet.search.TextExtractor;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetCollectionsHandler;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class SearchableThingTextExtractor implements TextExtractor<String> {
	Log log = LogFactory.getLog(SearchableThingTextExtractor.class);
	
	BeanSession getBeanSession() throws OperatorException, ClassNotFoundException {
		return CETBeans.createBeanSession(TupeloStore.getInstance().getContext());
	}
	Object fetchBean(String uri) throws OperatorException, ClassNotFoundException {
		return getBeanSession().fetchBean(Resource.uriRef(uri));
	}
	
	@Override
	/**
	 * Extract a text representation of an mmdb thing (e.g., a dataset or collection)
	 * for full-text indexing purposes.
	 */
	public String extractText(String uri) {
		assert uri != null;
		String text = "";
		try {
			Object bean = fetchBean(uri);
			if(bean instanceof CETBean) {
				text = text((CETBean)bean);
			}
		} catch(Exception x) {
			log.warn("unexpected bean session behavior: "+x.getMessage());
		}
		// it's either not a bean or not a CETBean
		try {
			text = text(uri);
		} catch(Exception x) { // something's wrong
			x.printStackTrace();
			return "";
		}
		//log.debug(uri+"="+text); // FIXME debug
		return text;
	}
	
	String text(String uri) throws OperatorException, ClassNotFoundException {
		return unsplit(title(uri), tags(uri), authors(uri), annotations(uri), collections(uri));
	}

	String text(CETBean bean) throws OperatorException, ClassNotFoundException {
		return unsplit(title(bean), tags(bean), authors(bean), annotations(bean), collections(bean));
	}
	
	String authors(DatasetBean bean) {
		List<PersonBean> contributors = new LinkedList<PersonBean>();
		contributors.add(bean.getCreator());
		contributors.addAll(bean.getContributors());
		List<String> names = new LinkedList<String>();
		for(PersonBean person : contributors) {
			if(person != null) {
				String name = person.getName();
				if(name != null) {
					names.add(name);
				}
				String email = person.getEmail();
				if(email != null) {
					names.add(email);
					names.add(atomize(email));
				}
			}
		}
		return unsplit(names);
	}

	String authors(CETBean bean) {
		if(bean instanceof DatasetBean) {
			return authors((DatasetBean)bean);
		} else {
			log.warn("unexpected bean class "+bean.getClass());
			return authors(bean.getUri());
		}
	}
	
	// aaagh, unsafe casts
	String authors(String uri) {
		try {
			Object bean = fetchBean(uri);
			if(bean instanceof CETBean) {
				return authors((CETBean)bean);
			} else {
				log.error(uri+" is not a bean, no authors extracted");
			}
		} catch(Exception x) {
		}
		return "";
	}
	
	String tags(CETBean bean) throws OperatorException, ClassNotFoundException {
		return tags(bean.getUri());
	}
	
	String tags(String uri) throws OperatorException, ClassNotFoundException {
		TagEventBeanUtil tebu = new TagEventBeanUtil(getBeanSession());
		TreeSet<String> tags = new TreeSet<String>();
		try {
			tags.addAll(tebu.getTags(uri));
			return unsplit(tags);
		} catch (OperatorException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	String annotations(CETBean bean) throws OperatorException, ClassNotFoundException {
		return annotations(bean.getUri());
	}
	
	String annotations(String uri) throws OperatorException, ClassNotFoundException {
		AnnotationBeanUtil abu = new AnnotationBeanUtil(getBeanSession());
		List<String> annotations = new LinkedList<String>();
		try {
			for(AnnotationBean annotation : abu.getAssociationsFor(uri)) {
				annotations.add(annotation.getDescription());
			}
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		return unsplit(annotations);
	}

	String collections(CETBean bean) {
		return collections(bean.getUri());
	}
	
	String collections(String uri) {
		GetCollectionsResult r = GetCollectionsHandler.getCollections(new GetCollections(uri));
		List<String> names = new LinkedList<String>();
		for(CollectionBean c : r.getCollections()) {
			names.add(c.getTitle());
		}
		return unsplit(names);
	}
	
	// split a string into words on non-whitespace boundaries
	static String atomize(String title) {
		String e = title.replaceAll("([a-z])([A-Z])","$1 $2");
		e = e.replaceAll("([-_\\[\\]\\.])"," $1 ");
		e = e.replaceAll("([0-9]+)"," $1 ");
		e = e.replaceAll("  +"," ");
		return title+" "+e;
	}
	
	/*
	public static void main(String args[]) {
		String examples[] = new String[] {
				"zephyrObservationCar.jpg",
				"3934741243_ab09b6a208_o.jpg [Crop Tool.OutputImage]",
				"114_0145.MOV",
				"source-mosaic-14400x6150.jpg",
				"london_map1807_large.jpg",
				"france_louis_xi.jpg"
		};
		for(String title : examples) {
			System.out.println(expandTitle(title));
		}
	}
	*/
	
	String title(CETBean bean) {
		return atomize(bean.getLabel());
	}
	
	String title(String uri) throws OperatorException, ClassNotFoundException {
		Thing thing = getBeanSession().getThingSession().fetchThing(Resource.uriRef(uri));
		String dcTitle = thing.getString(Dc.TITLE);
		String rdfsLabel = thing.getString(Rdfs.LABEL);
		return atomize((dcTitle != null ? dcTitle : "") + (rdfsLabel != null ? " " + rdfsLabel : ""));
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
