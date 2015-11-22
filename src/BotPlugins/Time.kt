/**
 * Time command plugin
 * Created by zingmars on 11.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem
import java.text.SimpleDateFormat
import java.util.*

public class Time : BasePlugin()
{
    override fun pubInit() :Boolean
    {
        return true
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")
        var commandSize = message.size
        when(message[0].toLowerCase()) {
            "!time" -> {
                var Format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss z")
                if(commandSize == 1 || message[1] == "") {
                    controller?.AddToBoxBuffer("Current time: " + Format.format(Date()))
                } else if(commandSize > 1) {
                    try {
                        var TZ = TimeZone.getTimeZone(message[1])
                        Format.timeZone = TZ
                        controller?.AddToBoxBuffer("Current time: " + Format.format(Date()))
                    } catch (e: Exception) {
                        controller?.AddToBoxBuffer("Current time: " + Format.format(Date()))
                    }
                }
            }
            "!timeuntil" -> {
                if(commandSize > 1) {
                    var Format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                    try {
                        // 1 - Date
                        if(message[1] == "") {
                            throw Exception("Wrong date")
                        }
                        var date = message[1]

                        // 2 - Hour
                        var hour = "00:00:00"
                        if(commandSize > 2 && message[2] != "") {
                            hour = message[2]
                        }

                        // 3 - Time zone
                        var timezone = "UTC"
                        if(commandSize > 3 && message[3] != "") {
                            timezone = message[3]
                        }

                        var currentDate = Date()
                        var currentUNIX = currentDate.time/1000

                        var TZ = TimeZone.getTimeZone(timezone)
                        Format.timeZone = TZ
                        var inputTime = Format.parse(date + " " + hour)
                        var inputUNIX = inputTime.time/1000

                        var difference = inputUNIX - currentUNIX
                        if(difference < 1) {
                            controller?.AddToBoxBuffer(settings?.generateTimeString(currentUNIX, inputUNIX, "ago") as String)
                        }
                        else {
                            controller?.AddToBoxBuffer(settings?.generateTimeString(inputUNIX, currentUNIX, "") as String)
                        }

                    } catch (e: Exception) {
                        controller?.AddToBoxBuffer("Syntax - dd-MM-yyyy HH:mm:ss z, where dd - date, MM - month, yyyy - year, HH - hour (24h), mm - minute, ss - second, z - timezone. You must specify a date, the rest is optional.")
                    }
                }
            }
        }
        return true
    }
}
