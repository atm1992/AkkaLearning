package com.shangguigu.akka.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
 * Created by liuquan on 2020/3/23.
 * 创建一个Actor，然后给自己发送消息
 */
// SayHelloActor继承了Actor之后，SayHelloActor就是一个Actor，核心方法receive需要重写
class SayHelloActor extends Actor {
    // receive方法会被该Actor的mailbox(实现了Runable接口)调用
    // 当该Actor的mailbox接收到消息时，就会调用receive方法，将消息推送给该Actor
    // receive方法是一个偏函数 type Receive = PartialFunction[Any, Unit]，第一个表示偏函数的入参类型，第二个表示偏函数的返回值类型
    override def receive: Receive = {
        // 这里就是偏函数的简写形式
        case "hello" => println("收到hello，回应 hello too")
        case "ok" => println("收到ok，回应 ok too")
        case "exit" => {
            println("接受到exit指令，退出系统……")
            context.stop(self) // 停止当前Actor的mailbox
            context.system.terminate() // 退出整个ActorSystem
        }
        case _ => println("匹配不到")
    }
}

object SayHelloActorDemo {
    // 1、创建一个Actor System，专门用于创建Actor
    private val actorFactory = ActorSystem("ActorFactory")
    // 2、创建一个Actor的同时，返回Actor的ActorRef
    // Props[SayHelloActor] 使用反射机制创建一个SayHelloActor的实例
    // 字符串"sayHelloActor" 是给Actor取的名字
    // 变量sayHelloActorRef: ActorRef 就是Props[SayHelloActor](一个Actor实例)的ActorRef
    // 创建的SayHelloActor实例被actorFactory接管了，因此在actorFactory这个ActorSystem中就有了SayHelloActor实例
    // 注意：sayHelloActorRef并不是一个对象实例，只是与SayHelloActor实例进行了关联的一个引用而已
    private val sayHelloActorRef: ActorRef = actorFactory.actorOf(Props[SayHelloActor], "sayHelloActor")

    def main(args: Array[String]): Unit = {
        // 给sayHelloActor(一个Actor实例)发送消息，即 给它的mailbox发消息
        // 这里使用的是哪个Actor的ActorRef，就表示给哪个Actor的mailbox发消息
        // Mailbox是一个对象，它实现了Runable接口。因此，Mailbox一直在运行过程中，所以程序不会自动退出
        sayHelloActorRef ! "hello"
        sayHelloActorRef ! "ok"
        sayHelloActorRef ! "ok55"
        // 退出ActorSystem
        sayHelloActorRef ! "exit"
    }
}
