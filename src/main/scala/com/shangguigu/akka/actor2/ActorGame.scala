package com.shangguigu.akka.actor2

import akka.actor.{ActorRef, ActorSystem, Props}

/**
 * Created by liuquan on 2020/3/23.
 */
object ActorGame extends App {
    // 使用apply方法创建一个ActorSystem
    val actorFactory = ActorSystem("actorFactory")
    // 先创建BActor的ActorRef
    val bActorRef: ActorRef = actorFactory.actorOf(Props[BActor], "bActor")
    // 创建AActor的ActorRef
    val aActorRef: ActorRef = actorFactory.actorOf(Props(new AActor(bActorRef)), "aActor")

    aActorRef ! "start"
}
