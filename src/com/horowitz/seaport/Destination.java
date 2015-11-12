package com.horowitz.seaport;

import com.horowitz.bigbusiness.model.Deserializable;
import com.horowitz.commons.ImageData;

public class Destination implements Deserializable {
  private String _name;
  private int _time;
  private String _image;
  private String _imageTitle;
  
  private transient ImageData _imageData;
  private transient ImageData _imageDataTitle;

  public Destination(String name, int time, ImageData imageData, ImageData imageDataTitle) {
    super();
    _name = name;
    _imageData = imageData;
    _imageDataTitle = imageDataTitle;
    _time = time;
  }
  
  public Destination(String name, int time, String image, String imageTitle) {
    super();
    _name = name;
    _image = image;
    _imageTitle = imageTitle;
    _time = time;
  }
  
  public Destination() {
    super();
  }

  public String getImage() {
    return _image;
  }

  public void setImage(String image) {
    _image = image;
  }

  public String getImageTitle() {
    return _imageTitle;
  }

  public void setImageTitle(String imageTitle) {
    _imageTitle = imageTitle;
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

  public ImageData getImageData() {
    return _imageData;
  }

  public void setImageData(ImageData imageData) {
    _imageData = imageData;
  }

  public ImageData getImageDataTitle() {
    return _imageDataTitle;
  }

  public void setImageDataTitle(ImageData imageDataTitle) {
    _imageDataTitle = imageDataTitle;
  }

  @Override
  public void postDeserialize(Object[] transientObjects) throws Exception {
    // TODO Auto-generated method stub
    
  }
}
