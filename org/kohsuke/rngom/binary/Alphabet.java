package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.nc.ChoiceNameClass;
import org.kohsuke.rngom.nc.NameClass;

class Alphabet
{
  private NameClass nameClass;
  
  boolean isEmpty()
  {
    return this.nameClass == null;
  }
  
  void addElement(NameClass nc)
  {
    if (this.nameClass == null) {
      this.nameClass = nc;
    } else if (nc != null) {
      this.nameClass = new ChoiceNameClass(this.nameClass, nc);
    }
  }
  
  void addAlphabet(Alphabet a)
  {
    addElement(a.nameClass);
  }
  
  void checkOverlap(Alphabet a)
    throws RestrictionViolationException
  {
    if ((this.nameClass != null) && (a.nameClass != null) && (this.nameClass.hasOverlapWith(a.nameClass))) {
      throw new RestrictionViolationException("interleave_element_overlap");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\Alphabet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */