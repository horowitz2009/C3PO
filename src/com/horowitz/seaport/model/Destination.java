package com.horowitz.seaport.model;

import com.horowitz.commons.Pixel;

public class Destination extends GameUnit {
	private static final long serialVersionUID = -1237748489318461109L;
	private String _abbrs;
	private int _time;
	private String _option;
	private boolean _favorite;
	private Pixel _relativePosition;
	public transient int x;
	public transient int y;

	public Destination(String name, int time, String image, String imageTitle) {
		super(name, image, imageTitle);
		_time = time;
	}

	public Destination() {
		super();
	}

	public int getTime() {
		return _time;
	}

	public void setTime(int time) {
		_time = time;
	}

	public boolean isFavorite() {
		return _favorite;
	}

	public void setFavorite(boolean favorite) {
		_favorite = favorite;
	}

	public Pixel getRelativePosition() {
		return _relativePosition;
	}

	public void setRelativePosition(Pixel relativePosition) {
		_relativePosition = relativePosition;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getAbbrs() {
		return _abbrs;
	}

	public void setAbbrs(String abbrs) {
		_abbrs = abbrs;
	}
	
	public String getAbbr() {
		String res = getName().substring(0, 1);
		if (_abbrs != null) {
			String[] ss = _abbrs.split(",");
			res = ss[0];
		} 
		return res;
	}

	public String getOption() {
		return _option;
	}

	public void setOption(String option) {
		_option = option;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

}
