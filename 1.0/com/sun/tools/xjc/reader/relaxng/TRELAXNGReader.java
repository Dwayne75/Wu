package com.sun.tools.xjc.reader.relaxng;

import com.sun.codemodel.JCodeModel;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.trex.TREXGrammar;
import com.sun.msv.reader.Controller;
import com.sun.msv.reader.State;
import com.sun.msv.reader.trex.ng.RELAXNGReader;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.GrammarReaderControllerAdaptor;
import com.sun.tools.xjc.reader.HierarchicalPackageTracker;
import com.sun.tools.xjc.reader.StackPackageManager;
import com.sun.tools.xjc.reader.annotator.Annotator;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.tools.xjc.reader.annotator.AnnotatorControllerImpl;
import com.sun.tools.xjc.reader.decorator.RoleBasedDecorator;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

public class TRELAXNGReader
  extends RELAXNGReader
{
  public TRELAXNGReader(ErrorReceiver errorReceiver, EntityResolver entityResolver, SAXParserFactory parserFactory, String defaultPackage)
  {
    this(new GrammarReaderControllerAdaptor(errorReceiver, entityResolver), parserFactory, defaultPackage);
  }
  
  private TRELAXNGReader(GrammarReaderControllerAdaptor _controller, SAXParserFactory parserFactory, String defaultPackage)
  {
    super(_controller, parserFactory);
    if (defaultPackage == null) {
      defaultPackage = "generated";
    }
    this.packageManager = new StackPackageManager(this.annGrammar.codeModel._package(defaultPackage));
    
    this.classFactory = new CodeModelClassFactory(_controller);
    this.annController = new AnnotatorControllerImpl(this, _controller, this.packageTracker);
    
    this.decorator = new RoleBasedDecorator(this, _controller, this.annGrammar, this.annController.getNameConverter(), this.packageManager, new DefaultDecorator(this, this.annController.getNameConverter()));
  }
  
  protected final HierarchicalPackageTracker packageTracker = new HierarchicalPackageTracker();
  protected final StackPackageManager packageManager;
  private final RoleBasedDecorator decorator;
  protected final CodeModelClassFactory classFactory;
  private final AnnotatorController annController;
  protected final AnnotatedGrammar annGrammar = new AnnotatedGrammar(this.pool);
  
  public AnnotatedGrammar getAnnotatedResult()
  {
    return this.annGrammar;
  }
  
  protected Expression interceptExpression(State state, Expression exp)
  {
    exp = super.interceptExpression(state, exp);
    if (this.controller.hadError()) {
      return exp;
    }
    if (exp == null) {
      return exp;
    }
    exp = this.decorator.decorate(state, exp);
    
    this.packageTracker.associate(exp, this.packageManager.getCurrentPackage());
    
    return exp;
  }
  
  public void wrapUp()
  {
    super.wrapUp();
    if (this.controller.hadError()) {
      return;
    }
    this.packageTracker.associate(this.annGrammar, this.packageManager.getCurrentPackage());
    
    this.annGrammar.exp = this.grammar.exp;
    Annotator.annotate(this.annGrammar, this.annController);
    this.grammar.exp = this.annGrammar.exp;
  }
  
  public void startElement(String a, String b, String c, Attributes atts)
    throws SAXException
  {
    this.packageManager.startElement(atts);
    super.startElement(a, b, c, atts);
  }
  
  public void endElement(String a, String b, String c)
    throws SAXException
  {
    super.endElement(a, b, c);
    this.packageManager.endElement();
  }
  
  protected String localizeMessage(String propertyName, Object[] args)
  {
    try
    {
      format = ResourceBundle.getBundle("com.sun.tools.xjc.reader.relaxng.Messages").getString(propertyName);
    }
    catch (Exception e)
    {
      try
      {
        String format;
        format = ResourceBundle.getBundle("com.sun.tools.xjc.reader.Messages").getString(propertyName);
      }
      catch (Exception ee)
      {
        String format;
        return super.localizeMessage(propertyName, args);
      }
    }
    String format;
    return MessageFormat.format(format, args);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\relaxng\TRELAXNGReader.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */