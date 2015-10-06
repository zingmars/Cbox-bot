/**
 * Overengineered cbox.ws bot for OverTheGun.org
 * Mostly a side project to learn kotlin
 * Created by zingmars on 29.09.2015.
 */
import java.util.*

fun main(args: Array<String>)
{
    //Change the default time zone to UTC to avoid outputting local time.
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    // Load and set overengineered settings
    var SettingsFile = "settings.cfg"
    if(!args.isEmpty()) SettingsFile = args[0]
    var Settings = Settings(SettingsFile)

    // Load an overengineered logging system
    val Logger = Logger(Settings)

    // Thread controller
    val ThreadController = ThreadController(Logger)

    // Initiate an instance of a cbox connection
    val Box = Box(Settings, Logger, ThreadController)

    // Load a plugin loader system
    val Plugins = Plugins(Settings, Logger, ThreadController)

    // Load an overengineered CLI access system
    val CLI = CLI(Settings, Logger, Box, Plugins, ThreadController)

    var refreshRate = Settings.GetSetting("refreshRate").toLong()
    while (Box.isActive() || CLI.isActive() || Plugins.isActive()) {
        Thread.sleep(refreshRate)
        //General commands. Feel free to extend with your own stuff.
        for(command in ThreadController.GetMainBuffer())  {
            var splitCommand = command.split(",")
            when (splitCommand[0]) {
                "Restart" -> {
                    Logger.LogMessage(56, splitCommand[1])
                    when(splitCommand[1]) {
                        "Box" -> Box.Reload()
                        "Plugins" -> Plugins.Reload()
                        "Logger" -> Logger.Reload()
                        "CLI" -> CLI.Reload()
                    }
                }
            }
        }
    }
    System.exit(1337) // Kill all rogue plugin daemons and whatnot
}