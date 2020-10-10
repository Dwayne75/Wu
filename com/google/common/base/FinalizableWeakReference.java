package com.google.common.base;

import java.lang.ref.WeakReference;

public abstract class FinalizableWeakReference<T>
  extends WeakReference<T>
  implements FinalizableReference
{
  protected FinalizableWeakReference(T referent, FinalizableReferenceQueue queue)
  {
    super(referent, queue.queue);
    queue.cleanUp();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\FinalizableWeakReference.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */