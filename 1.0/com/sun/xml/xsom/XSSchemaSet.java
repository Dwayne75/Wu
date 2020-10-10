package com.sun.xml.xsom;

import java.util.Iterator;

public abstract interface XSSchemaSet
{
  public abstract XSSchema getSchema(String paramString);
  
  public abstract XSSchema getSchema(int paramInt);
  
  public abstract int getSchemaSize();
  
  public abstract Iterator iterateSchema();
  
  public abstract XSSimpleType getSimpleType(String paramString1, String paramString2);
  
  public abstract XSAttributeDecl getAttributeDecl(String paramString1, String paramString2);
  
  public abstract XSElementDecl getElementDecl(String paramString1, String paramString2);
  
  public abstract XSModelGroupDecl getModelGroupDecl(String paramString1, String paramString2);
  
  public abstract XSAttGroupDecl getAttGroupDecl(String paramString1, String paramString2);
  
  public abstract XSComplexType getComplexType(String paramString1, String paramString2);
  
  public abstract Iterator iterateElementDecls();
  
  public abstract Iterator iterateTypes();
  
  public abstract Iterator iterateAttributeDecls();
  
  public abstract Iterator iterateAttGroupDecls();
  
  public abstract Iterator iterateModelGroupDecls();
  
  public abstract Iterator iterateSimpleTypes();
  
  public abstract Iterator iterateComplexTypes();
  
  public abstract Iterator iterateNotations();
  
  public abstract XSComplexType getAnyType();
  
  public abstract XSSimpleType getAnySimpleType();
  
  public abstract XSContentType getEmpty();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSSchemaSet.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */