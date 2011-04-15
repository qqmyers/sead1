package edu.illinois.ncsa.mmdb.web.client.dnd;

public enum UploadState {
	PENDING("Pending"), IN_PROGRESS("In progress"), FAILED("Failed"), COMPLETE(
			"Complete");

	String label;

	private UploadState(String label) {
		this.label = label;
	}

	public String toString() {
		return label;
	}
}
