package com.wurmonline.server.utils;

import com.wurmonline.shared.util.StringUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NameCountList
{
  final Map<String, Integer> localMap = new HashMap();
  
  public void add(String name)
  {
    int cnt = 1;
    if (this.localMap.containsKey(name)) {
      cnt = ((Integer)this.localMap.get(name)).intValue() + 1;
    }
    this.localMap.put(name, Integer.valueOf(cnt));
  }
  
  public boolean isEmpty()
  {
    return this.localMap.isEmpty();
  }
  
  public String toString()
  {
    String line = "";
    int count = 0;
    for (Map.Entry<String, Integer> entry : this.localMap.entrySet())
    {
      count++;
      if (line.length() > 0) {
        if (count == this.localMap.size()) {
          line = line + " and ";
        } else {
          line = line + ", ";
        }
      }
      line = line + StringUtilities.getWordForNumber(((Integer)entry.getValue()).intValue()) + " " + (String)entry.getKey();
    }
    return line;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\utils\NameCountList.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */