package com.horowitz.monitor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import com.horowitz.commons.DateUtils;
import com.horowitz.commons.Settings;

public class GameHealthMonitor {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private Settings _settings;
	private boolean _stopIt;
	private PropertyChangeSupport _support;

	private Runnable _runnable;
	private long _lastTime;

	public GameHealthMonitor() {
		this(Settings.createSettings("seaport.properties"));
	}

	public GameHealthMonitor(Settings settings) {
		super();
		_settings = settings;
		_stopIt = false;
		_support = new PropertyChangeSupport(this);
	}

	public void stopMonitoring() {
		_stopIt = true;
	}

	private boolean isRunning(String threadName) {
		boolean isRunning = false;
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Iterator<Thread> it = threadSet.iterator(); it.hasNext();) {
			Thread thread = it.next();
			if (thread.getName().equals(threadName)) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	public void startMonitoring() {
		if (!isRunning()) {
			_stopIt = false;
			_lastTime = System.currentTimeMillis();

			Thread t = new Thread(new Runnable() {
				public void run() {
					monitor();
				}
			}, "MONITOR");

			t.start();
		}
	}

	protected void monitor() {
		while (!_stopIt) {
			try {
				int secs = _settings.getInt("monitor.sleepTime", 15);
				for (int i = 0; i < secs && !_stopIt; i++)
					Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			long time = (System.currentTimeMillis() - _lastTime);
			long threshold = _settings.getInt("monitor.inactivityThreshold", 12) * 60000;
			LOGGER
			    .info("Time since last activity: " + DateUtils.fancyTime2(time) + "  <  " + DateUtils.fancyTime2(threshold));
			// LOGGER.info("time since no ship sent: " + ((now - _lastTime) / 60000) + " < " + minTime / 60000);
			checkHealth();
			if (time >= threshold) {
				// ALERT!!!!!
				triggerAlert();
			}
		}
		LOGGER.info("Monitor stopped!");
	}

	public void pingActivity() {
		_lastTime = System.currentTimeMillis();
	}
	
	public void checkHealth() {
		new Thread(_runnable).start();
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

	public boolean isRunning() {
		return !_stopIt && isRunning("MONITOR");
	}

	public Runnable getRunnable() {
		return _runnable;
	}

	public void setRunnable(Runnable runnable) {
		_runnable = runnable;
	}

	public void triggerAlert() {
		_support.firePropertyChange("NO_ACTIVITY", null, true);
		_lastTime = System.currentTimeMillis();
  }

}
