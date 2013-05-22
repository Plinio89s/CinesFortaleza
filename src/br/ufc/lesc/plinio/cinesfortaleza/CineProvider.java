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
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineVM;

public class CineProvider {

	private final static long MAX_PAGE_SIZE = 500000;
	private final static int TIMEOUT_CONNECTION = 5000;
	private final static int TIMEOUT_REQUEST = 10000;
	private final static String URL = "http://verdesmares.globo.com/cinema/";
	private final static String TAG_BEGIN = "Escolha o Cinema";
	private final static String TAG_END = "Escolha a Cidade";

	private static Vector<Cine> mCines;
	private static InputStream mInputStream;
	private static boolean mStop = false;

	public static Vector<Cine> getCines() {
		if (mCines == null) {
			return new Vector<Cine>();
		}
		return mCines;
	}

	public static int refreshCinesList(CinesFortaleza.Refresher refresher) {
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
			Log.d("CineIguatemi.refreshMoviesList()", "Getting HTTP: " + URL);
			HttpResponse response = httpclient.execute(new HttpGet(URL));

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
				contentReceived = streamToString(refresher);
				try {
					mCines = extractCines(contentReceived);
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

	private static Vector<Cine> extractCines(String rawHTMLCode) {
		int indexBeginCinema;
		int indexEndCinema;
		String cinema;
		int indexBeginName;

		final String TAG_CINEMA_BEGIN = "<option value='cinema_mais_info.asp?cinema=";
		final String TAG_CINEMA_END = "</option>";

		String resultToAnalyze = rawHTMLCode;
		String cineName;
		String cineCode;

		mCines = new Vector<Cine>();

		int indexBeginSection = resultToAnalyze.indexOf(TAG_BEGIN)
				+ TAG_BEGIN.length();
		if (indexBeginSection == -1)
			return mCines;
		int indexEndSection = resultToAnalyze.indexOf(TAG_END,
				indexBeginSection);
		if (indexEndSection == -1)
			return mCines;
		resultToAnalyze = resultToAnalyze.substring(indexBeginSection,
				indexEndSection);

		while (resultToAnalyze.indexOf(TAG_CINEMA_BEGIN) != -1) {

			Log.d("CineProvider.extractCines()","while");
			// get cinema
			indexBeginCinema = resultToAnalyze.indexOf(TAG_CINEMA_BEGIN)
					+ TAG_CINEMA_BEGIN.length();
			indexEndCinema = resultToAnalyze.indexOf(TAG_CINEMA_END,
					indexBeginCinema);
			if (indexEndCinema == -1)
				break;
			cinema = resultToAnalyze
					.substring(indexBeginCinema, indexEndCinema);

			cineCode = cinema.substring(0, 2);

			indexBeginName = cinema.indexOf(">") + 1;
			if (indexBeginName == -1)
				break;
			cineName = cinema.substring(indexBeginName).trim();

			//if (cineName.length() > 0) {
				mCines.add(new CineVM(cineName, cineCode));
			//}

			resultToAnalyze = resultToAnalyze.substring(indexEndCinema);
		}

		return mCines;
	}

	/**
	 * Função auxiliar para passar o conteúdo de um InputStream para String
	 * 
	 * @param is
	 *            InputStream que se deseja converter
	 * @return String com o conteúdo do de is
	 * @throws IOException
	 */
	private static String streamToString(CinesFortaleza.Refresher refresher)
			throws IOException {
		Log.d("DEBUG", "START");
		byte[] bytes = new byte[512];
		int count;
		long countTotal = 0;
		int total = 20000; // entity.getContentLength();
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
			if (new String(baos.toByteArray()).contains(TAG_END)) {
				break;
			}
		}
		Log.d("DEBUG", "END " + countTotal);
		return new String(baos.toByteArray(), "iso-8859-1");
	}

	public static void stop() {
		mStop = true;
		if (mInputStream == null)
			return;
		try {
			mInputStream.close();
		} catch (Exception ex) {
			//ex.printStackTrace();
		}

	}
}
