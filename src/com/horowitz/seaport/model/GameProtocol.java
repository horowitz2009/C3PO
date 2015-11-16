package com.horowitz.seaport.model;

import java.util.ArrayList;

import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;

public interface GameProtocol {

  public abstract void update();

  public abstract void execute() throws RobotInterruptedException;
  
  
  
}
