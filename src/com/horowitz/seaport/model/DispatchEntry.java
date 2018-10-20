package com.horowitz.seaport.model;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;

import com.horowitz.commons.DateUtils;
import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;

public class DispatchEntry implements Serializable, Deserializable {

	private static final long serialVersionUID = 3714349261562038406L;

	private String _ship;
	private String _dest;
	private int _times;
	private transient float _goal;
	private transient float _coef;
	private transient long _time;
	private String _lastTime;
	private transient Ship shipObj;
	private transient Destination destObj;

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

	public float getCoef() {
		return _coef;
	}

	public void setCoef(float coef) {
		_coef = coef;
	}

	public void setTime(long time) {
		_time = time;
		_lastTime = DateUtils.formatDateToISO(time);
	}

	public long getTime() {
		return _time;
	}

	public String getLastTime() {
		return _lastTime;
	}

	public void setLastTime(String lastTime) {
		_lastTime = lastTime;
		try {
			_time = DateUtils.parseFromISO(lastTime).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		int cap = 0;
		if (shipObj != null)
			cap = shipObj.getCapacity();
		return cap + " DE [" + _ship + ", dest=" + _dest + ", lastTime=" + _lastTime + ". Will arrive at: "
		    + DateUtils.formatDateToISO(willArriveAt()) + "]";
	}

	@Override
	public void deserialize(Deserializer deserializer) throws IOException {
		deserializer.deserialize(this);
	}

	public Ship getShipObj() {
		return shipObj;
	}

	public void setShipObj(Ship shipObj) {
		this.shipObj = shipObj;
	}

	public Destination getDestObj() {
		return destObj;
	}

	public void setDestObj(Destination destObj) {
		this.destObj = destObj;
	}

	public long willArriveAt() {
		int t;
		if (destObj != null) {
			t = destObj.getTime() * 60000;
		} else {
			t = 120 * 60000;
		}
		long newTime = _time + t;
		return newTime;
	}

	public DispatchEntry copy() {
		DispatchEntry copy = new DispatchEntry();
		copy._ship = _ship;
		copy._dest = _dest;
		copy._times = _times;
		copy._goal = _goal;
		copy._coef = _coef;
		copy._time = _time;
		copy._lastTime = _lastTime;
		copy.shipObj = shipObj;
		copy.destObj = destObj;
		
		return copy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_dest == null) ? 0 : _dest.hashCode());
		result = prime * result + ((_ship == null) ? 0 : _ship.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DispatchEntry other = (DispatchEntry) obj;
		if (_dest == null) {
			if (other._dest != null)
				return false;
		} else if (!_dest.equals(other._dest))
			return false;
		if (_ship == null) {
			if (other._ship != null)
				return false;
		} else if (!_ship.equals(other._ship))
			return false;
		return true;
	}

	
}
