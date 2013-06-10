package br.ufc.lesc.plinio.cinesfortaleza;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;
import br.ufc.lesc.plinio.cinesfortaleza.cines.CineVM;

public class StoreData {

	public static final String CINE_FILE = "cines";
	public static final String KEY_LAST_REFRESH = "LAST_REFRESH";
	public static final String KEY_CINE_NAME = "cine_name_";
	public static final String KEY_CINE_CODE = "cine_code_";
	public static final String KEY_MOVIE_NAME = "movie_name_";
	public static final String KEY_SESSION = "session_";
	public static final long REFRESH_TIME_MILISEC = (1000L * 60L * 60L * 4L);

	private Vector<Cine> mCines;
	private Context mContext;

	public StoreData(Context context) {
		mContext = context;
		mCines = new Vector<Cine>();

		loadCines();
	}

	public Vector<Cine> getCines() {
		return mCines;
	}

	public void setCines(Vector<Cine> cines) {
		this.mCines = cines;
	}

	public long getLastRefreshMili() {
		SharedPreferences storedData = mContext.getSharedPreferences(CINE_FILE,
				Context.MODE_PRIVATE);
		if (storedData.contains(KEY_LAST_REFRESH)) {
			return storedData.getLong(KEY_LAST_REFRESH, 0);
		} else {
			return 0;
		}
	}

	public long getLastRefreshCineMili(Cine cine) {
		SharedPreferences storedData = mContext.getSharedPreferences(
				cine.fileName(), Context.MODE_PRIVATE);
		if (storedData.contains(KEY_LAST_REFRESH)) {
			return storedData.getLong(KEY_LAST_REFRESH, 0);
		} else {
			return 0;
		}
	}

	public void setLastRefreshMili(long lastRefreshMili) {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(
				CINE_FILE, Context.MODE_PRIVATE).edit();
		editor.putLong(KEY_LAST_REFRESH, lastRefreshMili);
		editor.commit();
	}

	public boolean refreshNeeded() {
		long currentTime = System.currentTimeMillis();
		long lastRefreshTime = getLastRefreshMili();

		if (Time.getJulianDay(currentTime, 0) == Time.getJulianDay(
				lastRefreshTime, 0)) {
			return (currentTime - lastRefreshTime) > REFRESH_TIME_MILISEC;
		} else {
			return true;
		}

	}

	public boolean refreshNeededCine(Cine cine) {
		long currentTime = System.currentTimeMillis();
		long lastRefreshTime = getLastRefreshCineMili(cine);

		if (Time.getJulianDay(currentTime, 0) == Time.getJulianDay(
				lastRefreshTime, 0)) {
			return (currentTime - lastRefreshTime) > REFRESH_TIME_MILISEC;
		} else {
			return true;
		}

	}

	public void loadCines() {
		// load last stored data
		SharedPreferences storedData = mContext.getSharedPreferences(CINE_FILE,
				Context.MODE_PRIVATE);

		String key;
		String name;
		String code;
		for (int i = 0;; i++) {
			key = KEY_CINE_NAME + i;
			if (storedData.contains(key)) {
				name = storedData.getString(key, "");
				key = KEY_CINE_CODE + i;
				if (storedData.contains(key)) {
					code = storedData.getString(key, "");
					mCines.add(new CineVM(name, code));
				}
			} else {
				break;
			}
		}
	}

	public void saveCines() {

		// get store data manager
		SharedPreferences.Editor editor = mContext.getSharedPreferences(
				CINE_FILE, Context.MODE_PRIVATE).edit();

		// clear saved data
		editor.clear();
		editor.commit();

		// store
		String key, value;
		for (int i = 0; i < mCines.size(); i++) {
			key = KEY_CINE_NAME + i;
			value = mCines.get(i).getName();
			editor.putString(key, value);
			key = KEY_CINE_CODE + i;
			value = mCines.get(i).getURL();
			value = value.substring(value.lastIndexOf('=') + 1);
			editor.putString(key, value);
		}
		editor.putLong(KEY_LAST_REFRESH, System.currentTimeMillis());
		editor.commit();
	}

	public void loadMovies(Cine cine) {
		Vector<MovieData> movies = new Vector<MovieData>();

		// load last stored data
		SharedPreferences storedData = mContext.getSharedPreferences(
				cine.fileName(), Context.MODE_PRIVATE);

		String key;
		String name;
		Vector<String> sessions;
		for (int i = 0;; i++) {
			key = KEY_MOVIE_NAME + i;
			if (storedData.contains(key)) {
				name = storedData.getString(key, "");
				sessions = new Vector<String>();
				for (int j = 0;; j++) {
					key = KEY_SESSION + i + "_" + j;
					if (storedData.contains(key)) {
						sessions.add(storedData.getString(key, ""));
					} else {
						break;
					}
				}
				movies.add(new MovieData(name, sessions));
			} else {
				break;
			}
		}
		cine.setMovies(movies);

		Log.d(CinesFortaleza.TAG_DEBUG, "load");
		printPref(cine.fileName());
	}

	public void saveMovies(Cine cine) {
		// get store data manager
		SharedPreferences.Editor editor = mContext.getSharedPreferences(
				cine.fileName(), Context.MODE_PRIVATE).edit();

		// store
		String key, value;
		MovieData movie;
		for (int i = 0; i < cine.getMovies().size(); i++) {
			movie = cine.getMovies().get(i);
			key = KEY_MOVIE_NAME + i;
			value = movie.getName();
			editor.putString(key, value);
			for (int j = 0; j < movie.getSessions().size(); j++) {
				key = KEY_SESSION + i + "_" + j;
				value = movie.getSessions().get(j);
				editor.putString(key, value);
			}
		}
		editor.putLong(KEY_LAST_REFRESH, System.currentTimeMillis());
		editor.commit();

		Log.d(CinesFortaleza.TAG_DEBUG, "save");
		printPref(cine.fileName());
	}

	// private void clearSharedPrefs() {
	// deleteFileOrDir(new File(mContext.getFilesDir().getParent()
	// + "/shared_prefs"));
	// }

	protected void deleteFileOrDir(File file) {
		if (file.isDirectory()) {
			File[] inFiles = file.listFiles();
			for (File inFile : inFiles) {
				deleteFileOrDir(inFile);
			}
		} else {
			file.delete();
		}
	}

	private void printPref(String file) {
		Log.i(CinesFortaleza.TAG_DEBUG, "Pref File: " + file);
		SharedPreferences storedData = mContext.getSharedPreferences(file,
				Context.MODE_PRIVATE);
		Map<String, ?> map = storedData.getAll();
		Set<String> keys = map.keySet();
		for (String key : keys) {
			Log.i(CinesFortaleza.TAG_DEBUG, key + "=" + map.get(key));
		}
	}
}
