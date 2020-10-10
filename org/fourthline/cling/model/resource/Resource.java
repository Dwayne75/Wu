package org.fourthline.cling.model.resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.fourthline.cling.model.ExpirationDetails;

public class Resource<M>
{
  private URI pathQuery;
  private M model;
  
  public Resource(URI pathQuery, M model)
  {
    try
    {
      this.pathQuery = new URI(null, null, pathQuery.getPath(), pathQuery.getQuery(), null);
    }
    catch (URISyntaxException ex)
    {
      throw new RuntimeException(ex);
    }
    this.model = model;
    if (model == null) {
      throw new IllegalArgumentException("Model instance must not be null");
    }
  }
  
  public URI getPathQuery()
  {
    return this.pathQuery;
  }
  
  public M getModel()
  {
    return (M)this.model;
  }
  
  public boolean matches(URI pathQuery)
  {
    return pathQuery.equals(getPathQuery());
  }
  
  public void maintain(List<Runnable> pendingExecutions, ExpirationDetails expirationDetails) {}
  
  public void shutdown() {}
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Resource resource = (Resource)o;
    if (!getPathQuery().equals(resource.getPathQuery())) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    return getPathQuery().hashCode();
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") URI: " + getPathQuery();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\resource\Resource.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */