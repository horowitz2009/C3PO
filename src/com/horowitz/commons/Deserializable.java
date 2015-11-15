package com.horowitz.commons;

import java.io.IOException;

import com.horowitz.seaport.model.storage.Deserializer;

public interface Deserializable {
  
  void deserialize(Deserializer deserializer) throws IOException;
  
}
