/**
 * Stores last cbox activity and posts about it if records are broken
 * Created by zingmars on 04.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem
import java.util.*

public class DeadboxCheck : BasePlugin()
{
    private var RecordUsername = ""
    private var RecordTime = 0L
    private var lastMessage = 0L
    private var RecordDate = 0L
    override fun pubInit() :Boolean
    {
        //TODO: Test
        try {
            settings?.checkSetting("DeadBox", true)
            var savedData = settings?.GetSetting("DeadBox").toString()

            if(savedData != "")  {
                var data = savedData.split(",")
                RecordUsername = data[0]
                RecordTime = data[1].toLong()
                RecordDate = data[2].toLong()
            }
            return true
        } catch (e: Exception) {
            logger?.LogPlugin(this.pluginName.toString(), "Error: " + e.toString())
            return false
        }
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")
        if(message[0] == "!deadbox") controller?.AddToBoxBuffer("Currently longest recorded deadbox is " + ((((RecordTime/60)*100).toInt()).toDouble()/100).toString() + " minutes long, broken by " + RecordUsername + " on " + Date(RecordDate*1000).toString())
        try {
            var timeSinceLast = buffer.time.toLong() - lastMessage
            if(lastMessage != 0L && timeSinceLast >= 5400L) {
                if(timeSinceLast > RecordTime) {
                    RecordTime = timeSinceLast
                    RecordUsername = buffer.userName
                    RecordDate = buffer.time.toLong()
                    controller?.AddToBoxBuffer("Congratulations " + buffer.userName + "! You just revived the box and set a new record doing so! This deadbox lasted " + settings?.generateTimeString(buffer.time.toLong(), lastMessage) + ".")
                    logger?.LogPlugin(this.pluginName as String, "Deadbox ended with a new record.")
                    saveData()
                } else {
                    controller?.AddToBoxBuffer("Congratulations " + buffer.userName + "! You just revived the box. This deadbox lasted " + settings?.generateTimeString(buffer.time.toLong(), lastMessage) + ".")
                    logger?.LogPlugin(this.pluginName as String, "Deadbox ended.")
                }
            }
            lastMessage = settings?.getCurrentTime() as Long
            return true
        } catch (e: Exception) {
            logger?.LogPlugin(this.pluginName as String, "Error: " + e.toString())
            return false
        }
    }
    private fun saveData()
    {
        settings?.SetSetting("DeadBox", RecordUsername+","+RecordTime.toString()+","+RecordDate.toString())
    }
}
