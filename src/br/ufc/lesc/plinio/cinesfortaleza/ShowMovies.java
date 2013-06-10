package br.ufc.lesc.plinio.cinesfortaleza;

import java.util.ArrayList;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
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

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

public class ShowMovies extends Activity implements AdListener {

	protected static final String EXTRA_MOVIE = "MOVIE_SELECTED";
	protected static final String EXTRA_SESSIONS = "SESSIONS";
	protected static final String KEY_MOVIE_NAME = "MOVIE_NAME_";

	private Cine mCine;
	private ListView mListView;
	private String mMovieSelected;
	private AdView mAdView;
	private StoreData mStoredData;
	private Refresher mRefresher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// request permission to use (indeterminate) progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.show_movies_layout);

		// initialize member attributes
		mCine = null;

		// set ListView's onItemClick method
		mListView = (ListView) findViewById(R.id.list_view_movies);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				onClick(position);
			}
		});

		// reg for context menu
		registerForContextMenu(mListView);

		// decode cine choose
		String cineName = getIntent().getStringExtra(CinesFortaleza.EXTRA_CINE);

		if (cineName != null) {
			// Vector<Cine> cines = CineProviderVM.getCines();
			// load stored data
			mStoredData = new StoreData(this);
			Vector<Cine> cines = mStoredData.getCines();
			for (int i = 0; i < cines.size(); i++) {
				if (cineName.equalsIgnoreCase(cines.get(i).getName())) {
					mCine = cines.get(i);
					mStoredData.loadMovies(mCine);
					break;
				}
			}
		}

		if (mCine == null) {
			finish();
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// load stored data
		mStoredData.loadMovies(mCine);

		// create adapter
		ArrayAdapter<MovieData> adapter = new ArrayAdapter<MovieData>(this,
				android.R.layout.simple_list_item_1, mCine.getMovies());

		// set cine name
		((TextView) findViewById(R.id.title_cine)).setText(mCine.getName());

		// set list of movies
		mListView.setAdapter(adapter);

		// show last refresh time
		TextView tv = (TextView) findViewById(R.id.textViewLastRefreshCine);
		if (mStoredData.getLastRefreshCineMili(mCine) > 0) {
			Time time = new Time();
			time.set(mStoredData.getLastRefreshCineMili(mCine));
			tv.setText(getString(R.string.last_refresh)
					+ time.format(" %d/%m/%y (%R)."));
		} else {
			tv.setText(getString(R.string.empty));
		}

		// start refresher if needed
		if (mStoredData.refreshNeededCine(mCine)) {
			mRefresher = new Refresher(this);
			mRefresher.execute("");
		} else {
			// START AD REQUEST CODE
			mAdView = (AdView) findViewById(R.id.adView2);
			mAdView.setAdListener(this);
			AdRequest testAdRequest = new AdRequest();
			mAdView.loadAd(testAdRequest);
			// END AD REQUEST CODE
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mRefresher != null) {
			mRefresher.stop();
		}
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

	public void onRefreshClick(MenuItem item) {
		mRefresher = new Refresher(this);
		mRefresher.execute("");
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

		public void stop() {
			// stop progress bar
			publishProgress(0);
			mCine.stop();
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
			if (values[0] <= 0 || values[0] >= 10000) {
				setProgressBarIndeterminateVisibility(false);
				setProgressBarVisibility(false);
			} else {
				setProgressBarIndeterminateVisibility(true);
				setProgressBarVisibility(true);
			}
			setProgress(values[0]);
		}

		@SuppressLint("DefaultLocale")
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			setProgress(10000);

			// if error
			if (result != 0) {
				Toast.makeText(mParent,
						getString(R.string.error_refreshing_movie_list),
						Toast.LENGTH_LONG).show();
				return;
			}

			// set list view with list of cines
			ArrayAdapter<MovieData> adapter = new ArrayAdapter<MovieData>(
					mParent, android.R.layout.simple_list_item_1,
					mCine.getMovies());
			mListView.setAdapter(adapter);

			// store data
			mStoredData.saveMovies(mCine);

			// show last refresh time
			TextView tv = (TextView) findViewById(R.id.textViewLastRefreshCine);
			if (mStoredData.getLastRefreshCineMili(mCine) > 0) {
				Time time = new Time();
				time.set(mStoredData.getLastRefreshCineMili(mCine));
				tv.setText(getString(R.string.last_refresh)
						+ time.format(" %d/%m/%y (%R)."));
			} else {
				tv.setText(getString(R.string.empty));
			}

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
