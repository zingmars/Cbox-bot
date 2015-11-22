/**
 * Return a quote.
 * Created by zingmars on 11.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem

public class Quotes : BasePlugin()
{
    override fun pubInit() :Boolean
    {
        // Load quotes DB
        return false // Not implemented yet
    }
    override fun connector(buffer : PluginBufferItem) :Boolean
    {
        var message = buffer.message.split(" ")
        when(message[0].toLowerCase()) {
            "!quote" -> {
                var isAdmin = handler?.isPluginAdmin(buffer.userName) as Boolean
                if(message[1] != "") {
                    // Select specific quote
                } else {
                    if(isAdmin) {
                        if(message[1] == "add") {
                            // Add a quote. Check for privileges.
                        } else if(message[1] == "remove") {

                        }
                        else {
                            // Select a random quote
                        }
                    } else {

                    }
                }
            }
        }
        return true
    }
    override fun executeCommand(command :String) :String
    {
        // Reload quotes
        // Add
        // Remove
        return ""
    }
}
