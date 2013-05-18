package br.ufc.lesc.plinio.cinesfortaleza.cines;

import java.util.Vector;

import br.ufc.lesc.plinio.cinesfortaleza.Cine;
import br.ufc.lesc.plinio.cinesfortaleza.MovieData;

public class CinePatioDomLuis extends Cine {

	private static final String NAME = "Pátio Dom Luis";
	private static final String URL = "http://www.patiodomluis.com.br/cinemas";

	private static final String TAG_BEGIN = "<h2>FILMES EM CARTAZ</h2>";
	private static final String TAG_END = "</div> <!-- /#main -->";

	public CinePatioDomLuis() {
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

		int indexBeginFieldTitle;
		int indexEndFieldTitle;
		String fieldTitle;
		int indexBeginTitle;
		int indexEndTitle;
		String title;
		int indexBeginHorario;
		int indexEndHorario;
		String Horario;
		int indexBeginFieldHorario;
		int indexEndFieldHorario;
		String fieldHorario;
		Vector<String> sessions;

		String resultToAnalyze = rawHTMLCode;
		sessions = new Vector<String>();
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

		while (resultToAnalyze.indexOf("<div class=\"views-field-title\">") != -1) {

			// get title
			indexBeginFieldTitle = resultToAnalyze
					.indexOf("<div class=\"views-field-title\">") + 31;
			indexEndFieldTitle = resultToAnalyze.indexOf("</div>",
					indexBeginFieldTitle);
			if (indexEndFieldTitle == -1)
				break;
			fieldTitle = resultToAnalyze.substring(indexBeginFieldTitle,
					indexEndFieldTitle);

			indexBeginTitle = fieldTitle
					.indexOf("<span class=\"field-content\">") + 28;
			indexEndTitle = fieldTitle.indexOf("</span>", indexBeginTitle);
			if (indexEndTitle == -1)
				break;
			title = fieldTitle.substring(indexBeginTitle, indexEndTitle).trim();

			if (title.length() > 0) {
				m = new MovieData(title);
			} else {
				resultToAnalyze = resultToAnalyze.substring(indexEndFieldTitle);
				continue;
			}

			// get sessions
			indexBeginHorario = resultToAnalyze.indexOf("Horário:") + 8;
			indexEndHorario = resultToAnalyze.indexOf("</div>",
					indexBeginHorario);
			if (indexEndHorario == -1)
				break;
			Horario = resultToAnalyze.substring(indexBeginHorario,
					indexEndHorario);

			indexBeginFieldHorario = Horario
					.indexOf("<span class=\"field-content\">") + 28;
			indexEndFieldHorario = Horario.indexOf("</span>",
					indexBeginFieldHorario);
			if (indexEndFieldHorario == -1)
				break;
			fieldHorario = Horario.substring(indexBeginFieldHorario,
					indexEndFieldHorario);

			sessions = new Vector<String>();
			sessions.add(fieldHorario);

			m.setSessions(sessions);
			mMovies.add(m);

			resultToAnalyze = resultToAnalyze.substring(indexEndHorario);
		}

		return mMovies;
	}
}
