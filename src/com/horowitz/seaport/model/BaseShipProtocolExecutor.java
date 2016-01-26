package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;

public abstract class BaseShipProtocolExecutor implements GameProtocol {

	protected final static Logger LOGGER = Logger.getLogger("MAIN");

	private List<Pixel> _shipLocations;
	protected ScreenScanner _scanner;
	protected MouseRobot _mouse;
	protected MapManager _mapManager;
	protected LinkedList<Destination> _destChain;

	private PropertyChangeSupport _support;
	protected Ship _lastShip;

	public BaseShipProtocolExecutor() {
		super();
	}

	public BaseShipProtocolExecutor(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager) throws IOException {
		_scanner = scanner;
		_mouse = mouse;
		_mapManager = mapManager;

		_support = new PropertyChangeSupport(this);
		_destChain = new LinkedList<Destination>();
	}

	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException {
		return _scanner.ensureHome();
	}

	public void update() {
		_shipLocations = new ArrayList<>();
		Pixel[] shipLocations = _scanner.getShipLocations();
		Pixel r = _scanner.getRock();
		if (r != null)
			for (Pixel p : shipLocations) {
				Pixel goodP = new Pixel(r.x + p.x, r.y + p.y);
				_shipLocations.add(goodP);
			}
	}

	public void execute() throws RobotInterruptedException {
		if (_shipLocations != null && !_shipLocations.isEmpty()) {
			for (Pixel pixel : _shipLocations) {
				try {
					_mouse.checkUserMovement();
					_mouse.click(pixel);
					_mouse.delay(750);

					Rectangle miniArea = new Rectangle(pixel.x - 15, pixel.y + 50, 44, 60);
					// _scanner.writeImage(miniArea, "pin.bmp");
					Pixel pin = _scanner.scanOneFast(_scanner.getImageData("pin.bmp"), miniArea, false);
					if (pin != null) {
						doShip(pin);

					}
				} catch (AWTException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

	}

	abstract void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException;

	protected Ship scanShipName(Pixel pin) throws AWTException, RobotInterruptedException {
		// scan the name
		Rectangle nameArea = new Rectangle(pin.x - 75, pin.y - 67, 150, 40);
		List<Ship> ships = _mapManager.getShips();
		_lastShip = null;
		for (Ship ship : ships) {
			if (ship.isActive()) {
				_mouse.checkUserMovement();
				if (_scanner.scanOne(ship.getImageDataTitle(), nameArea, false) != null) {
					LOGGER.info("SHIP: " + ship.getName());
					_lastShip = ship;
					break;
				}
				;
			}
		}
		if (_lastShip == null) {
			LOGGER.info("SHIP: " + "UNKNOWN!!!");
		}
		return _lastShip;
	}

	protected void manageContractCases() throws IOException, AWTException, RobotInterruptedException {
		Rectangle buttonArea = new Rectangle(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2 - 10,
		    _scanner.getBottomRight().y - 240, 205, 240);
		Pixel destButton = _scanner.scanPrecise("dest/collect.bmp", buttonArea);
		if (destButton != null) {
			LOGGER.info("MISSION COMPLETED. MOVING ON...");
			_mouse.click(destButton);
			_mouse.delay(1000);
		}
	}

	protected boolean sendShip(LinkedList<Destination> chain) throws AWTException, RobotInterruptedException, IOException {
		LOGGER.info("CHAIN: " + chain);
		_mouse.checkUserMovement();
		Destination dest = chain.poll();
		if (dest != null) {

			Pixel marketPos = _mapManager.getSmallTownPos();
			if (marketPos == null) {
				_mapManager.ensureMap();
				marketPos = _mapManager.getSmallTownPos();
			}
			Destination _market = _mapManager.getSmallTown();

			int x = marketPos.x + dest.getRelativePosition().x;
			int y = marketPos.y + dest.getRelativePosition().y;

			_mouse.click(x, y);
			_mouse.delay(1000);

			// assume the dialog is open
			manageContractCases();
			
			Rectangle buttonArea = new Rectangle(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2,
			    _scanner.getBottomRight().y - 240, 205, 240);
			Pixel destButton = _scanner.scanOne("dest/setSail.bmp", buttonArea, false);
			if (destButton == null)
				destButton = _scanner.scanOne("dest/setSail2.bmp", buttonArea, false);
			if (destButton != null) {
				// nice. we can continue
				if (dest.getName().startsWith("Market")) {
					if ("Cocoa-XP".equalsIgnoreCase(dest.getOption())) {
						// XP
					} else if ("Cocoa-Coins".equalsIgnoreCase(dest.getOption())) {
						// coins
						Pixel coins = new Pixel(destButton.x - 30 - 90, destButton.y - 6 - 195);// xOff: 30, yOff: 6
						_mouse.checkUserMovement();
						_mouse.click(coins);
						_mouse.delay(650);
					}
				}
				_mouse.checkUserMovement();
				_mouse.click(destButton);
				_support.firePropertyChange("SHIP_SENT", dest, _lastShip);
				_mouse.checkUserMovement();
				_mouse.delay(1500);
				return true;
			} else {
				LOGGER.info(dest.getName() + " can't be done!");
				boolean found = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
				// if (found)
				_mouse.checkUserMovement();
				_mouse.delay(1500);
				// chain.poll();
				if (chain.isEmpty()) {
					LOGGER.info("reached the end of chain");
					// TODO close the map and move on
					return false;
				} else
					return sendShip(chain);
			}

		}
		return false;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		_support.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		_support.addPropertyChangeListener(propertyName, listener);
	}

}