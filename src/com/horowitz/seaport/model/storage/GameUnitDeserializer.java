package com.horowitz.seaport.model.storage;

import java.io.IOException;

import com.horowitz.commons.Deserializable;
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

      ImageData imageData = _scanner.getImageData(gameUnit.getImage());
      ImageData imageDataTitle = _scanner.getImageData(gameUnit.getImageTitle());
      gameUnit.setImageData(imageData);
      gameUnit.setImageDataTitle(imageDataTitle);

      if (imageData != null)
        imageData.setComparator(_scanner.getComparator());
      if (imageDataTitle != null)
        imageDataTitle.setComparator(_scanner.getComparator());
    }
  }
}
