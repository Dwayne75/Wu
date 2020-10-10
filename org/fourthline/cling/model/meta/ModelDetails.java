package org.fourthline.cling.model.meta;

import java.net.URI;

public class ModelDetails
{
  private String modelName;
  private String modelDescription;
  private String modelNumber;
  private URI modelURI;
  
  ModelDetails() {}
  
  public ModelDetails(String modelName)
  {
    this.modelName = modelName;
  }
  
  public ModelDetails(String modelName, String modelDescription)
  {
    this.modelName = modelName;
    this.modelDescription = modelDescription;
  }
  
  public ModelDetails(String modelName, String modelDescription, String modelNumber)
  {
    this.modelName = modelName;
    this.modelDescription = modelDescription;
    this.modelNumber = modelNumber;
  }
  
  public ModelDetails(String modelName, String modelDescription, String modelNumber, URI modelURI)
  {
    this.modelName = modelName;
    this.modelDescription = modelDescription;
    this.modelNumber = modelNumber;
    this.modelURI = modelURI;
  }
  
  public ModelDetails(String modelName, String modelDescription, String modelNumber, String modelURI)
    throws IllegalArgumentException
  {
    this.modelName = modelName;
    this.modelDescription = modelDescription;
    this.modelNumber = modelNumber;
    this.modelURI = URI.create(modelURI);
  }
  
  public String getModelName()
  {
    return this.modelName;
  }
  
  public String getModelDescription()
  {
    return this.modelDescription;
  }
  
  public String getModelNumber()
  {
    return this.modelNumber;
  }
  
  public URI getModelURI()
  {
    return this.modelURI;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\ModelDetails.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */