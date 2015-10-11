/**
 * Allows adding bot admins
 * Created by zingmars on 11.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem

public class HonourableUsers : BasePlugin()
{
    override fun pubInit() :Boolean
    {
        return true
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")
        when(message[0].toLowerCase()) {
            "@admins" -> {
                var admins = settings?.GetSetting("HonourableUsers").toString()
                var isAdmin = handler?.isPluginAdmin(buffer.userName) as Boolean
                if(message[1] != "") {
                    controller?.AddToBoxBuffer("Bot administrators: " + admins)
                } else {
                    if(isAdmin) {
                        if(message[1] == "add") {
                            if(message[2] == "") {
                                controller?.AddToBoxBuffer("Please enter username")
                            } else {
                                AddAdmin(message[2])
                            }
                        } else if (message[1] == "remove") {
                            if(message[2] == "") {
                                controller?.AddToBoxBuffer("Please enter username")
                            } else {
                                if(!RemoveAdmin(message[2])) {
                                    controller?.AddToBoxBuffer(message[2]+" is not an admin.")
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
            "add" -> { return "Result: " + AddAdmin(action[1]).toString()}
            "remove" -> { return "Result: " + RemoveAdmin(action[1]).toString() }
            else -> { return "Incorrect command"}
        }
    }

    // Admin management functions
    private fun AddAdmin(name :String) :Boolean
    {
        var admins = settings?.GetSetting("HonourableUsers") as String
        if(!admins.contains(name)) {
            settings?.SetSetting("HonourableUsers", admins+","+name)
            logger?.LogPlugin(this.pluginName as String, "Adding admin: " + name)
            return true
        }
        return false
    }
    private fun RemoveAdmin(name :String) :Boolean
    {
        var admins = settings?.GetSetting("HonourableUsers") as String
        if(admins.contains(name)) {
            admins = admins.substring(0, admins.indexOf(name)-1) + admins.substring(admins.indexOf(name)+name.length())
            settings?.SetSetting("HonourableUsers", admins)
            logger?.LogPlugin(this.pluginName as String, "Removed admin: " + name)
            return true
        }
        return false
    }
}

