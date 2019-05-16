package com.xiongjie.eb;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;

public class MyProxyServiceImpl implements MyProxyService{

    @Override
    public void sayHello(Handler<AsyncResult<String>> resultHandler) {
        try {
            resultHandler.handle(Future.succeededFuture("helloProxy 实现类调用"));
        }catch (Exception e){
            //ServiceException是vertx封装的异常类，可以指定错误信息进行返回。用于处理eventbus和http的请求
            //一般用于服务化的接口实现类中
            //前端在接收到后可以这样处理： result.cause() instanceof ServiceException
            resultHandler.handle(ServiceException.fail(500,e.getMessage(),new JsonObject()));
        }
    }
}
