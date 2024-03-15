package com.nailoy.mirai.command

import com.nailoy.mirai.MinecraftMotdInquiry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

object GetMotd : SimpleCommand(
    MinecraftMotdInquiry, "motd", description = "获取我的世界服务器信息"
) {
    private val ipRegex = Regex("^([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\d{1,3}(\\.\\d{1,3}){3})$")
    private val ipWithPortRegex = Regex("^([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\d{1,3}(\\.\\d{1,3}){3}):(\\d{1,5})$")

    @Handler
    suspend fun CommandSender.handle(address: String, defaultPort: Int = 25565) {
        val (ip, port) = parseAddress(address, defaultPort)
        if (ip.isEmpty()) {
            sendMessage("呜~域名或者IP格式不对哦")
            return
        }
        try {
            val pingResult = fetchServerData(ip, port)
            sendMessage(pingResult)
        } catch (e: IOException) {
            sendMessage("似乎无法连接到服务器呢：${e.localizedMessage}")
        }
    }

    private fun parseAddress(address: String, defaultPort: Int): Pair<String, Int> {
        return when {
            ipWithPortRegex.matches(address) -> {
                val parts = address.split(":")
                parts[0] to parts[1].toInt()
            }
            ipRegex.matches(address) -> address to defaultPort
            else -> "" to -1
        }
    }

    private suspend fun fetchServerData(ip: String, port: Int): String = withContext(Dispatchers.IO) {
        Socket().use { socket ->
            socket.soTimeout = 1000
            socket.connect(InetSocketAddress(ip, port), 1000)
            DataOutputStream(socket.getOutputStream()).use { out ->
                DataInputStream(socket.getInputStream()).use { `in` ->
                    InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_16BE).use { reader ->
                        out.write(byteArrayOf(0xFE.toByte(), 0x01))

                        val packetId = `in`.read()
                        if (packetId != 0xFF) throw IOException("无效的数据包ID：$packetId")

                        val length = reader.read()
                        if (length <= 0) throw IOException("无效的长度：$length")

                        val chars = CharArray(length)
                        if (reader.read(chars, 0, length) != length) throw IOException("数据流异常结束")

                        val string = String(chars)
                        if (!string.startsWith("§")) throw IOException("意外的响应：$string")

                        val data = string.split("\u0000").dropLastWhile { it.isEmpty() }.toTypedArray()
                        "成功查询到服务器喵！\n玩家数量：${data[4]}，最大玩家数：${data[5]}\n服务器标语：${
                            data[3].replace(
                                "\n",
                                "\\n"
                            ).replace("\r", "\\r")
                        }"
                    }
                }
            }
        }
    }
}
