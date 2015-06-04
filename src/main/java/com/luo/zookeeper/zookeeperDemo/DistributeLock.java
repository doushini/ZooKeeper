package com.luo.zookeeper.zookeeperDemo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

/**
 * @author hui.luo
 *
 * @version 2015年6月3日 下午6:20:48
 * 在分布式应用, 往往存在多个进程提供同一服务. 
 * 这些进程有可能在相同的机器上, 也有可能分布在不同的机器上.
 * 如果这些进程共享了一些资源, 可能就需要分布式锁来锁定对这些资源的访问.
 * 
 * 思路
 * 若一个进程需要访问共享数据时, 就在"/locks"节点下创建一个sequence类型的子节点, 称为thisPath.
 * 当thisPath在所有子节点中最小时, 说明该进程获得了锁. 进程获得锁之后, 就可以访问共享资源了. 访问完成后, 需要将thisPath删除. 锁由新的最小的子节点获得.
 * 有了清晰的思路之后, 还需要补充一些细节. 进程如何知道thisPath是所有子节点中最小的呢?
 * 可以在创建的时候, 通过getChildren方法获取子节点列表, 然后在列表中找到排名比thisPath前1位的节点, 称为waitPath,
 * 然后在waitPath上注册监听, 当waitPath被删除后, 进程获得通知, 此时说明该进程获得了锁.
 */
public class DistributeLock {
	// 超时时间  
    private static final int SESSION_TIMEOUT = 5000;  
    // zookeeper server列表  
    private String hosts = "localhost:2181";  
    private String groupNode = "locks";  
    private String subNode = "sub";  
  
    private ZooKeeper zk;  
    // 当前client创建的子节点  
    private String thisPath;  
    // 当前client等待的子节点  
    private String waitPath;  
  
    private CountDownLatch latch = new CountDownLatch(1); 
    
    private void connect() throws Exception {
		zk = new ZooKeeper(hosts,SESSION_TIMEOUT,new Watcher() {
			public void process(WatchedEvent event) {
				if(event.getState() == KeeperState.SyncConnected){
					latch.countDown();
				}
				//如果发生了"/sgroup"节点下的子节点变化事件, 更新server列表, 并重新注册监听
				if(event.getType()==EventType.NodeDeleted &&waitPath.equals(event.getPath())){
					try {
						doSomething();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		latch.await();
		
		Stat stat = zk.exists("/" + groupNode, false);
		if(stat==null){
			zk.create("/" + groupNode, "data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		
		//创建子节点  
		String s = "/" + groupNode + "/" + subNode;
        thisPath = zk.create(s, "a".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        Thread.sleep(1000);
        
        //注意, 没有必要监听"/locks"的子节点的变化情况  
        List<String> childrenNodes = zk.getChildren("/" + groupNode, false);
        // 列表中只有一个子节点, 那肯定就是thisPath, 说明client获得锁 
        if (childrenNodes.size() == 1) {
            doSomething();  
        } else {
        	String thisNode = thisPath.substring(("/" + groupNode + "/").length());
        	// 排序  
            Collections.sort(childrenNodes);
            int index = childrenNodes.indexOf(thisNode);
            if (index == -1) {  
                // never happened  
            } else if (index == 0) {  
                //inddx == 0, 说明thisNode在列表中最小, 当前client获得锁  
                doSomething();  
            } else {  
                //获得排名比thisPath前1位的节点
                this.waitPath = "/" + groupNode + "/" + childrenNodes.get(index - 1);  
                // 在waitPath上注册监听器, 当waitPath被删除时, zookeeper会回调监听器的process方法  
                zk.getData(waitPath, true, new Stat());
            }
        }
	}
    
	protected void doSomething() throws Exception {
		try {
            System.out.println("gain lock: " + thisPath);  
            Thread.sleep(2000);  
        } finally {  
            System.out.println("finished: " + thisPath);  
            // 将thisPath删除, 监听thisPath的client将获得通知  
            // 相当于释放锁  
            zk.delete(this.thisPath, -1);  
        }  
	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 20; i++) {
			new Thread(){
				public void run() {
					DistributeLock client = new DistributeLock();//10个分布式来抢锁
					try {
						client.connect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
		Thread.sleep(Long.MAX_VALUE);
	}

}
