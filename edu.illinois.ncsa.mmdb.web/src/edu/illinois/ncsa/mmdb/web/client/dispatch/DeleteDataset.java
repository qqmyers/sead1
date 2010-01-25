package edu.illinois.ncsa.mmdb.web.client.dispatch;


@SuppressWarnings("serial")
public class DeleteDataset extends SubjectAction<DeleteDatasetResult> {
	public DeleteDataset() {}
	
	public DeleteDataset(String uri) {
		super(uri);
	}
}
