package com.notessensei.demo.jsonmystery;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.google.common.io.Resources;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  void deploy_verticle(final Vertx vertx, final VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(),
        testContext.succeeding(id -> testContext.completeNow()));
  }

  JsonObject getJson(final String resourceName) {
    final URL url = this.getClass().getResource(resourceName);
    String openAPIString;
    try {
      openAPIString = Resources.toString(url, StandardCharsets.UTF_8);
      return new JsonObject(openAPIString);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return new JsonObject();
  }

  @Test
  void send_street_invalid(final Vertx vertx, final VertxTestContext testContext) throws Throwable {
    final WebClient client = WebClient.create(vertx);
    final JsonObject body = this.getJson("/street_bad.json");
    client
        .post(8888, "localhost", "/street")
        .sendJson(body)
        .onFailure(err -> testContext.failNow(err))
        .onSuccess(response -> {
          final String result = response.bodyAsString();
          Assertions.assertEquals("provided object should contain property name", result);
          testContext.completeNow();
        });
  }

  @Test
  void send_street_invalid_complete(final Vertx vertx, final VertxTestContext testContext)
      throws Throwable {
    final WebClient client = WebClient.create(vertx);
    final JsonObject body = this.getJson("/street_bad_complete.json");
    client
        .post(8888, "localhost", "/street")
        .sendJson(body)
        .onFailure(err -> testContext.failNow(err))
        .onSuccess(response -> {
          final String result = response.bodyAsString();
          System.out.println(result);
          Assertions.assertEquals(500, response.statusCode());
          testContext.completeNow();
        });
  }

  @Test
  void send_street_valid(final Vertx vertx, final VertxTestContext testContext) throws Throwable {
    final WebClient client = WebClient.create(vertx);
    final JsonObject body = this.getJson("/street_good.json");
    client
        .post(8888, "localhost", "/street")
        .sendJson(body)
        .onFailure(err -> testContext.failNow(err))
        .onSuccess(response -> {
          final String result = response.bodyAsString();
          Assertions.assertEquals("The submitted road is fine!", result);
          testContext.completeNow();
        });
  }

  @Test
  void send_street_valid_complete(final Vertx vertx, final VertxTestContext testContext)
      throws Throwable {
    final WebClient client = WebClient.create(vertx);
    final JsonObject body = this.getJson("/street_good_complete.json");
    client
        .post(8888, "localhost", "/street")
        .sendJson(body)
        .onFailure(err -> testContext.failNow(err))
        .onSuccess(response -> {
          final String result = response.bodyAsString();
          System.out.println(result);
          Assertions.assertEquals(200, response.statusCode());
          testContext.completeNow();
        });
  }

  @Test
  void send_traffic_light_invalid(final Vertx vertx, final VertxTestContext testContext)
      throws Throwable {
    final WebClient client = WebClient.create(vertx);
    final JsonObject body = this.getJson("/trafficwrong.json");
    client
        .post(8888, "localhost", "/trafficlight")
        .sendJson(body)
        .onFailure(err -> testContext.failNow(err))
        .onSuccess(response -> {
          final String result = response.bodyAsString();
          Assertions
              .assertTrue(result.startsWith("Input doesn't match one of allowed values of enum:"));
          testContext.completeNow();
        });
  }

  @Test
  void send_traffic_light_valid(final Vertx vertx, final VertxTestContext testContext)
      throws Throwable {
    final WebClient client = WebClient.create(vertx);
    final JsonObject body = this.getJson("/trafficlight.json");
    client
        .post(8888, "localhost", "/trafficlight")
        .sendJson(body)
        .onFailure(err -> testContext.failNow(err))
        .onSuccess(response -> {
          final String result = response.bodyAsString();
          Assertions.assertEquals("The submitted traffic light is complete!", result);
          testContext.completeNow();
        });
  }


  @Test
  void verticle_deployed(final Vertx vertx, final VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }
}
