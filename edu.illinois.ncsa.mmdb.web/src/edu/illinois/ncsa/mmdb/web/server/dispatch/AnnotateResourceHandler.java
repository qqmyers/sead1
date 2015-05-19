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

import java.util.HashSet;

import javax.mail.MessagingException;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResourceResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Annotate any resource in repository. Given a sessionId, retrieve the
 * PersonBean and add set it as the creator of the annotation. Store the
 * annotation in the context.
 *
 * @author Luigi Marini
 *
 */
public class AnnotateResourceHandler implements
        ActionHandler<AnnotateResource, AnnotateResourceResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(AnnotateResourceHandler.class);

    @Override
    public AnnotateResourceResult execute(AnnotateResource arg0,
            ExecutionContext arg1) throws ActionException {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        AnnotationBeanUtil abu = new AnnotationBeanUtil(beanSession);

        AnnotationBean annotation = arg0.getAnnotation();

        String resource = arg0.getUri();

        String sessionId = arg0.getSessionId();

        String personID = sessionId;

        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
                .getBeanSession());

        // FIXME only required until sessionid stores the full uri and not the
        // email address
        if (!sessionId.startsWith(PersonBeanUtil.getPersonID(""))) {
            personID = PersonBeanUtil.getPersonID(sessionId);
        }

        try {
            annotation.setCreator(pbu.get(personID));
        } catch (Exception e1) {
            log.error("Error getting creator of annotation", e1);
        }

        if (annotation.getCreator() == null) {
            annotation.setCreator(PersonBeanUtil.getAnonymous());
        }

        // store annotation
        try {
            beanSession.register(annotation);
            beanSession.save(annotation);
            abu.addAssociationTo(resource, annotation);
            log.debug("Annotated " + resource);
            TupeloStore.getInstance().changed(resource);
        } catch (OperatorException e2) {
            log.error("Error saving and associating an annotation bean", e2);
        } catch (Exception e) {
            log.error("Error saving and associating an annotation bean", e);
        }
        //Send a message to any/all mentioned in the comment, cc'ing the comment author

        try {
            String[] mailRecipients = getMailRecipients(annotation.getDescription());
            String type = "Collection";
            String label = "No name";
            Unifier uf = new Unifier();
            uf.addPattern(Resource.uriRef(resource), Dc.TITLE, "label");
            uf.addPattern(Resource.uriRef(resource), Rdf.TYPE, Cet.DATASET, true);
            uf.setColumnNames("label", "type");
            TupeloStore.getInstance().getContext().perform(uf);
            for (Tuple<Resource> t : uf.getResult() ) {
                label = t.get(0).toString();
                if (t.get(1) != null) {
                    type = "Dataset";
                }
            }
            StringBuffer message = new StringBuffer();

            StringBuffer link = new StringBuffer(" <a href=\"http://" + TupeloStore.getInstance().getConfiguration(ConfigurationKey.MediciName) + "/acr#");
            if (type.equals("Dataset")) {
                link.append("dataset?id=" + resource);
            } else {
                link.append("collection?uri=" + resource);
            }
            link.append("\">" + label + "</a>");
            message.append(annotation.getCreator().getName());
            message.append(" has mentioned you in a comment they made on the ");
            message.append(link);
            message.append(" " + type + ": \n\n");
            message.append(annotation.getDescription());
            message.append("\n\nTo respond, go to the " + link.toString() + " page");

            String[] cc = new String[1];
            cc[0] = annotation.getCreator().getEmail();

            Mail.sendMessage(mailRecipients, cc, "You've been mentioned!", message.toString());

        } catch (MessagingException e) {
            log.error("Unable to send mail notification for: " + resource, e);
        } catch (OperatorException e) {
            log.error("Unable to send mail notification for: " + resource, e);
        }

        return new AnnotateResourceResult();
    }

    /* Find the email addresses for everyone mentioned in an "@<name>" part of the comment text.
     * Note: Currently assumes there are no name collisions/names that are substrings of others
     * (which would be usual/good practice but is not enforced).
     */
    private String[] getMailRecipients(String description) throws ActionException {
        HashSet<String> recipients = new HashSet<String>();
        GetUsersResult usersResult = new GetUsersHandler().execute(null, null);
        int curIndex = 0;
        int nextIndex = description.indexOf("@", curIndex);
        while (nextIndex >= 0) {
            for (GetUsersResult.User u : usersResult.getUsers() ) {
                if ((u.name != null) && (u.name.length() > 0) && (u.email != null) && (u.email.length() > 0)) {
                    if (description.substring(nextIndex + 1).startsWith(u.name)) {
                        recipients.add(u.email);
                    }
                }
            }
            nextIndex = description.indexOf("@", curIndex);
        }
        return recipients.toArray(new String[0]);
    }

    @Override
    public Class<AnnotateResource> getActionType() {
        return AnnotateResource.class;
    }

    @Override
    public void rollback(AnnotateResource arg0, AnnotateResourceResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
