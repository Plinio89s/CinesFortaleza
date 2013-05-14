package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

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
	protected Vector<MovieData> extractFilms(String rawHTMLCode) {
		int indexBeginTitle;
		int indexEndTitle;
		String title;
		int indexBeginNspArt;
		int indexEndNspArt;
		String nspArt;
		int indexBeginHorarios;
		int indexEndHorarios;
		String horarios;

		String resultToAnalyze = rawHTMLCode;
		Vector<String> sessions = new Vector<String>();
		MovieData m = new MovieData();

		mMovies.clear();

		int indexBeginSection = resultToAnalyze.indexOf(TAG_BEGIN)
				+ TAG_BEGIN.length();
		if (indexBeginSection == -1)
			return mMovies;
		int indexEndSection = resultToAnalyze.indexOf(TAG_END,
				indexBeginSection);
		if (indexEndSection == -1)
			return mMovies;
		resultToAnalyze = resultToAnalyze.substring(indexBeginSection,
				indexEndSection);

		while (resultToAnalyze.indexOf("<div class=\"nsp_art") != -1) {

			// get title
			indexBeginNspArt = resultToAnalyze.indexOf("<div class=\"nsp_art") + 20;
			indexEndNspArt = resultToAnalyze.indexOf("</div></div>",
					indexBeginNspArt);
			if (indexEndNspArt == -1)
				break;
			nspArt = resultToAnalyze
					.substring(indexBeginNspArt, indexEndNspArt);

			indexBeginTitle = nspArt.indexOf("title=\"") + 7;
			if (indexBeginTitle == -1)
				break;
			indexEndTitle = nspArt.indexOf("\">", indexBeginTitle);
			if (indexEndTitle == -1)
				break;
			title = nspArt.substring(indexBeginTitle, indexEndTitle).trim();
			if (title.length() > 0) {
				m = new MovieData(title);
			} else {
				resultToAnalyze = resultToAnalyze.substring(indexEndNspArt);
				continue;
			}

			// get sessions
			sessions = new Vector<String>();
			while (nspArt.indexOf("<a href=\"/index.php/em-cartaz/") != -1) {
				indexBeginHorarios = nspArt
						.indexOf("<a href=\"/index.php/em-cartaz/") + 9;
				indexEndHorarios = nspArt.indexOf("\"", indexBeginHorarios);
				if (indexEndHorarios == -1)
					break;
				horarios = nspArt.substring(indexBeginHorarios,
						indexEndHorarios);

				sessions.add(horarios);
				nspArt = nspArt.substring(indexEndHorarios);
			}

			m.setSessions(sessions);
			mMovies.add(m);

			resultToAnalyze = resultToAnalyze.substring(indexEndNspArt);
		}

		return mMovies;
	}

	protected Vector<MovieData> extractSessions(String rawHTMLCode,
			Vector<MovieData> out) {
		return out;
	}
}
