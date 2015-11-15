package com.horowitz.seaport.macros;

import java.awt.AWTException;
import java.io.IOException;
import java.io.Serializable;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.model.GameUnit;

public abstract class Macros implements Serializable {

  private static final long serialVersionUID = -4336238263778301896L;

  private String _name;
  protected transient ScreenScanner _scanner;

  public ScreenScanner getScanner() {
    return _scanner;
  }

  public void setScanner(ScreenScanner scanner) {
    _scanner = scanner;
  }

  protected transient MouseRobot _mouse;

  public Macros() {
    super();
    try {
      _mouse = new MouseRobot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public abstract boolean doTheJob(GameUnit gameUnit) throws AWTException, IOException, RobotInterruptedException;

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;

  }

}
