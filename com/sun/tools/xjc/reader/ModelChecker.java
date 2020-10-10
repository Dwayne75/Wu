package com.sun.tools.xjc.reader;

import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

public final class ModelChecker
{
  private final Model model = (Model)Ring.get(Model.class);
  private final ErrorReceiver errorReceiver = (ErrorReceiver)Ring.get(ErrorReceiver.class);
  
  public void check()
  {
    for (CClassInfo ci : this.model.beans().values()) {
      check(ci);
    }
  }
  
  private void check(CClassInfo ci)
  {
    List<CPropertyInfo> props = ci.getProperties();
    Map<QName, CPropertyInfo> collisionTable = new HashMap();
    label272:
    for (int i = 0; i < props.size(); i++)
    {
      CPropertyInfo p1 = (CPropertyInfo)props.get(i);
      if (p1.getName(true).equals("Class"))
      {
        this.errorReceiver.error(p1.locator, Messages.PROPERTY_CLASS_IS_RESERVED.format(new Object[0]));
      }
      else
      {
        QName n = p1.collectElementNames(collisionTable);
        if (n != null)
        {
          CPropertyInfo p2 = (CPropertyInfo)collisionTable.get(n);
          this.errorReceiver.error(p1.locator, Messages.DUPLICATE_ELEMENT.format(new Object[] { n }));
          this.errorReceiver.error(p2.locator, Messages.ERR_RELEVANT_LOCATION.format(new Object[0]));
        }
        for (int j = i + 1; j < props.size(); j++) {
          if (checkPropertyCollision(p1, (CPropertyInfo)props.get(j))) {
            break label272;
          }
        }
        for (CClassInfo c = ci.getBaseClass(); c != null; c = c.getBaseClass()) {
          for (CPropertyInfo p2 : c.getProperties()) {
            if (checkPropertyCollision(p1, p2)) {
              break label272;
            }
          }
        }
      }
    }
  }
  
  private boolean checkPropertyCollision(CPropertyInfo p1, CPropertyInfo p2)
  {
    if (!p1.getName(true).equals(p2.getName(true))) {
      return false;
    }
    this.errorReceiver.error(p1.locator, Messages.DUPLICATE_PROPERTY.format(new Object[] { p1.getName(true) }));
    this.errorReceiver.error(p2.locator, Messages.ERR_RELEVANT_LOCATION.format(new Object[0]));
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\ModelChecker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */