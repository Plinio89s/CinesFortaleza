package br.ufc.lesc.plinio.cinesfortaleza;

import java.util.Vector;

import android.text.format.Time;

public class MovieData {

	private String mName;
	private Vector<Time> mSessions;
	
	/* Constructors methods */
	
	public MovieData() {
		mName = "";
		mSessions = new Vector<Time>();
		mSessions.clear();
	}

	public MovieData(String name) {
		mName = name;
		mSessions = new Vector<Time>();
		mSessions.clear();
	}

	public MovieData(String name, Vector<Time> sessions) {
		mName = name;
		mSessions = sessions;
	}
	
	/* Accessors methods */	
	
	public String getName() {
		return mName;
	}

	public Vector<Time> getSessions() {
		return mSessions;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public void setSessions(Vector<Time> sessions) {
		this.mSessions = sessions;
	}
	
}
