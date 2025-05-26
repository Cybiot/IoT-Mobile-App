package fr.cpe.hello.udpSocket

interface SocketReceiverListener {
    fun onReceive(message: String)
}