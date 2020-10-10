package com.wurmonline.common;

import com.wurmonline.properties.Property;

public enum CommonProperties
{
  VERSION("version", "build.properties", "UNKNOWN"),  BUILD_TIME("build-time", "build.properties", "unknown"),  COMMIT("git-sha-1", "build.properties", "unknown");
  
  Property property;
  
  private CommonProperties(String _key, String _file, String _default)
  {
    this.property = new Property(_key, CommonProperties.class.getResource(_file), _default);
  }
  
  public Property getProperty()
  {
    return this.property;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\common\CommonProperties.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */