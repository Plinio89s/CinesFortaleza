package br.ufc.lesc.plinio.cinesfortaleza;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public abstract class Cine {

	private final long MAX_PAGE_SIZE = 500000;
	private final int TIMEOUT_CONNECTION = 5000;
	private final int TIMEOUT_REQUEST = 10000;

	protected Vector<MovieData> mMovies;
	InputStream mInputStream;
	private boolean mStop = false;

	/* Abstract methods */

	abstract public String getName();

	abstract public String getURL();

	abstract public String getEndTag();

	protected abstract Vector<MovieData> extractFilms(String rawHTMLCode);

	/* Concrete methods */

	public Vector<MovieData> getMovies() {
		return mMovies;
	}

	public int refreshMoviesList(ShowMovies.Refresher refersher) {

		int error = 0;
		String contentReceived = "";
		mStop = false;

		try {
			// set http connection parameters
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams,
					TIMEOUT_CONNECTION);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_REQUEST);
			// httpParams.setParameter(CoreProtocolPNames.USER_AGENT,
			// "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");

			// create and execute HTTP request
			HttpClient httpclient = new DefaultHttpClient(httpParams);
			Log.d("CineIguatemi.refreshMoviesList()", "Getting HTTP: "
					+ getURL());
			HttpResponse response = httpclient.execute(new HttpGet(getURL()));

			// get response
			StatusLine statusLine = response.getStatusLine();
			HttpEntity entity = response.getEntity();
			Header headers[] = response.getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				Log.d("Cine.refreshMoviesList", headers[i].getName() + ": "
						+ headers[i].getValue());
			}
			mInputStream = entity.getContent();

			// check response's status
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

				// convert content from stream to string
				contentReceived = streamToString(refersher);
				try {
					mMovies = extractFilms(contentReceived);
				} catch (Exception e) {
					e.printStackTrace();
					error = 3;
				}
			} else {
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (SocketTimeoutException toEx) {
			Log.e("CineIguatemi.refreshMoviesList()", "TIMEOUT");
			toEx.printStackTrace();
			error = 1;
		} catch (IOException ioEx) {
			if (mStop) {
				error = 0;
			} else {
				Log.e("CineIguatemi.refreshMoviesList()", "GENERIC ERROR");
				ioEx.printStackTrace();
				error = 2;
			}
		} catch (Exception ex) {
			Log.e("CineIguatemi.refreshMoviesList()", "GENERIC ERROR");
			ex.printStackTrace();
			error = 2;
		}

		return error;
	}

	/* Auxiliary methods */

	/**
	 * Função auxiliar para passar o conteúdo de um InputStream para String
	 * 
	 * @param is
	 *            InputStream que se deseja converter
	 * @return String com o conteúdo do de is
	 * @throws IOException
	 */
	static String streamToString(InputStream is) throws IOException {
		byte[] bytes = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int count;
		while ((count = is.read(bytes)) > 0) {
			baos.write(bytes, 0, count);
			// set progress

		}
		return new String(baos.toByteArray());
	}

	/**
	 * Função auxiliar para passar o conteúdo de um InputStream para String
	 * 
	 * @param is
	 *            InputStream que se deseja converter
	 * @return String com o conteúdo do de is
	 * @throws IOException
	 */
	private String streamToString(ShowMovies.Refresher refresher)
			throws IOException {
		Log.d("DEBUG", "START");
		byte[] bytes = new byte[512];
		int count;
		long countTotal = 0;
		int total = 74000; // entity.getContentLength();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((count = mInputStream.read(bytes)) > 0) {
			countTotal += count;
			baos.write(bytes, 0, count);
			// set progress
			refresher.updateProgress((int) (10000 * countTotal / total));
			Log.d("Cine.streamToString()", "read: " + countTotal);
			if (countTotal > MAX_PAGE_SIZE) {
				break;
			}
			if (new String(baos.toByteArray())
					.contains("</section><!--/por filme-->")) {
				break;
			}
		}
		Log.d("DEBUG", "END " + countTotal);
		return new String(baos.toByteArray());
	}

	public void stop() {
		mStop = true;
		if (mInputStream == null)
			return;
		try {
			mInputStream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
