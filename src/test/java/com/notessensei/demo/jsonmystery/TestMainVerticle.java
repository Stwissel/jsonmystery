package com.notessensei.demo.jsonmystery;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.google.common.io.Resources;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  static String verticleId;

  @BeforeAll
  static void deploy_verticle(final Vertx vertx, final VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(),
        testContext.succeeding(id -> {
          TestMainVerticle.verticleId = id;
          testContext.completeNow();
        }));
  }

  @AfterAll
  static void undeploy_verticle(final Vertx vertx, final VertxTestContext testContext) {
    vertx.undeploy(verticleId)
        .onComplete(testContext.succeeding(response -> {
          testContext.verify(() -> testContext.completeNow());
        }));
  }

  // Get JSON from Test directory
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

  // Send JSON to a server
  Future<HttpResponse<Buffer>> send(final Vertx vertx, final String path, final String fileName) {
    final WebClient client = WebClient.create(vertx);
    final JsonObject body = this.getJson(fileName);
    return client
        .post(8888, "localhost", path)
        .sendJson(body);
  }

  @Test
  void send_street_invalid(final Vertx vertx, final VertxTestContext testContext) {
    this.send(vertx, "/street", "/street_bad.json")
        .onComplete(testContext.succeeding(response -> {
          testContext.verify(() -> {
            final String result = response.bodyAsString();
            System.out.format("send_street_invalid: %s (%s)\n", result, response.statusCode());
            Assertions.assertEquals("provided object should contain property name", result);
          });
          testContext.completeNow();

        }));
  }

  @Test
  void send_street_invalid_complete(final Vertx vertx, final VertxTestContext testContext) {
    this.send(vertx, "/street", "/street_bad_complete.json")
        .onComplete(testContext.succeeding(response -> {
          testContext.verify(() -> {
            final String result = response.bodyAsString();
            System.out.format("send_street_invalid_complete: %s (%s)\n", result,
                response.statusCode());
            Assertions.assertEquals(500, response.statusCode());
          });
          testContext.completeNow();
        }));
  }

  @Test
  void send_street_valid(final Vertx vertx, final VertxTestContext testContext) {
    this.send(vertx, "/street", "/street_good.json")
        .onComplete(testContext.succeeding(response -> {
          testContext.verify(() -> {
            final String result = response.bodyAsString();
            System.out.format("send_street_valid: %s (%s)\n", result, response.statusCode());
            Assertions.assertEquals("The submitted road is fine!", result);
          });
          testContext.completeNow();
        }));
  }

  @Test
  void send_street_valid_complete(final Vertx vertx, final VertxTestContext testContext) {
    this.send(vertx, "/street", "/street_good_complete.json")
        .onComplete(testContext.succeeding(response -> {
          testContext.verify(() -> {
            final String result = response.bodyAsString();
            System.out.format("send_street_valid_complete: %s (%s)\n", result,
                response.statusCode());
            Assertions.assertEquals(200, response.statusCode());
          });
          testContext.completeNow();
        }));
  }

  @Test
  void send_traffic_light_invalid(final Vertx vertx, final VertxTestContext testContext) {
    this.send(vertx, "/trafficlight", "/trafficwrong.json")
        .onComplete(testContext.succeeding(response -> {
          testContext.verify(() -> {
            final String result = response.bodyAsString();
            System.out.format("send_traffic_light_invalid: %s (%s)\n", result,
                response.statusCode());
            Assertions.assertTrue(
                result.startsWith("Input doesn't match one of allowed values of enum:"));
          });
          testContext.completeNow();
        }));
  }

  @Test
  void send_traffic_light_valid(final Vertx vertx, final VertxTestContext testContext) {

    this.send(vertx, "/trafficlight", "/trafficlight.json")
        .onComplete(testContext.succeeding(response -> {
          testContext.verify(() -> {
            final String result = response.bodyAsString();
            System.out.format("send_traffic_light_valid: %s (%s)\n", result, response.statusCode());
            Assertions.assertEquals("The submitted traffic light is complete!", result);
          });
          testContext.completeNow();
        }));
  }


  @Test
  void verticle_deployed(final Vertx vertx, final VertxTestContext testContext) {
    testContext.completeNow();
  }
}
