package com.shangguigu.akka.sparkmasterworker.master

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.shangguigu.akka.sparkmasterworker.common.{HeartBeat, RegisterWorkerInfo, RemoveTimeOutWorker, StartTimeOutWorker, WorkerInfo}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * Created by liuquan on 2020/3/24.
 */
class SparkMaster extends Actor {
    // 定义一个hashmap，用于管理workers
    val workers = mutable.Map[String, WorkerInfo]()
    // 将worker心跳超时的阈值设定为6秒
    val timeoutThreshold = 6000

    override def receive: Receive = {
        case "start" => {
            println("start Master服务器启动了")
            self ! StartTimeOutWorker

        }
        case RegisterWorkerInfo(id, cpu, ram) => {
            // 接收到worker的注册信息，然后将其装载入hashmap中
            if (!workers.contains(id)) {
                // 创建WorkerInfo对象
                val workerInfo = new WorkerInfo(id, cpu, ram)
                // 加入到workers中
                workers += (id -> workerInfo)
                println("当前向master注册成功的worker有 " + workers)
                // 当worker注册成功时，master给该worker返回一个case object RegisterWorkerInfo对象
                sender() ! RegisterWorkerInfo
            }
        }
        case HeartBeat(id) => {
            // 更新对应worker最近一次发送心跳的时间
            // 1、从workers取出WorkerInfo
            val workerInfo = workers(id)
            workerInfo.lastHeartBeat = System.currentTimeMillis()
            println("master更新了worker " + id + " 的心跳时间")
        }
        case StartTimeOutWorker => {
            println("开始了定时检测worker心跳的任务")
            import context.dispatcher       // 下面的schedule用到了隐式转换
            /**
             * worker注册成功后，就定义一个定时器，然后由该定时器每隔一定时间向自己发送一个SendHeartBeat对象
             * 参数说明：
             * 0 millis 表示不延时，立即执行定时器
             * 9000 millis 表示每隔9秒就执行一次
             * self 表示发给自己
             * RemoveTimeOutWorker 是发送的内容
             */
            context.system.scheduler.schedule(0 millis, 9000 millis, self, RemoveTimeOutWorker)
        }
        // 对收到的RemoveTimeOutWorker消息进行处理。需要检测是哪些worker心跳超时，然后将这些超时的worker从workers中删除
        case RemoveTimeOutWorker => {
            val workerInfos = workers.values
            val nowTime = System.currentTimeMillis()
            // 先过滤出所有心跳超时的worker，然后再将其从hashmap中删除
            workerInfos.filter(workerInfo => (nowTime - workerInfo.lastHeartBeat) > timeoutThreshold)
                .foreach(workerInfo => workers.remove(workerInfo.id))
            println("当前还有 " + workers.size + " 个worker存活")
        }
    }
}

// 主程序入口
object SparkMaster extends App {
    // 将需要动态指定的启动参数提出来。host、port、sparkMasterActor
    if (args.length != 3) {
        println("请输入参数host、port、sparkMasterActor的值")
        sys.exit()
    }
    val host = args(0)
    val port = args(1)
    val actorName = args(2)
    // val host = "127.0.0.1"
    // val port = 10005
    // val actorName = "sparkMasterActor"
    val config = ConfigFactory.parseString(
        s"""
           |akka.actor.provider="akka.remote.RemoteActorRefProvider"
           |akka.remote.netty.tcp.hostname=$host
           |akka.remote.netty.tcp.port=$port
           |""".stripMargin
    )
    val sparkMasterSystem = ActorSystem("sparkMaster", config)
    val sparkMasterActorRef: ActorRef = sparkMasterSystem.actorOf(Props[SparkMaster], actorName)
    sparkMasterActorRef ! "start"
 }