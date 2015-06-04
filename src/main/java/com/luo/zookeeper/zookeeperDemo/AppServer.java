package com.luo.zookeeper.zookeeperDemo;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * @author hui.luo
 *
 * @version 2015年6月3日 下午5:53:21
 */
public class AppServer {
	private String groupNode = "sgroup";  
	private String subNode = "sub";
	private String host = "localhost:4180,localhost:4181,localhost:4182";
	private final int SESSION_TIMEOUT= 5000;
	private CountDownLatch latch = new CountDownLatch(1);
	private ZooKeeper zk;
	
	public static void main(String[] args) throws Exception{
		AppServer server = new AppServer();
		server.connect(args[0]);
		server.handle();
	}

	private void handle() throws Exception{
		Thread.sleep(Long.MAX_VALUE);
	}

	private void connect( String address ) throws Exception {
		zk = new ZooKeeper(host,SESSION_TIMEOUT,new Watcher() {
			public void process(WatchedEvent event) {
				if(event.getState() == KeeperState.SyncConnected){
					latch.countDown();
				}
			}
		});
		latch.await();
		// 将server的地址数据关联到新创建的子节点上 
		String createPath = zk.create("/"+groupNode+"/"+subNode, address.getBytes("utf-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("create: " + createPath);  
	}

}
