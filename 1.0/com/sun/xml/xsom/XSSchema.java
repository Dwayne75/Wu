package com.sun.xml.xsom;

import java.util.Iterator;

public abstract interface XSSchema
  extends XSComponent
{
  public abstract String getTargetNamespace();
  
  public abstract Iterator iterateAttributeDecls();
  
  public abstract XSAttributeDecl getAttributeDecl(String paramString);
  
  public abstract Iterator iterateElementDecls();
  
  public abstract XSElementDecl getElementDecl(String paramString);
  
  public abstract Iterator iterateAttGroupDecls();
  
  public abstract XSAttGroupDecl getAttGroupDecl(String paramString);
  
  public abstract Iterator iterateModelGroupDecls();
  
  public abstract XSModelGroupDecl getModelGroupDecl(String paramString);
  
  public abstract Iterator iterateTypes();
  
  public abstract XSType getType(String paramString);
  
  public abstract Iterator iterateSimpleTypes();
  
  public abstract XSSimpleType getSimpleType(String paramString);
  
  public abstract Iterator iterateComplexTypes();
  
  public abstract XSComplexType getComplexType(String paramString);
  
  public abstract Iterator iterateNotations();
  
  public abstract XSNotation getNotation(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSSchema.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */