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

	protected Vector<String> extractFilms(String rawHTMLCode, Vector<String> out) {
		String resultToAnalyze = rawHTMLCode;

		while (resultToAnalyze.indexOf("<figcaption>") != -1) {
			int indexBegin = resultToAnalyze.indexOf("<figcaption>") + 12;
			int indexEnd = resultToAnalyze.indexOf("</figcaption>", indexBegin);
			String title = resultToAnalyze.substring(indexBegin, indexEnd).trim();
			if (!out.contains(title) && title.length() > 0) {
				out.add(title);
			}
			resultToAnalyze = resultToAnalyze.substring(indexEnd);
		}

		return out;
	}

}
