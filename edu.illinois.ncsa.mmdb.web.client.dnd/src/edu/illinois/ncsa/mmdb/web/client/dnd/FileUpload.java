package edu.illinois.ncsa.mmdb.web.client.dnd;

import java.io.File;
import java.io.StringWriter;

/**
 * Represents information about a file that is being uploaded
 * 
 * @author futrelle
 */
public class FileUpload implements Comparable<FileUpload> {
	/** Local file */
	File file;
	/** % complete */
	int progress;
	/** state of upload */
	UploadState state = UploadState.PENDING;
	/** URI of dataset on server */
	String uri;
	/** any associated collection */
	Collection collection;
	/** Ordinal 0-based index of file in total uploaded on this page */
	private int index;
	/** session key associated with this file */
	String sessionKey;

	public FileUpload(File file) {
		setFile(file);
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public long getLength() {
		return getFile().length();
	}

	public String getName() {
		return getFile().getName();
	}

	public UploadState getState() {
		return state;
	}

	public void setState(UploadState state) {
		this.state = state;
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

	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public boolean hasCollection() {
		return collection != null;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		sw.append("{");
		sw.append(String.format("%d [%s]: \"%s\"", getIndex(), getState(),
				getName()));
		if (getUri() != null) {
			sw.append(String.format(" (%s)", getUri()));
		}
		if (getCollection() != null) {
			sw.append(String.format(" in collection \"%s\"", getCollection()
					.getName()));
			if (getCollection().getUri() != null) {
				sw.append(String.format(" (%s)", getCollection().getUri()));
			}
		}
		sw.append("}");
		return sw.toString();
	}

	// equals. is this necessary?

	private String getIdentityToken() {
		return file.getAbsolutePath();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof FileUpload) {
			return equals((FileUpload) other);
		} else {
			return false;
		}
	}

	public boolean equals(FileUpload other) {
		return getIdentityToken().equals(other.getIdentityToken());
	}

	@Override
	public int hashCode() {
		return getIdentityToken().hashCode();
	}

	public int compareTo(FileUpload arg0) {
		return getIdentityToken().compareTo(arg0.getIdentityToken());
	}
}
