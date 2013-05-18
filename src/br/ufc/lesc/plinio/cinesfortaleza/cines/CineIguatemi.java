package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

import br.ufc.lesc.plinio.cinesfortaleza.Cine;
import br.ufc.lesc.plinio.cinesfortaleza.MovieData;

public class CineIguatemi extends Cine {

	private static final String NAME = "Iguatemi";
	private static final String URL = "http://www.iguatemifortaleza.com.br/cinema";

	private static final String TAG_BEGIN = "<section id=\"por-filme\" class=\"a-filme\">";
	private static final String TAG_END = "</section><!--/por filme-->";

	public CineIguatemi() {
		mMovies = new Vector<MovieData>();
		mMovies.clear();
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
			while (article.indexOf("<div class=\"horarios\">") != -1) {
				indexBeginHorarios = article
						.indexOf("<div class=\"horarios\">") + 22;
				indexEndHorarios = article
						.indexOf("</div>", indexBeginHorarios);
				if (indexEndHorarios == -1)
					break;
				horarios = article.substring(indexBeginHorarios,
						indexEndHorarios);

				sessions.add(horarios);
				article = article.substring(indexEndHorarios);
			}

			m.setSessions(sessions);
			mMovies.add(m);

			resultToAnalyze = resultToAnalyze.substring(indexEndArticle);
		}

		return mMovies;
	}

}
