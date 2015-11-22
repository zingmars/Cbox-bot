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
    private fun Demon()
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
        val SessionStartTime = Settings.getCurrentTime()
        val out = PrintWriter(client.outputStream, true)
        val _in = BufferedReader(InputStreamReader(client.inputStream))
        var connectionActive = true

        Logger.LogMessage(15, client.remoteSocketAddress.toString())
        out.println("Cbox.ws bot CLI interface - Welcome! Type :help for help. Please note that commands are not checked for correctness, and will accept any input. Use this interface at your own risk.") //Welcome message
        // Await input and react to it
        var input :String

        // Listen for input
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
            if(command.size > 1) {
                Logger.LogCommands(command[0], input.substring(input.indexOf(" ")+1))
            } else {
                Logger.LogCommands(input)
            }

            try {
                // Execute commands
                when(command[0].toLowerCase()) {
                    ":quit" -> { // Disconnect
                        if(ConfirmAction(out, _in))  {
                            Logger.LogMessage(16, "Session lasted " + (Settings.getCurrentTime() - SessionStartTime).toString() + " seconds")
                            client.close()
                            connectionActive = false
                        }
                        else {
                            Logger.LogMessage(17)
                        }
                    }
                    ":shutdowncli" -> { // Shut down the CLI interface. Warn - can't bring it up without restarting the server.
                        if(ConfirmAction(out, _in)) {
                            Logger.LogMessage(16, "Session lasted " + (Settings.getCurrentTime() - SessionStartTime).toString() + " seconds")
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
                            try {
                                Logger.LogMessage(16, "Session lasted " + (Settings.getCurrentTime() - SessionStartTime).toString() + " seconds")
                                Logger.LogMessage(55)
                                out.println("Shutdown initiated. Good bye!")
                                connectionActive = false

                                Box.stop()
                                Plugins.stop()
                                this.stop()
                            } catch (e :Exception){
                                System.exit(0);
                            }

                        }
                    }
                    ":reload" -> {
                        if(ConfirmAction(out, _in)) {
                            if(this.Reload()) out.println("Reload successful!")
                            else out.println("Error while reloading the CLI module (how can you still see this?)")
                        }
                    }
                    ":help" -> {
                        out.println("Please refer to https://github.com/zingmars/Cbox-bot/blob/master/misc/cli.txt for full command list.")
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
                        if(command[1] != "") out.println(command[1] + ":" + Settings.GetSetting(command[1]))
                        else out.println("Syntax: settings.getsetting <settingname>. All trailing arguments will be ignored");
                    }
                    "settings.setsetting" -> {
                        if(command[1] != "" && command[2] != "") {
                            Settings.SetSetting(command[1], command[2])
                            out.println(command[1] + " set to " + command[2])
                        } else {
                            out.println("Syntax: settings.setsetting <name> <value>. All trailing arguments will be ignored, setting values mustn't contain any spaces.")
                        }
                    }
                    "settings.changesettingsfile" -> {
                        if(command[1] != "") {
                            if(ConfirmAction(out, _in)) Settings.ChangeSettingsFile(command[1])
                            out.println("Settings reloaded successfully. Please reload modules for the changes to have an effect.")
                        } else {
                            out.println("Syntax: settings.changesettingsfile <filename>. All trailing arguments will be ignored.")
                        }
                    }
                    "settings.getSettings" -> {
                        out.println(Settings.GetAllSettings())
                    }
                    "logger.disable" -> {
                        if(ConfirmAction(out, _in)) {
                            var pam1 = true
                            var pam2 = true
                            var pam3 = true
                            try{
                                pam1 = command[1].toBoolean()
                                pam2 = command[2].toBoolean()
                                pam3 = command[3].toBoolean()
                            } catch (ex: Exception) {
                            }
                            if(Logger.Disable(pam1, pam2, pam3)) {
                                out.println("Logging disabled")
                            } else {
                                out.println("Either an error occurred, or logging was already disabled");
                            }
                        }
                    }
                    "logger.enable" -> {
                        var pam1 = true
                        var pam2 = true
                        var pam3 = true
                        try{
                            pam1 = command[1].toBoolean()
                            pam2 = command[2].toBoolean()
                            pam3 = command[3].toBoolean()
                        } catch (ex: Exception) {
                        }
                        if(Logger.Enable(pam1, pam2, pam3)) {
                            out.println("Logging enabled")
                        } else {
                            out.println("Either an error occurred, or logging was already enabled");
                        }
                    }
                    "logger.archive" -> {
                        if(ConfirmAction(out,_in)) {
                            Logger.ArchiveOldLogs()
                            out.println("Logs archived successfully")
                        }
                    }
                    "logger.toconsole" -> {
                        if(command[1] != "") {
                            Logger.ChangeConsoleLoggingState(command[1].toBoolean())
                            out.println("Changed console logging behaviour")
                        } else {
                            out.println("Syntax: logger.toconsole <true:false>")
                        }
                    }
                    "logger.tofile" -> {
                        if(command[1] != "") {
                            Logger.ChangeFileLoggingState(command[1].toBoolean())
                            out.println("Changed file logging behaviour")
                        } else {
                            out.println("Syntax: logger.tofile <true:false>")
                        }
                    }
                    "logger.chattofile" -> {
                        if(command[1] != "") {
                            Logger.ChangeChatLoggingState(command[1].toBoolean())
                            out.println("Changed chat logging behaviour")
                        } else {
                            out.println("Syntax: logger.chattofile <true:false>")
                        }
                    }
                    "logger.changelogfile" -> {
                        if(ConfirmAction(out, _in)) {
                            var pam1 = if(command[1] == "") Settings.GetSetting("logFile") else command[1]
                            var pam2 = if(command[2] == "") Settings.GetSetting("logFolder") else command[2]

                            if(Logger.ChangeLogFile(pam1, pam2)) {
                                out.println("Changed currently active log file")
                            } else {
                                out.println("Could not change the log file. Logging disabled.")
                            }
                        }
                    }
                    "logger.changechatlogfile" -> {
                        if(ConfirmAction(out, _in)) {
                            var pam1 = if(command[1] == "") Settings.GetSetting("chatLogFile") else command[1]
                            var pam2 = if(command[2] == "") Settings.GetSetting("logFolder") else command[2]

                            if(Logger.ChangeChatLogFile(pam1, pam2)) {
                                out.println("Changed currently active chat log file")
                            } else {
                                out.println("Could not change the log file. Chat logging disabled.")
                            }
                        }
                    }
                    "logger.changepluginlogfile" -> {
                        if(ConfirmAction(out, _in)) {
                            var pam1 = if(command[1] == "") Settings.GetSetting("chatLogFile") else command[1]
                            var pam2 = if(command[2] == "") Settings.GetSetting("logFolder") else command[2]

                            if(Logger.ChangePluginLogFile(pam1, pam2)) {
                                out.println("Changed currently active plugin log file")
                            } else {
                                out.println("Could not change the log file. Plugin logging disabled.")
                            }
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
                        if(command[1] != "" && command[2] != "") {
                            try {
                                Box.ChangeCredentials(command[1], command[2], command[3])
                            } catch (e: Exception) {
                                Box.ChangeCredentials(command[1], command[2], "")
                            }
                            ThreadController.AddToMainBuffer("Restart.Box")
                            out.println("Please wait...")
                            Thread.sleep(Settings.GetSetting("refreshRate").toLong()*3)
                        } else {
                            out.println("Syntax: box.relog <username> <password> <optional: avatar URL>")
                        }
                    }
                    "box.refreshrate" -> {
                        if(command[1] != "") {
                            Box.changeRefreshRate(command[1].toLong())
                            out.println("Box refresh rate changed")
                        } else {

                        }
                    }
                    "box.getip" -> {
                        if(command[1] != "") {
                            out.println("Response: " + Box.getIP(command[1]))
                        }
                        else {
                            out.println("Syntax: box.getip <optional: messageID>")
                        }
                    }
                    "box.send" -> {
                        if(Box.SendMessage(command.joinToString(" ").replace("send", ""))) {
                            out.println("Message sent successfully")
                        } else {
                            out.println("Could not send your message. Are you logged in?")
                        }
                    }
                    "plugins.reload" -> {
                        ThreadController.AddToMainBuffer("Restart.Plugins")
                    }
                    "plugins.disable" -> {
                        if(ConfirmAction(out, _in)) {
                            Plugins.Disable()
                            out.println("Plugins disabled.")
                        }
                    }
                    "plugins.enable" -> {
                        if(ConfirmAction(out, _in)) {
                            Plugins.Enable()
                            out.println("Plugins enabled.")
                        }
                    }
                    "plugins.refreshrate" -> {
                        if(command[1] != "") {
                            Plugins.changeRefreshRate(command[1].toLong())
                            out.println("Refresh rate changed.")
                        } else {
                            out.println("Syntax: plugins.refreshrate <miliseconds>")
                        }
                    }
                    "plugins.unload" -> {
                        if(command[1] != "") {
                            if(ConfirmAction(out, _in)) {
                                out.println(Plugins.unloadPlugin(command[1]))
                            }
                        } else {
                            out.println("Syntax: plugins.unload <plugin name>")
                        }
                    }
                    "plugins.load" -> {
                        //TODO: Bug - the plugin needs to be correctly capitalised or it will crash the thread
                        if(command[1] != "") {
                            out.println("Warning: There's currently a bug that will unrecoverably crash the CLI module if you misspell the plugin's name or write it in the wrong CaSe.")
                            if(Plugins.LoadPlugin(command[1]+".kt")) {
                                out.println("Plugin loaded successfully")
                            } else {
                                out.println("Plugin loading failed")
                            }
                        } else {
                            out.println("Syntax: plugins.load <plugin name>")
                        }
                    }
                    "plugins.reload" -> {
                        if(command[1] != "") {
                            if(Plugins.reloadPlugin(command[1])) {
                                out.println("Plugin reloaded successfully")
                            } else {
                                out.println("Plugin reloading failed")
                            }
                        } else {
                            out.println("Syntax: plugins.reload <plugin name>")
                        }
                    }
                    "plugins.savesettings" -> {
                        Plugins.SavePluginSettings()
                        out.println("Saved")
                    }
                    "plugins.getsettings" -> {
                        out.println(Plugins.GetAllSettings())
                    }
                    "plugins.setsetting" -> {
                        if(command[1] != "" && command[2] != "") {
                            Plugins.SetSetting(command[1], command[2])
                            out.println(command[1] + " set to " + command[2])
                        }  else {
                            out.println("Syntax: plugins.setsetting <name> <value>")
                        }
                    }
                    "plugins.command" -> {
                        if (command[1] == "" || command[2] == "") {
                            out.println("Wrong syntax. Syntax - plugins.command <pluginname> <command>")
                        } else {
                            var param = command.joinToString(",")
                            param = param.substring(param.indexOf(",")+1) //Pluginname
                            param = param.substring(param.indexOf(",")+1) //Command
                            out.println(Plugins.executeFunction(command[1], param))
                        }
                    }
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
        //TODO: Have CLI chat be independent of input
        // Very hacky CLI chat. It basically relies on timeout interruptions to receive messages and it isn't the most stable thing around.
        //Initialise environment
        val SessionStartTime = Settings.getCurrentTime();
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
                        Logger.LogMessage(58, "Session lasted " + (Settings.getCurrentTime() - SessionStartTime).toString() + " seconds")
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