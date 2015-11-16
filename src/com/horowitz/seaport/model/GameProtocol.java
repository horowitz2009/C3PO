package com.horowitz.seaport.model;

import com.horowitz.commons.RobotInterruptedException;

public interface GameProtocol {

  public abstract void update();

  public abstract void execute() throws RobotInterruptedException;
  
  
  
}
