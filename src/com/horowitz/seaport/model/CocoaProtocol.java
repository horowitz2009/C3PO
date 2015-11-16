package com.horowitz.seaport.model;

import com.horowitz.commons.RobotInterruptedException;

/**
 * The Cocoa protocol v.2. Divide the ships to two groups. Group1 delivers cocoa
 * from Cocoa Plantation to warehouses. Group2 sells it to market for money or
 * XP.
 * 
 * @author Zhivko Hristov
 *
 */
public class CocoaProtocol implements GameProtocol {

  private String _name;

  ShipCommand[] _shipCommands;

  public void update() {
  }

  public void execute() throws RobotInterruptedException {
  }

}
