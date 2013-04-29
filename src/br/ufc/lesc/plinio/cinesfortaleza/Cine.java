package br.ufc.lesc.plinio.cinesfortaleza;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public abstract class Cine {

	private final int TIMEOUT_CONNECTION = 5000;
	private final int TIMEOUT_REQUEST = 10000;
	private final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.2.2; Galaxy Nexus Build/JDQ39) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.58 Mobile Safari/537.31";

	/* Abstract methods */

	public abstract String getName();

	public abstract String getURL();

	public abstract Vector<MovieData> getMovies();

	protected abstract Vector<String> extractFilms(String rawHTMLCode,
			Vector<String> out);

	/* Concrete methods */

	public int refreshMoviesList() {

		int error = 0;
		String contentReceived = "";
		Vector<String> films = new Vector<String>();

		try {
			// set http connection parameters
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams,
					TIMEOUT_CONNECTION);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_REQUEST);
			Log.d("CineIguatemi.refreshMoviesList()", "UserAgent: " + httpParams.getParameter(CoreProtocolPNames.USER_AGENT));
			httpParams.setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
			Log.d("CineIguatemi.refreshMoviesList()", "UserAgent: " + httpParams.getParameter(CoreProtocolPNames.USER_AGENT));

			// create and execute HTTP request
			HttpClient httpclient = new DefaultHttpClient(httpParams);
			Log.d("CineIguatemi.refreshMoviesList()", "Getting HTTP: "
					+ getURL());
			HttpResponse response = httpclient.execute(new HttpGet(getURL()));

			// get response
			StatusLine statusLine = response.getStatusLine();
			InputStream inSt = response.getEntity().getContent();

			// check response's status
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

				// convert content from stream to string and print it
				contentReceived = streamToString(inSt);
				try {
					films = extractFilms(contentReceived, films);
				} catch (Exception e) {
					e.printStackTrace();
					error = 3;
				}

				// close input stream
				inSt.close();

			} else {
				// Closes the connection.
				inSt.close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (SocketTimeoutException toEx) {
			Log.e("CineIguatemi.refreshMoviesList()", "TIMEOUT");
			toEx.printStackTrace();
			error = 1;
		} catch (Exception ex) {
			Log.e("CineIguatemi.refreshMoviesList()", "GENERIC ERROR");
			ex.printStackTrace();
			error = 2;
		}

		if (error == 0) {
			Vector<MovieData> newMovies = new Vector<MovieData>();

			Log.d("CineIguatemi.refreshMoviesList()", "filmNames begin");
			for (int i = 0; i < films.size(); i++) {
				Log.d("CineIguatemi.refreshMoviesList()", films.get(i));
				newMovies.add(new MovieData(films.get(i)));
			}
			Log.d("CineIguatemi.refreshMoviesList()", "filmNames end");

			getMovies().clear();
			for (int i = 0; i < newMovies.size(); i++) {
				getMovies().add(newMovies.get(i));
			}
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
		}
		return new String(baos.toByteArray());
	}

	/**
	 * Função auxiliar para passar o conteúdo de um InputStream para String (não
	 * bloqueante)
	 * 
	 * @param is
	 *            InputStream que se deseja converter
	 * @param num
	 *            numero de bytes a serem lidos da stream
	 * @return String com o conteúdo do de is
	 * @throws IOException
	 */
	static String streamToString(InputStream is, int num) throws IOException {
		byte[] bytes = new byte[num];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int count;
		if (is.available() > 0) {
			count = is.read(bytes);
			baos.write(bytes, 0, count);
			return new String(baos.toByteArray());
		} else {
			return "";
		}
	}

}
