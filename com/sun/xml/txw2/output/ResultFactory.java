package com.sun.xml.txw2.output;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

public abstract class ResultFactory
{
  public static XmlSerializer createSerializer(Result result)
  {
    if ((result instanceof SAXResult)) {
      return new SaxSerializer((SAXResult)result);
    }
    if ((result instanceof DOMResult)) {
      return new DomSerializer((DOMResult)result);
    }
    if ((result instanceof StreamResult)) {
      return new StreamSerializer((StreamResult)result);
    }
    throw new UnsupportedOperationException("Unsupported Result type: " + result.getClass().getName());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\output\ResultFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */