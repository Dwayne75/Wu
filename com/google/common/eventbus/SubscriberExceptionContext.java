package com.google.common.eventbus;

import com.google.common.base.Preconditions;
import java.lang.reflect.Method;

public class SubscriberExceptionContext
{
  private final EventBus eventBus;
  private final Object event;
  private final Object subscriber;
  private final Method subscriberMethod;
  
  SubscriberExceptionContext(EventBus eventBus, Object event, Object subscriber, Method subscriberMethod)
  {
    this.eventBus = ((EventBus)Preconditions.checkNotNull(eventBus));
    this.event = Preconditions.checkNotNull(event);
    this.subscriber = Preconditions.checkNotNull(subscriber);
    this.subscriberMethod = ((Method)Preconditions.checkNotNull(subscriberMethod));
  }
  
  public EventBus getEventBus()
  {
    return this.eventBus;
  }
  
  public Object getEvent()
  {
    return this.event;
  }
  
  public Object getSubscriber()
  {
    return this.subscriber;
  }
  
  public Method getSubscriberMethod()
  {
    return this.subscriberMethod;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\eventbus\SubscriberExceptionContext.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */