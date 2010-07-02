package com.schneertz.medici;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;

public class PhotoPost extends AbstractPost {
	PartSource photoSource;
	String url;

	public PartSource getSource() {
		return photoSource;
	}

	public void setSource(PartSource photoSource) {
		this.photoSource = photoSource;
	}

	public void setFile(File f) throws FileNotFoundException {
		setSource(new FilePartSource(f));
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUrl(URL url) {
		this.url = url.toString();
	}

	public String getUrl() {
		return url;
	}

	public int post() throws HttpException, IOException {
		return postImpl(null);
	}

	public void asyncPost(final ProgressHandler progressHandler) {
		(new Thread() {
			public void run() {
				try {
					postImpl(progressHandler);
					progressHandler.onSuccess();
				} catch (Exception x) {
					progressHandler.onFailure(x);
				}
			}
		}).start();
	}

	int postImpl(final ProgressHandler progressHandler) throws HttpException,
			IOException {
		HttpClient client = new HttpClient();
		// FIXME get medici URI, username, and password from preferences
		PostMethod post = new PostMethod(getMedici()+"/UploadBlob");
		URL mediciUrl = new URL(getMedici());
		// if it's an invalid URL postImpl will throw MalformedURLException and fail
		client.getParams().setAuthenticationPreemptive(true);
		Credentials upc = new UsernamePasswordCredentials(getCreds().getEmail(), getCreds().getPassword());
		String host = mediciUrl.getHost();
		int port = mediciUrl.getPort();
		client.getState().setCredentials(new AuthScope(host,port,AuthScope.ANY_REALM), upc);
		Part photoPart = null;
		if (getUrl() != null) {
			photoPart = new StringPart("source", getUrl());
		} else {
			FilePart fpart = new FilePart("data", getSource()) {
				@Override
				protected void sendData(OutputStream out) throws IOException {
					PartSource source = getSource();
					InputStream in = source.createInputStream();
					long length = source.getLength();
					byte[] buf = new byte[8192];
					int i = 0;
					long bytesCopied = 0L;
					while (true) {
						i = in.read(buf);
						if (i == -1) {
							break;
						}
						out.write(buf, 0, i);
						bytesCopied += i;
						if (progressHandler != null) {
							int k = (int) ((double) bytesCopied * 100.0 / (double) length);
							progressHandler.onProgress(k);
						}
					} // end while
					out.flush();
				}
			};
			fpart.setContentType(getContentType());
			photoPart = fpart;
		}
		Part[] parts = { photoPart };
		post.setRequestEntity(new MultipartRequestEntity(parts, post
				.getParams()));
		return client.executeMethod(post);
	}
}
