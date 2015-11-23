package com.horowitz.seaport.model;

import java.io.Serializable;

public class DispatchEntry implements Serializable {

	private static final long serialVersionUID = 3714349261562038406L;

	private String _ship;
	private String _dest;
	private int _times;
	private transient float _goal;

	public String getShip() {
		return _ship;
	}

	public void setShip(String ship) {
		_ship = ship;
	}

	public String getDest() {
		return _dest;
	}

	public void setDest(String dest) {
		_dest = dest;
	}

	public int getTimes() {
		return _times;
	}

	public void setTimes(int times) {
		_times = times;
	}

	public float getGoal() {
		return _goal;
	}

	public void setGoal(float goal) {
		_goal = goal;
	}

}
