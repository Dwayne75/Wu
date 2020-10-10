package com.sun.tools.xjc.api;

import com.sun.tools.xjc.api.impl.j2s.JavaCompilerImpl;
import com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl;
import com.sun.xml.bind.api.impl.NameConverter;

public final class XJC
{
  public static JavaCompiler createJavaCompiler()
  {
    return new JavaCompilerImpl();
  }
  
  public static SchemaCompiler createSchemaCompiler()
  {
    return new SchemaCompilerImpl();
  }
  
  public static String getDefaultPackageName(String namespaceUri)
  {
    if (namespaceUri == null) {
      throw new IllegalArgumentException();
    }
    return NameConverter.standard.toPackageName(namespaceUri);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\XJC.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */