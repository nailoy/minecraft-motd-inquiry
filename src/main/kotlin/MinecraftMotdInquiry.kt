package com.nailoy.mirai

import com.nailoy.mirai.command.GetMotd
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister

object MinecraftMotdInquiry : KotlinPlugin(
    JvmPluginDescription(
        id = "com.nailoy.mirai.minecraft-motd-inquiry",
name = "Minecraft Motd Inquiry",
        version = "0.1.0",
    ) {

author("Nailoy")
    }
) {

    override fun onEnable() {
        logger.info { "Plugin loaded" }

        GetMotd.register()
    }
}