/**
 * Twitch checker plugin. Checks if the given user is streaming or not.
 * Configuration note: For this to work you need to add a key in Plugin settings named Streamer list
 * and it's value looks like <UserName>:0\\0 (these zeroes represent last stream on/off times, and the plugin will change them as people go on/off).
 * Created by zingmars on 04.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem
import java.util.*
import HTTP

public class TwitchCheck : BasePlugin()
{
    private var Daemon = Thread()
    private var stalkList :TreeMap<String, ArrayList<String>> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private var stalkState :TreeMap<String, String> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private var HTTPClient = HTTP()
    private val usernameDivisor = ":"
    private val dataDivisor = "\\"
    private val entryDivisor = ","

    override fun pubInit() :Boolean
    {
        try {
            HTTPClient.PassLogger(this.logger)
            settings?.checkSetting("StreamerList", true) //Create the settings variable if it doesn't exist, otherwise it will return a fail
            var savedDB = settings?.GetSetting("StreamerList").toString()
            if(savedDB != "")  {
                var users = savedDB.split(entryDivisor)
                for(user in users)  {
                    var data = user.split(usernameDivisor)
                    var userdata = data[1].split(dataDivisor)
                    stalkList.put(data[0], userdata.toArrayList())
                    stalkState.put(data[0], "false")
                }
            } else {
                throw Exception("Plugin not configured. Please add a list of usernames to plugin config files seperated by a comma in a <Twitch Username>:<StartTime>\\<EndTime> notation (you can put 0 if you want to, it uses UNIX time)")
            }

            Daemon = Thread(Runnable { this.Demon() })
            Daemon.isDaemon = true
            Daemon.start()
            return true

        } catch (e: Exception) {
            logger?.LogPlugin(pluginName.toString(), "Error: " + e.toString())
            return false
        }
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")
        if(message[0].toLowerCase() == "@laststream" && (buffer.privilvl == "mod" || buffer.privilvl == "user")) {
            if(message[1] != "") {
                if(stalkList.contains(message[1])) {
                    var user = stalkList.get(message[1])
                    var state = stalkState.get(message[1])
                    if(user != null && state != null) {
                        if(state == "true") {
                            controller?.AddToBoxBuffer("User is currently streaming.")
                        } else {
                            var lastStreamDate = user.get(1).toLong()
                            if(lastStreamDate != 0L) {
                                var stopTimeString = settings?.generateTimeString(settings?.getCurrentTime() as Long, lastStreamDate, "ago")
                                var timeString = Date((user.get(0).toLong()*1000))
                                controller?.AddToBoxBuffer("User last streamed " + timeString + " (stream ended " + stopTimeString + ")")
                            } else {
                                controller?.AddToBoxBuffer("User " + message[1] + " hasn't streamed yet.")
                            }
                        }
                    }
                } else {
                    controller?.AddToBoxBuffer("User is not monitored. (Currently monitoring: " + stalkList.keySet().join(",") + ")")
                }
            } else {
                controller?.AddToBoxBuffer("Please specify which streamer you want to know about. (Currently monitoring: " + stalkList.keySet().join(",") + ")")
            }
        }
        return true
    }
    override fun stop()
    {
        Daemon.interrupt()
    }

    private fun Demon()
    {
        var changes = false
        while(!Daemon.isInterrupted){
            try {
                Thread.sleep(30000) //Check every 30 seconds
                //TODO: Implement an actual JSON parser
                //Check for online state
                var keys = stalkList.keySet().iterator()
                while(keys.hasNext()) {
                    var key = keys.next()
                    var data = stalkList.get(key)
                    var state = stalkState.get(key)
                    if(data != null && state != null) {
                        var response = HTTPClient.GET("https://api.twitch.tv/kraken/streams/"+key+".json")

                        if(!response.contains("\"stream\":null") && state == "false") {
                            changes = true

                            data.set(0, settings?.getCurrentTime().toString())
                            stalkList.set(key, data)
                            stalkState.set(key, "true")

                            // Find game name
                            var gamename = ""
                            try {
                                gamename = response.substring(response.indexOf("\"game\":")+7,response.indexOf(",\"viewers"))
                            }
                            catch (e: Exception) {} //API Changed?
                            logger?.LogPlugin(this.pluginName.toString(), "User " + key + " has started streaming!")
                            controller?.AddToBoxBuffer("User " + key + " has started streaming " + gamename + ". Click here to view: http://www.twitch.tv/" + key)
                        } else if (response.contains("\"stream\":null") && state == "true") {
                            if(stalkState.get(key) == "true") {
                                changes = true

                                data.set(1, settings?.getCurrentTime().toString())
                                stalkList.set(key, data)
                                stalkState.set(key, "false")
                                logger?.LogPlugin(this.pluginName.toString(), "User " + key + " has stopped streaming!")
                                controller?.AddToBoxBuffer("User " + key + " has stopped streaming. Stream length was " + settings?.generateTimeString(data.get(1).toLong(), data.get(0).toLong()) + " minutes")
                            }
                        }
                    } else {
                        logger?.LogPlugin(this.pluginName.toString(), "Error while trying to modify database")
                    }
                }
                //If stuff happened, save it
                if(changes) {
                    keys = stalkList.keySet().iterator()
                    var dbdata = ""
                    var first = true
                    while(keys.hasNext()) {
                        var key = keys.next()
                        var data = stalkList.get(key)
                        if(data != null) {
                            if(!first) dbdata+=","
                            dbdata += key+usernameDivisor+data.get(0)+dataDivisor+data.get(1)
                            first = false
                        }
                    }
                    settings?.SetSetting("StreamerList", dbdata)
                    changes = false
                }
            } catch (e: Exception) {
                logger?.LogPlugin(this.pluginName.toString(), "Error: " + e.toString())
            }
        }
    }
}
