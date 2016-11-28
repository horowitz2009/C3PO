package com.horowitz.monitor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

import com.horowitz.commons.DateUtils;
import com.horowitz.commons.Settings;

public class GameHealthMonitor {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private Settings _settings;
	private boolean _stopIt;
	private PropertyChangeSupport _support;

	private long _lastTime;

	public GameHealthMonitor() {
		this(Settings.createSettings("seaport.properties"));
	}

	public GameHealthMonitor(Settings settings) {
		super();
		_settings = settings;
		_lastTime = System.currentTimeMillis();
		_stopIt = false;
		_support = new PropertyChangeSupport(this);
	}

	public void stopMonitoring() {
		_stopIt = true;
	}

	public void startMonitoring() {
		_stopIt = false;
		Thread t = new Thread(new Runnable() {
			public void run() {
				monitor();
			}
		}, "MONITOR");

		t.start();
	}

	protected void monitor() {
		while (!_stopIt) {
			try {
				Thread.sleep(_settings.getInt("monitor.sleepTime", 15) * 1000);
			} catch (InterruptedException e) {
			}

			long time = (System.currentTimeMillis() - _lastTime);
			long threshold = _settings.getInt("monitor.inactivityThreshold", 12) * 60000;
			LOGGER.info("Time since last activity: " + DateUtils.fancyTime2(time) + "  <  "
			    + DateUtils.fancyTime2(threshold));
			// LOGGER.info("time since no ship sent: " + ((now - _lastTime) / 60000) + " < " + minTime / 60000);
			if (time >= threshold) {
				// ALERT!!!!!
				_support.firePropertyChange("NO_ACTIVITY", null, true);
				_lastTime = System.currentTimeMillis();
			}
		}
	}

	public void pingActivity() {
		_lastTime = System.currentTimeMillis();
	}

	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		_support.addPropertyChangeListener(arg0);
	}

	public void addPropertyChangeListener(String arg0, PropertyChangeListener arg1) {
		_support.addPropertyChangeListener(arg0, arg1);
	}

	public void removePropertyChangeListener(PropertyChangeListener arg0) {
		_support.removePropertyChangeListener(arg0);
	}

	public void removePropertyChangeListener(String arg0, PropertyChangeListener arg1) {
		_support.removePropertyChangeListener(arg0, arg1);
	}

}
