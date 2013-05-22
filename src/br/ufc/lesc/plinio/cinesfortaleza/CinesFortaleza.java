package br.ufc.lesc.plinio.cinesfortaleza;

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

public class CinesFortaleza extends Activity {

	public static final String EXTRA_CINE = "CINE_NAME";

	private Vector<String> mCines;
	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// request permission to use (indeterminate) progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.activity_cines_fortaleza);

		// start progress bar
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);
		setProgress(0);

		// create cine list
		mCines = new Vector<String>();

		CineProvider.stop();

		new Refresher(this).execute("");
	}

	@Override
	protected void onResume() {
		super.onResume();

		// create adapter
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mCines);

		// set ListView with cines and the click listener
		mListView = (ListView) findViewById(R.id.list_view_cines);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				cineClick(v);
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		setProgressBarIndeterminateVisibility(false);
		setProgressBarVisibility(false);
		CineProvider.stop();
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
			return CineProvider.refreshCinesList(this);
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
				Toast.makeText(mParent,
						"Não foi possível acessar a lista de Cinemas.",
						Toast.LENGTH_LONG).show();
				mParent.finish();
			}
			
			// get list of cines and copy do mCines
			Vector<Cine> cines = CineProvider.getCines();
			mCines.clear();
			for (int i = 0; i < cines.size(); i++) {
				mCines.add(cines.get(i).getName());
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(mParent,
					android.R.layout.simple_list_item_1, mCines);

			mListView.setAdapter(adapter);

			setProgressBarIndeterminateVisibility(false);
			setProgressBarVisibility(false);
		}

	} // class Refresher

}
