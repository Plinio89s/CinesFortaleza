package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

import br.ufc.lesc.plinio.cinesfortaleza.Cine;
import br.ufc.lesc.plinio.cinesfortaleza.MovieData;

public class CineBenfica extends Cine {

	private Vector<MovieData> mMovies;
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
	public Vector<MovieData> getMovies() {
		return mMovies;
	}

	@Override
	protected Vector<String> extractFilms(String rawHTMLCode, Vector<String> out) {
		String resultToAnalyze = rawHTMLCode;
		out.clear();

		while (resultToAnalyze.indexOf("<div class=\"filme\">") != -1) {
			int indexBeginFilme = resultToAnalyze
					.indexOf("<div class=\"filme\">") + 19;
			int indexEndFilme = resultToAnalyze.indexOf("</div><!-- filme -->",
					indexBeginFilme);
			if (indexEndFilme == -1)
				break;
			String Filme = resultToAnalyze.substring(indexBeginFilme,
					indexEndFilme);

			int indexBeginTitle = Filme.indexOf("<div class=\"titulo\">") + 20;
			if (indexBeginTitle == -1)
				break;
			int indexEndTitle = Filme.indexOf("</div>", indexBeginTitle);
			if (indexEndTitle == -1)
				break;
			String title = Filme.substring(indexBeginTitle, indexEndTitle)
					.trim();
			if (!out.contains(title) && title.length() > 0) {
				out.add(title);
			}
			resultToAnalyze = resultToAnalyze.substring(indexEndFilme);
		}

		return out;
	}

}
