package com.sun.tools.xjc.addon.episode;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.schemagen.episode.Bindings;
import com.sun.xml.bind.v2.schemagen.episode.Klass;
import com.sun.xml.bind.v2.schemagen.episode.SchemaBindings;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.StreamSerializer;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSFunction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class PluginImpl
  extends Plugin
{
  private File episodeFile;
  
  public String getOptionName()
  {
    return "episode";
  }
  
  public String getUsage()
  {
    return "  -episode <FILE>    :  generate the episode file for separate compilation";
  }
  
  public int parseArgument(Options opt, String[] args, int i)
    throws BadCommandLineException, IOException
  {
    if (args[i].equals("-episode"))
    {
      this.episodeFile = new File(opt.requireArgument("-episode", args, ++i));
      return 2;
    }
    return 0;
  }
  
  public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
    throws SAXException
  {
    try
    {
      Map<XSSchema, List<ClassOutline>> perSchema = new HashMap();
      boolean hasComponentInNoNamespace = false;
      for (ClassOutline co : model.getClasses())
      {
        XSComponent sc = co.target.getSchemaComponent();
        if ((sc != null) && 
          ((sc instanceof XSDeclaration)))
        {
          XSDeclaration decl = (XSDeclaration)sc;
          if (!decl.isLocal())
          {
            List<ClassOutline> list = (List)perSchema.get(decl.getOwnerSchema());
            if (list == null)
            {
              list = new ArrayList();
              perSchema.put(decl.getOwnerSchema(), list);
            }
            list.add(co);
            if (decl.getTargetNamespace().equals("")) {
              hasComponentInNoNamespace = true;
            }
          }
        }
      }
      OutputStream os = new FileOutputStream(this.episodeFile);
      Bindings bindings = (Bindings)TXW.create(Bindings.class, new StreamSerializer(os, "UTF-8"));
      if (hasComponentInNoNamespace) {
        bindings._namespace("http://java.sun.com/xml/ns/jaxb", "jaxb");
      } else {
        bindings._namespace("http://java.sun.com/xml/ns/jaxb", "");
      }
      bindings.version("2.1");
      bindings._comment("\n\n" + opt.getPrologComment() + "\n  ");
      for (Map.Entry<XSSchema, List<ClassOutline>> e : perSchema.entrySet())
      {
        Bindings group = bindings.bindings();
        String tns = ((XSSchema)e.getKey()).getTargetNamespace();
        if (!tns.equals("")) {
          group._namespace(tns, "tns");
        }
        group.scd("x-schema::" + (tns.equals("") ? "" : "tns"));
        group.schemaBindings().map(false);
        for (ClassOutline co : (List)e.getValue())
        {
          Bindings child = group.bindings();
          child.scd((String)co.target.getSchemaComponent().apply(SCD));
          child.klass().ref(co.implClass.fullName());
        }
        group.commit(true);
      }
      bindings.commit();
      
      return true;
    }
    catch (IOException e)
    {
      errorHandler.error(new SAXParseException("Failed to write to " + this.episodeFile, null, e));
    }
    return false;
  }
  
  private static final XSFunction<String> SCD = new XSFunction()
  {
    private String name(XSDeclaration decl)
    {
      if (decl.getTargetNamespace().equals("")) {
        return decl.getName();
      }
      return "tns:" + decl.getName();
    }
    
    public String complexType(XSComplexType type)
    {
      return "~" + name(type);
    }
    
    public String simpleType(XSSimpleType simpleType)
    {
      return "~" + name(simpleType);
    }
    
    public String elementDecl(XSElementDecl decl)
    {
      return name(decl);
    }
    
    public String annotation(XSAnnotation ann)
    {
      throw new UnsupportedOperationException();
    }
    
    public String attGroupDecl(XSAttGroupDecl decl)
    {
      throw new UnsupportedOperationException();
    }
    
    public String attributeDecl(XSAttributeDecl decl)
    {
      throw new UnsupportedOperationException();
    }
    
    public String attributeUse(XSAttributeUse use)
    {
      throw new UnsupportedOperationException();
    }
    
    public String schema(XSSchema schema)
    {
      throw new UnsupportedOperationException();
    }
    
    public String facet(XSFacet facet)
    {
      throw new UnsupportedOperationException();
    }
    
    public String notation(XSNotation notation)
    {
      throw new UnsupportedOperationException();
    }
    
    public String identityConstraint(XSIdentityConstraint decl)
    {
      throw new UnsupportedOperationException();
    }
    
    public String xpath(XSXPath xpath)
    {
      throw new UnsupportedOperationException();
    }
    
    public String particle(XSParticle particle)
    {
      throw new UnsupportedOperationException();
    }
    
    public String empty(XSContentType empty)
    {
      throw new UnsupportedOperationException();
    }
    
    public String wildcard(XSWildcard wc)
    {
      throw new UnsupportedOperationException();
    }
    
    public String modelGroupDecl(XSModelGroupDecl decl)
    {
      throw new UnsupportedOperationException();
    }
    
    public String modelGroup(XSModelGroup group)
    {
      throw new UnsupportedOperationException();
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\addon\episode\PluginImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */