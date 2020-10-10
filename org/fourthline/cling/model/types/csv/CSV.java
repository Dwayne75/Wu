package org.fourthline.cling.model.types.csv;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;
import org.fourthline.cling.model.types.Datatype.Default;
import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.Reflections;

public abstract class CSV<T>
  extends ArrayList<T>
{
  protected final Datatype.Builtin datatype;
  
  public CSV()
  {
    this.datatype = getBuiltinDatatype();
  }
  
  public CSV(String s)
    throws InvalidValueException
  {
    this.datatype = getBuiltinDatatype();
    addAll(parseString(s));
  }
  
  protected List parseString(String s)
    throws InvalidValueException
  {
    String[] strings = ModelUtil.fromCommaSeparatedList(s);
    List values = new ArrayList();
    for (String string : strings) {
      values.add(this.datatype.getDatatype().valueOf(string));
    }
    return values;
  }
  
  protected Datatype.Builtin getBuiltinDatatype()
    throws InvalidValueException
  {
    Class csvType = (Class)Reflections.getTypeArguments(ArrayList.class, getClass()).get(0);
    Datatype.Default defaultType = Datatype.Default.getByJavaType(csvType);
    if (defaultType == null) {
      throw new InvalidValueException("No built-in UPnP datatype for Java type of CSV: " + csvType);
    }
    return defaultType.getBuiltinType();
  }
  
  public String toString()
  {
    List<String> stringValues = new ArrayList();
    for (T t : this) {
      stringValues.add(this.datatype.getDatatype().getString(t));
    }
    return ModelUtil.toCommaSeparatedList(stringValues.toArray(new Object[stringValues.size()]));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\csv\CSV.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */