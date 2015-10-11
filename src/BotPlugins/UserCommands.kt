/**
 * Checks chat for user written messages and responds to specific queries
 * Created by zingmars on 04.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem

public class UserCommands : BasePlugin()
{
    override fun pubInit() :Boolean
    {
        return true
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")
        when(message[0].toLowerCase()) {
            "@ping" -> controller?.AddToBoxBuffer(buffer.userName +  ": PONG!")
            "@nextstream" -> controller?.AddToBoxBuffer(buffer.userName +  ": Soon™")
            //TODO: have some sort of database where plugins can register their commands so this command can be automated.
            "@help" -> {
                controller?.AddToBoxBuffer("For more information please see https://github.com/zingmars/Cbox-bot. For feature requests ask zingmars.")
                controller?.AddToBoxBuffer("@about - About this bot; @lastseen <username> - Output the date of user's last message; @ping - check if I'm alive; @help - display this; @time - time related commands (try help as a parameter")
                controller?.AddToBoxBuffer("@laststream <username> - Get when an user has last streamed (try @laststream zingmars); @nextstream - OTG's next stream time")
                controller?.AddToBoxBuffer("Available commands:")
            }
            "@about" -> {
                 controller?.AddToBoxBuffer("Hi! My name is " + handler?.username + " and I'm a bot for cbox.ws written in Kotlin. Check me out at: https://github.com/zingmars/Cbox-bot")
            }
        }
        return true
    }
}