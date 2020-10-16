package com.notessensei.demo.jsonmystery;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import com.google.common.io.Resources;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

  public static void main(final String[] args) {
    final MainVerticle verticle = new MainVerticle();
    final VertxOptions options = new VertxOptions()
        .setBlockedThreadCheckIntervalUnit(TimeUnit.MINUTES)
        .setBlockedThreadCheckInterval(10);
    final Vertx vertx = Vertx.vertx(options);
    vertx.deployVerticle(verticle)
        .onFailure(Throwable::printStackTrace)
        .onSuccess(v -> System.out.println("Verticle launched"));
  }

  private URI openAPIUri;
  private JsonObject openAPI;
  private SchemaRouter schemaRouter;
  private SchemaParser schemaParser;

  @Override
  public void start(final Promise<Void> startPromise) throws Exception {

    this.loadSchemaStuff()
        .compose(v -> this.startWebServer())
        .onSuccess(v -> startPromise.complete())
        .onFailure(err -> startPromise.fail(err));

  }

  @Override
  public void stop(final Promise<Void> stopPromise) throws Exception {
    System.out.println("Verticle unloaded");
    stopPromise.complete();
  }

  Future<Void> startWebServer() {
    return Future.future(startPromise -> {
      final Router router = Router.router(this.getVertx());
      router.route().handler(BodyHandler.create(false));
      router.route(HttpMethod.GET, "/").handler(this::handleRoot);
      router.route(HttpMethod.POST, "/trafficlight").handler(this::handleTrafficLight);
      router.route(HttpMethod.POST, "/street").handler(this::handleStreet);

      this.getVertx().createHttpServer()
          .requestHandler(router)
          .listen(8888)
          .onSuccess(v -> {
            System.out.println("HTTP Running on 8888");
            startPromise.complete();
          })
          .onFailure(err -> startPromise.fail(err));
    });
  }

  Future<Schema> getSchema(final String pointerString) {

    return this.schemaRouter.resolveRef(JsonPointer.from(pointerString),
        JsonPointer.fromURI(this.openAPIUri), this.schemaParser);


    // return this.schemaRouter.resolveCachedSchema(
    // JsonPointer.from(pointerString),
    // JsonPointer.fromURI(this.openAPIUri),
    // this.schemaParser);
  }

  void handleRoot(final RoutingContext ctx) {
    ctx.response().end("Hi there, please post to /street or /trafficlight");
  }

  void handleStreet(final RoutingContext ctx) {
    final JsonObject body = ctx.getBodyAsJson();
    this.getSchema("/components/schemas/RoadLayout")
        .compose(schema -> schema.validateAsync(body))
        .onSuccess(v -> ctx.response().end("The submitted road is fine!"))
        .onFailure(e -> {
          e.printStackTrace();
          ctx.response().setStatusCode(500).end(e.getMessage());
        });
  }

  void handleTrafficLight(final RoutingContext ctx) {
    final JsonObject body = ctx.getBodyAsJson();
    this.getSchema("/components/schemas/TrafficLight")
        .compose(schema -> schema.validateAsync(body))
        .onSuccess(v -> ctx.response().end("The submitted traffic light is complete!"))
        .onFailure(e -> {
          e.printStackTrace();
          ctx.response().setStatusCode(500).end(e.getMessage());
        });
  }

  Future<Void> loadSchemaStuff() {
    return Future.future(promise -> {

      final URL url = this.getClass().getResource("/SchemaDemo.json");
      String openAPIString;
      try {
        openAPIString = Resources.toString(url, StandardCharsets.UTF_8);
        this.openAPIUri = url.toURI();
        this.openAPI = new JsonObject(openAPIString);

        final SchemaRouterOptions so = new SchemaRouterOptions();
        this.schemaRouter = SchemaRouter.create(this.getVertx(), so);
        this.schemaRouter.addJson(this.openAPIUri, this.openAPI);

        this.schemaParser = SchemaParser.createOpenAPI3SchemaParser(this.schemaRouter);
      } catch (final IOException | URISyntaxException e) {
        promise.fail(e);
      }
      promise.complete();
    });
  }
}
