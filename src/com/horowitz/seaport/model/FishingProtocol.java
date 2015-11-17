package com.horowitz.seaport.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;

public class FishingProtocol implements GameProtocol {

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private Pixel _rock;

  private List<Pixel> _fishes;

  private ScreenScanner _scanner;
  private MouseRobot _mouse;

  public FishingProtocol(ScreenScanner scanner, MouseRobot mouse) {
    _scanner = scanner;
    _mouse = mouse;
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

  @Override
  public void update() {
    _rock = _scanner.getRock();
    Pixel[] fishes = _scanner.getFishes();
    _fishes = new ArrayList<Pixel>();
    for (Pixel fish : fishes) {
      Pixel goodFish = new Pixel(_rock.x + fish.x, _rock.y + fish.y);
      _fishes.add(goodFish);
    }

  }
}
