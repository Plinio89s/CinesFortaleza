package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

import br.ufc.lesc.plinio.cinesfortaleza.Cine;
import br.ufc.lesc.plinio.cinesfortaleza.MovieData;

public class CineIguatemi extends Cine {

	private Vector<MovieData> mMovies;
	private static final String NAME = "Iguatemi";
	private static final String URL = "http://www.iguatemifortaleza.com.br/cinema";

	public CineIguatemi() {
		mMovies = new Vector<MovieData>();
		mMovies.clear();
	}

	public String getName() {
		return NAME;
	}

	public Vector<MovieData> getMovies() {
		return mMovies;
	}

	public String getURL() {
		return URL;
	}

	protected Vector<MovieData> extractFilms(String rawHTMLCode,
			Vector<MovieData> out) {
		int indexBeginArticle;
		int indexEndArticle;
		String article;
		int indexBeginFooter;
		int indexEndFooter;
		String footer;
		int indexBeginTitle;
		int indexEndTitle;
		String title;
		int indexBeginHorarios;
		int indexEndHorarios;
		String horarios;

		String tagBegin = "<section id=\"por-filme\" class=\"a-filme\">";
		String tagEnd = "</section><!--/por filme-->";

		String resultToAnalyze = rawHTMLCode;
		Vector<String> sessions = new Vector<String>();
		MovieData m = new MovieData();

		mMovies.clear();

		int indexBeginSection = resultToAnalyze.indexOf(tagBegin)
				+ tagBegin.length();
		if (indexBeginSection == -1)
			return out;
		int indexEndSection = resultToAnalyze
				.indexOf(tagEnd, indexBeginSection);
		if (indexEndSection == -1)
			return out;
		resultToAnalyze = resultToAnalyze.substring(indexBeginSection,
				indexEndSection);

		while (resultToAnalyze.indexOf("<article>") != -1) {

			// get title
			indexBeginArticle = resultToAnalyze.indexOf("<article>") + 9;
			indexEndArticle = resultToAnalyze.indexOf(
					"</article><!--/filme-->", indexBeginArticle);
			if (indexEndArticle == -1)
				break;
			article = resultToAnalyze.substring(indexBeginArticle,
					indexEndArticle);

			indexBeginFooter = article.indexOf("<footer>") + 8;
			if (indexBeginFooter == -1)
				break;
			indexEndFooter = article.indexOf("</footer>", indexBeginFooter);
			if (indexEndFooter == -1)
				break;
			footer = article.substring(indexBeginFooter, indexEndFooter);

			indexBeginTitle = footer.indexOf("<h4>") + 4;
			if (indexBeginTitle == -1)
				break;
			indexEndTitle = footer.indexOf("</h4>", indexBeginTitle);
			if (indexEndTitle == -1)
				break;
			title = footer.substring(indexBeginTitle, indexEndTitle).trim();
			if (title.length() > 0) {
				m = new MovieData(title);
			} else {
				resultToAnalyze = resultToAnalyze.substring(indexEndArticle);
				continue;
			}

			// get sessions
			sessions = new Vector<String>();
			while (resultToAnalyze.indexOf("<div class=\"horarios\">") != -1) {
				indexBeginHorarios = resultToAnalyze
						.indexOf("<div class=\"horarios\">") + 22;
				indexEndHorarios = resultToAnalyze.indexOf(
						"</article><!--/filme-->", indexBeginHorarios);
				if (indexEndHorarios == -1)
					break;
				horarios = resultToAnalyze.substring(indexBeginHorarios,
						indexEndHorarios);

				sessions.add(horarios);
			}

			m.setSessions(sessions);
			mMovies.add(m);

			resultToAnalyze = resultToAnalyze.substring(indexEndArticle);
		}

		return mMovies;
	}

}
