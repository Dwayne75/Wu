package org.fourthline.cling.support.model;

import java.net.URI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DescMeta<M>
{
  protected String id;
  protected String type;
  protected URI nameSpace;
  protected M metadata;
  
  public DescMeta() {}
  
  public DescMeta(String id, String type, URI nameSpace, M metadata)
  {
    this.id = id;
    this.type = type;
    this.nameSpace = nameSpace;
    this.metadata = metadata;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public void setId(String id)
  {
    this.id = id;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public void setType(String type)
  {
    this.type = type;
  }
  
  public URI getNameSpace()
  {
    return this.nameSpace;
  }
  
  public void setNameSpace(URI nameSpace)
  {
    this.nameSpace = nameSpace;
  }
  
  public M getMetadata()
  {
    return (M)this.metadata;
  }
  
  public void setMetadata(M metadata)
  {
    this.metadata = metadata;
  }
  
  public Document createMetadataDocument()
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      Document d = factory.newDocumentBuilder().newDocument();
      Element rootElement = d.createElementNS("urn:fourthline-org:cling:support:content-directory-desc-1-0", "desc-wrapper");
      d.appendChild(rootElement);
      return d;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\DescMeta.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */