package org.controlsfx.control;

import javafx.collections.ObservableList;

public abstract interface IndexedCheckModel<T>
  extends CheckModel<T>
{
  public abstract T getItem(int paramInt);
  
  public abstract int getItemIndex(T paramT);
  
  public abstract ObservableList<Integer> getCheckedIndices();
  
  public abstract void checkIndices(int... paramVarArgs);
  
  public abstract void clearCheck(int paramInt);
  
  public abstract boolean isChecked(int paramInt);
  
  public abstract void check(int paramInt);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\IndexedCheckModel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */