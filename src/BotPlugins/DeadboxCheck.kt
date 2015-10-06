/**
 * Stores last cbox activity and posts about it if records are broken
 * Created by zingmars on 04.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem

public class DeadboxCheck : BasePlugin()
{
    private var RecordUsername = ""
    private var RecordTime = 0L
    private var lastMessage = 0L
    override fun pubInit() :Boolean
    {
        try {
            settings?.checkSetting("DeadBox", true)
            var savedData = settings?.GetSetting("DeadBox").toString()

            if(savedData != "")  {
                var data = savedData.split(",")
                RecordUsername = data[0]
                RecordTime = data[1].toLong()
            }
            return true
        } catch (e: Exception) {
            logger?.LogPlugin(this.pluginName.toString(), "Error: " + e.toString())
            return false
        }
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var timeSinceLast = buffer.time.toLong() - lastMessage
        if(lastMessage != 0L && timeSinceLast >= 3600L) {
            if(timeSinceLast > RecordTime) {
                RecordTime = timeSinceLast
                RecordUsername = buffer.userName
                controller?.AddToBoxBuffer("Congratz " + buffer.userName + "! You just revived the box and set a new record doing so! This deadbox lasted " + (timeSinceLast.toDouble()/60).toString() + " minutes.")
                saveData()
            } else {
                controller?.AddToBoxBuffer("Congratz " + buffer.userName + "! You just revived the box. This deadbox lasted " + (timeSinceLast.toDouble()/60).toString() + " minutes. The longest recorded deadbox was " + (RecordTime.toDouble()/60).toString() + " minutes long and it was broken by " + RecordUsername)
            }
        }
        return true
    }
    private fun saveData()
    {
        settings?.SetSetting("DeadBox", RecordUsername+","+RecordTime.toString())
    }
}
