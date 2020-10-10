package com.sun.tools.xjc.reader.xmlschema.ct;

final class ComplexTypeFlag
{
  private final String name;
  
  private ComplexTypeFlag(String name)
  {
    this.name = name;
  }
  
  public String toString()
  {
    return this.name;
  }
  
  static final ComplexTypeFlag NORMAL = new ComplexTypeFlag("normal");
  static final ComplexTypeFlag FALLBACK_CONTENT = new ComplexTypeFlag("fallback(content)");
  static final ComplexTypeFlag FALLBACK_REST = new ComplexTypeFlag("fallback(rest)");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\ComplexTypeFlag.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */