package com.sun.dtdparser;

import java.util.EventListener;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract interface DTDEventListener
  extends EventListener
{
  public static final short CONTENT_MODEL_EMPTY = 0;
  public static final short CONTENT_MODEL_ANY = 1;
  public static final short CONTENT_MODEL_MIXED = 2;
  public static final short CONTENT_MODEL_CHILDREN = 3;
  public static final short USE_NORMAL = 0;
  public static final short USE_IMPLIED = 1;
  public static final short USE_FIXED = 2;
  public static final short USE_REQUIRED = 3;
  public static final short CHOICE = 0;
  public static final short SEQUENCE = 1;
  public static final short OCCURENCE_ZERO_OR_MORE = 0;
  public static final short OCCURENCE_ONE_OR_MORE = 1;
  public static final short OCCURENCE_ZERO_OR_ONE = 2;
  public static final short OCCURENCE_ONCE = 3;
  
  public abstract void setDocumentLocator(Locator paramLocator);
  
  public abstract void processingInstruction(String paramString1, String paramString2)
    throws SAXException;
  
  public abstract void notationDecl(String paramString1, String paramString2, String paramString3)
    throws SAXException;
  
  public abstract void unparsedEntityDecl(String paramString1, String paramString2, String paramString3, String paramString4)
    throws SAXException;
  
  public abstract void internalGeneralEntityDecl(String paramString1, String paramString2)
    throws SAXException;
  
  public abstract void externalGeneralEntityDecl(String paramString1, String paramString2, String paramString3)
    throws SAXException;
  
  public abstract void internalParameterEntityDecl(String paramString1, String paramString2)
    throws SAXException;
  
  public abstract void externalParameterEntityDecl(String paramString1, String paramString2, String paramString3)
    throws SAXException;
  
  public abstract void startDTD(InputEntity paramInputEntity)
    throws SAXException;
  
  public abstract void endDTD()
    throws SAXException;
  
  public abstract void comment(String paramString)
    throws SAXException;
  
  public abstract void characters(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException;
  
  public abstract void ignorableWhitespace(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException;
  
  public abstract void startCDATA()
    throws SAXException;
  
  public abstract void endCDATA()
    throws SAXException;
  
  public abstract void fatalError(SAXParseException paramSAXParseException)
    throws SAXException;
  
  public abstract void error(SAXParseException paramSAXParseException)
    throws SAXException;
  
  public abstract void warning(SAXParseException paramSAXParseException)
    throws SAXException;
  
  public abstract void startContentModel(String paramString, short paramShort)
    throws SAXException;
  
  public abstract void endContentModel(String paramString, short paramShort)
    throws SAXException;
  
  public abstract void attributeDecl(String paramString1, String paramString2, String paramString3, String[] paramArrayOfString, short paramShort, String paramString4)
    throws SAXException;
  
  public abstract void childElement(String paramString, short paramShort)
    throws SAXException;
  
  public abstract void mixedElement(String paramString)
    throws SAXException;
  
  public abstract void startModelGroup()
    throws SAXException;
  
  public abstract void endModelGroup(short paramShort)
    throws SAXException;
  
  public abstract void connector(short paramShort)
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\dtdparser\DTDEventListener.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */