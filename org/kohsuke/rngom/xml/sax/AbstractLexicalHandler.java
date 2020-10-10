package org.kohsuke.rngom.xml.sax;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class AbstractLexicalHandler
  implements LexicalHandler
{
  public void startDTD(String s, String s1, String s2)
    throws SAXException
  {}
  
  public void endDTD()
    throws SAXException
  {}
  
  public void startEntity(String s)
    throws SAXException
  {}
  
  public void endEntity(String s)
    throws SAXException
  {}
  
  public void startCDATA()
    throws SAXException
  {}
  
  public void endCDATA()
    throws SAXException
  {}
  
  public void comment(char[] chars, int start, int length)
    throws SAXException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\xml\sax\AbstractLexicalHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */