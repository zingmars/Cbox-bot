/**
 * Created by zingmars on 11.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem

public class AbuserList : BasePlugin()
{
    override fun pubInit() :Boolean
    {
        return true
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")
        when(message[0].toLowerCase()) {
            "@ignorelist" -> {
                var ignored = settings?.GetSetting("AbuserList") as String
                var isAdmin = handler?.isPluginAdmin(buffer.userName) as Boolean
                if(message[1] != "") {
                    controller?.AddToBoxBuffer("Users banned from using the bot: " + ignored)
                } else {
                    if (isAdmin) {
                        if(message[1] == "add") {
                            if(message[2] == "") {
                                controller?.AddToBoxBuffer("Please enter an username")
                            } else {
                                AddIgnored(message[2])
                            }
                        } else if (message[1] == "remove") {
                            if(message[2] == "") {
                                controller?.AddToBoxBuffer("Please enter an username")
                            } else {
                                if(!RemoveIgnored(message[2])) {
                                    controller?.AddToBoxBuffer(message[2] + " is not in the ignored list.")
                                }
                            }
                        }
                    } else {
                        controller?.AddToBoxBuffer("Insufficient rights")
                    }
                }
            }
        }
        return true
    }
    override fun executeCommand(command :String) :String
    {
        var action = command.split(",")
        when(action[0].toLowerCase()) {
            "add" -> { return "Result: " + AddIgnored(action[1]).toString()}
            "remove" -> { return "Result: " + RemoveIgnored(action[1]).toString() }
            else -> { return "Incorrect command"}
        }
    }

    private fun AddIgnored(name :String) :Boolean
    {
        var ignorelist = settings?.GetSetting("AbuserList") as String
        if(!ignorelist.contains(name)) {
            settings?.SetSetting("AbuserList", ignorelist+","+name)
            logger?.LogPlugin(this.pluginName as String, "User ignored: " + name)
            return true
        }
        return false
    }
    private fun RemoveIgnored(name :String) :Boolean
    {
        var ignorelist = settings?.GetSetting("AbuserList") as String
        if(ignorelist.contains(name)) {
            ignorelist = ignorelist.substring(0, ignorelist.indexOf(name)-1) + ignorelist.substring(ignorelist.indexOf(name)+name.length())
            settings?.SetSetting("AbuserList", ignorelist)
            logger?.LogPlugin(this.pluginName as String, "Removed ignored user: " + name)
            return true
        }
        return false
    }
}
