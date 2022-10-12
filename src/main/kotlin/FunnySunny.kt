package me.xiaozhangup.funnysunny

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChain.Companion.deserializeFromMiraiCode
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info

object FunnySunny : KotlinPlugin(
    JvmPluginDescription(
        id = "me.xiaozhangup.funnysunny",
        name = "FunnySunny",
        version = "0.1.0",
    ) {
        author("xiaozhangup")
    }
) {
    private var total = 0
    private var minute = 0
    private const val rootgroup = 423179929
    private val invitList = HashMap<Int, BotInvitedJoinGroupRequestEvent>()
    private var invitId = 0

    override fun onEnable() {
        logger.info { "FunnySunny loaded!" }
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
//            group.sendMessage("收到的消息为: ${message.content}")
            val content = message.content
            if (content == "/groups") {
                group.sendMessage("""
                    Sunny目前总共添加了: ${bot.groups.size}个群聊
                """.trimIndent())
            }
            if (content == "/speed") {
                group.sendMessage("""
                    Sunny消息统计:
                    自启动以来共收到了: ${total}条消息
                    目前的消息处理速度为: ${minute}条/分
                """.trimIndent())
            }
            if (content.startsWith("/gaccept ") && group.id == rootgroup.toLong()) {
                val id = content.replace("/gaccept ", "")
                if (sender.isOwner() || sender.isOperator() || sender.isAdministrator()) {
                    val event = invitList[id.toInt()]
                    if (event == null) group.sendMessage("不存在的事件ID")
                    event?.accept()
                    group.sendMessage("成功同意了 ${event?.groupId} 的加群请求")
                    invitList.remove(id.toInt())
                } else {
                    group.sendMessage("你不是群主/管理员,不能使用此命令")
                }
            }
            if (content == "/listinvite") {
                var lmessage = "目前未处理的群号有(群号:ID):"
                if (group.id == rootgroup.toLong()) {
                    invitList.forEach { (t, u) -> lmessage += "\n${u.groupId}:$t" }
                }
                group.sendMessage(lmessage)
            }
        }
        globalEventChannel().subscribeAlways<MessageEvent> {
            total++
            minute++
            launch {
                delay(60000)
                minute--
            }
        }
        globalEventChannel().subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            bot.getGroup(rootgroup.toLong())?.sendMessage("""
收到了一个入群申请:
群号为: $groupId
申请人为: $invitorId

如需同意,请输入下方命令:
/gaccept $invitId
            """.trimMargin())
            invitList[invitId] = this
            invitId++

        }
    }
}