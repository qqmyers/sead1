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
package edu.illinois.ncsa.mmdb.web.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.illinois.ncsa.mmdb.web.server.dispatch.AddCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddGeoLocationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddToCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AdminAddUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AnnotateResourceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ChangeUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ChangeUserPIDHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.CheckUserExistsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ClearGeoLocationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ContextConvertHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.Create3DImageHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.CreateRoleHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DefaultRoleHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DeleteAnnotationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DeleteDatasetHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DeleteDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DeleteRelationshipHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DeleteRoleHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.EditRoleHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.EditUserRetirementHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ExtractionServiceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GeoSearchHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetAccessLevelHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetAllTagsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetAllowedValuesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetAnnotationsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetCollectionsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetConfigurationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetsByTagHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetsInCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDerivedFromHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDownloadCountHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetGeoNamesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetGeoPointHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetItemsBySetHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetLicenseHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetLikeDislikeHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetMimeTypeCategoriesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetOauth2ServerFlowStateHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetPermissionsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetPreviewsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetRecentActivityHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetRelationshipHandlerNew;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetRolesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetSearchHitHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetSectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetServiceTokenHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetSubclassesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetTagsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUploadDestinationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUserActionsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUserMetadataFieldsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUserPIDHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUsersHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetViewCountHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GoogleOauth2PropsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GoogleUserInfoHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.HasPermissionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.InitializeRolesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.IsPreviewPendingHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.JiraIssueHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListQueryHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListRelationshipTypesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListUserMetadataFieldsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.MintHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.Oauth2ServerFlowTokenRequestHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.Oauth2ServerFlowUserInfoHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ReindexLuceneHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RemoveFromCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RemoveMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RemoveUserMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RequestNewPasswordHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RunSparqlQueryHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SearchHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SearchWithFilterHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetAccessLevelHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetConfigurationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetInfoHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetLicenseHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetPermissionsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetRelationshipHandlerNew;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetRoleAccessLevelHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetTitleHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetUserMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SystemInfoHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.TagResourceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.UnpackZipHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.UpdateMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.UserGroupMembershipHandler;

/**
 * Setup registry of action handlers when the servlet context is initialized.
 *
 * @author Luigi Marini
 *
 */
public class MyActionHandlersConfig implements ServletContextListener {
    public void contextInitialized(ServletContextEvent evt) {
        DispatchUtil.registerHandler(new GetDatasetHandler());
        DispatchUtil.registerHandler(new AnnotateResourceHandler());
        DispatchUtil.registerHandler(new TagResourceHandler());
        DispatchUtil.registerHandler(new GetTagsHandler());
        DispatchUtil.registerHandler(new GetAnnotationsHandler());
        DispatchUtil.registerHandler(new GetDatasetsByTagHandler());
        DispatchUtil.registerHandler(new GetMetadataHandler());
        DispatchUtil.registerHandler(new AddMetadataHandler());
        DispatchUtil.registerHandler(new UpdateMetadataHandler());
        DispatchUtil.registerHandler(new RemoveMetadataHandler());
        DispatchUtil.registerHandler(new GetGeoPointHandler());
        DispatchUtil.registerHandler(new GetGeoNamesHandler());
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
        DispatchUtil.registerHandler(new EditRoleHandler());
        DispatchUtil.registerHandler(new GetRolesHandler());
        DispatchUtil.registerHandler(new GetUserHandler());
        DispatchUtil.registerHandler(new RequestNewPasswordHandler());
        DispatchUtil.registerHandler(new ChangeUserHandler());
        DispatchUtil.registerHandler(new GetDerivedFromHandler());
        DispatchUtil.registerHandler(new RunSparqlQueryHandler());
        DispatchUtil.registerHandler(new GetUserMetadataFieldsHandler());
        DispatchUtil.registerHandler(new ExtractionServiceHandler());
        DispatchUtil.registerHandler(new DeleteAnnotationHandler());
        DispatchUtil.registerHandler(new SearchHandler());
        DispatchUtil.registerHandler(new RemoveFromCollectionHandler());
        DispatchUtil.registerHandler(new GetAllTagsHandler());
        DispatchUtil.registerHandler(new GetRecentActivityHandler());
        DispatchUtil.registerHandler(new ReindexLuceneHandler());
        DispatchUtil.registerHandler(new IsPreviewPendingHandler());
        DispatchUtil.registerHandler(new ContextConvertHandler());
        DispatchUtil.registerHandler(new GetLicenseHandler());
        DispatchUtil.registerHandler(new SetLicenseHandler());
        DispatchUtil.registerHandler(new GetViewCountHandler());
        DispatchUtil.registerHandler(new GetLikeDislikeHandler());
        DispatchUtil.registerHandler(new ListQueryHandler());
        DispatchUtil.registerHandler(new JiraIssueHandler());
        DispatchUtil.registerHandler(new ListUserMetadataFieldsHandler());
        DispatchUtil.registerHandler(new DeleteDatasetsHandler());
        DispatchUtil.registerHandler(new SystemInfoHandler());
        DispatchUtil.registerHandler(new GetPermissionsHandler());
        DispatchUtil.registerHandler(new SetPermissionsHandler());
        DispatchUtil.registerHandler(new InitializeRolesHandler());
        DispatchUtil.registerHandler(new SetRelationshipHandlerNew());
        DispatchUtil.registerHandler(new GetRelationshipHandlerNew());
        DispatchUtil.registerHandler(new GetDownloadCountHandler());
        DispatchUtil.registerHandler(new SetTitleHandler());
        DispatchUtil.registerHandler(new SetInfoHandler());
        DispatchUtil.registerHandler(new SetUserMetadataHandler());
        DispatchUtil.registerHandler(new RemoveUserMetadataHandler());
        DispatchUtil.registerHandler(new CreateRoleHandler());
        DispatchUtil.registerHandler(new DeleteRoleHandler());
        DispatchUtil.registerHandler(new GetMimeTypeCategoriesHandler());
        DispatchUtil.registerHandler(new ListRelationshipTypesHandler());
        DispatchUtil.registerHandler(new DeleteRelationshipHandler());
        DispatchUtil.registerHandler(new MintHandler());
        DispatchUtil.registerHandler(new GetSubclassesHandler());
        DispatchUtil.registerHandler(new GetAllowedValuesHandler());
        DispatchUtil.registerHandler(new GeoSearchHandler());
        DispatchUtil.registerHandler(new GetUserActionsHandler());
        DispatchUtil.registerHandler(new AddGeoLocationHandler());
        DispatchUtil.registerHandler(new GetSearchHitHandler());
        DispatchUtil.registerHandler(new GetSectionHandler());
        DispatchUtil.registerHandler(new Create3DImageHandler());
        DispatchUtil.registerHandler(new GetConfigurationHandler());
        DispatchUtil.registerHandler(new SetConfigurationHandler());
        DispatchUtil.registerHandler(new SearchWithFilterHandler());
        DispatchUtil.registerHandler(new GetItemsBySetHandler());
        DispatchUtil.registerHandler(new GetDatasetsInCollectionHandler());
        DispatchUtil.registerHandler(new DefaultRoleHandler());
        DispatchUtil.registerHandler(new GetAccessLevelHandler());
        DispatchUtil.registerHandler(new SetAccessLevelHandler());
        DispatchUtil.registerHandler(new SetRoleAccessLevelHandler());
        DispatchUtil.registerHandler(new GoogleUserInfoHandler());
        DispatchUtil.registerHandler(new CheckUserExistsHandler());
        DispatchUtil.registerHandler(new GoogleOauth2PropsHandler());
        DispatchUtil.registerHandler(new GetUserPIDHandler());
        DispatchUtil.registerHandler(new ChangeUserPIDHandler());
        DispatchUtil.registerHandler(new AdminAddUserHandler());
        DispatchUtil.registerHandler(new ClearGeoLocationHandler());
        DispatchUtil.registerHandler(new GetServiceTokenHandler());
        DispatchUtil.registerHandler(new UnpackZipHandler());
        DispatchUtil.registerHandler(new GetOauth2ServerFlowStateHandler());
        DispatchUtil.registerHandler(new Oauth2ServerFlowTokenRequestHandler());
        DispatchUtil.registerHandler(new Oauth2ServerFlowUserInfoHandler());
        DispatchUtil.registerHandler(new EditUserRetirementHandler());
    }

    public void contextDestroyed(ServletContextEvent evt) {
    }
}
