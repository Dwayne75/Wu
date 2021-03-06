package org.apache.http.impl.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class RedirectLocations
{
  private final Set<URI> unique;
  private final List<URI> all;
  
  public RedirectLocations()
  {
    this.unique = new HashSet();
    this.all = new ArrayList();
  }
  
  public boolean contains(URI uri)
  {
    return this.unique.contains(uri);
  }
  
  public void add(URI uri)
  {
    this.unique.add(uri);
    this.all.add(uri);
  }
  
  public boolean remove(URI uri)
  {
    boolean removed = this.unique.remove(uri);
    if (removed)
    {
      Iterator<URI> it = this.all.iterator();
      while (it.hasNext())
      {
        URI current = (URI)it.next();
        if (current.equals(uri)) {
          it.remove();
        }
      }
    }
    return removed;
  }
  
  public List<URI> getAll()
  {
    return new ArrayList(this.all);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\RedirectLocations.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */