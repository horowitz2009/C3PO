package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
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

  void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException {

    // scan the name
    Rectangle nameArea = new Rectangle(pin.x - 75, pin.y - 67, 150, 40);
    List<Ship> ships = _mapManager.getShips();
    Ship whatShip = null;
    for (Ship ship : ships) {
      if (ship.isActive()) {
        if (_scanner.scanOne(ship.getImageDataTitle(), nameArea, false) != null) {
          whatShip = ship;
          break;
        }
        ;
      }
    }

    Destination dest = null;
    // Destination dest2 = null;
    if (whatShip != null) {
      if (whatShip.getName().equals(_sellingShip)) {
        dest = _mapManager.getMarket();

      } else {
        for (String name : _cocoaShips) {
          if (whatShip.getName().equals(name)) {
            dest = _mapManager.getDestination("Cocoa Plant");
            break;
          }
        }
        if (dest == null) {
          for (String name : _otherShips) {
            if (whatShip.getName().equals(name)) {
              dest = _mapManager.getDestination("Coastline");
              break;
            }
          }
        }
        if (dest == null) {
          dest = _mapManager.getDestination("Gulf");
        }
      }
    }

    if (dest == null) {
      dest = _mapManager.getDestination("Gulf");
    }

    if (dest != null) {
      _mouse.click(pin);
      _mouse.delay(50);
      _mouse.mouseMove(_scanner.getParkingPoint());
      _mouse.delay(500);

      Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
      if (anchor != null) {
        // MAP IS OPEN
        _mapManager.ensureMap();
        final LinkedList<Destination> chain = generatePartialChain(dest);
        sendShip(chain);
      }
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

  @Override
  public void update() {
    super.update();
    // build the chain for this protocol
    _destChain.clear();
    _destChain.add(_mapManager.getDestination("Cocoa Plant"));
    _destChain.add(_mapManager.getMarket());
    _destChain.add(_mapManager.getDestination("Gulf"));
    _destChain.add(_mapManager.getDestination("Coastline"));
    _destChain.add(_mapManager.getDestination("Small Town"));
  }

}
