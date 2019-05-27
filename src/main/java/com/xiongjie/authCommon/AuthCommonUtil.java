package com.xiongjie.authCommon;

import io.netty.util.AsciiString;
import io.vertx.core.*;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 权限demo类，默认的权限Json的格式如下：
 * {
 * "material":{
 * "roles":["write","read"]
 * }
 * }
 */
public class AuthCommonUtil extends AbstractVerticle {

    public void initRouteAndAuth(Vertx vertx) {

        OpenAPI3RouterFactory.create(vertx, "src/main/resources/openAPI.yaml", ar2 -> {
            if (ar2.succeeded()) {
                OpenAPI3RouterFactory routerFactory = ar2.result();

                //增加权限验证
                routerFactory.addSecurityHandler("oauth2", context -> {
                    String pswStr = context.request().headers().get(AsciiString.cached("authorization"));
                    int idx = pswStr.indexOf(' ');
                    String tokenStr = pswStr.substring(idx + 1);

                    String[] segments = tokenStr.split("\\.");
                    JsonObject accessToken = new JsonObject(new String(Base64.getUrlDecoder().decode(segments[1]), StandardCharsets.UTF_8));
                    context.setUser(new MyUser(accessToken));
                    context.next();
                });

                routerFactory.addHandlerByOperationId("sayHello", context -> isAuthorized(context, "material:read", this::sayHelloHandler));
                Router router = routerFactory.getRouter();
                vertx.createHttpServer()
                        .requestHandler(router)
                        .listen(8879);
            } else {
                System.out.println("创建路由工厂失败！");
            }
        });
    }

    //判断是否授权
    private void isAuthorized(RoutingContext context, String authority, Handler<RoutingContext> handler) {
        context.user().isAuthorized(authority, auth -> {
            if (auth.succeeded()) {
                if (auth.result()) {
                    handler.handle(context);
                } else {
                    System.out.println("权限失败：403");
                    context.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "no required permission"));
                }
            } else {
                context.fail(auth.cause());
            }
        });
    }

    private void sayHelloHandler(RoutingContext context) {
        String res = "测试authCommon权限";
        context.response().end(res);
    }

}

class MyUser extends AbstractUser {

    private JsonObject accessToken;

    public MyUser(JsonObject accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * 判断是否有权限的重要方法
     *
     * @param permission
     * @param resultHandler
     */
    @Override
    protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
        boolean res;
        String[] parts = permission.split(":");
        JsonObject appRoles = accessToken.getJsonObject("resource_access", new JsonObject())
                .getJsonObject(parts[0]);

        if (appRoles == null) {
            res = false;
        } else {
            res = appRoles.getJsonArray("roles", new JsonArray()).contains(parts[1]);
        }
        resultHandler.handle(Future.succeededFuture(res));
    }

    @Override
    public JsonObject principal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {
        throw new UnsupportedOperationException();
    }
}