package com.sun.tools.xjc.reader.dtd;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.msv.datatype.xsd.DatatypeFactory;
import com.sun.msv.datatype.xsd.IDREFType;
import com.sun.msv.datatype.xsd.IDType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.trex.TREXGrammar;
import com.sun.msv.grammar.trex.TREXGrammar.RefContainer;
import com.sun.msv.reader.AbortException;
import com.sun.msv.reader.Controller;
import com.sun.msv.reader.dtd.DTDReader;
import com.sun.msv.scanner.dtd.DTDParser;
import com.sun.msv.scanner.dtd.InputEntity;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.id.IDREFTransducer;
import com.sun.tools.xjc.grammar.id.IDTransducer;
import com.sun.tools.xjc.reader.GrammarReaderControllerAdaptor;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;
import com.sun.tools.xjc.reader.annotator.Annotator;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.tools.xjc.reader.annotator.FieldCollisionChecker;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIAttribute;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIContent;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIContent.MismatchException;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIElement;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIInterface;
import com.sun.tools.xjc.reader.dtd.bindinfo.BindInfo;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.JAXBAssertionError;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import org.relaxng.datatype.DatatypeException;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public class TDTDReader
  extends DTDReader
  implements AnnotatorController, PackageTracker
{
  private AnnotatedGrammar annGrammar;
  private final Options opts;
  private final BindInfo bindInfo;
  
  public static AnnotatedGrammar parse(InputSource dtd, InputSource bindingInfo, ErrorReceiver errorReceiver, Options opts, ExpressionPool pool)
  {
    try
    {
      TDTDReader reader = new TDTDReader(new GrammarReaderControllerAdaptor(errorReceiver, opts.entityResolver), opts, pool, bindingInfo);
      
      DTDParser parser = new DTDParser();
      parser.setDtdHandler(reader);
      if (opts.entityResolver != null) {
        parser.setEntityResolver(opts.entityResolver);
      }
      try
      {
        parser.parse(dtd);
      }
      catch (SAXParseException e)
      {
        return null;
      }
      return reader.getAnnotatedResult();
    }
    catch (IOException e)
    {
      errorReceiver.error(new SAXParseException(e.getMessage(), null, e));
      return null;
    }
    catch (SAXException e)
    {
      errorReceiver.error(new SAXParseException(e.getMessage(), null, e));
      return null;
    }
    catch (AbortException e) {}
    return null;
  }
  
  protected TDTDReader(GrammarReaderControllerAdaptor _controller, Options opts, ExpressionPool pool, InputSource _bindInfo)
    throws AbortException
  {
    super(_controller, pool);
    this.opts = opts;
    this.bindInfo = new BindInfo(_bindInfo, _controller, this.codeModel, opts);
    this.errorReceiver = _controller;
    this.classFactory = new CodeModelClassFactory(this.errorReceiver);
  }
  
  private final JCodeModel codeModel = new JCodeModel();
  private final CodeModelClassFactory classFactory;
  private final ErrorReceiver errorReceiver;
  
  public AnnotatedGrammar getAnnotatedResult()
  {
    if (this.controller.hadError()) {
      return null;
    }
    return this.annGrammar;
  }
  
  public void startDTD(InputEntity entity)
    throws SAXException
  {
    super.startDTD(entity);
    
    this.annGrammar = new AnnotatedGrammar(this.grammar, this.codeModel);
  }
  
  public void endDTD()
    throws SAXException
  {
    super.endDTD();
    if (this.controller.hadError()) {
      return;
    }
    resetStartPattern();
    
    processInterfaceDeclarations();
    
    this.annGrammar.exp = this.grammar.getTopLevel();
    
    this.annGrammar.serialVersionUID = this.bindInfo.getSerialVersionUID();
    this.annGrammar.rootClass = this.bindInfo.getSuperClass();
    
    Annotator.annotate(this.annGrammar, this);
    FieldCollisionChecker.check(this.annGrammar, this);
    
    processConstructorDeclarations();
  }
  
  private void resetStartPattern()
  {
    Expression exp = Expression.nullSet;
    
    Iterator itr = this.bindInfo.elements();
    while (itr.hasNext())
    {
      BIElement e = (BIElement)itr.next();
      if (e.isRoot())
      {
        ReferenceExp rexp = this.grammar.namedPatterns.getOrCreate(e.name());
        if (!rexp.isDefined()) {
          error(e.getSourceLocation(), "TDTDReader.UndefinedElementInBindInfo", e.name());
        } else {
          exp = this.grammar.pool.createChoice(exp, rexp);
        }
      }
    }
    if (exp != Expression.nullSet) {
      this.grammar.exp = exp;
    }
  }
  
  private void processInterfaceDeclarations()
  {
    Map decls = new HashMap();
    Iterator itr = this.bindInfo.interfaces();
    while (itr.hasNext())
    {
      BIInterface decl = (BIInterface)itr.next();
      
      decls.put(decl, this.annGrammar.createInterfaceItem(this.classFactory.createInterface(getTargetPackage(), decl.name(), copyLocator()), Expression.nullSet, copyLocator()));
    }
    Map fromName = new HashMap();
    
    itr = this.annGrammar.iterateClasses();
    while (itr.hasNext())
    {
      ClassItem ci = (ClassItem)itr.next();
      fromName.put(ci.getTypeAsDefined().name(), ci);
    }
    itr = this.annGrammar.iterateInterfaces();
    while (itr.hasNext())
    {
      InterfaceItem itf = (InterfaceItem)itr.next();
      fromName.put(itf.getTypeAsClass().name(), itf);
    }
    itr = decls.entrySet().iterator();
    while (itr.hasNext())
    {
      Map.Entry e = (Map.Entry)itr.next();
      BIInterface decl = (BIInterface)e.getKey();
      InterfaceItem item = (InterfaceItem)e.getValue();
      
      String[] members = decl.members();
      for (int i = 0; i < members.length; i++)
      {
        Expression exp = (Expression)fromName.get(members[i]);
        if (exp == null) {
          error(decl.getSourceLocation(), "TDTDReader.BindInfo.NonExistentInterfaceMember", members[i]);
        } else {
          item.exp = this.annGrammar.getPool().createChoice(item.exp, exp);
        }
      }
    }
  }
  
  private JPackage getTargetPackage()
  {
    if (this.opts.defaultPackage != null) {
      return this.codeModel._package(this.opts.defaultPackage);
    }
    return this.bindInfo.getTargetPackage();
  }
  
  private void processConstructorDeclarations()
  {
    Iterator itr = this.bindInfo.elements();
    while (itr.hasNext())
    {
      BIElement decl = (BIElement)itr.next();
      ReferenceExp rexp = this.grammar.namedPatterns._get(decl.name());
      if (rexp == null)
      {
        error(decl.getSourceLocation(), "TDTDReader.BindInfo.NonExistentElementDeclaration", decl.name());
      }
      else if (decl.isClass())
      {
        _assert(rexp.exp instanceof ClassItem);
        
        ClassItem ci = (ClassItem)rexp.exp;
        
        decl.declareConstructors(ci, this);
      }
    }
  }
  
  protected Expression createAttributeBody(String elementName, String attributeName, String attributeType, String[] enums, short attributeUse, String defaultValue)
    throws SAXException
  {
    Expression exp = super.createAttributeBody(elementName, attributeName, attributeType, enums, attributeUse, defaultValue);
    
    BIElement edecl = this.bindInfo.element(elementName);
    BIAttribute decl = null;
    if (edecl != null) {
      decl = edecl.attribute(attributeName);
    }
    if (decl != null)
    {
      BIConversion conv = decl.getConversion();
      if (conv != null) {
        exp = this.annGrammar.createPrimitiveItem(conv.getTransducer(), StringType.theInstance, exp, copyLocator());
      }
      FieldItem fi = new FieldItem(decl.getPropertyName(), exp, copyLocator());
      fi.realization = decl.getRealization();
      exp = fi;
    }
    else
    {
      if (attributeType.equals("ID")) {
        exp = this.annGrammar.createPrimitiveItem(new IDTransducer(this.codeModel, this.annGrammar.defaultSymbolSpace), IDType.theInstance, exp, copyLocator());
      }
      if (attributeType.equals("IDREF")) {
        exp = this.annGrammar.createPrimitiveItem(new IDREFTransducer(this.codeModel, this.annGrammar.defaultSymbolSpace, true), IDREFType.theInstance, exp, copyLocator());
      }
      if (attributeType.equals("IDREFS")) {
        try
        {
          exp = this.grammar.pool.createList(this.grammar.pool.createOneOrMore(this.annGrammar.createPrimitiveItem(new IDREFTransducer(this.codeModel, this.annGrammar.defaultSymbolSpace, false), DatatypeFactory.getTypeByName("IDREFS"), this.grammar.pool.createData(IDREFType.theInstance), copyLocator())));
        }
        catch (DatatypeException e)
        {
          e.printStackTrace();
          throw new JAXBAssertionError();
        }
      }
      exp = new FieldItem(NameConverter.standard.toPropertyName(attributeName), exp, copyLocator());
    }
    return exp;
  }
  
  protected ReferenceExp createElementDeclaration(String elementName)
  {
    BIElement decl = this.bindInfo.element(elementName);
    
    Locator loc = getDeclaredLocationOf(this.grammar.namedPatterns.getOrCreate(elementName));
    if ((decl == null) || (decl.isClass())) {
      this.elementDecls.put(elementName, performContentAnnotation(elementName, decl, (Expression)this.elementDecls.get(elementName), loc));
    }
    ReferenceExp exp = super.createElementDeclaration(elementName);
    
    ElementExp eexp = (ElementExp)exp.exp;
    if (decl == null)
    {
      exp.exp = new ClassCandidateItem(this.classFactory, this.annGrammar, getTargetPackage(), getNameConverter().toClassName(elementName), loc, eexp);
    }
    else if (decl.isClass())
    {
      ClassItem t = this.annGrammar.createClassItem(decl.getClassObject(), eexp, loc);
      setDeclaredLocationOf(t);
      exp.exp = t;
    }
    else
    {
      if (eexp.contentModel != Expression.anyString) {
        error(eexp, "TDTDReader.ConversionForNonValueElement", elementName);
      }
      BIConversion cnv = decl.getConversion();
      if (cnv != null)
      {
        PrimitiveItem pi = this.annGrammar.createPrimitiveItem(cnv.getTransducer(), StringType.theInstance, eexp, loc);
        
        exp.exp = pi;
      }
      else
      {
        PrimitiveItem pi = this.annGrammar.createPrimitiveItem(this.codeModel, StringType.theInstance, eexp, loc);
        
        exp.exp = pi;
      }
    }
    return exp;
  }
  
  private Expression performContentAnnotation(String elementName, BIElement decl, Expression exp, Locator loc)
  {
    if ((exp == Expression.anyString) && (decl == null)) {
      return exp;
    }
    if (exp == Expression.epsilon) {
      return exp;
    }
    Expression[] children;
    Expression[] children;
    if ((exp instanceof SequenceExp)) {
      children = ((SequenceExp)exp).getChildren();
    } else {
      children = new Expression[] { exp };
    }
    int idx = 0;
    
    Expression newContentModel = Expression.epsilon;
    if (decl != null)
    {
      Iterator itr = decl.iterateContents();
      while (itr.hasNext()) {
        try
        {
          BIContent bic = (BIContent)itr.next();
          if (idx == children.length) {
            throw new BIContent.MismatchException();
          }
          newContentModel = this.grammar.pool.createSequence(newContentModel, bic.wrap(children[idx]));
          
          idx++;
        }
        catch (BIContent.MismatchException mme)
        {
          error(exp, "TDTDReader.ContentProperty.ParticleMismatch", elementName);
        }
      }
    }
    BIContent restDecl = decl != null ? decl.getRest() : null;
    if (restDecl != null)
    {
      Expression rest = Expression.epsilon;
      while (idx < children.length) {
        rest = this.grammar.pool.createSequence(rest, children[(idx++)]);
      }
      FieldItem fi = new FieldItem(restDecl.getPropertyName(), rest, restDecl.getType(), loc);
      
      fi.realization = restDecl.getRealization();
      rest = fi;
      
      newContentModel = this.grammar.pool.createSequence(newContentModel, rest);
    }
    else
    {
      for (int i = idx; i < children.length; i++)
      {
        Expression item = children[i].peelOccurence();
        if ((!(item instanceof ReferenceExp)) || (item == getAnyExp())) {
          break;
        }
      }
      if (i != children.length)
      {
        if (idx == 0)
        {
          newContentModel = new FieldItem("Content", exp, loc);
        }
        else
        {
          error(exp, "TDTDReader.ContentProperty.DeclarationTooShort", elementName);
          
          return Expression.nullSet;
        }
      }
      else {
        for (i = idx; i < children.length; i++) {
          newContentModel = this.grammar.pool.createSequence(newContentModel, new FieldItem(NameConverter.standard.toPropertyName(((ReferenceExp)children[i].peelOccurence()).name), children[i], loc));
        }
      }
    }
    return newContentModel;
  }
  
  private Locator copyLocator()
  {
    return new LocatorImpl(this.locator);
  }
  
  public JPackage get(ReferenceExp exp)
  {
    return getTargetPackage();
  }
  
  public NameConverter getNameConverter()
  {
    return NameConverter.standard;
  }
  
  public PackageTracker getPackageTracker()
  {
    return this;
  }
  
  public void reportError(Expression[] srcs, String msg)
  {
    Vector vec = new Vector();
    for (int i = 0; i < srcs.length; i++)
    {
      Locator loc = getDeclaredLocationOf(srcs[i]);
      if (loc != null) {
        vec.add(loc);
      }
    }
    reportError((Locator[])vec.toArray(new Locator[0]), msg);
  }
  
  public void reportError(Locator[] locs, String msg)
  {
    this.controller.error(locs, msg, null);
  }
  
  public ErrorReceiver getErrorReceiver()
  {
    return this.errorReceiver;
  }
  
  protected final void error(Expression loc, String prop)
  {
    error(loc, prop, null);
  }
  
  protected final void error(Expression loc, String prop, Object arg1)
  {
    error(loc, prop, new Object[] { arg1 });
  }
  
  protected final void error(Expression loc, String prop, Object[] args)
  {
    reportError(new Expression[] { loc }, Messages.format(prop, args));
  }
  
  protected final void error(Locator loc, String prop, Object arg1)
  {
    error(loc, prop, new Object[] { arg1 });
  }
  
  protected final void error(Locator loc, String prop, Object[] args)
  {
    reportError(new Locator[] { loc }, Messages.format(prop, args));
  }
  
  private static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\TDTDReader.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */