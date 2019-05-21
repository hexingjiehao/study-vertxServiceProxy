package com.xiongjie.cluster;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;

public class VertxInfinispan {

    public static void main(String[] args) {
        useDefault();
        System.out.println("1");
        useDefault();
        System.out.println("2");
    }

    //同一实例被启动两遍，测试时组成集群
    private static void useDefault(){
        ClusterManager mgr = new InfinispanClusterManager();
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                System.out.println("成功:"+res.toString());
            } else {
                System.out.println("失败");
            }
        });
    }

}
