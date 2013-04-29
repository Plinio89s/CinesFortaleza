package br.ufc.lesc.plinio.cinesfortaleza;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineIguatemi;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineViaSul;

public class ShowMovies extends Activity {

	private Cine mCine;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.show_movies_layout);
		mContext = this;

		setProgressBarIndeterminateVisibility(true);

		mCine = new CineIguatemi();

		String cineName = getIntent().getStringExtra(CinesFortaleza.EXTRA_CINE);

		if (cineName.equalsIgnoreCase(getResources().getString(
				R.string.iguatemi))) {
			mCine = new CineIguatemi();
		} else if (cineName.equalsIgnoreCase(getResources().getString(
				R.string.via_sul))) {
			mCine = new CineViaSul();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		setProgressBarIndeterminateVisibility(true);

		// get parent container
		LinearLayout l = (LinearLayout) findViewById(R.id.show_movies_parent_layout);

		// clear container
		l.removeAllViews();

		// put cine name
		TextView tvCine = new TextView(mContext);
		tvCine.setText(mCine.getName());
		tvCine.setTextSize(30);
		tvCine.setGravity(Gravity.CENTER_HORIZONTAL);
		tvCine.setTypeface(Typeface.DEFAULT_BOLD);
		l.addView(tvCine);

		if (mCine.getMovies() != null) {
			// put movie list
			for (int i = 0; i < mCine.getMovies().size(); i++) {
				TextView tv = new TextView(mContext);
				tv.setText(mCine.getMovies().get(i).getName());
				tv.setTextSize(20);
				l.addView(tv);
			}
		}

		new Refresher(this).execute("");
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	class Refresher extends AsyncTask<String, Integer, Integer> {

		Activity mParent;

		public Refresher(Activity parent) {
			mParent = parent;
		}

		@Override
		protected Integer doInBackground(String... params) {

			// refreshMoviesList
			return mCine.refreshMoviesList();
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (result != 0) {
				mParent.finish();
			}

			// get parent container
			LinearLayout l = (LinearLayout) findViewById(R.id.show_movies_parent_layout);

			// clear container
			l.removeAllViews();

			// put cine name
			TextView tvCine = new TextView(mContext);
			tvCine.setText(mCine.getName());
			tvCine.setTextSize(30);
			tvCine.setGravity(Gravity.CENTER_HORIZONTAL);
			tvCine.setTypeface(Typeface.DEFAULT_BOLD);
			l.addView(tvCine);

			if (mCine.getMovies() != null) {
				// put movie list
				for (int i = 0; i < mCine.getMovies().size(); i++) {
					TextView tv = new TextView(mContext);
					tv.setText(mCine.getMovies().get(i).getName());
					tv.setTextSize(20);
					l.addView(tv);
				}
			}

			setProgressBarIndeterminateVisibility(false);
		}

	}
}
