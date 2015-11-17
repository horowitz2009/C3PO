package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
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

public abstract class ShipsProtocol implements GameProtocol {

  public static final Logger LOGGER = Logger.getLogger(ShipsProtocol.class.getName());
  private List<Pixel> _shipLocations;
  protected ScreenScanner _scanner;
  protected MouseRobot _mouse;
  protected MapManager _mapManager;
  protected LinkedList<Destination> _destChain;

  public ShipsProtocol() {
    super();
  }

  public ShipsProtocol(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager) throws IOException {
    _scanner = scanner;
    _mouse = mouse;
    _mapManager = mapManager;

    _destChain = new LinkedList<Destination>();
  }

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

  abstract void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException;

  protected void sendShip(LinkedList<Destination> chain) throws AWTException, RobotInterruptedException, IOException {
    Destination dest = chain.poll();
    if (dest != null) {

      Pixel _marketPos = _mapManager.getMarketPos();
      Destination _market = _mapManager.getMarket();

      Pixel destP = _marketPos;
      if (!dest.getName().equals("Market")) {
        int x = _marketPos.x + dest.getRelativePosition().x - _market.getImageData().get_xOff() - 35;
        int y = _marketPos.y + dest.getRelativePosition().y - _market.getImageData().get_yOff() - 35;
        Rectangle destArea = new Rectangle(x, y, 153 + 20 + 40, 25 + 40);
        Pixel pp = _scanner.ensureAreaInGame(destArea);
        if (pp.x != 0 || pp.y != 0) {
          _mapManager.ensureMap();
          x = _marketPos.x + dest.getRelativePosition().x - _market.getImageData().get_xOff() - 35;
          y = _marketPos.y + dest.getRelativePosition().y - _market.getImageData().get_yOff() - 35;
          destArea = new Rectangle(x, y, 153 + 20 + 40, 25 + 40);
        }
        LOGGER.info("Using custom area for " + dest.getImage());
        destP = _scanner.scanOneFast(dest.getImageData(), destArea, false);
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
            LOGGER.info(dest.getName() + " can't be done!");
            boolean found = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
            // if (found)
            _mouse.delay(1800);
            // chain.poll();
            if (chain.isEmpty())
              LOGGER.info("reached the end of chain");
            else
              sendShip(chain);
          }

        }
      } else {
        LOGGER.info("========");
        LOGGER.info("Can't locate destination: " + dest.getName());
        LOGGER.info("========");
      }
    }
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

}