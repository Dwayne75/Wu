package com.sun.tools.xjc.reader.xmlschema.ct;

final class ComplexTypeBindingMode
{
  private final String name;
  
  private ComplexTypeBindingMode(String name)
  {
    this.name = name;
  }
  
  public String toString()
  {
    return this.name;
  }
  
  static final ComplexTypeBindingMode NORMAL = new ComplexTypeBindingMode("normal");
  static final ComplexTypeBindingMode FALLBACK_CONTENT = new ComplexTypeBindingMode("fallback(content)");
  static final ComplexTypeBindingMode FALLBACK_REST = new ComplexTypeBindingMode("fallback(rest)");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\ComplexTypeBindingMode.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */