package fr.cpe.hello.udpSocket

import android.os.Handler
import android.os.Looper
import java.net.DatagramSocket
import java.net.InetAddress

class UDPMessageSocket (address: InetAddress, port: Int) {
    private val socket: DatagramSocket = DatagramSocket();

    private val sender: SocketSender = SocketSender(socket, address, port);
    private val receiver: SocketReceiver;

    private val listeners: ArrayList<UDPMessageSocketListener> = ArrayList();

    init {
        sender.start();
        println("UDP socket : " + socket.localPort)
        receiver = SocketReceiver(socket, object : SocketReceiverListener {
            override fun onReceive(message: String) {
                Handler(Looper.getMainLooper()).post {
                    listeners.forEach{ it.onMessage(message)}
                };
            }
        })
        receiver.start()
    }

    fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    fun addListener(listener: UDPMessageSocketListener) {
        listeners.add(listener);
    }

    fun close() {
        sender.close()
        receiver.close()
    }
}