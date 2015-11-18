package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;

public class CocoaProtocol2 extends ShipsProtocol {

	private List<String> _cocoaShips;
	private List<String> _otherShips;

	private String _sellingShip;

	public CocoaProtocol2(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager) throws IOException {
		super(scanner, mouse, mapManager);

		_cocoaShips = new ArrayList<>();
		_cocoaShips.add("Mary Rose");
		_cocoaShips.add("Berrio");
		_cocoaShips.add("Hulk Zigmund");
		_cocoaShips.add("Sao Miguel");
		// _cocoaShips.add("Peter Von Danzig");

		_otherShips = new ArrayList<>();
		_otherShips.add("Sao Rafael");

		_sellingShip = "Sao Gabriel";
	}

	private boolean isCocoaShip(Ship whatShip) {
		for (String name : _cocoaShips) {
			if (whatShip.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException {

		Ship whatShip = scanShipName(pin);

		_mouse.click(pin);
		_mouse.delay(50);
		_mouse.mouseMove(_scanner.getParkingPoint());
		_mouse.delay(500);

		Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
		if (anchor != null) {
			// MAP IS OPEN
			_mapManager.ensureMap();

			// load the chain
			_destChain.clear();
			// _destChain.add(_mapManager.getDestination("Cocoa Plant"));
			// _destChain.add(_mapManager.getMarket());
			// _destChain.add(_mapManager.getDestination("Gulf"));
			// _destChain.add(_mapManager.getDestination("Coastline"));
			// _destChain.add(_mapManager.getDestination("Small Town"));

			Destination dest = null;

			if (whatShip != null) {
				if (whatShip.getName().equals(_sellingShip)) {
					LOGGER.info("SELLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
					_destChain.add(_mapManager.getMarket());
					_destChain.add(_mapManager.getDestination("Coastline"));
					_destChain.add(_mapManager.getDestination("Small Town"));
				} else if (isCocoaShip(whatShip)) {
					LOGGER.info("COCOCOCOCOCOCCOCOCOCCOCOCOCOCOCOAAAAAAA");
					_destChain.add(_mapManager.getDestination("Cocoa Plant"));
					_destChain.add(_mapManager.getDestination("Coastline"));

				} else {
					// other ships goes to Gulf
					LOGGER.info("GULFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
					_destChain.add(_mapManager.getDestination("Gulf"));
					_destChain.add(_mapManager.getDestination("Coastline"));
				}
			} else {
				LOGGER.info("WARNING: This ship is uknown to me!");
				LOGGER.info("WARNING: Applying default chain: ");
				LOGGER.info("WARNING: Gulf->Coastline->Small Town ");

				_destChain.add(_mapManager.getDestination("Gulf"));
				_destChain.add(_mapManager.getDestination("Coastline"));
				_destChain.add(_mapManager.getDestination("Small Town"));
			}

			sendShip(new LinkedList<Destination>(_destChain));
		}

	}

	private LinkedList<Destination> generatePartialChain(Destination starter) {
		// A copy of original chain
		LinkedList<Destination> res = new LinkedList<Destination>(_destChain);
		boolean found = false;
		while (!res.isEmpty() && !found) {
			Destination head = res.peek();
			if (!head.getName().equals(starter.getName())) {
				res.removeFirst();
			} else {
				found = true;
			}
		}
		return res;
	}


}
