package com.sun.tools.xjc.reader.xmlschema;

import com.sun.xml.xsom.XSModelGroup;
import java.text.ParseException;

public final class NameGenerator
{
  public static String getName(BGMBuilder builder, XSModelGroup mg)
    throws ParseException
  {
    StringBuffer name = new StringBuffer();
    
    mg.visit(new NameGenerator.1(name, builder));
    if (name.length() == 0) {
      throw new ParseException("no element", -1);
    }
    return name.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\NameGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */