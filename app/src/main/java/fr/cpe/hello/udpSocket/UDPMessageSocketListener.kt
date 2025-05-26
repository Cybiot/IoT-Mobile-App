package fr.cpe.hello.udpSocket

interface UDPMessageSocketListener {
    fun onMessage(message: String)
}