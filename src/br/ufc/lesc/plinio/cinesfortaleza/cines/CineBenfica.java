package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

import br.ufc.lesc.plinio.cinesfortaleza.Cine;
import br.ufc.lesc.plinio.cinesfortaleza.MovieData;

public class CineBenfica extends Cine {

	private static final String NAME = "Benfica";
	private static final String URL = "http://www.shoppingbenfica.com.br/benfica/cinema";

	public CineBenfica() {
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
	protected Vector<MovieData> extractFilms(String rawHTMLCode,
			Vector<MovieData> out) {

		int indexBeginFilme;
		int indexEndFilme;
		String Filme;
		int indexBeginTitle;
		int indexEndTitle;
		String title;
		int indexBeginHora;
		int indexEndHora;
		String Hora;
		Vector<String> sessions;

		String resultToAnalyze = rawHTMLCode;
		MovieData m = new MovieData();

		mMovies.clear();

		while (resultToAnalyze.indexOf("<div class=\"filme\">") != -1) {

			// get title
			indexBeginFilme = resultToAnalyze.indexOf("<div class=\"filme\">") + 19;
			indexEndFilme = resultToAnalyze.indexOf("</div><!-- filme -->",
					indexBeginFilme);
			if (indexEndFilme == -1)
				break;
			Filme = resultToAnalyze.substring(indexBeginFilme, indexEndFilme);

			indexBeginTitle = Filme.indexOf("<div class=\"titulo\">") + 20;
			if (indexBeginTitle == -1)
				break;
			indexEndTitle = Filme.indexOf("</div>", indexBeginTitle);
			if (indexEndTitle == -1)
				break;
			title = Filme.substring(indexBeginTitle, indexEndTitle).trim();
			if (title.length() > 0) {
				m = new MovieData(title);
			} else {
				resultToAnalyze = resultToAnalyze.substring(indexEndFilme);
				continue;
			}

			// get sessions
			indexBeginHora = resultToAnalyze.indexOf("<span class=\"hora\">") + 19;
			indexEndHora = resultToAnalyze.indexOf("</span>", indexBeginHora);
			if (indexEndFilme == -1)
				break;
			Hora = resultToAnalyze.substring(indexBeginHora, indexEndHora);

			sessions = new Vector<String>();
			sessions.add(Hora);

			m.setSessions(sessions);
			mMovies.add(m);

			resultToAnalyze = resultToAnalyze.substring(indexEndFilme);
		}

		return mMovies;
	}
}
