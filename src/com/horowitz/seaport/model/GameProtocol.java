package com.horowitz.seaport.model;

import com.horowitz.commons.RobotInterruptedException;

public interface GameProtocol {

  public void update();

  public void execute() throws RobotInterruptedException;
  
  
  
}
