package com.sun.tools.xjc.addon.at_generated;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.Driver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.xml.sax.ErrorHandler;

public class PluginImpl
  extends Plugin
{
  private JClass annotation;
  
  public String getOptionName()
  {
    return "mark-generated";
  }
  
  public String getUsage()
  {
    return "  -mark-generated    :  mark the generated code as @javax.annotation.Generated";
  }
  
  public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
  {
    this.annotation = model.getCodeModel().ref("javax.annotation.Generated");
    for (ClassOutline co : model.getClasses()) {
      augument(co);
    }
    for (EnumOutline eo : model.getEnums()) {
      augument(eo);
    }
    return true;
  }
  
  private void augument(EnumOutline eo)
  {
    annotate(eo.clazz);
  }
  
  private void augument(ClassOutline co)
  {
    annotate(co.implClass);
    for (JMethod m : co.implClass.methods()) {
      annotate(m);
    }
    for (JFieldVar f : co.implClass.fields().values()) {
      annotate(f);
    }
  }
  
  private void annotate(JAnnotatable m)
  {
    m.annotate(this.annotation).param("value", Driver.class.getName()).param("date", getISO8601Date()).param("comments", "JAXB RI v" + Options.getBuildID());
  }
  
  private String date = null;
  
  private String getISO8601Date()
  {
    if (this.date == null)
    {
      StringBuffer tstamp = new StringBuffer();
      tstamp.append(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").format(new Date()));
      
      tstamp.insert(tstamp.length() - 2, ':');
      this.date = tstamp.toString();
    }
    return this.date;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\addon\at_generated\PluginImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */