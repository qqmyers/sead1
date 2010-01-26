package edu.illinois.ncsa.mmdb.web.client.dispatch;

@SuppressWarnings("serial")
public class GetDerivedFrom extends SubjectAction<GetDerivedFromResult> {
	public GetDerivedFrom() { }
	public GetDerivedFrom(String uri) {
		super(uri);
	}
}
