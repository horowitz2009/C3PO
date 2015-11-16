package com.horowitz.seaport.model;

import java.io.Serializable;

import com.horowitz.commons.RobotInterruptedException;

public class Task implements Cloneable, Serializable {

  private String _name;
  private int _frequency;
  private boolean _active;
  private String _imageName;
  private GameProtocol _protocol;

  public Task(String name, int frequency) {
    super();
    _name = name;
    _frequency = frequency;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public int getFrequency() {
    return _frequency;
  }

  public void setFrequency(int frequency) {
    _frequency = frequency;
  }

  public String getImageName() {
    return _imageName;
  }

  public void setImageName(String imageName) {
    _imageName = imageName;
  }

  public boolean isActive() {
    return _active;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public GameProtocol getProtocol() {
    return _protocol;
  }

  public void setProtocol(GameProtocol protocol) {
    _protocol = protocol;
  }

  public void update() {
    if (_protocol != null)
      _protocol.update();
  }

  public void execute() throws RobotInterruptedException {
    // TODO Auto-generated method stub
    if (_protocol != null)
      _protocol.execute();
  }

}
