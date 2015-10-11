/**
 * Class to manage settings loading, editing, saving
 * Created by zingmars on 02.10.2015.
 */
import java.util.*
import java.io.File

public class Settings(private var settingsFileName :String = "settings.cfg", private var empty :Boolean = false)
{
    private val settingsContainer   = Properties()
    private var settingsFile        = File(settingsFileName)
    private var logger :Logger?     = null
    private var changed :Boolean    = false

    init
    {
        // Check if the settings file exists. If not, create one.
        if (!settingsFile.exists() && !empty)  {
            settingsFile.createNewFile()
            //Fill default values
            settingsContainer.set("maxLogs", "100")
            settingsContainer.set("logFolder", "logs")
            settingsContainer.set("logFile", "log.txt")
            settingsContainer.set("chatLogFile", "chat.txt")
            settingsContainer.set("pluginLogFile", "plugins.txt")
            settingsContainer.set("fileLog", "true")
            settingsContainer.set("logChat", "true")
            settingsContainer.set("consoleLog", "true")
            settingsContainer.set("logLevel", "1")
            settingsContainer.set("pluginDirectory", "BotPlugins")
            settingsContainer.set("logRotate", "true")

            var input :String?
            println("Settings file not found/not configured, please enter the following details")

            // CLI daemon configuration
            println("Enable CLI daemon? (true):")
            input = readLine()
            if(input == "") input = "true"
            settingsContainer.set("daemonEnabled", input)

            if(input == "true") {
                println("Enter daemon port (9970):")
                input = readLine()
                if(input == "") input = "9970"
                settingsContainer.set("daemonPort", input)
            } else {
                settingsContainer.set("daemonPort", "9970")
            }

            // Log Archiving
            println("Enable log archiving? [true/false] (true):")
            input = readLine()
            if(input == "") input = "true"
            settingsContainer.set("archiveLogs", input)

            // Plugins
            // Log Archiving
            println("Enable plugins? [true/false] (true):")
            input = readLine()
            if(input == "") input = "true"
            settingsContainer.set("enablePlugins", input)

            // cbox.ws connection configuration
            println("cbox.ws server (0):")
            input = readLine()
            if(input == "") input = "0"
            settingsContainer.set("server", input)

            println("Box ID (0):")
            input = readLine()
            if(input == "") input = "0"
            settingsContainer.set("boxid", input)

            println("Box tag (0):")
            input = readLine()
            if(input == "") input = "0"
            settingsContainer.set("boxtag", input)

            println("Box refresh rate in miliseconds (5000):")
            input = readLine()
            if(input == "") input = "5000"
            settingsContainer.set("refreshRate", input)

            println("Username (blank for no login): ")
            input = readLine()
            settingsContainer.set("username", input)
            if(input == "") {
                settingsContainer.set("avatar", input)
                settingsContainer.set("password", input)
            } else {
                println("Avatar URL: ")
                input = readLine()
                settingsContainer.set("avatar", input)

                println("Password - WARNING: If you enter a password, please ensure that the settings file has proper privileges since it is available in plain text: ")
                input = readLine()
                settingsContainer.set("password", input)
            }

            changed = true
            this.SaveSettings()
        }

        //Load the settings file using Java's properties lib
        this.LoadSettings()
    }

    // Get
    public fun GetSetting (setting :String) :String
    {
        try {
            return settingsContainer.getProperty(setting)
        }
        catch (e:Exception) {
            return ""
        }
    }
    public fun checkSetting (setting :String, create :Boolean = false) :Boolean
    {
        try {
            settingsContainer.getProperty(setting)
            return true
        } catch (e: Exception){
            if(create) {
                this.SetSetting(setting, "")
                return true
            }
            return false
        }
    }
    public fun LoadSettings ()
    {
        if(!settingsFile.exists()) settingsFile.createNewFile()

        settingsContainer.load(settingsFile.inputStream())
        logger?.LogMessage(9)
    }
    public fun GetAllSettings () :String
    {
        var output = ""
        var keys = settingsContainer.keySet().iterator()
        while(keys.hasNext()) {
            var key = keys.next().toString()
            var data = settingsContainer.get(key)
            output += key + ":" + data + "\n"
        }
        return output
    }

    // Set / Save
    public fun SetSetting (setting :String, value :String)
    {
        settingsContainer.setProperty(setting, value)
        if(setting != "password") logger?.LogBoringMessage(2, setting+":"+value)
        changed = true
    }
    public fun SaveSettings ()
    {
        if(changed) {
            settingsContainer.store(settingsFile.outputStream(), "CBox Bot settings file. Please don't touch unless you know specifically what to change, else you might cause a crash.")
            logger?.LogBoringMessage(1)
        } else {
            logger?.LogBoringMessage(3)
        }
    }

    // API (management) functions
    public fun SetLogger(newLogger :Logger?)
    {
        logger = newLogger
        logger?.LogMessage(10)
    }
    public fun ChangeSettingsFile( FileName :String )
    {
        settingsFile = File(FileName)
        this.LoadSettings()
    }
    public fun getCurrentTime() :Long
    {
        return System.currentTimeMillis() / 1000L
    }
    public fun generateTimeString(current :Long, past :Long, endString :String = "") :String
    {
        var streamMinutesAgo = (current-past).toDouble()/60
        var streamHoursAgo = if(streamMinutesAgo/60.0 >= 1.0) {streamMinutesAgo/60.0} else 0.0
        var streamDaysAgo = if(streamHoursAgo/24.0 >= 1.0) {streamHoursAgo/24.0} else 0.0
        return if(streamDaysAgo != 0.0) {Math.round(streamDaysAgo*100).toString() + " days " + endString} else if (streamHoursAgo != 0.0) {Math.round(streamHoursAgo*100).toString() + " hours " + endString} else {Math.round(streamMinutesAgo).toString() + " minutes " + endString}
    }
}