package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;

public final class TransducedAccessor_field_Boolean
  extends DefaultTransducedAccessor
{
  public String print(Object o)
  {
    return DatatypeConverterImpl._printBoolean(((Bean)o).f_boolean);
  }
  
  public void parse(Object o, CharSequence lexical)
  {
    ((Bean)o).f_boolean = DatatypeConverterImpl._parseBoolean(lexical);
  }
  
  public boolean hasValue(Object o)
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\TransducedAccessor_field_Boolean.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */