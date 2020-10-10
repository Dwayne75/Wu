package com.sun.tools.xjc.reader.xmlschema.parser;

import java.io.InputStream;
import java.io.Reader;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

public class LSInputSAXWrapper
  implements LSInput
{
  private InputSource core;
  
  public LSInputSAXWrapper(InputSource inputSource)
  {
    assert (inputSource != null);
    this.core = inputSource;
  }
  
  public Reader getCharacterStream()
  {
    return this.core.getCharacterStream();
  }
  
  public void setCharacterStream(Reader characterStream)
  {
    this.core.setCharacterStream(characterStream);
  }
  
  public InputStream getByteStream()
  {
    return this.core.getByteStream();
  }
  
  public void setByteStream(InputStream byteStream)
  {
    this.core.setByteStream(byteStream);
  }
  
  public String getStringData()
  {
    return null;
  }
  
  public void setStringData(String stringData) {}
  
  public String getSystemId()
  {
    return this.core.getSystemId();
  }
  
  public void setSystemId(String systemId)
  {
    this.core.setSystemId(systemId);
  }
  
  public String getPublicId()
  {
    return this.core.getPublicId();
  }
  
  public void setPublicId(String publicId)
  {
    this.core.setPublicId(publicId);
  }
  
  public String getBaseURI()
  {
    return null;
  }
  
  public void setBaseURI(String baseURI) {}
  
  public String getEncoding()
  {
    return this.core.getEncoding();
  }
  
  public void setEncoding(String encoding)
  {
    this.core.setEncoding(encoding);
  }
  
  public boolean getCertifiedText()
  {
    return true;
  }
  
  public void setCertifiedText(boolean certifiedText) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\parser\LSInputSAXWrapper.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */