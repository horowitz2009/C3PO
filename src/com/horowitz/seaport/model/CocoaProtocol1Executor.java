package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.io.IOException;
import java.util.LinkedList;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;

public class CocoaProtocol1Executor extends BaseShipProtocolExecutor {

	private String _noCocoaShip;

	public CocoaProtocol1Executor(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager) throws IOException {
		super(scanner, mouse, mapManager);
		_noCocoaShip = "Mary Rose";

	}

	void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException {

		scanShipName(pin);

		_mouse.click(pin);
		_mouse.delay(50);
		_mouse.mouseMove(_scanner.getParkingPoint());
		_mouse.delay(500);

		Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
		if (anchor != null) {
			// MAP IS OPEN
			_mapManager.ensureMap();

			//load the chain
			_destChain.clear();
			if (_lastShip != null && _lastShip.getName().equals(_noCocoaShip)) {
				_destChain.add(_mapManager.getDestination("Coastline"));
			} else {
				_destChain.add(_mapManager.getSmallTown());// market
				_destChain.add(_mapManager.getDestination("Cocoa Plant"));
			}
			
			sendShip(new LinkedList<Destination>(_destChain));
		}

	}


}
