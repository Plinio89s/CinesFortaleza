package br.ufc.lesc.plinio.cinesfortaleza;

import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineBenfica;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

public class ShowMovies extends Activity implements AdListener {

	protected static final String EXTRA_MOVIE = "MOVIE_SELECTED";
	protected static final String EXTRA_SESSIONS = "SESSIONS";

	private Cine mCine;
	private ListView mListView;
	private String mMovieSelected;
	private AdView mAdView;

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

		// reg for context menu
		registerForContextMenu(mListView);

		// start progress bar
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);
		setProgress(0);

		// decode cine choose
		String cineName = getIntent().getStringExtra(CinesFortaleza.EXTRA_CINE);

		if (cineName != null) {
			Vector<Cine> cines = CineProviderVM.getCines();
			for (int i = 0; i < cines.size(); i++) {
				if (cineName.equalsIgnoreCase(cines.get(i).getName())) {
					mCine = cines.get(i);
				}
			}
		}

		mCine.stop();

		new Refresher(this).execute("");
	}

	@Override
	protected void onResume() {
		super.onResume();

		((TextView) findViewById(R.id.title_cine)).setText(mCine.getName());

		ArrayAdapter<MovieData> adapter = new ArrayAdapter<MovieData>(this,
				android.R.layout.simple_list_item_1, mCine.getMovies());

		mListView.setAdapter(adapter);

		// reg for context menu
		registerForContextMenu(mListView);
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

	public void shearchMovie(MenuItem item) {
		Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
		i.putExtra(SearchManager.QUERY, mMovieSelected);
		startActivity(i);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		mMovieSelected = mCine.getMovies().get(info.position).getName();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu_movies, menu);
	}

	/**
	 * Class to execute the refresh in another thread
	 */
	class Refresher extends AsyncTask<String, Integer, Integer> {

		Activity mParent;

		public Refresher(Activity parent) {
			mParent = parent;
		}

		public void updateProgress(int progress) {
			publishProgress(progress);
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

			// reg for context menu
			registerForContextMenu(mListView);

			setProgressBarIndeterminateVisibility(false);
			setProgressBarVisibility(false);

			// START AD REQUEST CODE
			mAdView = (AdView) findViewById(R.id.adView2);
			mAdView.setAdListener((ShowMovies) mParent);
			AdRequest testAdRequest = new AdRequest();
			mAdView.loadAd(testAdRequest);
			// END AD REQUEST CODE

		}

	} // class Refresher

	// ADLISTENER METHODS IMPLEMENTATION

	@Override
	public void onReceiveAd(Ad arg0) {
		ScaleAnimation zoomIn = new ScaleAnimation(.5f, 1f, .5f, 1f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		zoomIn.setDuration(500);
		mAdView.startAnimation(zoomIn);
	}

	@Override
	public void onDismissScreen(Ad arg0) {
		// do nothing
	}

	@Override
	public void onLeaveApplication(Ad arg0) {
		// do nothing
	}

	@Override
	public void onPresentScreen(Ad arg0) {
		// do nothing
	}

	@Override
	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
		// do nothing
	}
}
