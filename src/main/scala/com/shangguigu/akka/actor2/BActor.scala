package com.shangguigu.akka.actor2

import akka.actor.Actor

/**
 * Created by liuquan on 2020/3/23.
 */
class BActor extends Actor {
    override def receive: Receive = {
        case "我打" => {
            println("BActor(乔峰)：挺猛！看我降龙十八掌")
            Thread.sleep(1000)
            // 通过sender方法获取发送消息的Actor的ActorRef
            sender() ! "我打"
        }
    }
}
