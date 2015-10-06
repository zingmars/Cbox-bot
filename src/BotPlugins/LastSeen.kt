/**
 * Stores user identifieable data and responds to queries about specific users from mods
 * Created by zingmars on 04.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem
import java.util.*

public class LastSeen : BasePlugin() {
    var users :TreeMap<String, ArrayList<String>> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    var HonourableUsers = ArrayList<String>()
    //TODO: Convert the data into a JSON string or something, because if someone uses these characters in their username the loading will be broken
    var dbUserNameSeparator = "="
    var dbUserDataSeparator = "+"
    var dbRecordSeparator = "<>"
    var ipSeparatorList = "?"
    var saverThread = Thread()
    var changed = false

    private fun databaseSaver()
    {
        while (!saverThread.isInterrupted) {
            try {
                Thread.sleep(30000) //Save to disk every 30 seconds
                if(changed) {
                    var data = ""
                    var keys = users.keySet().iterator()
                    var first = true

                    while(keys.hasNext()) {
                        var key = keys.next()
                        var user = users.get(key)
                        if (!first) {
                            data += dbRecordSeparator
                        }
                        data += key + dbUserNameSeparator + user?.join(dbUserDataSeparator)
                        first = false
                    }

                    settings?.SetSetting("LastSeen", data)
                    logger?.LogBoringPluginData(this.pluginName.toString(), "Saved user data to disk")
                }
            } catch (e: Exception) {
                settings?.SaveSettings() //Will happen if the program is shutting down
            }
        }
    }
    override fun pubInit() :Boolean
    {
        //Load data saved in the database
        try {
            settings?.checkSetting("LastSeen", true) //Create the settings variable if it doesn't exist, otherwise it will return a fail
            settings?.checkSetting("LastSeenHonourable", true)

            var savedHonours = settings?.GetSetting("LastSeenHonourable")
            var savedDB = settings?.GetSetting("LastSeen").toString()
            if(savedDB != "")  {
                var data = savedDB.split(dbRecordSeparator)
                for(user in data) {
                    var username = user.split(dbUserNameSeparator)[0]
                    var userdata = user.split(dbUserNameSeparator)[1].split(dbUserDataSeparator).toArrayList()
                    users.put(username, userdata)
                }
            }
            if(savedHonours != "" && savedHonours != null) {
                HonourableUsers = savedHonours.split(",").toArrayList()
            }
            //Run a separate thread to save users in the background
            saverThread = Thread(Runnable { this.databaseSaver() })
            saverThread.isDaemon = true
            saverThread.start()
            return true
        } catch (e: Exception) {
            logger?.LogPlugin(pluginName.toString(), "Error: " + e.toString())
            return false
        }
    }
    override fun connector(buffer :PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")

        // Respond to @lastseen
        if(message[0].toLowerCase() == "@lastseen" && (buffer.privilvl == "mod" || buffer.privilvl == "user")) {
            var user = users.get(message[1])
            if(user != null) {
                if(message[1].toLowerCase() != handher?.username?.toLowerCase()) {
                    controller?.AddToBoxBuffer(buffer.userName+": User \"" + message[1] + "\" last seen " + Date(user[2].toLong()*1000).toString())
                } else {
                    controller?.AddToBoxBuffer(buffer.userName+": I'm here!")
                }
            } else {
                controller?.AddToBoxBuffer(buffer.userName+": User \"" + message[1] + "\" not in database")
            }
        }

        // Add user to database
        var user = users.get(buffer.userName)
        if(user != null) {
            // Greet Honourable users
            if(HonourableUsers.contains(buffer.userName) && buffer.time.toLong() - user[2].toLong() > 86400) {
                controller?.AddToBoxBuffer("Welcome back " + buffer.userName + "!")
            }

            if(!user[1].contains(buffer.extradata) && buffer.extradata != "") user[1] += buffer.extradata+ipSeparatorList //new IP to the list
            user[2] = buffer.time
        } else {
            var userData :ArrayList<String> = ArrayList()
            userData.add(0, buffer.userID)
            userData.add(1, buffer.extradata)
            userData.add(2, buffer.time)
            users.set(buffer.userName, userData)
            logger?.LogPlugin(pluginName.toString(), "New user encountered: " + buffer.userName)
        }
        changed = true
        return true
    }
    override fun stop()
    {
        saverThread.interrupt()
    }
}
