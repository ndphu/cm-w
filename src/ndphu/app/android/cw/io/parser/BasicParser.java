package ndphu.app.android.cw.io.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class BasicParser {
	private static final String TAG = BasicParser.class.getSimpleName();

	public static interface LineHandler {
		/**
		 * @param line
		 * @return true when gathering enough information
		 */
		boolean processLine(String line);
	}

	public static InputStream getFromUrl(String url) throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		// String fullCookie = getCookieString();
		// Log.i(TAG, "Cookie: " + fullCookie);
		// get.setHeader("Cookie", fullCookie);
		HttpResponse response = client.execute(get);
		InputStream content = response.getEntity().getContent();
		for (Header header : response.getHeaders("Content-Encoding")) {
			if ("gzip".equals(header.getValue())) {
				return new GZIPInputStream(content);
			}
		}
		return content;
	}

	public static void processLineByLine(String url, LineHandler handler) throws IOException {
		InputStream is = getFromUrl(url);
		processLineByLine(is, handler);
	}

	public static void processLineByLine(InputStream is, LineHandler handler) throws IOException {
		if (handler == null) {
			throw new RuntimeException("Line handler is null");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				boolean end = handler.processLine(line);
				if (end) {
					break;
				}
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

}
