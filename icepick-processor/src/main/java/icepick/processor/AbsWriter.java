package icepick.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import javax.tools.JavaFileObject;

abstract class AbsWriter {

  protected static final String BASE_KEY = "BASE_KEY";

  private final JavaFileObject javaFileObject;
  private final String suffix;
  private final EnclosingClass enclosingClass;

  public AbsWriter(JavaFileObject jfo, String suffix, EnclosingClass enclosingClass) {
    this.javaFileObject = jfo;
    this.suffix = suffix;
    this.enclosingClass = enclosingClass;
  }

  public void withFields(Collection<AnnotatedField> annotatedFields) throws IOException {
    Writer writer = javaFileObject.openWriter();
    writer.write(brewJava(annotatedFields));
    writer.flush();
    writer.close();
  }

  private String brewJava(Collection<AnnotatedField> annotatedFields) {
    StringBuilder builder = new StringBuilder();
    builder.append("// Generated code from Icepick. Do not modify!\n");
    builder.append("package ").append(enclosingClass.getClassPackage()).append(";\n\n");
    builder.append("import android.os.Bundle;\n");
    builder.append("import android.os.Parcelable;\n");
    builder.append("public class ").append(enclosingClass.getClassName() + suffix).
        append(" implements icepick.StateHelper<" + getType() + "> {\n");
    builder.append("  private static final String ").append(BASE_KEY).append(" = \"")
        .append(enclosingClass.getClassPackage()).append(".")
        .append(enclosingClass.getClassName() + suffix).append(".\";\n");

    builder.append(emitRestoreStateStart(enclosingClass, suffix));
    for (AnnotatedField field : annotatedFields) {
      builder.append(emitRestoreState(field));
    }
    builder.append(emitRestoreStateEnd(enclosingClass, suffix));

    builder.append('\n');

    builder.append(emitSaveStateStart(enclosingClass, suffix));
    for (AnnotatedField field : annotatedFields) {
      builder.append(emitSaveState(field));
    }

    builder.append(emitSaveStateEnd(enclosingClass, suffix));

    builder.append("}\n");
    return builder.toString();

  }

  protected abstract String getType();

  protected abstract String emitRestoreStateStart(EnclosingClass enclosingClass, String suffix);

  protected String emitRestoreState(AnnotatedField field) {
    return "    target." + field.getName() + " = " + field.getTypeCast() + " savedInstanceState.get"
        + field.getBundleMethod() + "(" + BASE_KEY + " + \"" + field.getName() + "\");\n";
  }

  protected abstract String emitRestoreStateEnd(EnclosingClass enclosingClass, String suffix);

  protected abstract String emitSaveStateStart(EnclosingClass enclosingClass, String suffix);

  protected String emitSaveState(AnnotatedField field) {
    return "    outState.put" + field.getBundleMethod() + "(" + BASE_KEY + " + \""
        + field.getName() + "\", target." + field.getName() + ");\n";
  }

  protected abstract String emitSaveStateEnd(EnclosingClass enclosingClass, String suffix);
}
