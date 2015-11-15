package com.horowitz.seaport.model;

import com.horowitz.commons.Pixel;

public class Destination extends GameUnit {
  private static final long serialVersionUID = -1237748489318461109L;
  private int _time;
  private boolean _favorite;
  private Pixel _relativePosition;

  public Destination(String name, int time, String image, String imageTitle) {
    super(name, image, imageTitle);
    _time = time;
  }

  public Destination() {
    super();
  }

  public int getTime() {
    return _time;
  }

  public void setTime(int time) {
    _time = time;
  }

  public boolean isFavorite() {
    return _favorite;
  }

  public void setFavorite(boolean favorite) {
    _favorite = favorite;
  }

  public Pixel getRelativePosition() {
    return _relativePosition;
  }

  public void setRelativePosition(Pixel relativePosition) {
    _relativePosition = relativePosition;
  }
}
