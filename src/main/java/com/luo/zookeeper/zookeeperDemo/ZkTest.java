package com.luo.zookeeper.zookeeperDemo;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * @author hui.luo
 *
 * @version 2015年6月3日 下午5:29:26
 */
public class ZkTest {

	private String host = "localhost:2181";
	private final int SESSION_TIMEOUT= 5000;
	private CountDownLatch latch = new CountDownLatch(1);
	
	public static void main(String[] args) throws Exception {
		ZkTest zt = new ZkTest();
		ZooKeeper zk = zt.connect();
//		zk.create("/testPath", "hello".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		zk.create("/testPath/subPath", "sub".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
		List<String> subList = zk.getChildren("/testPath", false);
		for (String sub : subList) {
			System.out.println(sub);
		}
	}

	public ZooKeeper connect() throws Exception{
		ZooKeeper zk = new ZooKeeper(host,SESSION_TIMEOUT,new Watcher() {
			public void process(WatchedEvent event) {
				if(event.getState()==KeeperState.SyncConnected){
					latch.countDown();
				}
			}
		});
		latch.await();
		return zk;
	}
	
}
