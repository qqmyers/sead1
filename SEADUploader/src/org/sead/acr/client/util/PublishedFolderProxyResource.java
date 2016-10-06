/*******************************************************************************
 * Copyright 2016 University of Michigan
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
package org.sead.acr.client.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.json.JSONObject;

/**
 * This class represents a file (2.0 Dataset) that includes the
 * metadata/tags/comments associated with a directory (2.0 folder). When
 * importing a SEAD publication, metadata for folders will be recorded in a file
 * added to that folder.
 * 
 * @author Jim
 *
 */

public class PublishedFolderProxyResource implements Resource {

	private PublishedResource resource;
	private String message;

	public PublishedFolderProxyResource(PublishedResource pr, String folderId) {
		resource = pr;

		StringBuilder sb = new StringBuilder();
		sb.append("SEAD Importer README:\n\n");
		sb.append("The metadata, tags, and comments on this file are intended to apply to the SEAD 2.0 Folder it was created in:\n");
		sb.append("\tID: " + folderId );
		sb.append("\n\tPath: " + resource.getPath());
		sb.append("\n\nThis file manages the difference between the SEAD 1.x and current SEAD 2.0 data models, ensuring that information about published SEAD 1.x sub-collections is available when they are imported into SEAD 2.0.");
		sb.append("\n\nNote that generated metadata, such as the file type, size, and cryptographic hash values apply to the file (containing this message) and not to the folder.");
		message = sb.toString();

	}

	@Override
	public String getName() {
		return "SEADImport.ReadMe.txt";
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public String getPath() {
		return resource.getPath() + "/" + getName();
	}

	@Override
	public Iterator<Resource> iterator() {
		return listResources().iterator();
	}

	@Override
	public Iterable<Resource> listResources() {
		ArrayList<Resource> resources = new ArrayList<Resource>();
		return resources;
	}

	@Override
	public long length() {
		long size = message.getBytes(StandardCharsets.UTF_8).length;
		return size;
	}

	@Override
	public String getAbsolutePath() {
		return resource.getAbsolutePath() + "/" + getName();
	}

	@Override
	public ContentBody getContentBody() {
		InputStream stream = new ByteArrayInputStream(
				message.getBytes(StandardCharsets.UTF_8));
		return new InputStreamBody(stream, ContentType.create("text/plain"),
				getName());
	}

	private String hash = null;

	@Override
	public String getSHA1Hash() {
		if (hash == null) {
			hash = DigestUtils.sha1Hex(message);
		}
		return hash;
	}

	@Override
	public JSONObject getMetadata() {
		return (resource.getMetadata());
	}
}
