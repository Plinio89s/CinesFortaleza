package br.ufc.lesc.plinio.cinesfortaleza;

import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineBenfica;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineIguatemi;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineViaSul;

public class ShowMovies extends Activity {

	protected static final String EXTRA_MOVIE = "MOVIE_SELECTED";
	protected static final String EXTRA_SESSIONS = "SESSIONS";

	private Cine mCine;
	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// request permission to use (indeterminate) progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.show_movies_layout);

		// initialize member attributes
		mCine = new CineBenfica();
		mListView = (ListView) findViewById(R.id.list_view_movies);

		// set ListView's onItemClick method
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				onClick(position);
			}
		});

		// start progress bar
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);

		// decode cine choose
		String cineName = getIntent().getStringExtra(CinesFortaleza.EXTRA_CINE);
		CineBenfica cineBenfica = new CineBenfica();
		CineIguatemi cineIguatemi = new CineIguatemi();
		CineViaSul cineViaSul = new CineViaSul();

		if (cineName != null) {
			if (cineName.equalsIgnoreCase(cineIguatemi.getName())) {
				mCine = cineIguatemi;
			} else if (cineName.equalsIgnoreCase(cineViaSul.getName())) {
				mCine = cineViaSul;
			} else if (cineName.equalsIgnoreCase(cineBenfica.getName())) {
				mCine = cineBenfica;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);
		setProgress(0);
		mCine.stop();

		((TextView) findViewById(R.id.title_cine)).setText(mCine.getName());

		ArrayAdapter<MovieData> adapter = new ArrayAdapter<MovieData>(this,
				android.R.layout.simple_list_item_1, mCine.getMovies());

		mListView.setAdapter(adapter);

		new Refresher(this).execute("");
	}

	@Override
	protected void onPause() {
		super.onPause();
		setProgressBarIndeterminateVisibility(false);
		setProgressBarVisibility(false);
		mCine.stop();
	}

	private void onClick(int pos) {
		ArrayList<String> ss = new ArrayList<String>();
		Vector<String> sessions = mCine.getMovies().get(pos).getSessions();
		for (int j = 0; j < sessions.size(); j++) {
			ss.add(sessions.get(j));
		}
		Intent i = new Intent(this, ShowSessions.class);
		i.putStringArrayListExtra(EXTRA_SESSIONS, ss);
		i.putExtra(EXTRA_MOVIE, mCine.getMovies().get(pos).getName());
		startActivity(i);
		setProgressBarIndeterminateVisibility(false);
		setProgressBarVisibility(false);
	}

	class Refresher extends AsyncTask<String, Integer, Integer> {

		Activity mParent;

		public Refresher(Activity parent) {
			mParent = parent;
		}

		public void updateProgress(int progress) {
			publishProgress(progress);
			// Log.d("Refresher.serProgress", ""+progress);
		}

		@Override
		protected Integer doInBackground(String... params) {
			// refreshMoviesList
			return mCine.refreshMoviesList(this);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			setProgress(100000);

			if (result != 0) {
				Toast.makeText(
						mParent,
						"Não foi possível acessar a lista de filmes do cinema selecionado.",
						Toast.LENGTH_LONG).show();
				mParent.finish();
			}

			ArrayAdapter<MovieData> adapter = new ArrayAdapter<MovieData>(
					mParent, android.R.layout.simple_list_item_1,
					mCine.getMovies());

			mListView.setAdapter(adapter);

			setProgressBarIndeterminateVisibility(false);
			setProgressBarVisibility(false);
		}

	} // class Refresher

}
