package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.io.IOException;

import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.GameErrorException;

public interface GameProtocol {

  public void update();

  public void execute() throws RobotInterruptedException, GameErrorException;

	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException;
  
  
  
}
