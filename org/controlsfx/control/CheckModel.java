package org.controlsfx.control;

import javafx.collections.ObservableList;

public abstract interface CheckModel<T>
{
  public abstract int getItemCount();
  
  public abstract ObservableList<T> getCheckedItems();
  
  public abstract void checkAll();
  
  public abstract void clearCheck(T paramT);
  
  public abstract void clearChecks();
  
  public abstract boolean isEmpty();
  
  public abstract boolean isChecked(T paramT);
  
  public abstract void check(T paramT);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\CheckModel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */