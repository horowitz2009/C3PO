package com.horowitz.seaport.model.storage;

import java.io.IOException;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;
import com.horowitz.commons.ImageData;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.model.GameUnit;

public class GameUnitDeserializer implements Deserializer {
  private ScreenScanner _scanner;

  public GameUnitDeserializer(ScreenScanner scanner) {
    super();
    _scanner = scanner;
  }

  public void deserialize(Deserializable deserializable) throws IOException {
    if (deserializable instanceof GameUnit) {
      GameUnit gameUnit = (GameUnit) deserializable;
      if (gameUnit.getImage() != null) {
        ImageData imageData = _scanner.getImageData(gameUnit.getImage());
        if (imageData != null) {
          gameUnit.setImageData(imageData);
          imageData.setComparator(_scanner.getComparator());
        }
      }
      if (gameUnit.getImageTitle() != null) {
        ImageData imageData = _scanner.getImageData(gameUnit.getImageTitle());
        if (imageData != null) {
          gameUnit.setImageDataTitle(imageData);
          imageData.setComparator(_scanner.getComparator());
        }
      }
    }
  }
}
