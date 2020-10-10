package com.sun.tools.xjc.reader;

public abstract interface NameConverter
{
  public static final NameConverter standard = new NameConverter.Standard();
  public static final NameConverter jaxrpcCompatible = new NameConverter.1();
  public static final NameConverter smart = new NameConverter.2();
  
  public abstract String toClassName(String paramString);
  
  public abstract String toInterfaceName(String paramString);
  
  public abstract String toPropertyName(String paramString);
  
  public abstract String toConstantName(String paramString);
  
  public abstract String toVariableName(String paramString);
  
  public abstract String toPackageName(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\NameConverter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */