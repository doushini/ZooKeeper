package com.luo.zookeeper.zookeeperDemo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

/**
 * @author hui.luo
 *
 * @version 2015年6月3日 下午6:07:17
 */
public class AppClient {
	private String groupNode = "sgroup";  
	private String subNode = "sub";
	private String host = "localhost:4180,localhost:4181,localhost:4182";
	private final int SESSION_TIMEOUT= 5000;
	private CountDownLatch latch = new CountDownLatch(1);
	private ZooKeeper zk;
	private volatile List<String> serverList;
	private Stat stat = new Stat();
	
	public static void main(String[] args) {
	}
	
	private void connect() throws Exception {
		zk = new ZooKeeper(host,SESSION_TIMEOUT,new Watcher() {
			public void process(WatchedEvent event) {
				if(event.getState() == KeeperState.SyncConnected){
					latch.countDown();
				}
				//如果发生了"/sgroup"节点下的子节点变化事件, 更新server列表, 并重新注册监听
				if(event.getType()==EventType.NodeChildrenChanged &&
						("/"+groupNode).equals(event.getPath())){
					try {
						updateServerList();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		latch.await();
	}

	protected void updateServerList() throws Exception{
		List<String> newList = new ArrayList<String>();
		List<String> subList = zk.getChildren("/"+groupNode, true);//true标识加监听
		for (String subNode : subList) {
			byte[] data = zk.getData("/" + groupNode + "/" + subNode, false, stat);
			newList.add(new String(data));
		}
		serverList = newList;
		System.out.println("server list updated: " + serverList);
	}
}
