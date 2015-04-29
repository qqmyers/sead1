/**
 *
 */
package edu.illinois.ncsa.mmdb.web.server.resteasy;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.tupeloproject.kernel.Operator;
import org.tupeloproject.kernel.TableProvider;
import org.tupeloproject.kernel.TripleSetProvider;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.query.sparql.SparqlOperatorFactory;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 *
 */
@Path("/sparql")
@NoCache
public class SparqlRestService {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(SparqlRestService.class);

    @POST
    @Path("")
    @Produces("text/csv")
    public Response executeSparqlCSV(@FormParam("query") String query) {
        try {
            StringBuilder sb = new StringBuilder();

            Operator o = SparqlOperatorFactory.newOperator(query);
            TupeloStore.getInstance().getContext().perform(o);
            Iterable<? extends Tuple<Resource>> rows = null;
            String[] columns = null;
            if (o instanceof TableProvider) {
                TableProvider tp = (TableProvider) o;

                // head
                columns = tp.getResult().getColumnNames().toArray(new String[0]);

                // results
                rows = tp.getResult();
            } else if (o instanceof TripleSetProvider) {
                TripleSetProvider tp = (TripleSetProvider) o;

                // head
                columns = new String[] { "subject", "predicate", "object" };

                // results
                rows = tp.getResult();
            }

            if (rows != null) {
                for (int i = 0; i < columns.length; i++ ) {
                    if (i != 0) {
                        sb.append("\t");
                    }
                    sb.append(columns[i]);
                }
                sb.append("\n");

                for (Tuple<Resource> row : rows ) {
                    for (int i = 0; i < row.size(); i++ ) {
                        if (i != 0) {
                            sb.append("\t");
                        }
                        if (row.get(i) == null) {
                            continue;
                        }
                        sb.append(row.get(i).toNTriples());
                    }
                    sb.append("\n");
                }
            }
            return Response.status(200).entity(sb.toString()).build();
        } catch (Exception e) {
            log.error("Error running sparql query [" + query + "]", e);
            return Response.status(500).entity("Error running sparql query [" + e.getMessage() + "]").build();
        }
    }

    @POST
    @Path("")
    @Produces("text/xml")
    public Response executeSparqlXML(@FormParam("query") String query) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"?>").append("\n");
            sb.append("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">").append("\n");

            Operator o = SparqlOperatorFactory.newOperator(query);
            TupeloStore.getInstance().getContext().perform(o);
            Iterable<? extends Tuple<Resource>> rows = null;
            String[] columns = null;
            if (o instanceof TableProvider) {
                TableProvider tp = (TableProvider) o;

                // head
                columns = tp.getResult().getColumnNames().toArray(new String[0]);

                // results
                rows = tp.getResult();
            } else if (o instanceof TripleSetProvider) {
                TripleSetProvider tp = (TripleSetProvider) o;

                // head
                columns = new String[] { "subject", "predicate", "object" };

                // results
                rows = tp.getResult();
            }

            if (rows != null) {
                sb.append("  <head>").append("\n");
                for (String h : columns ) {
                    sb.append("    <variable name=\"").append(h).append("\"/>").append("\n");
                }
                sb.append("  </head>").append("\n");

                sb.append("  <results>").append("\n");
                for (Tuple<Resource> row : rows ) {
                    sb.append("    <result>").append("\n");
                    for (int i = 0; i < row.size(); i++ ) {
                        if (row.get(i) == null) {
                            continue;
                        }
                        sb.append("      <binding name=\"").append(columns[i]).append("\">").append("\n");
                        String enc = row.get(i).getString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                        if (row.get(i).isUri()) {
                            sb.append("        <uri>").append(row.get(i).getString()).append("</uri>").append("\n");
                        } else if (row.get(i).isLiteral()) {
                            sb.append("        <literal");
                            if (row.get(i).isTypedLiteral()) {
                                sb.append(" datatype=\"").append(row.get(i).getDatatype().getString()).append("\"");
                            }
                            if (row.get(i).hasLanguageTag()) {
                                sb.append(" datatype=\"").append(row.get(i).getLanguageTag()).append("\"");
                            }
                            sb.append(">").append(enc).append("</literal>").append("\n");
                        } else if (row.get(i).isBlank()) {
                            sb.append("        <bnode>").append(row.get(i).getString()).append("</bnode>").append("\n");
                        } else {
                            sb.append("        <literal>").append(enc).append("</literal>").append("\n");
                        }
                        sb.append("      </binding>").append("\n");
                    }
                    sb.append("    </result>").append("\n");
                }
                sb.append("  </results>").append("\n");
            }

            sb.append("</sparql>").append("\n");
            return Response.status(200).entity(sb.toString()).build();
        } catch (Exception e) {
            log.error("Error running sparql query [" + query + "]", e);
            return Response.status(500).entity("Error running sparql query [" + e.getMessage() + "]").build();
        }
    }
}
