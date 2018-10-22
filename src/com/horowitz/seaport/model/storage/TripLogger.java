package com.horowitz.seaport.model.storage;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.DispatchEntry;
import com.horowitz.seaport.model.Ship;

public class TripLogger {
	private String _folder = "data";
	private String _filename = "tripLog.txt";
	private String _timePattern = "dd-MM HH:mm";
	private int _limit = 1000;

	public File getFile() throws IOException {
		File d = new File(_folder);
		if (!d.exists()) {
			d.mkdirs();
		}

		File f = new File(d, _filename);
		if (!f.exists())
			f.createNewFile();
		return f;
	}
	
	

	public TripLogger(String filename) {
		super();
		_filename = filename;
	}
	
	public TripLogger() {
	}



	public void log(Ship ship, Destination dest) throws IOException {
		File f = getFile();
		List<String> lines = FileUtils.readLines(f);
		List<String> newLines = new LinkedList<String>();
		int i = 0;
		for (Iterator<String> it = lines.iterator(); it.hasNext() && i < _limit; i++) {
			String line = it.next();
			newLines.add(line);
		}
		String newLog = DateFormatUtils.format(System.currentTimeMillis(), _timePattern) + " " + dest.getAbbr() + " "
		    + ship.getName();
		newLines.add(0, newLog);

		FileUtils.writeLines(f, newLines);
	}
	
	public void log2(Ship ship, Destination dest) throws IOException {
		File f = getFile();
		String newLog = DateFormatUtils.format(System.currentTimeMillis(), _timePattern) + " " + dest.getAbbr() + " "
				+ ship.getName();
		FileUtils.writeStringToFile(f, newLog + System.lineSeparator(), true);
	}

	public void log3(DispatchEntry de) throws IOException {
		File f = getFile();
		String newLog = de.getLastTime() + "," + de.getDest() + "," + de.getShip();
		FileUtils.writeStringToFile(f, newLog + System.lineSeparator(), true);
	}
}
