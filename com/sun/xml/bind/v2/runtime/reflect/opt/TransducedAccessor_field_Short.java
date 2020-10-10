package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;

public final class TransducedAccessor_field_Short
  extends DefaultTransducedAccessor
{
  public String print(Object o)
  {
    return DatatypeConverterImpl._printShort(((Bean)o).f_short);
  }
  
  public void parse(Object o, CharSequence lexical)
  {
    ((Bean)o).f_short = DatatypeConverterImpl._parseShort(lexical);
  }
  
  public boolean hasValue(Object o)
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\TransducedAccessor_field_Short.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */