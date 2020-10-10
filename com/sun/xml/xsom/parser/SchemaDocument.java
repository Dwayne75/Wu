package com.sun.xml.xsom.parser;

import com.sun.xml.xsom.XSSchema;
import java.util.Set;

public abstract interface SchemaDocument
{
  public abstract String getSystemId();
  
  public abstract String getTargetNamespace();
  
  public abstract XSSchema getSchema();
  
  public abstract Set<SchemaDocument> getReferencedDocuments();
  
  public abstract Set<SchemaDocument> getIncludedDocuments();
  
  public abstract Set<SchemaDocument> getImportedDocuments(String paramString);
  
  public abstract boolean includes(SchemaDocument paramSchemaDocument);
  
  public abstract boolean imports(SchemaDocument paramSchemaDocument);
  
  public abstract Set<SchemaDocument> getReferers();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\parser\SchemaDocument.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */