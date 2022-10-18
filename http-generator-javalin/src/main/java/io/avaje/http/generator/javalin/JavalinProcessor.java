package io.avaje.http.generator.javalin;

import java.io.IOException;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;

public class JavalinProcessor extends BaseProcessor {

  private boolean useJsonB;

  public JavalinProcessor() {
    try {
      Class.forName("io.avaje.jsonb.Jsonb");
      useJsonB = true;
    } catch (final ClassNotFoundException e) {
      useJsonB = false;
    }
  }

  public JavalinProcessor(boolean useJsonb) {
    useJsonB = useJsonb;
  }

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new JavalinAdapter();
  }

  @Override
  public void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader)
      throws IOException {
    new ControllerWriter(reader, ctx, useJsonB).write();
  }
}
