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
		out.clear();

		String tagBegin = "<section id=\"por-filme\" class=\"a-filme\">";
		String tagEnd = "</section><!--/por filme-->";
		
		int indexBeginSection = resultToAnalyze.indexOf(tagBegin) + tagBegin.length();
		if(indexBeginSection == -1) return out;
		int indexEndSection = resultToAnalyze.indexOf(tagEnd, indexBeginSection);
		if(indexEndSection == -1) return out;
		resultToAnalyze = resultToAnalyze.substring(indexBeginSection, indexEndSection);
		
		while (resultToAnalyze.indexOf("<article>") != -1) {
			int indexBeginArticle = resultToAnalyze.indexOf("<article>") + 9;
			int indexEndArticle = resultToAnalyze.indexOf("</article><!--/filme-->", indexBeginArticle);
			if(indexEndArticle == -1) break;
			String article = resultToAnalyze.substring(indexBeginArticle, indexEndArticle);
			
			int indexBeginFooter = article.indexOf("<footer>") + 8;
			if(indexBeginFooter == -1) break;
			int indexEndFooter = article.indexOf("</footer>", indexBeginFooter);
			if(indexEndFooter == -1) break;
			String footer = article.substring(indexBeginFooter, indexEndFooter);
			

			int indexBeginTitle = footer.indexOf("<h4>") + 4;
			if(indexBeginTitle == -1) break;
			int indexEndTitle = footer.indexOf("</h4>", indexBeginTitle);
			if(indexEndTitle == -1) break;
			String title = footer.substring(indexBeginTitle, indexEndTitle).trim();
			if (!out.contains(title) && title.length() > 0) {
				out.add(title);
			}
			resultToAnalyze = resultToAnalyze.substring(indexEndArticle);
		}

		return out;
	}

}
