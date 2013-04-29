package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

import br.ufc.lesc.plinio.cinesfortaleza.Cine;
import br.ufc.lesc.plinio.cinesfortaleza.MovieData;

public class CineViaSul extends Cine {

	private Vector<MovieData> mMovies;
	private static final String NAME = "Via Sul";
	private static final String URL = "http://www.shoppingviasul.com.br/index.php/entretenimento";

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
	public Vector<MovieData> getMovies() {
		return mMovies;
	}

	@Override
	protected Vector<String> extractFilms(String rawHTMLCode, Vector<String> out) {
		String resultToAnalyze = rawHTMLCode;
		out.clear();

		String tagBegin = "<span class=\"next\">Next</span>";
		String tagEnd = "<script type=\"text/javascript\">";

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
			if (!out.contains(title) && title.length() > 0) {
				out.add(title);
			}
			resultToAnalyze = resultToAnalyze.substring(indexEndNspArt);
		}

		return out;
	}

}
