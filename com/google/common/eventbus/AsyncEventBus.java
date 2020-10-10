package com.google.common.eventbus;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

@Beta
public class AsyncEventBus
  extends EventBus
{
  private final Executor executor;
  private final ConcurrentLinkedQueue<EventBus.EventWithSubscriber> eventsToDispatch = new ConcurrentLinkedQueue();
  
  public AsyncEventBus(String identifier, Executor executor)
  {
    super(identifier);
    this.executor = ((Executor)Preconditions.checkNotNull(executor));
  }
  
  public AsyncEventBus(Executor executor, SubscriberExceptionHandler subscriberExceptionHandler)
  {
    super(subscriberExceptionHandler);
    this.executor = ((Executor)Preconditions.checkNotNull(executor));
  }
  
  public AsyncEventBus(Executor executor)
  {
    super("default");
    this.executor = ((Executor)Preconditions.checkNotNull(executor));
  }
  
  void enqueueEvent(Object event, EventSubscriber subscriber)
  {
    this.eventsToDispatch.offer(new EventBus.EventWithSubscriber(event, subscriber));
  }
  
  protected void dispatchQueuedEvents()
  {
    for (;;)
    {
      EventBus.EventWithSubscriber eventWithSubscriber = (EventBus.EventWithSubscriber)this.eventsToDispatch.poll();
      if (eventWithSubscriber == null) {
        break;
      }
      dispatch(eventWithSubscriber.event, eventWithSubscriber.subscriber);
    }
  }
  
  void dispatch(final Object event, final EventSubscriber subscriber)
  {
    Preconditions.checkNotNull(event);
    Preconditions.checkNotNull(subscriber);
    this.executor.execute(new Runnable()
    {
      public void run()
      {
        AsyncEventBus.this.dispatch(event, subscriber);
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\eventbus\AsyncEventBus.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */