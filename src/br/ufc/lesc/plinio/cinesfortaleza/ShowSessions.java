package br.ufc.lesc.plinio.cinesfortaleza;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShowSessions extends Activity {

	private ListView mListView;
	private String mMovieName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.show_movies_layout);

		// initialize member attributes
		mListView = (ListView) findViewById(R.id.list_view_movies);

		// decode movie name
		mMovieName = getIntent().getStringExtra(ShowMovies.EXTRA_MOVIE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		((TextView) findViewById(R.id.title_cine)).setText(mMovieName);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getIntent()
						.getStringArrayListExtra(ShowMovies.EXTRA_SESSIONS));

		mListView.setAdapter(adapter);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
