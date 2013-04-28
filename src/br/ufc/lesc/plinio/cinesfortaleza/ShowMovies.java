package br.ufc.lesc.plinio.cinesfortaleza;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineIguatemi;

public class ShowMovies extends Activity {

	private Cine mCine;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_movies_layout);
		context = this;

		mCine = new CineIguatemi();

		int cine_num = getIntent().getIntExtra(CinesFortaleza.EXTRA_CINE, -1);

		switch (cine_num) {
		case 0:
			mCine = new CineIguatemi();
			break;
		default:
			mCine = new CineIguatemi();
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// get parent container
		LinearLayout l = (LinearLayout) findViewById(R.id.show_movies_parent_layout);

		// clear container
		l.removeAllViews();
		
		// put cine name
		TextView tvCine = new TextView(context);
		tvCine.setText(mCine.getName());
		tvCine.setTextSize(30);
		tvCine.setGravity(Gravity.CENTER_HORIZONTAL);
		tvCine.setTypeface(Typeface.DEFAULT_BOLD);
		l.addView(tvCine);

		// put movie list
		for (int i = 0; i < mCine.getMovies().size(); i++) {
			TextView tv = new TextView(context);
			tv.setText(mCine.getMovies().get(i).getName());
			tv.setTextSize(20);
			l.addView(tv);
		}
		
		new Refresher().execute("");
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	class Refresher extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... params) {

			// refreshMoviesList
			mCine.refreshMoviesList();

			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			// get parent container
			LinearLayout l = (LinearLayout) findViewById(R.id.show_movies_parent_layout);

			// clear container
			l.removeAllViews();
			
			// put cine name
			TextView tvCine = new TextView(context);
			tvCine.setText(mCine.getName());
			tvCine.setTextSize(30);
			tvCine.setGravity(Gravity.CENTER_HORIZONTAL);
			tvCine.setTypeface(Typeface.DEFAULT_BOLD);
			l.addView(tvCine);

			// put movie list
			for (int i = 0; i < mCine.getMovies().size(); i++) {
				TextView tv = new TextView(context);
				tv.setText(mCine.getMovies().get(i).getName());
				tv.setTextSize(20);
				l.addView(tv);
			}
			
			
		}

	}
}
