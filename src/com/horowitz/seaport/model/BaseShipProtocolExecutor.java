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
import com.horowitz.commons.Settings;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;

public abstract class BaseShipProtocolExecutor implements GameProtocol {

	protected final static Logger LOGGER = Logger.getLogger("MAIN");

	private List<Pixel> _shipLocations;
	protected ScreenScanner _scanner;
	protected MouseRobot _mouse;
	protected MapManager _mapManager;
	protected LinkedList<Destination> _destChain;
	private boolean _shipwreckAvailable;
	private int _shipLocationDelay = 800;
	private int _shipLocationDelaySlow = 1200;

	private PropertyChangeSupport _support;
	protected Ship _lastShip;

	protected Settings _settings;

	public BaseShipProtocolExecutor() {
		super();
	}

	public BaseShipProtocolExecutor(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager, Settings settings) throws IOException {
		_scanner = scanner;
		_mouse = mouse;
		_mapManager = mapManager;
		_settings = settings;
		_support = new PropertyChangeSupport(this);
		_destChain = new LinkedList<Destination>();
	}

	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException {

		// check are there map notifications
		Pixel p = _scanner.scanOne("dest/mapNotification.bmp", null, false);
		_shipwreckAvailable = p != null;

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
		_shipLocationDelay = _settings.getInt("shipProtocol.shipLocationDelay", 800);
		_shipLocationDelaySlow = _settings.getInt("shipProtocol.shipLocationDelay.slow", 1200);
	}

	public void execute() throws RobotInterruptedException {
		if (_shipLocations != null && !_shipLocations.isEmpty()) {
			for (Pixel pixel : _shipLocations) {
				try {
					_mouse.checkUserMovement();
					_mouse.click(pixel);
					_mouse.delay(_shipLocationDelay);
					if (_mouse.getMode() == MouseRobot.SLOW)
						_mouse.delay(_shipLocationDelaySlow);

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

	protected boolean manageContractCases(Destination dest) throws IOException, AWTException, RobotInterruptedException {
		Rectangle buttonArea = new Rectangle(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2 - 60,
		    _scanner.getBottomRight().y - 240, 270, 240);
		Pixel destButton = null;
//		if (dest.getAbbr().equalsIgnoreCase("F")) {
//			destButton = _scanner.scanOneFast("dest/collect_friend.bmp", buttonArea, false);
//		} else if (dest.isContract()) {
			
			destButton = _scanner.scanContractButton("dest/collect_contract_new.bmp", buttonArea);
//			if (destButton == null) {
//			  destButton = _scanner.scanOneFast("dest/collect_contract.bmp", buttonArea, false);
//			}
//		}
		if (destButton != null) {
			LOGGER.info("CONTRACT COMPLETED. MOVING ON...");
			_mouse.click(destButton);
			_mouse.delay(1000);
			return true;
		}
		return false;
	}

	protected boolean sendShip(LinkedList<Destination> chain) throws AWTException, RobotInterruptedException, IOException {
		LOGGER.info("CHAIN: " + chain);
		_mouse.checkUserMovement();
		Destination dest = chain.poll();
		if (dest != null) {
			boolean good = true;

			Pixel smallTownPos = _mapManager.getSmallTownPos();
			if (smallTownPos == null) {
				_mapManager.ensureMap();
				smallTownPos = _mapManager.getSmallTownPos();
			}

			int x = smallTownPos.x + dest.getRelativePosition().x;
			int y = smallTownPos.y + dest.getRelativePosition().y;
			// what if dest is shipwreck
			boolean shipwreck = false;
			if (dest.getAbbr().equalsIgnoreCase("SW")) {
				good = false;
				if (_shipwreckAvailable) {
					LOGGER.info("LOOKING for shipwreck...");
					// locate the shipwreck
					Pixel p = _scanner.scanOne("dest/shipwreck.bmp", null, false);
					if (p != null) {
						LOGGER.info("FOUND IT!");
						shipwreck = true;
						good = true;
						x = p.x;
						y = p.y;
					} else {
						LOGGER.info("CAN'T FIND IT!");
					}
				} else {
					LOGGER.info("No shipwreck available right now. Moving on...");
				}
			}

			if (good) {

				_mouse.click(x, y);
				_mouse.delay(750);
				if (_mouse.getMode() == MouseRobot.SLOW)
					_mouse.delay(500);

				// assume the dialog is open
				if (manageContractCases(dest) && dest.getAbbr().equalsIgnoreCase("F")) {
					//if friend and collected, need to click again
					_mouse.click(x, y);
					_mouse.delay(750);
					if (_mouse.getMode() == MouseRobot.SLOW)
						_mouse.delay(500);
				}

				// manage market
				if (dest.getName().startsWith("Market")) {
					Pixel marketTitle = _scanner.scanOne("dest/MarketTownTitle2.bmp", null, false);
					if (marketTitle != null) {
						//FIXME the hardcoded approach
						String[] ss = dest.getOption().split("-");
						String commodity = ss[0];
						String prize = ss[1];
						
						if (commodity.equalsIgnoreCase("hat")||commodity.equalsIgnoreCase("1")) {
							_mouse.mouseMove(marketTitle.x - 182, marketTitle.y + 171);
							for (int i = 0; i < 13; i++) {
								_mouse.wheelDown(-2);// scroll up to the first commodity which is hat
								_mouse.delay(150);
							}

							_mouse.delay(33);
							_mouse.click();
							_mouse.delay(300);
							if (_mouse.getMode() == MouseRobot.SLOW)
								_mouse.delay(600);


						} else if (commodity.equalsIgnoreCase("cocoa")) {
							_mouse.mouseMove(marketTitle.x - 182, marketTitle.y + 171);
							for (int i = 0; i < 13; i++) {
								_mouse.wheelDown(2);// scroll down to the last commodity, then click second last, which is cocoa
								_mouse.delay(150);
							}
							_mouse.delay(33);
							_mouse.click(marketTitle.x - 182, marketTitle.y + 266);
							_mouse.delay(300);
							if (_mouse.getMode() == MouseRobot.SLOW)
								_mouse.delay(600);


						} else if (commodity.equalsIgnoreCase("cannon")||commodity.equalsIgnoreCase("2")) {
							_mouse.mouseMove(marketTitle.x - 182, marketTitle.y + 171);
							for (int i = 0; i < 13; i++) {
								_mouse.wheelDown(-2);// scroll up to the first commodity which is hat
								_mouse.delay(150);
							}

							_mouse.delay(33);
							_mouse.click(marketTitle.x - 182, marketTitle.y + 266);
							_mouse.delay(300);
							if (_mouse.getMode() == MouseRobot.SLOW)
								_mouse.delay(700);


						}

						if (prize.equalsIgnoreCase("XP") || prize.equalsIgnoreCase("1")) {
							_mouse.click(marketTitle.x + 312, marketTitle.y + 188);
							_mouse.delay(300);
						} else if (prize.equalsIgnoreCase("coins") || prize.equalsIgnoreCase("2")) {
							_mouse.click(marketTitle.x + 312, marketTitle.y + 266);
							_mouse.delay(300);
						}
						if (_mouse.getMode() == MouseRobot.SLOW)
							_mouse.delay(600);

					}

				}

				Rectangle buttonArea = new Rectangle(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2 - 50,
				    _scanner.getBottomRight().y - 175, 255, 90);
				int opt = 4;
				Pixel destButton = _scanner.scanOne("dest/setSail4.bmp", buttonArea, false);
				if (destButton == null) {
					opt = 0;
					destButton = _scanner.scanOne("dest/setSail.bmp", buttonArea, false);
				}
				if (destButton == null) {
					opt = 2;
					destButton = _scanner.scanOne("dest/setSail2.bmp", buttonArea, false);
				}
				if (destButton == null) {
					// check for got it button
					LOGGER.info("CHECK FOR BLUE GOT IT...");
					buttonArea = new Rectangle(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2 - 75,
					    _scanner.getBottomRight().y - 240, 205, 240);
					Pixel gotitButtonBlue = _scanner.scanOne("dest/gotitButton3.bmp", buttonArea, false);
					if (gotitButtonBlue == null) {
						gotitButtonBlue = _scanner.scanOne("dest/gotitButton2.bmp", buttonArea, false);
					}
					if (gotitButtonBlue != null) {
						_mouse.click(gotitButtonBlue);
						LOGGER.info("DESTINATION COMPLETED!");
						_mouse.delay(800);
						if (_mouse.getMode() == MouseRobot.SLOW)
							_mouse.delay(1000);

						return false;
					}
				}
				if (destButton != null) {
					LOGGER.info("set sail " + opt);
					// nice. we can continue
					// if (dest.getName().startsWith("Market")) {
					//
					// // FIXME
					// if ("Hat-XP".equalsIgnoreCase(dest.getOption())) {
					// // XP
					// Pixel phat = new Pixel(destButton.x - 222 - 27, destButton.y - 247 - 5);// xOff: 27, yOff: 5
					// _mouse.checkUserMovement();
					// _mouse.click(phat);
					// _mouse.delay(650);
					//
					// Pixel pxp = new Pixel(destButton.x + 282 - 27, destButton.y - 227 - 5);// xOff: 27, yOff: 5
					// _mouse.checkUserMovement();
					// _mouse.click(pxp);
					// _mouse.delay(650);
					// } else if ("Cocoa-XP".equalsIgnoreCase(dest.getOption())) {
					// // XP
					// Pixel pxp = new Pixel(destButton.x + 282 - 27, destButton.y - 227 - 5);// xOff: 27, yOff: 5
					// _mouse.checkUserMovement();
					// _mouse.click(pxp);
					// _mouse.delay(650);
					// } else if ("Cocoa-Coins".equalsIgnoreCase(dest.getOption())) {
					// // coins
					// Pixel coins = new Pixel(destButton.x + 282 - 27, destButton.y - 150 - 5);// xOff: 27, yOff: 5
					// _mouse.checkUserMovement();
					// _mouse.click(coins);
					// _mouse.delay(650);
					// }
					// }
					_mouse.checkUserMovement();
					_mouse.click(destButton);

					if (shipwreck) {
						// do screenshot
						_scanner.captureScreen("shipwreck ", true);
					}

					_support.firePropertyChange("SHIP_SENT", dest, _lastShip);
					_mouse.checkUserMovement();
					_mouse.delay(1300);
					if (_mouse.getMode() == MouseRobot.SLOW)
						_mouse.delay(1000);

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
			} else
				return sendShip(chain);
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