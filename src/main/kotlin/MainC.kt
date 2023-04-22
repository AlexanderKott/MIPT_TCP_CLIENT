import java.io.*
import java.net.Socket

 const val server = "npm.mipt.ru"
 const val port = 9048


fun main() {
    client()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun client() {
    println("Starting Client")
    val socket = Socket(server, port)
    println("Connected")

    val inStream: InputStream = BufferedInputStream(socket.getInputStream())
    val outStream: OutputStream = BufferedOutputStream(socket.getOutputStream())

    try {
        //step1 - get Hello
        val data1 = inStream.readNBytes(6)
        println("received1:  ${String(data1).replace("\n", "\\n")} ")
        require(checkHello(data1)) { "Сервер вернул некорректное приветствие" }

        //step 2 - send hello to server
        println("send: HELLO\\n  to server")
        outStream.write("HELLO\n".toByteArray())
        outStream.flush()

        //step 3 - get RESxxxxx message
        val data2 = inStream.readNBytes(3)
        require(checkRES(data2)) { "Сервер начал сообщение не с RES" }

        val data3 = inStream.readNBytes(1)
        require(checkMessageSize(data3)) { "Сервер прислал некорректное значение параметра размера сообщения" }

        val data3c = data3[0].toInt()
        val data4 = inStream.readNBytes(data3c).toUByteArray()
        require(checkByteArraySize(data3c, data4)) { "Длина сообщения не соответствует размеру ${data3c}" }
        println("received2: data:\"${data4.joinToString()}\"")

        //step 4 - processing sum
        val byteSum = calcByteSum(data4)

        //step 5 - send
        println("send sum: $byteSum to server")
        outStream.write("SUM$byteSum\n".toByteArray())
        outStream.flush()

        //step 6 - get response
        val uselessData = inStream.readNBytes(2)  // zero and \n
        val data5 = inStream.readNBytes(3)  //get OK\n
        require(checkResponseIsOK(data5)) { "Ответ не был ОК. Вместо этого пришло: ${String(data5)}\",\"${data5.joinToString()}\"" }
        println("ответ от сервера: ОК ")
    } catch (e: Exception) {
        println(e);

    } finally {
        println("закрытие соединения")
        inStream.close()
        outStream.close()
        socket.close();
    }
}


fun checkHello(array: ByteArray): Boolean {
    return String(array) == "HELLO\n"
}

fun checkRES(array: ByteArray): Boolean {
    return String(array) == "RES"
}

fun checkMessageSize(array: ByteArray?): Boolean {
    return array?.get(0) != null
}

fun checkByteArraySize(size: Int, array: UByteArray): Boolean {
    return array.size == size
}

fun checkResponseIsOK(array: ByteArray): Boolean {
    return  String(array) == "OK\n"
}

private fun calcByteSum(data4: UByteArray): Int {
    var byteSum = 0
    for (b in data4) {
        byteSum += b.toInt()
    }
    return byteSum
}




