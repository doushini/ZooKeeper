package com.luo.zookeeper.curatorDemo;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Created by luohui on 15/8/31.
 */
public class CreateSession {
    public static void main(String[] args) throws Exception{
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
//        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181",5000,3000,retryPolicy);

        //fluent风格
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(retryPolicy)
                .namespace("base")//隔离空间，以这个为基础
                .build();

        client.start();

        client.create().forPath("/names");//默认是持久节点，初始是空的
        client.create().forPath("/names","tom".getBytes());//默认是持久节点
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/blackNames/names");//默认是持久节点，初始是空的
        client.create().withMode(CreateMode.PERSISTENT).forPath("/names");//默认是持久节点，初始是空的

        client.delete().forPath("/names");
        client.delete().deletingChildrenIfNeeded().forPath("/names");//递归删除子节点
        client.delete().guaranteed().forPath("/names");//强制删除一个节点



        Thread.sleep(Integer.MAX_VALUE);


    }
}
