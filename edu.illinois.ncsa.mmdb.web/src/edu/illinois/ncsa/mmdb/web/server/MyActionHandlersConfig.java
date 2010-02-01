/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.illinois.ncsa.mmdb.web.server.dispatch.AddCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddToCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AnnotateResourceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AuthenticateHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DeleteDatasetHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.EditPermissionsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetAnnotationsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetCollectionsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetsByTagHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDerivedFromHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetGeoPointHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetPreviewsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetTagsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUploadDestinationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUsersHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.HasPermissionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.NewPasswordHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RequestNewPasswordHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RunSparqlQueryHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.TagResourceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.UserGroupMembershipHandler;

/**
 * Setup registry of action handlers when the servlet context is initialized.
 * 
 * @author Luigi Marini
 * 
 */
public class MyActionHandlersConfig implements ServletContextListener {
	public void contextInitialized(ServletContextEvent evt) {
		DispatchUtil.registerHandler(new GetDatasetsHandler());
		DispatchUtil.registerHandler(new GetDatasetHandler());
		DispatchUtil.registerHandler(new AnnotateResourceHandler());
		DispatchUtil.registerHandler(new TagResourceHandler());
		DispatchUtil.registerHandler(new GetTagsHandler());
		DispatchUtil.registerHandler(new GetAnnotationsHandler());
		DispatchUtil.registerHandler(new GetDatasetsByTagHandler());
		DispatchUtil.registerHandler(new ListDatasetsHandler());
		DispatchUtil.registerHandler(new AuthenticateHandler());
        DispatchUtil.registerHandler(new GetMetadataHandler());
        DispatchUtil.registerHandler(new GetGeoPointHandler());
		DispatchUtil.registerHandler(new GetCollectionsHandler());
		DispatchUtil.registerHandler(new AddCollectionHandler());
		DispatchUtil.registerHandler(new GetCollectionHandler());
		DispatchUtil.registerHandler(new AddToCollectionHandler());
		DispatchUtil.registerHandler(new GetPreviewsHandler());
		DispatchUtil.registerHandler(new GetUploadDestinationHandler());
		DispatchUtil.registerHandler(new DeleteDatasetHandler());
		DispatchUtil.registerHandler(new GetUsersHandler());
		DispatchUtil.registerHandler(new UserGroupMembershipHandler());
		DispatchUtil.registerHandler(new AddUserHandler());
		DispatchUtil.registerHandler(new HasPermissionHandler());
		DispatchUtil.registerHandler(new EditPermissionsHandler());
		DispatchUtil.registerHandler(new GetUserHandler());
		DispatchUtil.registerHandler(new RequestNewPasswordHandler());
		DispatchUtil.registerHandler(new NewPasswordHandler());
		DispatchUtil.registerHandler(new GetDerivedFromHandler());
		DispatchUtil.registerHandler(new RunSparqlQueryHandler());
	}

	public void contextDestroyed(ServletContextEvent evt) {
	}
}
