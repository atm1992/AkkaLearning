package com.shangguigu.akka.sparkmasterworker.common

/**
 * Created by liuquan on 2020/3/24.
 */
// worker注册信息，向master注册时，告诉master自己的CPU、RAM大小
case class RegisterWorkerInfo(id: String, cpu: Int, ram: Int)

// 这个是WorkerInfo，这个信息将来会保存到master的hashmap中，该hashmap中会有很多个worker，用于管理worker
// 将来这个WorkerInfo会扩展，例如：增加worker上一次的心跳时间
// 它并不参与master与worker之间的通信，所以它不是case class
class WorkerInfo(val id: String, val cpu: Int, val ram: Int) {
    var lastHeartBeat: Long = _
}

// 当worker注册成功时，master给worker返回一个RegisterWorkerInfo对象
case object RegisterWorkerInfo

// worker定义一个定时器，然后由该定时器每隔一定时间向自己发送一个SendHeartBeat对象
case object SendHeartBeat

// worker每隔一定时间由定时器触发，向master发送心跳消息(一个协议对象)
case class HeartBeat(id: String)

// master给自己发送一个触发检查超时Worker的信息
case object StartTimeOutWorker

// master每隔一定时间给自己发送一个信息，触发自己去检测worker的心跳，若某个worker的心跳超时，则删除该worker
// 其实这个case object和上一个case object的功能有点重复，这里只是沿用了Spark源码中的写法
case object RemoveTimeOutWorker