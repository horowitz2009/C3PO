package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.io.IOException;
import java.util.LinkedList;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Settings;
import com.horowitz.seaport.GameErrorException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;

public class ManualShipProtocolExecutor extends BaseShipProtocolExecutor {

	private Destination _dest;

	public ManualShipProtocolExecutor(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager, Settings settings) throws IOException {
		super(scanner, mouse, mapManager, settings);
	}

	void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException, GameErrorException {
		if (_dest != null) {

			scanShipName(pin);

			_mouse.click(pin);
			_mouse.delay(50);
			_mouse.mouseMove(_scanner.getParkingPoint());
			_mouse.delay(500);

			Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
			if (anchor != null) {
				// MAP IS OPEN
				_mapManager.ensureMap();

				_destChain.clear();
				_destChain.add(_dest);

				sendShip(new LinkedList<Destination>(_destChain));
			}
		}
	}

	public Destination getDestination() {
		return _dest;
	}

	public void setDestination(Destination dest) {
		_dest = dest;
	}

}
