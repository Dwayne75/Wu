package com.sun.tools.xjc.reader.relaxng;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent.Package;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.xml.bind.api.impl.NameConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DDefine;
import org.kohsuke.rngom.digested.DElementPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DRefPattern;
import org.kohsuke.rngom.digested.DValuePattern;
import org.kohsuke.rngom.nc.NameClass;

public final class RELAXNGCompiler
{
  final DPattern grammar;
  final Set<DDefine> defs;
  final Options opts;
  final Model model;
  final JPackage pkg;
  final Map<String, DatatypeLib> datatypes = new HashMap();
  final Map<DPattern, CTypeInfo[]> classes = new HashMap();
  final Map<CClassInfo, DPattern> bindQueue = new HashMap();
  final TypeUseBinder typeUseBinder = new TypeUseBinder(this);
  
  public static Model build(DPattern grammar, JCodeModel codeModel, Options opts)
  {
    RELAXNGCompiler compiler = new RELAXNGCompiler(grammar, codeModel, opts);
    compiler.compile();
    return compiler.model;
  }
  
  public RELAXNGCompiler(DPattern grammar, JCodeModel codeModel, Options opts)
  {
    this.grammar = grammar;
    this.opts = opts;
    this.model = new Model(opts, codeModel, NameConverter.smart, opts.classNameAllocator, null);
    
    this.datatypes.put("", DatatypeLib.BUILTIN);
    this.datatypes.put("http://www.w3.org/2001/XMLSchema-datatypes", DatatypeLib.XMLSCHEMA);
    
    DefineFinder deff = new DefineFinder();
    grammar.accept(deff);
    this.defs = deff.defs;
    if (opts.defaultPackage2 != null) {
      this.pkg = codeModel._package(opts.defaultPackage2);
    } else if (opts.defaultPackage != null) {
      this.pkg = codeModel._package(opts.defaultPackage);
    } else {
      this.pkg = codeModel.rootPackage();
    }
  }
  
  private void compile()
  {
    promoteElementDefsToClasses();
    promoteTypeSafeEnums();
    
    promoteTypePatternsToClasses();
    for (Map.Entry<CClassInfo, DPattern> e : this.bindQueue.entrySet()) {
      bindContentModel((CClassInfo)e.getKey(), (DPattern)e.getValue());
    }
  }
  
  private void bindContentModel(CClassInfo clazz, DPattern pattern)
  {
    pattern.accept(new ContentModelBinder(this, clazz));
  }
  
  private void promoteTypeSafeEnums()
  {
    List<CEnumConstant> members = new ArrayList();
    for (DDefine def : this.defs)
    {
      DPattern p = def.getPattern();
      if ((p instanceof DChoicePattern))
      {
        DChoicePattern cp = (DChoicePattern)p;
        
        members.clear();
        
        DValuePattern vp = null;
        
        Iterator i$ = cp.iterator();
        for (;;)
        {
          if (!i$.hasNext()) {
            break label203;
          }
          DPattern child = (DPattern)i$.next();
          if (!(child instanceof DValuePattern)) {
            break;
          }
          DValuePattern c = (DValuePattern)child;
          if (vp == null) {
            vp = c;
          } else {
            if ((!vp.getDatatypeLibrary().equals(c.getDatatypeLibrary())) || (!vp.getType().equals(c.getType()))) {
              break;
            }
          }
          members.add(new CEnumConstant(this.model.getNameConverter().toConstantName(c.getValue()), null, c.getValue(), c.getLocation()));
        }
        if (!members.isEmpty())
        {
          CNonElement base = CBuiltinLeafInfo.STRING;
          
          DatatypeLib lib = (DatatypeLib)this.datatypes.get(vp.getNs());
          if (lib != null)
          {
            TypeUse use = lib.get(vp.getType());
            if ((use instanceof CNonElement)) {
              base = (CNonElement)use;
            }
          }
          CEnumLeafInfo xducer = new CEnumLeafInfo(this.model, null, new CClassInfoParent.Package(this.pkg), def.getName(), base, new ArrayList(members), null, null, cp.getLocation());
          
          this.classes.put(cp, new CTypeInfo[] { xducer });
        }
      }
    }
    label203:
  }
  
  private void promoteElementDefsToClasses()
  {
    for (DDefine def : this.defs)
    {
      DPattern p = def.getPattern();
      if ((p instanceof DElementPattern))
      {
        DElementPattern ep = (DElementPattern)p;
        
        mapToClass(ep);
      }
    }
    this.grammar.accept(new DPatternWalker()
    {
      public Void onRef(DRefPattern p)
      {
        return null;
      }
      
      public Void onElement(DElementPattern p)
      {
        RELAXNGCompiler.this.mapToClass(p);
        return null;
      }
    });
  }
  
  private void mapToClass(DElementPattern p)
  {
    NameClass nc = p.getName();
    if (nc.isOpen()) {
      return;
    }
    Set<QName> names = nc.listNames();
    
    CClassInfo[] types = new CClassInfo[names.size()];
    int i = 0;
    for (QName n : names)
    {
      String name = this.model.getNameConverter().toClassName(n.getLocalPart());
      
      this.bindQueue.put(types[(i++)] = new CClassInfo(this.model, this.pkg, name, p.getLocation(), null, n, null, null), p.getChild());
    }
    this.classes.put(p, types);
  }
  
  private void promoteTypePatternsToClasses() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\relaxng\RELAXNGCompiler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */