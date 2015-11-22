/**
 * Checks chat for user written messages and responds to specific queries
 * Created by zingmars on 04.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem
import java.util.*

public class AdminCommands : BasePlugin()
{
    private var DB : HashMap<String, String> = HashMap()
    override fun pubInit() :Boolean
    {
        try {
            if(handler?.isAdmin() == false) {
                throw Exception("User does not have mod rights to the given box. Exiting.")
            }

            settings?.checkSetting("IPDB", true)
            var savedDB = settings?.GetSetting("LastSeen").toString()

            if(savedDB != "")  {
                var data = savedDB.split(",")
                for(user in data) {
                    var IP = savedDB.split(":")[0]
                    var Usernames = savedDB.split(":")[1]
                    DB.put(IP, Usernames)
                }
            }
            return true
        } catch (e: Exception) {
            logger?.LogPlugin(pluginName.toString(), "Error: " + e.toString())
            return false
        }
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")
        var changed :Boolean
        //TODO: Rewrite, this is a horrible 2AM energy drink powered way to do this.
        if(DB.containsKey(buffer.extradata)) {
            //Remove from the old entry
            var keys = DB.keys.iterator()
            while(keys.hasNext())
            {
                var key = keys.next()
                var userlist = DB.get(key)
                if(userlist != null) {
                    var replaceableString = buffer.userName
                    if(userlist.indexOf(";"+buffer.userName) > 0) replaceableString = ";" + replaceableString
                    if (userlist.contains(buffer.userName)) {
                        userlist.replace(replaceableString, "")
                    }
                }
            }
            //Add to DB
            var entry = DB.get(buffer.extradata)
            if (entry != null && !entry.contains(buffer.userName)) DB.set(buffer.userName, ";"+buffer.extradata)
            changed = true
        } else {
            logger?.LogPlugin(this.pluginName.toString(), "New user encountered: " + buffer.userName)
            DB.put(buffer.userName, buffer.extradata)
            changed = true
        }

        if(changed) {
            var data = ""
            var keys = DB.keys.iterator()
            while(keys.hasNext()) {
                var key = keys.next()
                var user = DB.get(key)
                data += key + ":" + user
            }
            settings?.SetSetting("IPDB", data)
        }

        if(message[0].toLowerCase() == "!alias") {
            var keys = DB.keys.iterator()
            while(keys.hasNext())
            {
                var key = keys.next()
                var userlist = DB.get(key)
                if(userlist != null) {
                    if(userlist.contains(message[1])) {
                        controller?.AddToBoxBuffer("User aliases: " + userlist)
                        break
                    } else {
                        controller?.AddToBoxBuffer("No data")
                        logger?.LogPlugin(this.pluginName.toString(), "Error: Could not find data for " + message[1])
                    }
                }
            }
        }
        return true
    }
}