package br.ufc.lesc.plinio.cinesfortaleza;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CinesFortaleza extends Activity {

	public static final String EXTRA_CINE = "CINE_NUMBER";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cines_fortaleza);
	}
	
	public void allClick(View v){
		Intent i = new Intent(this, ShowMovies.class);
		startActivity(i);
	}
	
	public void cineClick(View v){
		Intent i = new Intent(this, ShowMovies.class);
		i.putExtra(EXTRA_CINE, 0);
		startActivity(i);
	}
}
