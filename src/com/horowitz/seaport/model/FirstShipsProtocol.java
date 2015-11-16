package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.BuildingManager;
import com.horowitz.seaport.dest.MapManager;

public class FirstShipsProtocol implements GameProtocol {

  public final static Logger LOGGER = Logger.getLogger(FirstShipsProtocol.class.getName());

  private List<Pixel> _shipLocations;

  private ScreenScanner _scanner;
  private MouseRobot _mouse;

  private MapManager _mapManager;

  private List<String> _cocoaShips;
  private List<String> _otherShips;

  private String _sellingShip;

  public FirstShipsProtocol(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager) throws IOException {
    _scanner = scanner;
    _mouse = mouse;
    _mapManager = mapManager;
    _cocoaShips = new ArrayList<>();
    _cocoaShips.add("Berrio");
    _cocoaShips.add("Sao Rafael");
    _cocoaShips.add("Venetian");
    _cocoaShips.add("Roland Von Bremen");

    _otherShips = new ArrayList<>();
    _otherShips.add("Mary Rose");
    _otherShips.add("Hulk Zigmund");
    _otherShips.add("Sao Gabriel");
    _otherShips.add("Peter Von Danzig");

    _sellingShip = "Sao Miguel";
  }

  @Override
  public void execute() throws RobotInterruptedException {
    if (_shipLocations != null && !_shipLocations.isEmpty()) {
      for (Pixel pixel : _shipLocations) {
        try {
          boolean shipSent = false;
          _mouse.click(pixel);
          _mouse.delay(250);

          Rectangle miniArea = new Rectangle(pixel.x - 15, pixel.y + 65, 44, 44);
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

    List<Ship> ships = _mapManager.getShips();
    for (Ship b : ships) {
      if (b.isActive()) {
        // TODO
      }
    }
  }

  private void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException {

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
        if (dest == null)
          dest = _mapManager.getDestination("Gulf");
      }
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
        sendShip(dest, true);
      }
    }

  }

  private void sendShip(Destination dest, boolean redirect) throws AWTException, RobotInterruptedException, IOException {
    Pixel _marketPos = _mapManager.getMarketPos();
    Destination _market = _mapManager.getMarket();

    Pixel destP;
    if (!dest.getName().equals("Market") && _marketPos != null) {
      int x = _marketPos.x + dest.getRelativePosition().x - _market.getImageData().get_xOff() - 35;
      int y = _marketPos.y + dest.getRelativePosition().y - _market.getImageData().get_yOff() - 35;
      Rectangle destArea = new Rectangle(x, y, 153 + 20 + 40, 25 + 40);

      LOGGER.info("Using custom area for " + dest.getImage());
      destP = _scanner.scanOneFast(dest.getImageData(), destArea, false);
    } else {
      destP = _marketPos;
    }

    if (destP != null) {
      LOGGER.info("Sending to " + dest.getName() + "...");
      _mouse.click(destP);
      _mouse.mouseMove(_scanner.getParkingPoint());
      _mouse.delay(400);

      Pixel destTitle = _scanner.scanOneFast(dest.getImageDataTitle(), null, false);

      if (destTitle != null) {
        LOGGER.info("SEND POPUP OPEN...");
        _mouse.mouseMove(_scanner.getParkingPoint());
        _mouse.delay(100);

        Pixel destButton = _scanner.scanOneFast("dest/setSail.bmp", null, false);
        if (destButton != null) {
          // nice. we can continue
          if (dest.getName().equals("Market")) {
            LOGGER.info("Market! I choose " + _mapManager.getMarketStrategy());
            if (_mapManager.getMarketStrategy().equals("XP")) {
              // do XP
            } else {
              // do money
              Pixel coins = new Pixel(destTitle);
              coins.y += 228;
              _mouse.click(coins);
              _mouse.delay(250);
            }
          }

          _mouse.click(destButton);
        } else {
          if (redirect)
            if (dest.getName().equals("Market")) {
              Pixel destButtonGray = _scanner.scanOneFast("dest/setSailGray.bmp", null, false);
              if (destButtonGray != null) {
                LOGGER.info("Material not available! Go do something else...");
                boolean found = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
                _mouse.delay(1800);
                sendShip(_mapManager.getDestination("Gulf"), false);
                // SEND to Cocoa plant (for now)

              }
            }
          LOGGER.info("Destination unreachable! Skipping...");
          _mouse.delay(300);
        }

      }
    } else {
      LOGGER.info("Destination: UNKNOWN!!!");
    }

  }

  @Override
  public void update() {
    _shipLocations = new ArrayList<>();
    Pixel[] shipLocations = _scanner.getShipLocations();
    _shipLocations = new ArrayList<Pixel>();
    Pixel r = _scanner.getRock();
    if (r != null)
      for (Pixel p : shipLocations) {
        Pixel goodP = new Pixel(r.x + p.x, r.y + p.y);
        _shipLocations.add(goodP);
      }
  }
}
