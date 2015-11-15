package com.horowitz.seaport.model.storage;

import java.io.IOException;

import com.horowitz.commons.Deserializable;

public interface Deserializer {

  void deserialize(Deserializable deserializable) throws IOException;
}
