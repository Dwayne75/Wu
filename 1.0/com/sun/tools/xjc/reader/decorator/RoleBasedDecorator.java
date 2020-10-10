package com.sun.tools.xjc.reader.decorator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.reader.Controller;
import com.sun.msv.reader.GrammarReader;
import com.sun.msv.reader.State;
import com.sun.msv.util.StartTagInfo;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.field.ArrayFieldRenderer;
import com.sun.tools.xjc.generator.field.TypedListFieldRenderer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.ext.DOMItemFactory;
import com.sun.tools.xjc.grammar.ext.DOMItemFactory.UndefinedNameException;
import com.sun.tools.xjc.grammar.util.NameFinder;
import com.sun.tools.xjc.grammar.xducer.UserTransducer;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageManager;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class RoleBasedDecorator
  extends DecoratorImpl
{
  private final CodeModelClassFactory classFactory;
  private final Decorator defaultDecorator;
  private final PackageManager packageManager;
  
  public RoleBasedDecorator(GrammarReader _reader, ErrorReceiver _errorReceiver, AnnotatedGrammar _grammar, NameConverter _nc, PackageManager pkgMan, Decorator _defaultDecorator)
  {
    super(_reader, _grammar, _nc);
    this.defaultDecorator = _defaultDecorator;
    this.packageManager = pkgMan;
    this.classFactory = new CodeModelClassFactory(_errorReceiver);
  }
  
  public Expression decorate(State state, Expression exp)
  {
    StartTagInfo tag = state.getStartTag();
    
    String role = getAttribute(tag, "role");
    if (role == null)
    {
      if (this.defaultDecorator != null) {
        exp = this.defaultDecorator.decorate(state, exp);
      }
      return exp;
    }
    role = role.intern();
    if (role == "none") {
      return exp;
    }
    OtherExp roleExp;
    OtherExp roleExp;
    if (role == "superClass")
    {
      roleExp = new SuperClassItem(null, state.getLocation());
    }
    else
    {
      OtherExp roleExp;
      if (role == "class")
      {
        roleExp = this.grammar.createClassItem(this.classFactory.createInterface(this.packageManager.getCurrentPackage(), decideName(state, exp, role, "", state.getLocation()), state.getLocation()), null, state.getLocation());
      }
      else if (role == "field")
      {
        String collection = getAttribute(tag, "collection");
        String typeAtt = getAttribute(tag, "baseType");
        String delegation = getAttribute(tag, "delegate");
        
        JClass type = null;
        if (typeAtt != null) {
          try
          {
            type = this.codeModel.ref(typeAtt);
          }
          catch (ClassNotFoundException e)
          {
            reportError(Messages.format("ClassNotFound", typeAtt), state.getLocation());
          }
        }
        FieldItem fi = new FieldItem(decideName(state, exp, role, "", state.getLocation()), null, type, this.reader.locator);
        
        OtherExp roleExp = fi;
        if ((delegation != null) && (delegation.equals("true"))) {
          fi.setDelegation(true);
        }
        if (collection != null)
        {
          if (collection.equals("array")) {
            fi.realization = ArrayFieldRenderer.theFactory;
          }
          if (collection.equals("list")) {
            fi.realization = TypedListFieldRenderer.theFactory;
          }
          if (fi.realization == null) {
            reportError(Messages.format("InvalidCollectionType", collection), state.getLocation());
          }
        }
      }
      else
      {
        OtherExp roleExp;
        if (role == "interface")
        {
          roleExp = this.grammar.createInterfaceItem(this.classFactory.createInterface(this.packageManager.getCurrentPackage(), decideName(state, exp, role, "", state.getLocation()), state.getLocation()), null, state.getLocation());
        }
        else
        {
          OtherExp roleExp;
          if (role == "primitive")
          {
            String name = getAttribute(tag, "name");
            String parseMethod = getAttribute(tag, "parseMethod");
            String printMethod = getAttribute(tag, "printMethod");
            boolean hasNsContext = BooleanType.load(getAttribute(tag, "hasNsContext", "false")).booleanValue();
            try
            {
              roleExp = this.grammar.createPrimitiveItem(new UserTransducer(TypeUtil.getType(this.codeModel, name, this.reader.controller, state.getLocation()), parseMethod != null ? parseMethod : "new", printMethod != null ? printMethod : "toString", hasNsContext), StringType.theInstance, null, state.getLocation());
            }
            catch (SAXException e)
            {
              OtherExp roleExp;
              roleExp = new OtherExp();
            }
            catch (IllegalArgumentException e)
            {
              OtherExp roleExp;
              reportError(e.getMessage(), state.getLocation());
              roleExp = new OtherExp();
            }
          }
          else
          {
            OtherExp roleExp;
            if (role == "dom")
            {
              String type = getAttribute(tag, "type");
              if (type == null) {
                type = "w3c";
              }
              try
              {
                roleExp = DOMItemFactory.getInstance(type).create(NameFinder.findElement(exp), this.grammar, state.getLocation());
              }
              catch (DOMItemFactory.UndefinedNameException e)
              {
                OtherExp roleExp;
                reportError(e.getMessage(), state.getLocation());
                return exp;
              }
            }
            else if (role == "ignore")
            {
              roleExp = new IgnoreItem(state.getLocation());
            }
            else
            {
              reportError(Messages.format("UndefinedRole", role), state.getLocation());
              return exp;
            }
          }
        }
      }
    }
    this.reader.setDeclaredLocationOf(roleExp);
    
    roleExp.exp = exp;
    return roleExp;
  }
  
  private void reportError(String msg, Locator locator)
  {
    this.reader.controller.error(new Locator[] { locator }, msg, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\decorator\RoleBasedDecorator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */