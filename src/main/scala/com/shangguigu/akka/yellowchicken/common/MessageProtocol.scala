package com.shangguigu.akka.yellowchicken.common

/**
 * Created by liuquan on 2020/3/24.
 */

// 使用样例类来构建协议
// 客户端发给服务端的协议(以序列化的对象形式发送)
// 因为样例类默认实现了序列化功能。主构造器中的变量mes会自动成为样例类的一个只读属性
case class ClientMessage(mes: String)

// 服务端发给客户端的协议(以序列化的对象形式发送)
case class ServerMessage(mes: String)
