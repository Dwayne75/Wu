package impl.org.controlsfx.tools;

import java.util.Objects;

public class MathTools
{
  public static boolean isInInterval(double lowerBound, double value, double upperBound)
  {
    return (lowerBound <= value) && (value <= upperBound);
  }
  
  public static double inInterval(double lowerBound, double value, double upperBound)
  {
    if (value < lowerBound) {
      return lowerBound;
    }
    if (upperBound < value) {
      return upperBound;
    }
    return value;
  }
  
  public static double min(double... values)
  {
    Objects.requireNonNull(values, "The specified value array must not be null.");
    if (values.length == 0) {
      throw new IllegalArgumentException("The specified value array must contain at least one element.");
    }
    double min = Double.MAX_VALUE;
    for (double value : values) {
      min = Math.min(value, min);
    }
    return min;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\MathTools.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */