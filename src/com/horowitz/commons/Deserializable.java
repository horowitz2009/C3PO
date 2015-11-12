package com.horowitz.commons;

public interface Deserializable {
  
  void postDeserialize(Object[] transientObjects) throws Exception;
  
}
