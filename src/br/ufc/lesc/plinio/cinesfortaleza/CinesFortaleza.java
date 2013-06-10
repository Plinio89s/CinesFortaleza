package br.ufc.lesc.plinio.cinesfortaleza;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
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

public class CinesFortaleza extends Activity implements AdListener {

	public static final String EXTRA_CINE = "CINE_NAME";
	public static final String TAG_DEBUG = "CinesFortaleza";

	private Vector<String> mCines;
	private ListView mListView;
	private AdView mAdView;
	private StoreData mStoredData;
	private Refresher mRefresher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// request permission to use (indeterminate) progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.activity_cines_fortaleza);

		// set listener to list view
		mListView = (ListView) findViewById(R.id.list_view_cines);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				cineClick(v);
			}
		});

		// create cine list
		mCines = new Vector<String>();

		// retrieve stored data (if any)
		mStoredData = new StoreData(this);
		Vector<Cine> c = mStoredData.getCines();
		for (int i = 0; i < c.size(); i++) {
			mCines.add(c.get(i).getName());
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
		mStoredData.loadCines();

		// create adapter
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mCines);

		// set ListView with cines
		mListView.setAdapter(adapter);

		// show last refresh time
		TextView tv = (TextView) findViewById(R.id.textViewLastRefresh);
		if (mStoredData.getLastRefreshMili() > 0) {
			Time time = new Time();
			time.set(mStoredData.getLastRefreshMili());
			tv.setText(getString(R.string.last_refresh)
					+ time.format(" %d/%m/%y (%R)."));
		} else {
			tv.setText(getString(R.string.text_choise));
		}

		// start refresher if needed
		if (mStoredData.refreshNeeded()) {
			mRefresher = new Refresher(this);
			mRefresher.execute("");
		} else {
			// START AD REQUEST CODE
			mAdView = (AdView) findViewById(R.id.adView);
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

	public void cineClick(View v) {
		Intent i = new Intent(this, ShowMovies.class);
		i.putExtra(EXTRA_CINE, ((TextView) v).getText());
		startActivity(i);
	}

	public void onRefreshClick(MenuItem item) {
		mRefresher = new Refresher(this);
		mRefresher.execute("");
	}

	class Refresher extends AsyncTask<String, Integer, Integer> {

		Activity mParent;

		public Refresher(Activity parent) {
			mParent = parent;
		}

		public void stop() {
			// stop progress bar
			publishProgress(0);

			CineProviderVM.stop();
		}

		public void updateProgress(int progress) {
			publishProgress(progress);
		}

		@Override
		protected Integer doInBackground(String... params) {

			// start progress bar
			publishProgress(0);

			// refreshMoviesList
			return CineProviderVM.refreshCinesList(this);
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

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			publishProgress(10000);

			// if error
			if (result != 0) {
				Toast.makeText(mParent,
						getString(R.string.error_refreshing_cine_list),
						Toast.LENGTH_LONG).show();
				return;
			}

			// get list of cines and copy do mCines
			Vector<Cine> cines = CineProviderVM.getCines();
			mCines.clear();
			for (int i = 0; i < cines.size(); i++) {
				mCines.add(cines.get(i).getName());
			}

			// set list view with list of cines
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(mParent,
					android.R.layout.simple_list_item_1, mCines);
			mListView.setAdapter(adapter);

			// save data
			mStoredData.setCines(cines);
			mStoredData.saveCines();

			// show last refresh time
			TextView tv = (TextView) findViewById(R.id.textViewLastRefresh);
			if (mStoredData.getLastRefreshMili() > 0) {
				Time time = new Time();
				time.set(mStoredData.getLastRefreshMili());
				tv.setText(getString(R.string.last_refresh)
						+ time.format(" %d/%m/%y (%R)."));
			} else {
				tv.setText(getString(R.string.text_choise));
			}

			// START AD REQUEST CODE
			mAdView = (AdView) findViewById(R.id.adView);
			mAdView.setAdListener((CinesFortaleza) mParent);
			AdRequest testAdRequest = new AdRequest();
			/*
			 * HashSet<String> testDevices = new HashSet<String>();
			 * TelephonyManager tm = (TelephonyManager)
			 * getSystemService(TELEPHONY_SERVICE); String deviceid =
			 * tm.getDeviceId(); testDevices.add(deviceid);
			 * testDevices.add("4D489C049448C846D466A2D068F96F23");
			 * testDevices.add("0A3C28C006019012");
			 * testAdRequest.setTestDevices(testDevices);
			 */
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
