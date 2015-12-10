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
					_mouse.click(pixel);
					_mouse.delay(750);

					Rectangle miniArea = new Rectangle(pixel.x - 15, pixel.y + 50, 44, 60);
					//_scanner.writeImage(miniArea, "pin.bmp");
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

	protected boolean sendShip(LinkedList<Destination> chain) throws AWTException, RobotInterruptedException, IOException {
		LOGGER.info("CHAIN: " + chain);
		Destination dest = chain.poll();
		if (dest != null) {

			Pixel marketPos = _mapManager.getMarketPos();
			if (marketPos == null)
				marketPos = _mapManager.ensureMap();
			Destination _market = _mapManager.getMarket();

			Pixel destP = marketPos;
			if (!dest.getName().startsWith("Market")) {
				int x = marketPos.x + dest.getRelativePosition().x - _market.getImageData().get_xOff() - 35;
				int y = marketPos.y + dest.getRelativePosition().y - _market.getImageData().get_yOff() - 35;
				Rectangle destArea = new Rectangle(x, y, 153 + 20 + 40, 25 + 40);
				Pixel pp = _scanner.ensureAreaInGame(destArea);
				if (pp.x != 0 || pp.y != 0) {
					marketPos = _mapManager.ensureMap();
					x = marketPos.x + dest.getRelativePosition().x - _market.getImageData().get_xOff() - 35;
					y = marketPos.y + dest.getRelativePosition().y - _market.getImageData().get_yOff() - 35;
					destArea = new Rectangle(x, y, 153 + 20 + 40, 25 + 40);
				}
				LOGGER.fine("Using custom area for " + dest.getImage());
				destP = _scanner.scanOneFast(dest.getImageData(), destArea, false);
			}

			if (destP != null) {
				LOGGER.info("Sending to " + dest.getName() + "...");
				_mouse.click(destP);
				_mouse.mouseMove(_scanner.getParkingPoint());
				_mouse.delay(800);

				Pixel destTitle = _scanner.scanOneFast(dest.getImageDataTitle(), null, false);

				if (destTitle != null) {
					LOGGER.fine("SEND POPUP OPEN...");
					LOGGER.info("OPTION: " + dest.getOption());
					_mouse.mouseMove(_scanner.getParkingPoint());
					_mouse.delay(300);

					Pixel destButton = _scanner.scanOneFast("dest/setSail.bmp", null, false);
					if (destButton != null) {
						// nice. we can continue
						if (dest.getName().startsWith("Market")) {
							if ("Cocoa-XP".equalsIgnoreCase(dest.getOption())) {
								//XP
							} else if ("Cocoa-Coins".equalsIgnoreCase(dest.getOption())) {
								//coins
								Pixel coins = new Pixel(destTitle);
								coins.y += 228;
								_mouse.click(coins);
								_mouse.delay(650);
							}
						}

						_mouse.click(destButton);
						_support.firePropertyChange("SHIP_SENT", dest, _lastShip);
						_mouse.delay(1500);
						return true;
					} else {
						LOGGER.info(dest.getName() + " can't be done!");
						boolean found = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
						// if (found)
						_mouse.delay(1500);
						// chain.poll();
						if (chain.isEmpty()) {
							LOGGER.info("reached the end of chain");
							//TODO close the map and move on
							return false;
						}
						else
							return sendShip(chain);
					}

				}
			} else {
				LOGGER.info("========");
				LOGGER.info("Can't locate destination: " + dest.getName());
				LOGGER.info("========");
				//TODO close the map and move on
				return false;
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