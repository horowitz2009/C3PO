package com.horowitz.seaport.model.storage;

import java.io.IOException;

import com.horowitz.commons.Deserializable;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.macros.Macros;
import com.horowitz.seaport.model.Building;

public class BuildingDeserializer implements Deserializer {
  private ScreenScanner _scanner;

  public BuildingDeserializer(ScreenScanner scanner) {
    super();
    _scanner = scanner;
  }

  public void deserialize(Deserializable deserializable) throws IOException {
    if (deserializable instanceof Building) {
      Building b = (Building) deserializable;
      if (b.getMacrosClass() != null) {
        Macros clazz = null;
        try {
          clazz = (Macros) Class.forName(b.getMacrosClass()).newInstance();
        } catch (InstantiationException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        b.setMacros(clazz);
      }
    }
  }
}
