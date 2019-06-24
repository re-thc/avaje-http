package io.dinject.javalin.generator;

import io.dinject.javalin.generator.openapi.SchemaBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static io.dinject.javalin.generator.Constants.GENERATED;
import static io.dinject.javalin.generator.Constants.OPENAPIDEFINITION;

class ProcessingContext {

  private final Messager messager;
  private final Filer filer;
  private final Elements elements;
  private final Types types;
  private final SchemaBuilder schemaBuilder;
  private final boolean generatedAvailable;
  private final boolean openApiAvailable;

  private OpenAPI openAPI;

  private final Map<String, PathItem> pathMap = new TreeMap<>();

  ProcessingContext(ProcessingEnvironment env) {
    this.messager = env.getMessager();
    this.filer = env.getFiler();
    this.elements = env.getElementUtils();
    this.types = env.getTypeUtils();
    this.schemaBuilder = new SchemaBuilder(env.getTypeUtils(), elements);
    this.generatedAvailable = isTypeAvailable(GENERATED);
    this.openApiAvailable = isTypeAvailable(OPENAPIDEFINITION);
  }

  private boolean isTypeAvailable(String canonicalName) {
    return null != getTypeElement(canonicalName);
  }

  TypeElement getTypeElement(String canonicalName) {
    return elements.getTypeElement(canonicalName);
  }

  boolean isGeneratedAvailable() {
    return generatedAvailable;
  }

  boolean isOpenApiAvailable() {
    return openApiAvailable;
  }

  void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  void logDebug(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

//  public void logWarn(String msg, Object... args) {
//    messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
//  }

  /**
   * Create a file writer for the given class name.
   */
  JavaFileObject createWriter(String cls, Element origin) throws IOException {
    return filer.createSourceFile(cls, origin);
  }

  FileObject createResource(String path, String name, Element origin) throws IOException {
    return filer.createResource(StandardLocation.CLASS_OUTPUT, path, name, origin);
  }

  String getDocComment(Element param) {
    return elements.getDocComment(param);
  }

  OpenAPI getOpenAPI() {
    return openAPI;
  }

  OpenAPI getOpenAPIForWriting() {
    Paths paths = openAPI.getPaths();
    if (paths == null) {
      paths = new Paths();
      openAPI.setPaths(paths);
    }
    // add paths by natural order
    for (Map.Entry<String, PathItem> entry : pathMap.entrySet()) {
      paths.addPathItem(entry.getKey(), entry.getValue());
    }
    return openAPI;
  }

  void setOpenAPI(OpenAPI openAPI) {
    this.openAPI = openAPI;
    this.schemaBuilder.setOpenAPI(openAPI);
  }

  Schema toSchema(String rawType, Element element) {
    TypeElement typeElement = elements.getTypeElement(rawType);
    if (typeElement == null) {
      // primitive types etc
      return schemaBuilder.toSchema(element.asType());
    } else {
      return schemaBuilder.toSchema(typeElement.asType());
    }
  }

  Content createContent(TypeMirror returnType, String mediaType) {
    return schemaBuilder.createContent(returnType, mediaType);
  }

  Element asElement(TypeMirror typeMirror) {
    return types.asElement(typeMirror);
  }

  TypeMirror asMemberOf(DeclaredType declaredType, Element element) {
    return types.asMemberOf(declaredType, element);
  }

  PathItem pathItem(String fullPath) {
    return pathMap.computeIfAbsent(fullPath, s -> new PathItem());
  }
}
