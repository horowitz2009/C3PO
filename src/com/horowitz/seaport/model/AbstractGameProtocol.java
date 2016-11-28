package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.io.IOException;

import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.GameErrorException;

public abstract class AbstractGameProtocol implements GameProtocol {
	private boolean _interrupted = false;

	public abstract void execute() throws RobotInterruptedException, GameErrorException;

	public abstract boolean preExecute() throws AWTException, IOException, RobotInterruptedException;

	public abstract void update();
	
	@Override
	public void interrupt() {
		_interrupted = true;
	}

	public boolean isInterrupted() {
		return _interrupted;
	}
	
	public void setInterrupted(boolean interrupted) {
		_interrupted = interrupted;
	}

	public boolean isNotInterrupted() {
		return !_interrupted;
	}
	
	@Override
	public void reset() {
	  _interrupted = false;
	}
}
