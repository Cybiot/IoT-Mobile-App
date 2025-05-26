package fr.cpe.hello.udpSocket

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class SocketSender(
    private val socket: DatagramSocket,
    private val address: InetAddress,
    private val port: Int
) : Thread() {

    private val queue: BlockingQueue<String> = ArrayBlockingQueue(10)

    private var keep= true

    fun sendMessage(message: String) {
        queue.put(message);
    }

    fun close() {
        keep = false
    }

    override fun run() {
        while (keep) {
            val message = queue.take();
            val data = message.toByteArray()
            val packet = DatagramPacket(data, data.size, address, port)
            socket.send(packet)
            println("Send message : $message")
        }
    }
}