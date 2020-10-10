package org.kohsuke.rngom.binary;

import java.util.ArrayList;
import java.util.List;
import org.kohsuke.rngom.nc.NameClass;

class DuplicateAttributeDetector
{
  private List nameClasses;
  private Alternative alternatives;
  
  DuplicateAttributeDetector()
  {
    this.nameClasses = new ArrayList();
    this.alternatives = null;
  }
  
  private static class Alternative
  {
    private int startIndex;
    private int endIndex;
    private Alternative parent;
    
    private Alternative(int startIndex, Alternative parent)
    {
      this.startIndex = startIndex;
      this.endIndex = startIndex;
      this.parent = parent;
    }
  }
  
  boolean addAttribute(NameClass nc)
  {
    int lim = this.nameClasses.size();
    for (Alternative a = this.alternatives; a != null; a = a.parent)
    {
      for (int i = a.endIndex; i < lim; i++) {
        if (nc.hasOverlapWith((NameClass)this.nameClasses.get(i))) {
          return false;
        }
      }
      lim = a.startIndex;
    }
    for (int i = 0; i < lim; i++) {
      if (nc.hasOverlapWith((NameClass)this.nameClasses.get(i))) {
        return false;
      }
    }
    this.nameClasses.add(nc);
    return true;
  }
  
  void startChoice()
  {
    this.alternatives = new Alternative(this.nameClasses.size(), this.alternatives, null);
  }
  
  void alternative()
  {
    this.alternatives.endIndex = this.nameClasses.size();
  }
  
  void endChoice()
  {
    this.alternatives = this.alternatives.parent;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\DuplicateAttributeDetector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */