package br.ufc.lesc.plinio.cinesfortaleza;

import java.util.Vector;

import br.ufc.lesc.plinio.cinesfortaleza.cines.CineBenfica;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineIguatemi;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineViaSul;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CinesFortaleza extends Activity {

	public static final String EXTRA_CINE = "CINE_NAME";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cines_fortaleza);

		// create cine list
		Vector<String> cines = new Vector<String>();
		cines.add(new CineBenfica().getName());
		cines.add(new CineIguatemi().getName());
		cines.add(new CineViaSul().getName());
		cines.add(new CineBenfica().getName());
		cines.add(new CineIguatemi().getName());
		cines.add(new CineViaSul().getName());
		cines.add(new CineBenfica().getName());
		cines.add(new CineIguatemi().getName());
		cines.add(new CineViaSul().getName());
		cines.add(new CineBenfica().getName());
		cines.add(new CineIguatemi().getName());
		cines.add(new CineViaSul().getName());

		// create adapter
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, cines);

		// set ListView with cines and the click listener
		ListView lv = (ListView) findViewById(R.id.list_view_cines);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				cineClick(v);
			}
		});
	}

	public void cineClick(View v) {
		Intent i = new Intent(this, ShowMovies.class);
		i.putExtra(EXTRA_CINE, ((TextView) v).getText());
		startActivity(i);
	}
}
