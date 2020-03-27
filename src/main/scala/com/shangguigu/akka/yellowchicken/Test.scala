package com.shangguigu.akka.yellowchicken

import com.typesafe.config.ConfigFactory

/**
 * Created by liuquan on 2020/3/24.
 */
object Test {
    def main(args: Array[String]): Unit = {
        val (clientHost,clientPort) = ("127.0.0.1",9998)

        // 不使用stripMargin方法
        println(s"""
                   |akka.actor.provider="akka.remote.RemoteActorRefProvider"
                   |akka.remote.netty.tcp.hostname=$clientHost
                   |akka.remote.netty.tcp.port=$clientPort
                   |""")

        // 使用stripMargin方法。默认使用分隔符 | 对多行字符串进行分割
        println(s"""
                   |akka.actor.provider="akka.remote.RemoteActorRefProvider"
                   |akka.remote.netty.tcp.hostname=$clientHost
                   |akka.remote.netty.tcp.port=$clientPort
                   |""".stripMargin('|'))


    }
}
