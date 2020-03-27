package com.shangguigu.akka.yellowchicken.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.shangguigu.akka.yellowchicken.common.{ClientMessage, ServerMessage}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

/**
 * Created by liuquan on 2020/3/24.
 */
class YellowChickenServer extends Actor {
    override def receive: Receive = {
        case "start" => println("start 小黄鸡客服启动啦")
        // 若接收到了客户端发过来的ClientMessage对象。这里会使用到ClientMessage样例类中自动生成的unapply方法，将其中的message提取出来
        case ClientMessage(message) => {
            message match {
                case "大数据学费" => sender() ! ServerMessage("15000RMB")
                case "学校地址" => sender() ! ServerMessage("北京市海淀区")
                case "学习什么技术" => sender() ! ServerMessage("大数据 Python Scala")
                case mes => {
                    // 与客户进行对话
                    println(s"客户咨询问题：$mes \n请输入回复：")
                    sender() ! ServerMessage(StdIn.readLine)
                }
            }
        }
    }
}

// 主程序入口
object YellowChickenServerDemo extends App {
    /**
     * 这里是之前单机版Actor的创建方式，无法指定host、port
     * // 创建ActorSystem，名字为server
     * val serverActorSystem = ActorSystem("server")
     * // 在server这个ActorSystem中创建一个名为yellowChickenServer的Actor，并同时返回一个名为yellowChickenServerRef的ActorRef
     * val yellowChickenServerRef: ActorRef = serverActorSystem.actorOf(Props[YellowChickenServer],"yellowChickenServer")
     */

    // 这里是网络版本的Actor创建方式
    val host = "127.0.0.1"
    val port = 9999
    // 创建config对象，指定协议类型、监听的IP及端口
    val config = ConfigFactory.parseString(
        s"""
           |akka.actor.provider="akka.remote.RemoteActorRefProvider"
           |akka.remote.netty.tcp.hostname=$host
           |akka.remote.netty.tcp.port=$port
           |""".stripMargin
    )

    val serverActorSystem = ActorSystem("server", config)
    val yellowChickenServerRef: ActorRef = serverActorSystem.actorOf(Props[YellowChickenServer], "yellowChickenServer")
    yellowChickenServerRef ! "start"
}