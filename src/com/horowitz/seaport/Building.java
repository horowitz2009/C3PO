package com.horowitz.seaport;

import com.horowitz.bigbusiness.model.Deserializable;
import com.horowitz.mickey.Pixel;

public class Building implements Deserializable {
  private String _name;
  private int _time;
  private Pixel _position;
  private boolean _enabled;

  public Building(String name, Pixel position, boolean enabled) {
    super();
    _name = name;
    _position = position;
    _enabled = enabled;
  }

  public Building() {
    super();
  }

  public int getTime() {
    return _time;
  }

  public void setTime(int time) {
    _time = time;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  @Override
  public void postDeserialize(Object[] transientObjects) throws Exception {
    // TODO Auto-generated method stub

  }

  public boolean isEnabled() {
    return _enabled;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public Pixel getPosition() {
    return _position;
  }

  public void setPosition(Pixel position) {
    _position = position;
  }

  @Override
  public String toString() {
    return "Building [" + _name + ", " + _position + ", " + _enabled + "]";
  }
  
  
}
