package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

/**
 * @deprecated
 */
public class Caverphone
  implements StringEncoder
{
  private final Caverphone2 encoder = new Caverphone2();
  
  public String caverphone(String source)
  {
    return this.encoder.encode(source);
  }
  
  public Object encode(Object pObject)
    throws EncoderException
  {
    if (!(pObject instanceof String)) {
      throw new EncoderException("Parameter supplied to Caverphone encode is not of type java.lang.String");
    }
    return caverphone((String)pObject);
  }
  
  public String encode(String pString)
  {
    return caverphone(pString);
  }
  
  public boolean isCaverphoneEqual(String str1, String str2)
  {
    return caverphone(str1).equals(caverphone(str2));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\language\Caverphone.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */