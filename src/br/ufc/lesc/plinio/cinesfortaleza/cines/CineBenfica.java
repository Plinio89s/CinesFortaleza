package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

import br.ufc.lesc.plinio.cinesfortaleza.Cine;
import br.ufc.lesc.plinio.cinesfortaleza.MovieData;

public class CineBenfica extends Cine {

	private static final String NAME = "Benfica";
	private static final String URL = "http://verdesmares.globo.com/v3/canais/cinema_mais_info.asp?cinema=39";

	private static final String TAG_BEGIN = "<h4 class=\"h4PC\">Salas</h4>";
	private static final String TAG_END = "<h4 class=\"h4PC\">Ingressos</h4>";

	public CineBenfica() {
		mMovies = new Vector<MovieData>();
	}

	public String getName() {
		return NAME;
	}

	public String getURL() {
		return URL;
	}

	public String getEndTag() {
		return TAG_END;
	}

	@Override
	protected Vector<MovieData> extractFilms(String rawHTMLCode) {
		int indexBeginFilme;
		int indexEndFilme;
		String filme;
		int indexBeginTitle;
		int indexEndTitle;
		String title;
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
		
		while (resultToAnalyze.indexOf("Filme: </em><a href=\"") != -1) {

			// get title
			indexBeginFilme = resultToAnalyze.indexOf("Filme: </em><a href=\"") + 21;
			indexEndFilme = resultToAnalyze.indexOf("Classificação:",
					indexBeginFilme);
			if (indexEndFilme == -1)
				break;
			filme = resultToAnalyze.substring(indexBeginFilme, indexEndFilme);

			indexBeginTitle = filme.indexOf(">") + 1;
			if (indexBeginTitle == -1)
				break;
			indexEndTitle = filme.indexOf("</a>", indexBeginTitle);
			if (indexEndTitle == -1)
				break;
			title = filme.substring(indexBeginTitle, indexEndTitle).trim();

			if (title.length() > 0) {
				m = new MovieData(title);
			} else {
				resultToAnalyze = resultToAnalyze.substring(indexEndFilme);
				continue;
			}

			// get sessions
			sessions = new Vector<String>();
			while (filme.indexOf("Sessões: </em>") != -1) {

				indexBeginHorarios = filme.indexOf("Sessões: </em>") + 14;
				indexEndHorarios = filme.indexOf("</span>", indexBeginHorarios);
				if (indexEndHorarios == -1)
					break;
				horarios = filme
						.substring(indexBeginHorarios, indexEndHorarios);

				sessions.add(horarios);
				filme = filme.substring(indexEndHorarios);
			}

			m.setSessions(sessions);
			mMovies.add(m);

			resultToAnalyze = resultToAnalyze.substring(indexEndFilme);
		}

		return mMovies;
	}
}
