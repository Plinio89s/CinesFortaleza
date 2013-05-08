package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

import android.text.format.Time;
import android.util.Log;
import br.ufc.lesc.plinio.cinesfortaleza.Cine;
import br.ufc.lesc.plinio.cinesfortaleza.MovieData;

public class CineViaSul extends Cine {

	private Vector<MovieData> mMovies;
	private static final String NAME = "Via Sul";
	private static final String URL = "http://www.shoppingviasul.com.br/index.php/entretenimento";

	private final String TAG_BEGIN = "<span class=\"next\">Next</span>";
	private final String TAG_END = "<script type=\"text/javascript\">";

	public CineViaSul() {
		mMovies = new Vector<MovieData>();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getURL() {
		return URL;
	}

	@Override
	public String getEndTag() {
		return TAG_END;
	}

	@Override
	public Vector<MovieData> getMovies() {
		return mMovies;
	}

	@Override
	protected Vector<MovieData> extractFilms(String rawHTMLCode,
			Vector<MovieData> out) {
		String resultToAnalyze = rawHTMLCode;
		Vector<String> films = new Vector<String>();

		int indexBeginSection = resultToAnalyze.indexOf(TAG_BEGIN)
				+ TAG_BEGIN.length();
		if (indexBeginSection == -1)
			return out;
		int indexEndSection = resultToAnalyze.indexOf(TAG_END,
				indexBeginSection);
		if (indexEndSection == -1)
			return out;
		resultToAnalyze = resultToAnalyze.substring(indexBeginSection,
				indexEndSection);

		while (resultToAnalyze.indexOf("<div class=\"nsp_art") != -1) {
			int indexBeginNspArt = resultToAnalyze
					.indexOf("<div class=\"nsp_art") + 20;
			int indexEndNspArt = resultToAnalyze.indexOf("</div></div>",
					indexBeginNspArt);
			if (indexEndNspArt == -1)
				break;
			String nspArt = resultToAnalyze.substring(indexBeginNspArt,
					indexEndNspArt);

			int indexBeginTitle = nspArt.indexOf("title=\"") + 7;
			if (indexBeginTitle == -1)
				break;
			int indexEndTitle = nspArt.indexOf("\">", indexBeginTitle);
			if (indexEndTitle == -1)
				break;
			String title = nspArt.substring(indexBeginTitle, indexEndTitle)
					.trim();
			if (title.length() > 0) {
				films.add(title);
			}
			resultToAnalyze = resultToAnalyze.substring(indexEndNspArt);
		}

		mMovies.clear();

		Log.d("CineIguatemi.refreshMoviesList()", "filmNames begin");
		for (int i = 0; i < films.size(); i++) {
			Log.d("CineIguatemi.refreshMoviesList()", films.get(i));
			MovieData m = new MovieData(films.get(i));
			Vector<String> sessions = new Vector<String>();
			sessions.add(new Time().toString());
			try {
				Thread.sleep(10);
				sessions.add(new Time().toString());
				Thread.sleep(10);
				sessions.add(new Time().toString());
				Thread.sleep(10);
				sessions.add(new Time().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			m.setSessions(sessions);
			mMovies.add(m);
		}
		Log.d("CineIguatemi.refreshMoviesList()", "filmNames end");

		return mMovies;
	}

}
