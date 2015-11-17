package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;

public class CocoaProtocol1 extends ShipsProtocol {

	private String _noCocoaShip;

	public CocoaProtocol1(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager) throws IOException {
		super(scanner, mouse, mapManager);
		_noCocoaShip = "Mary Rose";

	}

	void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException {

		scanShipName(pin);

		Destination dest = _mapManager.getMarket();
		Destination dest2 = _mapManager.getDestination("Cocoa Plant");
		// FIXME dest.setNext(dest2);

		// scan the name
		Rectangle nameArea = new Rectangle(pin.x - 75, pin.y - 67, 150, 40);
		Ship ship = _mapManager.getShip(_noCocoaShip);
		if (_scanner.scanOne(ship.getImageDataTitle(), nameArea, false) != null) {
			dest = _mapManager.getDestination("Coastline");
			_destChain.clear();
			_destChain.add(dest);
			
			
			_mouse.click(pin);
			_mouse.delay(50);
			_mouse.mouseMove(_scanner.getParkingPoint());
			_mouse.delay(500);
			
			
			System.out.println(_destChain);
			sendShip(_destChain);
		} else {
			_mouse.click(pin);
			_mouse.delay(50);
			_mouse.mouseMove(_scanner.getParkingPoint());
			_mouse.delay(500);

			Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
			if (anchor != null) {
				// MAP IS OPEN
				_mapManager.ensureMap();

				_destChain.clear();
				_destChain.add(_mapManager.getMarket());// market
				_destChain.add(_mapManager.getDestination("Cocoa Plant"));// if market
				                                                          // empty

				sendShip(_destChain);
			}
		}

	}

	@Override
	public void update() {
		super.update();
		_destChain.clear();
	}
}
