package com.horowitz.seaport.model;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.ImageData;
import com.horowitz.seaport.model.storage.Deserializer;

@SuppressWarnings("rawtypes")
public class GameUnit implements Cloneable, Serializable, Deserializable, Comparable {

  private static final long serialVersionUID = 2085718203184534985L;
  private String _name;
  private String _image;
  private String _imageTitle;
  private ImageData _imageData;
  private ImageData _imageDataTitle;

  public GameUnit() {
    super();
  }

  public GameUnit(String name) {
    super();
    _name = name;
  }

  public GameUnit(String name, String image, String imageTitle) {
    super();
    _name = name;
    _image = image;
    _imageTitle = imageTitle;
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

  public void deserialize(Deserializer deserializer) throws IOException {
    deserializer.deserialize(this);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    // !!!!!!!!!!!!!!!!
    // _labelImage and _pictureImage refs will not be cloned
    // This is what I want (for now)
    // //////////////////////////////////////////////////////

    return super.clone();
  }

  public int compareTo(Object o) {
    return new CompareToBuilder().append(this._name, ((GameUnit) o)._name).toComparison();
  }

}