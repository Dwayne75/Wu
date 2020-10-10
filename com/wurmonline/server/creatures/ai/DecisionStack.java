package com.wurmonline.server.creatures.ai;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DecisionStack
{
  private static final Logger logger = Logger.getLogger(DecisionStack.class.getName());
  private static final int MAX_ORDERS = 5;
  private final LinkedList<Order> orders = new LinkedList();
  
  public DecisionStack()
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Created new DecisionStack");
    }
  }
  
  public void clearOrders()
  {
    this.orders.clear();
  }
  
  public boolean addOrder(Order order)
  {
    if ((mayReceiveOrders()) && (order != null))
    {
      this.orders.addLast(order);
      return true;
    }
    return false;
  }
  
  public boolean removeOrder(Order order)
  {
    if (order != null) {
      return this.orders.remove(order);
    }
    logger.warning("Tried to remove a null Order from " + this);
    return false;
  }
  
  public Order getFirst()
  {
    return (Order)this.orders.getFirst();
  }
  
  public boolean hasOrders()
  {
    return !this.orders.isEmpty();
  }
  
  public boolean mayReceiveOrders()
  {
    return this.orders.size() < 5;
  }
  
  int getNumberOfOrders()
  {
    return this.orders.size();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\ai\DecisionStack.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */