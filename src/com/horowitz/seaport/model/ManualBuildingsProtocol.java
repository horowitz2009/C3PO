package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.BuildingManager;

public class ManualBuildingsProtocol extends AbstractGameProtocol {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;
	private MouseRobot _mouse;

	private BuildingManager _buildingManager;
	private Map<String, Long> _lastTimes;

	public ManualBuildingsProtocol(ScreenScanner scanner, MouseRobot mouse, BuildingManager buildinghManager)
	    throws IOException {
		_scanner = scanner;
		_mouse = mouse;
		_buildingManager = buildinghManager;
		_lastTimes = new Hashtable<>(4);
		// _buildingManager.loadBuildings();
	}

	@Override
	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException {
		return _scanner.ensureHome();
	}
	
	@Override
	public void reset() {
		super.reset();
		_lastTimes.clear();
	}

	@Override
	public void update() {
		try {
			Pixel rock = _scanner.getRock();
			List<Building> buildings = _buildingManager.getBuildings();

			for (Building b : buildings) {
				Pixel absPos = new Pixel(rock.x + b.getRelativePosition().x, rock.y + b.getRelativePosition().y);
				b.setPosition(absPos);
			}
		} catch (IOException e) {
			LOGGER.warning("Failed to load buildings");
		}

	}

	@Override
	public void execute() throws RobotInterruptedException {
		try {
			List<Building> buildings = _buildingManager.getBuildings();
			for (Building b : buildings) {
				if (b.isEnabled() && isNotInterrupted()) {
					try {
						_mouse.checkUserMovement();
						doBuilding(b);

					} catch (IOException e) {
						e.printStackTrace();
					} catch (AWTException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			LOGGER.warning("Failed to load buildings");
		}
	}

	private void doBuilding(Building b) throws IOException, AWTException, RobotInterruptedException {
		Long lastTime = _lastTimes.get(b.getName());
		if (lastTime == null || System.currentTimeMillis() - lastTime > 5 * 60000) {

			Pixel p1 = b.getPosition();
			// Rectangle miniArea = new Rectangle(p1.x - 28, p1.y - 22, 28 * 2, 22 * 2);
			Rectangle miniArea = new Rectangle(p1.x - 40, p1.y - 12, 80, 10);
			Pixel p = _scanner.scanOneFast("buildings/blueBar.bmp", miniArea, false);
			// if (p == null)
			// p = _scanner.scanOneFast("buildings/whiteBar.bmp", miniArea, false);
			if (p != null) {
				LOGGER.info(b.getName() + " busy! Moving on...");
			} else {
				_mouse.click(p1);
				_mouse.delay(800);
				_mouse.mouseMove(_scanner.getParkingPoint());

				// check if popup is opened, else click again
				Rectangle area = new Rectangle(p1.x - 40 + 18, p1.y + 59, 50, 50);
				Pixel gears = _scanner.scanOneFast("buildings/produce_" + b.getMaterial() + ".bmp", area, true);
				if (gears == null) {
					_mouse.delay(100);
					_mouse.mouseMove(_scanner.getParkingPoint());
					_mouse.delay(800);
					_mouse.click(p1);
					_mouse.delay(800);
					gears = _scanner.scanOneFast("buildings/produce_" + b.getMaterial() + ".bmp", area, true);
				}

				// if (gears == null) {
				// LOGGER.info("click again...");
				// _mouse.click(p1);
				// _mouse.delay(800);
				// _mouse.mouseMove(_scanner.getParkingPoint());
				// gears = _scanner.scanOneFast("buildings/gears2.bmp", area, true);
				// }
				if (gears != null) {
					LOGGER.info("GEARS...");
					_mouse.mouseMove(_scanner.getParkingPoint());
					_mouse.delay(1000);

					Pixel produceButton = _scanner.scanOneFast("buildings/produce.png", null, true);
					if (produceButton == null)
						produceButton = _scanner.scanOneFast("buildings/produce2.bmp", null, true);
					if (produceButton != null) {
						_mouse.delay(2000);

						LOGGER.info(b.getName() + " started production");
						_lastTimes.put(b.getName(), System.currentTimeMillis());

					} else {
						// try if gray
						produceButton = _scanner.scanOneFast("buildings/produceGray.bmp", null, true);
						if (produceButton != null) {
							LOGGER.info("Production not possible...");
							_scanner.scanOneFast("buildings/x.bmp", null, true);
							_mouse.delay(1000);
						} else {
							LOGGER.info(b.getName() + " not ready...");
							_mouse.delay(300);
							_scanner.scanOneFast("buildings/x.bmp", null, true);

							_mouse.delay(300);
							_mouse.click(_scanner.getSafePoint());
						}

					}

				}

				_mouse.delay(100);
				_mouse.click(_scanner.getSafePoint());
				_mouse.delay(400);
			}
		} else {
			LOGGER.info("building " + b.getName() + " skipped");
		}
	}
}
