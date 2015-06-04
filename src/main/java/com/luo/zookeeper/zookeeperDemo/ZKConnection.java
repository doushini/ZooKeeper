package com.luo.zookeeper.zookeeperDemo;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author hui.luo
 *
 * @version 2015年6月3日 下午5:46:03
 */
public class ZKConnection {
	private final String host = "localhost:2181";
	private final int SESSION_TIMEOUT= 5000;
	private ZooKeeper zk;
	private CountDownLatch latch = new CountDownLatch(1);
	
	public void connect() throws Exception{
		zk = new ZooKeeper(host,SESSION_TIMEOUT,new Watcher() {
			
			public void process(WatchedEvent event) {
				if(event.getState() == KeeperState.SyncConnected){
					latch.countDown();
				}
			}
		});
		latch.await();
	}
}
