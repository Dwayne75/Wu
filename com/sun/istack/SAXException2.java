package com.sun.istack;

import org.xml.sax.SAXException;

public class SAXException2
  extends SAXException
{
  public SAXException2(String message)
  {
    super(message);
  }
  
  public SAXException2(Exception e)
  {
    super(e);
  }
  
  public SAXException2(String message, Exception e)
  {
    super(message, e);
  }
  
  public Throwable getCause()
  {
    return getException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\SAXException2.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */