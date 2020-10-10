package com.sun.istack;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

public class XMLStreamException2
  extends XMLStreamException
{
  public XMLStreamException2(String msg)
  {
    super(msg);
  }
  
  public XMLStreamException2(Throwable th)
  {
    super(th);
  }
  
  public XMLStreamException2(String msg, Throwable th)
  {
    super(msg, th);
  }
  
  public XMLStreamException2(String msg, Location location)
  {
    super(msg, location);
  }
  
  public XMLStreamException2(String msg, Location location, Throwable th)
  {
    super(msg, location, th);
  }
  
  public Throwable getCause()
  {
    return getNestedException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\XMLStreamException2.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */