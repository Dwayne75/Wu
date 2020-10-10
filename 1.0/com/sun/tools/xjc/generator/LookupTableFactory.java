package com.sun.tools.xjc.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.xml.bind.JAXBAssertionError;

class LookupTableFactory
  implements LookupTableBuilder
{
  private JDefinedClass tableClass;
  private final JPackage pkg;
  private int id = 0;
  
  public LookupTableFactory(JPackage _pkg)
  {
    this.pkg = _pkg;
  }
  
  JDefinedClass getTableClass()
  {
    if (this.tableClass == null) {
      try
      {
        this.tableClass = this.pkg._class(1, "Table");
      }
      catch (JClassAlreadyExistsException e)
      {
        throw new JAXBAssertionError();
      }
    }
    return this.tableClass;
  }
  
  public LookupTableUse buildTable(ChoiceExp exp)
  {
    Expression[] children = exp.getChildren();
    if (children.length < 3) {
      return null;
    }
    int nullBranchCount = 0;
    LookupTableFactory.Branch[] branches = new LookupTableFactory.Branch[children.length];
    for (int i = 0; i < children.length; i++) {
      if ((branches[i] = LookupTableFactory.Branch.create(children[i])) == null) {
        nullBranchCount++;
      }
    }
    if (nullBranchCount > 1) {
      return null;
    }
    LookupTableFactory.Branch dominant;
    int anomaly = -1;
    if (LookupTableFactory.Branch.access$000(branches[0], branches[1]))
    {
      dominant = branches[0];
    }
    else if (LookupTableFactory.Branch.access$000(branches[0], branches[2]))
    {
      dominant = branches[0];
      anomaly = 1;
    }
    else if (LookupTableFactory.Branch.access$000(branches[1], branches[2]))
    {
      dominant = branches[1];
      anomaly = 0;
    }
    else
    {
      return null;
    }
    for (int i = 2; i < branches.length; i++) {
      if (!LookupTableFactory.Branch.access$000(dominant, branches[i]))
      {
        if (anomaly != -1) {
          return null;
        }
        anomaly = i;
      }
    }
    if (anomaly != -1) {
      branches[anomaly] = null;
    }
    LookupTable t = new LookupTable(this, this.id++);
    for (int i = 0; i < branches.length; i++) {
      if (branches[i] != null)
      {
        LookupTable.Entry e = branches[i].toEntry();
        if (!t.isConsistentWith(e)) {
          return null;
        }
        t.add(e);
      }
    }
    return new LookupTableUse(t, anomaly == -1 ? null : children[anomaly], dominant.attName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\LookupTableFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */