package com.wurmonline.server;

import java.io.File;

public class DummyTestClass
  extends XMLSerializer
{
  @XMLSerializer.Saved
  long test = 0L;
  
  public long getTest()
  {
    return this.test;
  }
  
  public void setTest(long aTest)
  {
    this.test = aTest;
  }
  
  public String getMyClass()
  {
    return this.myClass;
  }
  
  public void setMyClass(String aMyClass)
  {
    this.myClass = aMyClass;
  }
  
  public float getDontSave()
  {
    return 0.9333222F;
  }
  
  public void setSaveThis(float aSaveThis)
  {
    this.saveThis = aSaveThis;
  }
  
  @XMLSerializer.Saved
  String myClass = "my Class is dummy";
  @XMLSerializer.Saved
  float saveThis = 3.24324E-4F;
  
  public float getSaveThis()
  {
    return this.saveThis;
  }
  
  final float dontSave = 0.9333222F;
  
  public final DummyTestClass createInstanceAndCallLoadXML(File file)
  {
    loadXML(file);
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\DummyTestClass.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */