package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.util.NotAllowedRemover;
import com.sun.tools.xjc.util.Util;
import com.sun.tools.xjc.writer.Writer;
import java.io.PrintStream;

public class Annotator
{
  private static PrintStream debug = Util.getSystemProperty(Annotator.class, "debug") != null ? System.out : null;
  
  public static void annotate(AnnotatedGrammar grammar, AnnotatorController controller)
  {
    if (debug != null)
    {
      debug.println("---------------------------------------------");
      debug.println("initial grammar");
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("removing notAllowed");
    }
    NotAllowedRemover visitor = new NotAllowedRemover(grammar.getPool());
    grammar.visit(visitor);
    if (grammar.exp == Expression.nullSet) {
      return;
    }
    ClassItem[] classes = grammar.getClasses();
    for (int i = 0; i < classes.length; i++) {
      classes[i].exp = classes[i].exp.visit(visitor);
    }
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("removing empty JavaItems");
    }
    EmptyJavaItemRemover visitor = new EmptyJavaItemRemover(grammar.getPool());
    grammar.visit(visitor);
    if (grammar.exp == Expression.nullSet) {
      return;
    }
    classes = grammar.getClasses();
    for (int i = 0; i < classes.length; i++) {
      classes[i].exp = classes[i].exp.visit(visitor);
    }
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("removing mixed");
    }
    MixedRemover visitor = new MixedRemover(grammar);
    grammar.visit(visitor);
    if (grammar.exp == Expression.nullSet) {
      return;
    }
    classes = grammar.getClasses();
    for (int i = 0; i < classes.length; i++) {
      classes[i].exp = classes[i].exp.visit(visitor);
    }
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("simplifying datatypes");
    }
    grammar.visit(new DatatypeSimplifier(grammar.getPool()));
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("examining primitive types");
    }
    PrimitiveTypeAnnotator visitor = new PrimitiveTypeAnnotator(grammar, controller);
    grammar.visit(visitor);
    if (grammar.exp == Expression.nullSet) {
      return;
    }
    classes = grammar.getClasses();
    for (int i = 0; i < classes.length; i++) {
      classes[i].exp = classes[i].exp.visit(visitor);
    }
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("annotating complex choices");
    }
    ChoiceAnnotator.annotate(grammar, controller);
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("removing temporary class items");
    }
    TemporaryClassItemRemover.remove(grammar);
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("adding field items");
    }
    FieldItemAnnotation.annotate(grammar, controller);
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("computing type hierarchy");
    }
    HierarchyAnnotator.annotate(grammar, controller);
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("determining types for symbol spaces");
    }
    SymbolSpaceTypeAssigner.assign(grammar, controller);
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
    if (debug != null) {
      debug.println("normalizing relations");
    }
    RelationNormalizer.normalize(grammar, controller);
    if (debug != null)
    {
      Writer.writeToConsole(true, grammar);
      debug.println("---------------------------------------------");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\Annotator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */