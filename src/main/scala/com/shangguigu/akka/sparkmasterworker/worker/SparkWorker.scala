package com.shangguigu.akka.sparkmasterworker.worker

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.shangguigu.akka.sparkmasterworker.common.{HeartBeat, RegisterWorkerInfo, SendHeartBeat}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
 * Created by liuquan on 2020/3/24.
 */
class SparkWorker(masterHost: String, masterPort: Int, masterActorName: String) extends Actor {
    var masterProxy: ActorSelection = _ // 即 masterActorRef
    val id = UUID.randomUUID().toString

    override def preStart(): Unit = {
        masterProxy = context.actorSelection(s"akka.tcp://sparkMaster@$masterHost:$masterPort/user/$masterActorName")
        println("masterProxy = " + masterProxy)
    }

    override def receive: Receive = {
        case "start" => {
            println("start Worker启动了")
            // 向master发送注册信息。16个CPU core，64G内存
            masterProxy ! RegisterWorkerInfo(id, 16, 64 * 1024)
        }
        case RegisterWorkerInfo => {
            println("workerId= " + id + " 注册成功")
            import context.dispatcher // 下面的schedule用到了隐式转换
            /**
             * worker注册成功后，就定义一个定时器，然后由该定时器每隔一定时间向自己发送一个SendHeartBeat对象
             * 参数说明：
             * 0 millis 表示不延时，立即执行定时器
             * 3000 millis 表示每隔3秒就执行一次
             * self 表示发给自己
             * SendHeartBeat 是发送的内容
             */
            context.system.scheduler.schedule(0 millis, 3000 millis, self, SendHeartBeat)
        }
        // 若worker收到了SendHeartBeat对象，则向master发送HeartBeat(一个协议对象)
        case SendHeartBeat => {
            println("workerId= " + id + " 给master发送心跳")
            masterProxy ! HeartBeat(id)
        }
    }
}

// 主程序入口。这里的object SparkWorker和上面的class SparkWorker并没有任何关系，他两的名字可以不一样
object SparkWorker extends App {
    // 将需要动态指定的启动参数提出来。workerHost、workerPort、workerActorName、masterHost、masterPort、masterActorName
    // val (workerHost, workerPort, masterHost, masterPort) = ("127.0.0.1", 10001, "127.0.0.1", 10005)
    if (args.length != 6) {
        println("请输入参数 workerHost、workerPort、workerActorName、masterHost、masterPort、masterActorName 的值")
        sys.exit()
    }

    val workerHost = args(0)
    val workerPort = args(1)
    val workerActorName = args(2)
    val masterHost = args(3)
    val masterPort = args(4)
    val masterActorName = args(5)
    val config = ConfigFactory.parseString(
        s"""
           |akka.actor.provider="akka.remote.RemoteActorRefProvider"
           |akka.remote.netty.tcp.hostname=$workerHost
           |akka.remote.netty.tcp.port=$workerPort
           |""".stripMargin
    )
    val sparkWorkerSystem = ActorSystem("sparkWorker-1", config)
    val sparkWorkerActorRef: ActorRef = sparkWorkerSystem.actorOf(Props(new SparkWorker(masterHost, masterPort.toInt, masterActorName)), workerActorName)
    sparkWorkerActorRef ! "start"
}