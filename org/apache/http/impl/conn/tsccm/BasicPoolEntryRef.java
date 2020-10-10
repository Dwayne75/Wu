package org.apache.http.impl.conn.tsccm;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import org.apache.http.conn.routing.HttpRoute;

@Deprecated
public class BasicPoolEntryRef
  extends WeakReference<BasicPoolEntry>
{
  private final HttpRoute route;
  
  public BasicPoolEntryRef(BasicPoolEntry entry, ReferenceQueue<Object> queue)
  {
    super(entry, queue);
    if (entry == null) {
      throw new IllegalArgumentException("Pool entry must not be null.");
    }
    this.route = entry.getPlannedRoute();
  }
  
  public final HttpRoute getRoute()
  {
    return this.route;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\conn\tsccm\BasicPoolEntryRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */