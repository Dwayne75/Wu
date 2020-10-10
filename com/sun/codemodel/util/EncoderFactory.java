package com.sun.codemodel.util;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class EncoderFactory
{
  public static CharsetEncoder createEncoder(String encodin)
  {
    Charset cs = Charset.forName(System.getProperty("file.encoding"));
    CharsetEncoder encoder = cs.newEncoder();
    if (cs.getClass().getName().equals("sun.nio.cs.MS1252")) {
      try
      {
        Class ms1252encoder = Class.forName("com.sun.codemodel.util.MS1252Encoder");
        Constructor c = ms1252encoder.getConstructor(new Class[] { Charset.class });
        
        return (CharsetEncoder)c.newInstance(new Object[] { cs });
      }
      catch (Throwable t)
      {
        return encoder;
      }
    }
    return encoder;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\util\EncoderFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */