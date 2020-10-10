package com.sun.tools.xjc.grammar;

public abstract interface JavaItemVisitor
{
  public abstract Object onClass(ClassItem paramClassItem);
  
  public abstract Object onField(FieldItem paramFieldItem);
  
  public abstract Object onIgnore(IgnoreItem paramIgnoreItem);
  
  public abstract Object onInterface(InterfaceItem paramInterfaceItem);
  
  public abstract Object onPrimitive(PrimitiveItem paramPrimitiveItem);
  
  public abstract Object onExternal(ExternalItem paramExternalItem);
  
  public abstract Object onSuper(SuperClassItem paramSuperClassItem);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\JavaItemVisitor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */