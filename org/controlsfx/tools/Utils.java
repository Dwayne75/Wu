package org.controlsfx.tools;

import java.util.Iterator;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

public class Utils
{
  public static Window getWindow(Object owner)
    throws IllegalArgumentException
  {
    if (owner == null)
    {
      Window window = null;
      
      Iterator<Window> windows = Window.impl_getWindows();
      while (windows.hasNext())
      {
        window = (Window)windows.next();
        if ((window.isFocused()) && (!(window instanceof PopupWindow))) {
          break;
        }
      }
      return window;
    }
    if ((owner instanceof Window)) {
      return (Window)owner;
    }
    if ((owner instanceof Node)) {
      return ((Node)owner).getScene().getWindow();
    }
    throw new IllegalArgumentException("Unknown owner: " + owner.getClass());
  }
  
  public static final String getExcelLetterFromNumber(int number)
  {
    String letter = "";
    while (number >= 0)
    {
      int remainder = number % 26;
      letter = (char)(remainder + 65) + letter;
      number = number / 26 - 1;
    }
    return letter;
  }
  
  public static double clamp(double min, double value, double max)
  {
    if (value < min) {
      return min;
    }
    if (value > max) {
      return max;
    }
    return value;
  }
  
  public static double nearest(double less, double value, double more)
  {
    double lessDiff = value - less;
    double moreDiff = more - value;
    if (lessDiff < moreDiff) {
      return less;
    }
    return more;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\tools\Utils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */