package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;

public class FishingProtocol implements GameProtocol {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private List<Pixel> _fishes;

	private ScreenScanner _scanner;
	private MouseRobot _mouse;

	public FishingProtocol(ScreenScanner scanner, MouseRobot mouse) {
		_scanner = scanner;
		_mouse = mouse;
	}

	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException {
		return _scanner.ensureHome();
	}

	@Override
	public void update() {
		_fishes = new ArrayList<Pixel>();
		Pixel rock = _scanner.getRock();
		if (rock != null) {
			Pixel[] fishes = _scanner.getFishes();
			for (Pixel fish : fishes) {
				Pixel goodFish = new Pixel(rock.x + fish.x, rock.y + fish.y);
				_fishes.add(goodFish);
			}
		}
	}

	@Override
	public void execute() throws RobotInterruptedException {
		if (_fishes != null && !_fishes.isEmpty()) {
			for (Pixel pixel : _fishes) {

				_mouse.click(pixel);
				_mouse.delay(200);
			}
		} else {
			LOGGER.info("Fishes empty! Why?");
		}
	}
}
