package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.parser.SchemaDocument;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SchemaDocumentImpl
  implements SchemaDocument
{
  private final SchemaImpl schema;
  private final String schemaDocumentURI;
  final Set<SchemaDocumentImpl> references = new HashSet();
  final Set<SchemaDocumentImpl> referers = new HashSet();
  
  protected SchemaDocumentImpl(SchemaImpl schema, String _schemaDocumentURI)
  {
    this.schema = schema;
    this.schemaDocumentURI = _schemaDocumentURI;
  }
  
  public String getSystemId()
  {
    return this.schemaDocumentURI;
  }
  
  public String getTargetNamespace()
  {
    return this.schema.getTargetNamespace();
  }
  
  public SchemaImpl getSchema()
  {
    return this.schema;
  }
  
  public Set<SchemaDocument> getReferencedDocuments()
  {
    return Collections.unmodifiableSet(this.references);
  }
  
  public Set<SchemaDocument> getIncludedDocuments()
  {
    return getImportedDocuments(getTargetNamespace());
  }
  
  public Set<SchemaDocument> getImportedDocuments(String targetNamespace)
  {
    if (targetNamespace == null) {
      throw new IllegalArgumentException();
    }
    Set<SchemaDocument> r = new HashSet();
    for (SchemaDocumentImpl doc : this.references) {
      if (doc.getTargetNamespace().equals(targetNamespace)) {
        r.add(doc);
      }
    }
    return Collections.unmodifiableSet(r);
  }
  
  public boolean includes(SchemaDocument doc)
  {
    if (!this.references.contains(doc)) {
      return false;
    }
    return doc.getSchema() == this.schema;
  }
  
  public boolean imports(SchemaDocument doc)
  {
    if (!this.references.contains(doc)) {
      return false;
    }
    return doc.getSchema() != this.schema;
  }
  
  public Set<SchemaDocument> getReferers()
  {
    return Collections.unmodifiableSet(this.referers);
  }
  
  public boolean equals(Object o)
  {
    SchemaDocumentImpl rhs = (SchemaDocumentImpl)o;
    if ((this.schemaDocumentURI == null) || (rhs.schemaDocumentURI == null)) {
      return this == rhs;
    }
    if (!this.schemaDocumentURI.equals(rhs.schemaDocumentURI)) {
      return false;
    }
    return this.schema == rhs.schema;
  }
  
  public int hashCode()
  {
    if (this.schemaDocumentURI == null) {
      return super.hashCode();
    }
    return this.schemaDocumentURI.hashCode() ^ this.schema.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\SchemaDocumentImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */