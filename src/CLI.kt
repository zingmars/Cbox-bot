/**
 * CLI manager daemon
 * Created by zingmars on 03.10.2015.
 */
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

public class CLI (private val Settings :Settings, private val Logger :Logger, private val Box :Box, private val Plugins :Plugins, private val ThreadController :ThreadController)
{
    private var port :Int           = Settings.GetSetting("daemonPort").toInt()
    private var Daemon              = Thread()
    private var enabled             = false
    private var active              = false

    init
    {
        enabled = Settings.GetSetting("daemonEnabled").toBoolean()
        if(enabled) {
            this.start()
        } else {
            Logger.LogMessage(13)
        }

    }

    //Daemon functions
    public fun isActive() :Boolean
    {
        return active
    }
    public fun start()
    {
        if(enabled) {
            this.active = true
            Daemon = Thread(Runnable () { this.Demon() })
            Daemon.isDaemon = true
            Logger.LogMessage(14, port.toString())
            Daemon.start()
            ThreadController.ConnectCLI(Daemon)
        }
    }
    public fun stop()
    {
        this.active = false;
        ThreadController.DisconnectCLI()
        Daemon.stop()
        Logger.LogMessage(60)
    }
    public fun Reload() :Boolean
    {
        try {
            Logger.LogMessage(59)
            this.stop()
            this.start()
            return true
        } catch (e: Exception) {
            Logger.LogMessage(65)
            this.active = false
            return false
        }
    }
    private fun Demon() //Yes, the spelling's on purpose
    {
        // Create a simple daemon listening on a specified port
        val socket = ServerSocket(port)

        while(active)  {
            val client = socket.accept() // Only allow one at a time since it's not supposed to be a public interface
            if(client.isConnected)  {
                this.ActiveConnectionHandler(client)
            }
        }
    }
    private fun ActiveConnectionHandler(client :java.net.Socket)
    {
        val SessionStartTime = System.currentTimeMillis() / 1000L;
        val out = PrintWriter(client.outputStream, true)
        val _in = BufferedReader(InputStreamReader(client.inputStream))
        var connectionActive = true

        Logger.LogMessage(15, client.remoteSocketAddress.toString())
        out.println("Cbox.ws bot CLI interface - Welcome! Type :help for help. Please note that commands are not checked for correctness, and will accept any input. Use this interface at your own risk.") //Welcome message
        // Await input and react to it
        var input :String

        while(true)  {
            for(msg in ThreadController.GetCLIBuffer()) {
                out.println(msg)
            }
            out.printf(">> ")
            try {
                input = _in.readLine() //My major gripe with kotlin - you can't have expression within if, while etc. Sort of annoying.
            } catch(e: Exception)  {
                break; //Client has disconnected, close socket.
            }

            // Separate command from parameters and log it
            var command :List<String> = input.split(" ")
            if(command.size() > 1) {
                Logger.LogCommands(command[0], input.substring(input.indexOf(" ")+1))
            } else {
                Logger.LogCommands(input)
            }

            try {
                // Execute commands
                when(command[0].toLowerCase()) {
                    //TODO: have all of the functions return some sort of indication of their actions
                    ":quit" -> { // Disconnect
                        if(ConfirmAction(out, _in))  {
                            Logger.LogMessage(16, "Session lasted " + (System.currentTimeMillis() / 1000L - SessionStartTime).toString() + " seconds")
                            client.close()
                            connectionActive = false
                        }
                        else {
                            Logger.LogMessage(17)
                        }
                    }
                    ":shutdowncli" -> { // Shut down the CLI interface. Warn - can't bring it up without restarting the server.
                        if(ConfirmAction(out, _in)) {
                            Logger.LogMessage(16, "Session lasted " + (System.currentTimeMillis() / 1000L - SessionStartTime).toString() + " seconds")
                            Logger.LogMessage(29)
                            client.close()
                            this.stop()
                            this.enabled = false
                            connectionActive = false
                        } else {
                            Logger.LogMessage(17)
                        }
                    }
                    ":ping" -> {
                        out.println("Pong!")
                    }
                    ":shutdown" -> {
                        if(ConfirmAction(out, _in)) {
                            Logger.LogMessage(16, "Session lasted " + (System.currentTimeMillis() / 1000L - SessionStartTime).toString() + " seconds")
                            Logger.LogMessage(55)
                            out.println("Shutdown initiated. Good bye!")
                            connectionActive = false

                            Box.stop()
                            Plugins.stop()
                            this.stop()
                        }
                    }
                    ":reload" -> {
                        if(ConfirmAction(out, _in)) {
                            this.Reload()
                        }
                    }
                    ":help" -> {
                        out.println("Please read CLI.kt for full command list.")
                    }
                    "settings.savesettings" -> {
                        Settings.SaveSettings()
                        out.println("Changes saved successfully.")
                    }
                    "settings.loadsettings" -> {
                        if(ConfirmAction(out, _in))  Settings.LoadSettings()
                        out.println("Settings reloaded successfully. Please reload modules for the changes to have an effect.")
                    }
                    "settings.getsetting" -> {
                        out.println(command[1] + ":" + Settings.GetSetting(command[1]))
                    }
                    "settings.setsetting" -> {
                        Settings.SetSetting(command[1], command[2])
                        out.println(command[1] + " set to " + command[2])
                    }
                    "settings.changesettingsfile" -> {
                        if(ConfirmAction(out, _in)) Settings.ChangeSettingsFile(command[1])
                        out.println("Settings reloaded successfully. Please reload modules for the changes to have an effect.")
                    }
                    "settings.getSettings" -> {
                        out.println(Settings.GetAllSettings())
                    }
                    "logger.disable" -> {
                        if(ConfirmAction(out, _in)) {
                            var pam1 = true
                            var pam2 = true
                            try{
                                pam1 = command[1].toBoolean()
                                pam2 = command[2].toBoolean()

                            } catch (ex: Exception) {
                            }
                            Logger.Disable(pam1, pam2)
                            out.println("Logging disabled")
                        }
                    }
                    "logger.enable" -> {
                        var pam1 = true
                        var pam2 = true
                        try{
                            pam1 = command[1].toBoolean()
                            pam2 = command[2].toBoolean()

                        } catch (ex: Exception) {
                        }
                        Logger.Enable(pam1, pam2)
                        out.println("Logging enabled")
                    }
                    "logger.archive" -> {
                        if(ConfirmAction(out,_in)) {
                            Logger.ArchiveOldLogs()
                            out.println("Logs archived successfully")
                        }
                    }
                    "logger.toconsole" -> {
                        Logger.ChangeConsoleLoggingState(command[1].toBoolean())
                        out.println("Changed console logging behaviour")
                    }
                    "logger.tofile" -> {
                        Logger.ChangeFileLoggingState(command[1].toBoolean())
                        out.println("Changed file logging behaviour")
                    }
                    "logger.chattofile" -> {
                        Logger.ChangeChatLoggingState(command[1].toBoolean())
                        out.println("Changed chat logging behaviour")
                    }
                    "logger.changelogfile" -> {
                        if(ConfirmAction(out, _in)) {
                            var pam1 = if(command[1] == "") Settings.GetSetting("logFile") else command[1]
                            var pam2 = if(command[2] == "") Settings.GetSetting("logFolder") else command[2]

                            Logger.ChangeLogFile(pam1, pam2)
                            out.println("Changed currently active log file")
                        }
                    }
                    "logger.changechatlogfile" -> {
                        if(ConfirmAction(out, _in)) {
                            var pam1 = if(command[1] == "") Settings.GetSetting("chatLogFile") else command[1]
                            var pam2 = if(command[2] == "") Settings.GetSetting("logFolder") else command[2]

                            Logger.ChangeChatLogFile(pam1, pam2)
                            out.println("Changed currently active chat log file")
                        }
                    }
                    "logger.changepluginlogfile" -> {
                        if(ConfirmAction(out, _in)) {
                            var pam1 = if(command[1] == "") Settings.GetSetting("chatLogFile") else command[1]
                            var pam2 = if(command[2] == "") Settings.GetSetting("logFolder") else command[2]

                            Logger.ChangePluginLogFile(pam1, pam2)
                            out.println("Changed currently active chat log file")
                        }
                    }
                    "logger.reload" -> {
                        ThreadController.AddToMainBuffer("Restart,Logger")
                    }
                    "global.reload" -> {
                        out.println("This will initiate a global reload. You might be disconnected and the app might not reload successfully.")
                        if(ConfirmAction(out, _in))  {
                            Logger.LogMessage(54)
                            ThreadController.AddToMainBuffer("Restart,Logger")
                            ThreadController.AddToMainBuffer("Restart,Box")
                            ThreadController.AddToMainBuffer("Restart,Plugins")
                            out.println("Please wait...")
                            Thread.sleep(Settings.GetSetting("refreshRate").toLong()*3) //wait for the main process to sync and the other processes to do their thing
                            out.println("Modules reloaded")
                        }
                    }
                    "box.reload" -> {
                        ThreadController.AddToMainBuffer("Restart.Box")
                        out.println("Please wait...")
                        Thread.sleep(Settings.GetSetting("refreshRate").toLong()*3)
                    }
                    "box.tocli" -> {
                        this.CLIChat(out, _in, client)
                    }
                    "box.relog" -> {
                        out.println("Relogging with given credentials (username, password, avatarURL) and reloading the box")
                        try {
                            Box.ChangeCredentials(command[1], command[2], command[3])
                        } catch (e: Exception) {
                            Box.ChangeCredentials(command[1], command[2], "")
                        }
                        ThreadController.AddToMainBuffer("Restart.Box")
                        out.println("Please wait...")
                        Thread.sleep(Settings.GetSetting("refreshRate").toLong()*3)
                    }
                    "box.refreshrate" -> {
                        Box.changeRefreshRate(command[1].toLong())
                        out.println("Box refresh rate changed")
                    }
                    "box.getip" -> {
                        out.println("Response: " + Box.getIP(command[1]))
                    }
                    "box.send" -> {
                        Box.SendMessage(command.join(" ").replace("send", ""))
                    }
                    "plugins.reload" -> {
                        ThreadController.AddToMainBuffer("Restart.Plugins")
                    }
                    "plugins.disable" -> {
                        if(ConfirmAction(out, _in)) {
                            Plugins.Disable()
                        }
                    }
                    "plugins.enable" -> {
                        if(ConfirmAction(out, _in)) {
                            Plugins.Enable()
                        }
                    }
                    "plugins.refreshrate" -> {
                        Plugins.changeRefreshRate(command[1].toLong())
                    }
                    "plugins.unload" -> {
                        if(ConfirmAction(out, _in)) {
                            out.println(Plugins.unloadPlugin(command[1]))
                        }
                    }
                    "plugins.load" -> {
                        //TODO: Bug - the plugin needs to be correctly capitalised or it will crash the thread
                        out.println("Warning: There's currently a bug that will unrecoverably crash the CLI module if you misspell the plugin's name or write it in the wrong CaSe.")
                        if(ConfirmAction(out, _in)) {
                            Plugins.LoadPlugin(command[1]+".kt")
                        }
                    }
                    "plugins.reloadplugin" -> {
                        out.println("Warning: There's currently a bug that will unrecoverably crash the CLI module if you misspell the plugin's name or write it in the wrong CaSe.")
                        if(ConfirmAction(out, _in)) {
                            Plugins.reloadPlugin(command[1])
                        }
                    }
                    "plugins.savesettings" -> {
                        Plugins.SavePluginSettings()
                        out.println("Saved")
                    }
                    "plugins.getSettings" -> {
                        out.println(Plugins.GetAllSettings())
                    }
                    "plugins.setSetting" -> {
                        Plugins.SetSetting(command[1], command[2])
                        out.println(command[1] + " set to " + command[2])
                    }
                    //TODO: Create a way to talk directly to plugins from CLI
                    else -> {
                        out.println("Unrecognised command")
                    }
                }
            } catch (e: Exception) {
                out.println("An error occurred while executing your command")
                Logger.LogMessage(999, "CLI command execution failure: " + e.toString())
            }


            //Special case : Break out of the while loop if the client has quit
            if(!connectionActive) {
                break;
            }
        }
    }

    //CLI misc functions
    private fun ConfirmAction(output :PrintWriter, input :BufferedReader) :Boolean
    {
        output.println("Are you sure? (no)")

        try {
            output.printf("> ")
            var userInput = input.readLine() //My major gripe with kotlin - you can't have expression within if, while etc. Sort of annoying.
            if(userInput == "yes") {
                return true
            }
            else {
                output.println("Command cancelled")
                return false
            }
        } catch(e: Exception)  {
            //Client has DCed
        }
        return false
    }
    private fun CLIChat(output :PrintWriter, input :BufferedReader, client :java.net.Socket)
    {
        // Very hacky CLI chat. It basically relies on timeout interruptions to receive messages and it isn't the most stable thing around.
        //Initialise environment
        val SessionStartTime = System.currentTimeMillis() / 1000L;
        Logger.LogMessage(57)
        ThreadController.GetCLIBuffer() //Clear buffer
        Box.toCLI = true
        client.soTimeout = Settings.GetSetting("refreshRate").toInt()

        //Welcome message
        output.print("\u001B[2J")
        output.flush()
        output.println("CLI Chat 1.0. Enjoy your stay. Please note that this will only show messages written since chat was started. To see logs, please refer to your logs directory.")
        output.println("Write really short messages to the char just write anything and press enter, :quit to quit, :write .> enter -> message to type longer messages without being interrupted. (this will pause receiving of messages though)")

        //Print messages and allow input
        var CLIActive = true
        while(CLIActive) {
            for(msg in ThreadController.GetCLIBuffer()) {
                output.println(msg)
            }

            try {
                var userInput = input.readLine()
                when (userInput) {
                    ":quit" -> {
                        CLIActive = false
                        client.soTimeout = 0
                        Box.toCLI = false
                        Logger.LogMessage(58, "Session lasted " + (System.currentTimeMillis() / 1000L - SessionStartTime).toString() + " seconds")
                        ThreadController.GetCLIBuffer()
                        output.print("\u001B[2J")
                    }
                    ":write" -> {
                        output.printf(":")
                        client.soTimeout = 0
                        userInput = input.readLine()
                        Box.SendMessage(userInput)
                        client.soTimeout = Settings.GetSetting("refreshRate").toInt()
                    }
                    else -> {
                        Box.SendMessage(userInput)
                    }
                }
            } catch (e: Exception) {
            }
        }

        return
    }
}