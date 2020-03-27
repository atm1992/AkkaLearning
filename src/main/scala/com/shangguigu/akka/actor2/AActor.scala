package com.shangguigu.akka.actor2

import akka.actor.{Actor, ActorRef}

/**
 * Created by liuquan on 2020/3/23.
 */
class AActor(actorRef: ActorRef) extends Actor {
    val bActorRef = actorRef

    override def receive: Receive = {
        case "start" => {
            println("AActor 出招了，start ok")
            self ! "我打" // AActor给自己发消息
        }
        case "我打" => {
            // 给BActor发消息，前提是要持有BActor的ActorRef，可通过在初始化的时候传入一个BActor的ActorRef
            println("AActor(黄飞鸿)：厉害！看我佛山无影脚")
            Thread.sleep(1000)
            bActorRef ! "我打"
        }

    }
}
