package br.ufc.lesc.plinio.cinesfortaleza;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineVM;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

public class CinesFortaleza extends Activity implements AdListener {

	public static final String EXTRA_CINE = "CINE_NAME";
	public static final String CINE_FILE = "cines";

	private Vector<String> mCines;
	private ListView mListView;
	private AdView mAdView;

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

		// start progress bar
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);
		setProgress(0);

		// create cine list
		mCines = new Vector<String>();

		// load last stored data
		SharedPreferences storedData = getSharedPreferences(CINE_FILE,
				MODE_PRIVATE);

		Set<String> keys = storedData.getAll().keySet();
		String key, value;
		Vector<Cine> cines = new Vector<Cine>();
		for (Iterator<String> i = keys.iterator(); i.hasNext();) {
			key = i.next();
			value = storedData.getString(key, "");
			// if (value.matches("[\\d]+")) {
			if (value.length() > 0) {
				cines.add(new CineVM(key, value));
				mCines.add(key);
			}
		}
		CineProviderVM.setCines(cines);

		new Refresher(this).execute("");
	}

	@Override
	protected void onResume() {
		super.onResume();

		// create adapter
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mCines);

		// set ListView with cines
		mListView.setAdapter(adapter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		setProgressBarIndeterminateVisibility(false);
		setProgressBarVisibility(false);
		CineProviderVM.stop();
	}

	public void cineClick(View v) {
		Intent i = new Intent(this, ShowMovies.class);
		i.putExtra(EXTRA_CINE, ((TextView) v).getText());
		startActivity(i);
	}

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
			return CineProviderVM.refreshCinesList(this);
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

			// if error
			if (result != 0) {
				Toast.makeText(mParent,
						getString(R.string.error_refreshing_cine_list),
						Toast.LENGTH_LONG).show();

				// stop progress bar
				setProgressBarIndeterminateVisibility(false);
				setProgressBarVisibility(false);

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

			// store data
			SharedPreferences.Editor editor = getSharedPreferences(CINE_FILE,
					MODE_PRIVATE).edit();
			editor.clear();
			editor.commit();
			String key, value;
			for (int i = 0; i < cines.size(); i++) {
				key = cines.get(i).getName();
				value = cines.get(i).getURL();
				value = value.substring(value.lastIndexOf('=') + 1);
				editor.putString(key, value);
			}
			editor.commit();

			// stop progress bar
			setProgressBarIndeterminateVisibility(false);
			setProgressBarVisibility(false);

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
