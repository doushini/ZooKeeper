package com.luo.zookeeper.curatorDemo;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

/**
 * Created by luohui on 15/8/31.
 */
public class Connection {
    private static final String path = "/example/cache";

    public static void main(String[] args) {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .namespace("/brokers")
                .retryPolicy(new RetryNTimes( Integer.MAX_VALUE,1000))
                .connectionTimeoutMs(5000).build();

        client.start();
    }
}
