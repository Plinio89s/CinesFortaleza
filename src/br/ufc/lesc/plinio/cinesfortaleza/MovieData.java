package br.ufc.lesc.plinio.cinesfortaleza;

import java.util.Vector;

public class MovieData {

	private String mName;
	private Vector<String> mSessions;
	
	/* Constructors methods */
	
	public MovieData() {
		mName = "";
		mSessions = new Vector<String>();
		mSessions.clear();
	}

	public MovieData(String name) {
		mName = name;
		mSessions = new Vector<String>();
		mSessions.clear();
	}

	public MovieData(String name, Vector<String> sessions) {
		mName = name;
		mSessions = sessions;
	}
	
	/* Accessors methods */	
	
	public String getName() {
		return mName;
	}

	public Vector<String> getSessions() {
		return mSessions;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public void setSessions(Vector<String> sessions) {
		this.mSessions = sessions;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
