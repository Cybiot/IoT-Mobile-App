package fr.cpe.hello.udpSocket

import java.net.DatagramPacket
import java.net.DatagramSocket

class SocketReceiver (
    private val socket: DatagramSocket,
    private val listener: SocketReceiverListener
) : Thread() {
    private var keep = true

    fun close() {
        keep = false;
    }

    override fun run() {
        while (keep) {
            val data = ByteArray(1024)
            val packet = DatagramPacket(data, data.size)
            socket.receive(packet) //bloquant
            listener.onReceive(String(data, 0, packet.length));
        }
    }
}