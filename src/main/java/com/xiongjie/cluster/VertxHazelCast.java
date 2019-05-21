package com.xiongjie.cluster;

import com.hazelcast.config.Config;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class VertxHazelCast {

    public static void main(String[] args) {
        useDefault();
        System.out.println("1");
        useDefault();
        System.out.println("2");
        useDefaultConfig();
        System.out.println("3");
        useDefaultConfig();
        System.out.println("4");
    }

    private static void useDefault(){
        ClusterManager mgr = new HazelcastClusterManager();
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                System.out.println("成功:"+res.toString());
            } else {
                System.out.println("失败");
            }
        });
    }

    private static void useDefaultConfig(){
        Config hazelcastConfig = new Config();
        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
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
