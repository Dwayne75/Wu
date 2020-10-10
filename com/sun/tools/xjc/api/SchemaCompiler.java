package com.sun.tools.xjc.api;

import com.sun.istack.NotNull;
import com.sun.tools.xjc.Options;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public abstract interface SchemaCompiler
{
  public abstract ContentHandler getParserHandler(String paramString);
  
  public abstract void parseSchema(InputSource paramInputSource);
  
  public abstract void setTargetVersion(SpecVersion paramSpecVersion);
  
  public abstract void parseSchema(String paramString, Element paramElement);
  
  public abstract void parseSchema(String paramString, XMLStreamReader paramXMLStreamReader)
    throws XMLStreamException;
  
  public abstract void setErrorListener(ErrorListener paramErrorListener);
  
  public abstract void setEntityResolver(EntityResolver paramEntityResolver);
  
  public abstract void setDefaultPackageName(String paramString);
  
  public abstract void forcePackageName(String paramString);
  
  public abstract void setClassNameAllocator(ClassNameAllocator paramClassNameAllocator);
  
  public abstract void resetSchema();
  
  public abstract S2JJAXBModel bind();
  
  /**
   * @deprecated
   */
  @NotNull
  public abstract Options getOptions();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\SchemaCompiler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */