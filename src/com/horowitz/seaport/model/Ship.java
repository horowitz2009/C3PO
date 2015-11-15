package com.horowitz.seaport.model;

public class Ship extends GameUnit {
  private static final long serialVersionUID = -1237748489318461109L;
  private int _capacity;
  private boolean _active;
  private boolean _favorite;

  public Ship(String name, int capacity, String image, String imageTitle) {
    super(name, image, imageTitle);
    _capacity = capacity;
    _active = false;
  }

  public Ship() {
    super();
  }

  public Ship(String name) {
    super(name);
  }

  public boolean isFavorite() {
    return _favorite;
  }

  public void setFavorite(boolean favorite) {
    _favorite = favorite;
  }

  public int getCapacity() {
    return _capacity;
  }

  public void setCapacity(int capacity) {
    _capacity = capacity;
  }

  public boolean isActive() {
    return _active;
  }

  public void setActive(boolean active) {
    _active = active;
  }

}