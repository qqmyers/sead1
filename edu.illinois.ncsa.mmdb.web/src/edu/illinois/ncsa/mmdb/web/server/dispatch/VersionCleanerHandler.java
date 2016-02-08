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

/*
 *  NB: For these services to work, -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true must be set on the server. This
 * potentially opens the door for attacks related to CVE-2007-0450 if any code on the server follows paths sent
 * in URLs.
 */

package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.util.Tuple;

import com.hp.hpl.jena.vocabulary.DCTerms;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.VersionCleaner;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class VersionCleanerHandler implements ActionHandler<VersionCleaner, EmptyResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(VersionCleanerHandler.class);

    @Override
    public EmptyResult execute(final VersionCleaner action, ExecutionContext context) throws ActionException {

        log.debug("Cleaning up old test versions");

        //Get List of Versions
        TripleMatcher tMatcher = new TripleMatcher();
        tMatcher.match(null, DcTerms.HAS_VERSION, null);
        try {
            TupeloStore.getInstance().getContext().perform(tMatcher);

            Set<Resource> publishedItems = tMatcher.subjects();
            String testPIDMatches = TupeloStore.getInstance().getConfiguration(ConfigurationKey.DefaultTestPIDShoulders);
            String[] pidMatches = testPIDMatches.split(",");
            for (String s : pidMatches ) {
                log.debug("Will check PIDs that include the string \"" + s + "\"");
            }

            for (Resource subject : publishedItems ) {
                Unifier uf = new Unifier();
                uf.addPattern(subject, DcTerms.HAS_VERSION, "version");
                uf.addPattern("version", RequestPublicationHandler.hasVersionNum, "num");
                UriRef issued = Resource.uriRef(DCTerms.issued.getURI());
                UriRef identifier = Resource.uriRef(DCTerms.identifier.getURI());
                uf.addPattern("version", issued, "date");
                uf.addPattern("version", identifier, "pid");

                uf.setColumnNames("version", "num", "date", "pid");
                try {
                    TupeloStore.getInstance().getContext().perform(uf);
                } catch (OperatorException e) {
                    log.error("Unable to get versions for " + subject.toString(), e);
                }
                Set<Tuple<Resource>> goodVersionSet = new HashSet<Tuple<Resource>>();
                SortedSet<Integer> deletedVersionNums = new TreeSet<Integer>();
                log.debug("Checking versions of " + subject.toString());
                for (Tuple<Resource> t : uf.getResult() ) {

                    boolean bad = false;
                    //If pid, and its temporary, check date and delete if older than 2 weeks
                    //Could delete items without pid, but these can still be deleted via the collection page directly
                    boolean isTest = false;
                    for (String s : pidMatches ) {
                        if (t.get(3).toString().indexOf(s) != -1) {

                            isTest = true;
                            break;
                        }
                    }
                    if (isTest) {

                        Date date;
                        try {
                            date = DateFormat.getDateTimeInstance().parse(t.get(2).toString());

                            Date cutoffDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 14l); //2 weeks
                            if (date.before(cutoffDate)) {
                                bad = true;
                                log.debug("Removing version " + t.get(1).toString() + ": " + t.get(0).toString());
                                log.debug("         Created: " + t.get(2).toString());
                                log.debug("      Identifier: " + t.get(3).toString());

                                TripleWriter tw = new TripleWriter();
                                tw.remove(subject, DcTerms.HAS_VERSION, t.get(0));
                                tw.remove(t.get(0), RequestPublicationHandler.hasVersionNum, t.get(1));
                                tw.remove(t.get(0), issued, t.get(2));
                                tw.remove(t.get(0), identifier, t.get(3));
                                TupeloStore.getInstance().getContext().perform(tw);
                            }
                        } catch (ParseException e) {
                            log.warn("Unable to parse issue date for version " + t.get(0).toString() + " - not removing", e);
                        } catch (OperatorException e) {
                            log.error("Unable to remove version " + t.get(0).toString(), e);
                        }

                    }
                    if (!bad) {
                        //Keep track of good versions so they can be renumbered
                        goodVersionSet.add(t);
                    } else {
                        deletedVersionNums.add(Integer.parseInt(t.get(1).toString()));
                    }
                }
                TripleWriter tw = new TripleWriter();
                if (!deletedVersionNums.isEmpty()) {
                    for (Tuple<Resource> t : goodVersionSet ) {
                        int vNum = Integer.parseInt(t.get(1).toString());
                        vNum = vNum - deletedVersionNums.headSet(vNum).size();
                        log.debug("Version " + t.get(1).toString() + " being set to version " + vNum);
                        tw.remove(t.get(0), RequestPublicationHandler.hasVersionNum, t.get(1));
                        tw.add(t.get(0), RequestPublicationHandler.hasVersionNum, Resource.literal(Integer.toString(vNum)));
                    }
                    try {
                        Unifier uf2 = new Unifier();
                        uf2.addPattern(subject, DcTerms.HAS_VERSION, "version");
                        uf2.addPattern("version", RequestPublicationHandler.hasVersionNum, "num");
                        uf2.addPattern("version", RequestPublicationHandler.hasSalt, "salt");
                        uf2.setColumnNames("version", "num");
                        TupeloStore.getInstance().getContext().perform(uf2);
                        for (Tuple<Resource> active : uf2.getResult() ) {
                            tw.remove(active.get(0), RequestPublicationHandler.hasVersionNum, active.get(1));
                            tw.add(active.get(0), RequestPublicationHandler.hasVersionNum,
                                    Resource.literal(Integer.toString(Integer.parseInt(active.get(1).toString()) - deletedVersionNums.size())));
                        }
                    } catch (OperatorException o) {
                        log.warn("Could not update version number for active request", o);
                    }
                    try {
                        TupeloStore.getInstance().getContext().perform(tw);
                    } catch (OperatorException e) {
                        log.error("Unable to update numbers of good versions for " + subject.toString(), e);
                    }
                }
            }
        } catch (OperatorException e1) {
            log.error("Unable to retrieve versions", e1);
        }
        return new EmptyResult();
    }

    @Override
    public void rollback(VersionCleaner action, EmptyResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

    @Override
    public Class<VersionCleaner> getActionType() {
        return VersionCleaner.class;
    }

}
