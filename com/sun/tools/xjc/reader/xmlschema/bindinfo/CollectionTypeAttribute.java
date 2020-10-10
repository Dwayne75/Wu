package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.model.Model;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

final class CollectionTypeAttribute
{
  @XmlValue
  String collectionType = null;
  @XmlTransient
  private FieldRenderer fr;
  
  FieldRenderer get(Model m)
  {
    if (this.fr == null) {
      this.fr = calcFr(m);
    }
    return this.fr;
  }
  
  private FieldRenderer calcFr(Model m)
  {
    FieldRendererFactory frf = m.options.getFieldRendererFactory();
    if (this.collectionType == null) {
      return frf.getDefault();
    }
    if (this.collectionType.equals("indexed")) {
      return frf.getArray();
    }
    return frf.getList(m.codeModel.ref(this.collectionType));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\CollectionTypeAttribute.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */