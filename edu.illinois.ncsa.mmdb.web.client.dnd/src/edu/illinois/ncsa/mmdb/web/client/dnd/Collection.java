package edu.illinois.ncsa.mmdb.web.client.dnd;

import java.io.File;

public class Collection {
	/** local folder */
	File folder;
	/** name */
	String name;
	/** uri */
	String uri;

	public Collection(File folder) {
		setFolder(folder);
		setName(folder.getName());
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean hasUri() {
		return uri != null;
	}
}
