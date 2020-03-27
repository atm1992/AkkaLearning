package com.shangguigu.akka.yellowchicken.client

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.shangguigu.akka.yellowchicken.common.{ClientMessage, ServerMessage}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

/**
 * Created by liuquan on 2020/3/24.
 */
class CustomerActor(serverHost: String, serverPort: Int) extends Actor {
    // 定义一个YellowChickenServer的引用Ref，数据类型不能为ActorRef，而是用ActorSelection
    var serverActorRef: ActorSelection = _

    // 在Actor中有一个方法preStart，它会在Actor运行前执行。通常将初始化的工作放在preStart方法中
    override def preStart(): Unit = {
        println("preStart方法被调用")
        // 地址解析 akka.tcp://server@127.0.0.1:9999
        // akka.tcp是协议类型，server是serverActorSystem的名字，127.0.0.1:9999是serverActorSystem的host和port
        // path路径解析：yellowChickenServer 是serverActorSystem中的一个Actor
        // 最终返回serverActorSystem中yellowChickenServer这个Actor的ActorRef
        serverActorRef = context.actorSelection(s"akka.tcp://server@${serverHost}:${serverPort}/user/yellowChickenServer")
        println("serverActorRef = " + serverActorRef)
    }

    override def receive: Receive = {
        case "start" => println("start 客户端开始运行，可以咨询问题了")
        case message: String => {
            // 发给小黄鸡服务端。这里使用到了ClientMessage样例类中自动生成的apply方法
            // 这里将message封装成一个协议对象，然后进行发送
            serverActorRef ! ClientMessage(message)
        }
        case ServerMessage(message) => {
            println("收到小黄鸡服务端的回复：" + message)
        }
    }
}

// 主程序入口
object CustomerActorDemo extends App {
    // 这里是网络版本的Actor创建方式
    val (clientHost, clientPort, serverHost, serverPort) = ("127.0.0.1", 9998, "127.0.0.1", 9999)
    // 创建config对象，指定协议类型、监听的IP及端口
    val config = ConfigFactory.parseString(
        s"""
           |akka.actor.provider="akka.remote.RemoteActorRefProvider"
           |akka.remote.netty.tcp.hostname=$clientHost
           |akka.remote.netty.tcp.port=$clientPort
           |""".stripMargin
    )

    val clientActorSystem = ActorSystem("client", config)
    // 创建CustomerActor的实例以及引用Ref
    val customerActorRef: ActorRef = clientActorSystem.actorOf(Props(new CustomerActor(serverHost, serverPort)), "customerActor")
    // 启动customerActor
    customerActorRef ! "start"

    // 客户端可以发送消息给服务器
    while (true) {
        println("请输入要咨询的问题：")
        val mes = StdIn.readLine()
        customerActorRef ! mes
    }
}
