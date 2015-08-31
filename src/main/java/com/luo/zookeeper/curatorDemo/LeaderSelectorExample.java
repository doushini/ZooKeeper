package com.luo.zookeeper.curatorDemo;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * Created by luohui on 15/8/31.
 */
public class LeaderSelectorExample {
    private static final String path = "/example/leader";
    private static final int CLIENT_QTY = 10;


    public static void main(String[] args) {
        List<CuratorFramework> clients = Lists.newArrayList();
    }
}
