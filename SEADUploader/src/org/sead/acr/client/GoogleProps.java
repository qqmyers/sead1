/*******************************************************************************
 * Copyright 2014 University of Michigan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.sead.acr.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "installed")
@JsonIgnoreProperties({ "client_email", "redirect_uris", "client_x509_cert_url", "auth_provider_x509_cert_url" })
public class GoogleProps {
	  public String auth_uri;
	  public String client_secret;
	  public String token_uri;
	  public String client_id;
	
	
}
