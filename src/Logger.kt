/**
 * Overengineered logger class
 * Created by zingmars on 02.10.2015.
 */
import Containers.Message
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

public class Logger(private val Settings :Settings) {
    private var DateFormat              = SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS")
    private var log                     = File("logs/log.txt")
    private var chatLog                 = File("logs/chat.txt")
    private var pluginLog               = File("logs/plugins.txt")
    private var enableStatus            = true
    private var loadedFile              = false
    private var logLineRotate           = Settings.GetSetting("logLineRotate").toBoolean()
    private var lineCount               = Settings.GetSetting("logSysLineCount").toInt()
    private var pluginLineCount         = Settings.GetSetting("logPluginLineCount").toInt()
    private var chatLineCount           = Settings.GetSetting("logChatLineCount").toInt()
    private var logLevel :Int           = Settings.GetSetting("logLevel").toInt()
    private val maxLogs :Int            = Settings.GetSetting("maxLogs").toInt()
    private var logFolder :String       = Settings.GetSetting("logFolder")
    private var logFileName :String     = Settings.GetSetting("logFile")
    private var chatLogFile :String     = Settings.GetSetting("chatLogFile")
    private var pluginLogFile :String   = Settings.GetSetting("chatLogFile")
    private var consoleLog :Boolean     = Settings.GetSetting("consoleLog").toBoolean()
    private var fileLog :Boolean        = Settings.GetSetting("fileLog").toBoolean()
    private var logChat :Boolean        = Settings.GetSetting("logChat").toBoolean()
    private var logPlugins :Boolean     = Settings.GetSetting("enablePlugins").toBoolean() //plugins are always logged if there's file/console output. It's up to the plugins themselves to use this function.
    private var logArchiving :Boolean   = Settings.GetSetting("archiveLogs").toBoolean()
    private var logRotate :Boolean      = Settings.GetSetting("logRotate").toBoolean()

    //TODO: rotate log file after certain amount of lines saved
    init
    {
        if (fileLog)  {
            log = File(logFolder+"/"+logFileName)
            if(this.loginit()) {
                log.writeText("Logging started at " + Date())
                if (consoleLog) println("Logger initiated at " + Date())
                loadedFile = true
            } else {
                println("Error while initialising the logging module.")
            }
        }
        else {
            if(consoleLog) println("Logger disabled, but the class was loaded. Please use the .enable() function and then re-initialise")
        }
        Settings.SetLogger(this)
    }

    //Various differ kinds lof loggers
    public fun LogMessage(logMessage :Int, logData :String = "")
    {
        var loggedString = "["+DateFormat.format(Date())+"] "
        when(logMessage) {
            1-> loggedString += "MISC: Test message, please ignore"
            2-> loggedString += "Cbox: Error while connecting to cbox.ws"
            3-> loggedString += "Logger: Could not re-enable the logger class - logging not initiated. Please reload with Logger.Reload"
            4-> loggedString += "SettingsManager: Deleting file"
            5-> loggedString += "SettingsManager: Folder successfully cleaned out"
            6-> loggedString += "SettingsManager: Compressing logs to Zip file"
            7-> loggedString += "SettingsManager: File Added"
            8-> loggedString += "SettingsManager: Folder successfully compressed"
            9-> loggedString += "SettingsManager: New settings loaded, please reload all services manually by running Global.Reload"
            10-> loggedString += "SettingsManager: Loaded and ready"
            11-> loggedString += "Unused error code"
            12-> loggedString += "Unused error code"
            13-> loggedString += "CLI: CLI is disabled, moving on"
            14-> loggedString += "CLI: Daemon started on port"
            15-> loggedString += "CLI: Client connected"
            16-> loggedString += "CLI: Client disconnected"
            17-> loggedString += "CLI: Command cancelled"
            18-> loggedString += "Logger: Disabled"
            19-> loggedString += "Logger: Enabled"
            20-> loggedString += "Logger: Archivation successful"
            21-> loggedString += "Logger: Log file location changed"
            22-> loggedString += "Logger: Disabled console logging"
            23-> loggedString += "Logger: Enabled console logging"
            24-> loggedString += "Logger: Disabled console logging"
            25-> loggedString += "Logger: Enabled console logging"
            26-> loggedString += "Logger: Restarting logger module"
            27-> loggedString += "Logger: Error while restarting, but still alive."
            28-> loggedString += "Logger: Restarted successfully"
            29-> loggedString += "CLI: CLI turned off through CLI mode."
            30-> loggedString += "Box: Loaded and ready"
            31-> loggedString += "Box: Initialising box module"
            32-> loggedString += "Box: Error while trying to get messages"
            33-> loggedString += "Box: Could not log in, going on in read-only mode. Use CLI or edit the config file to re-configure and try again."
            34-> loggedString += "Plugins: System started up"
            35-> loggedString += "Plugins: System disabled, moving on"
            36-> loggedString += "Box: Attempting to log in"
            37-> loggedString += "Box: Logging out"
            38-> loggedString += "ThreadController: CLI thread disconnected"
            39-> loggedString += "ThreadController: Box thread disconnected"
            40-> loggedString += "ThreadController: Plugin thread disconnected"
            41-> loggedString += "Box: Reloading module"
            42-> loggedString += "Box: Sending message"
            43-> loggedString += "Box: Logged in successfully"
            44-> loggedString += "Box: Something happened and now the user is no longer logged in. Please re-log."
            45-> loggedString += "Box: Daemon started"
            46-> loggedString += "ThreadController: Listening and waiting for commands"
            47-> loggedString += "ThreadController: CLI thread connected"
            48-> loggedString += "ThreadController: Plugin thread connected"
            49-> loggedString += "ThreadController: Box thread connected"
            50-> loggedString += "Box: Admin mode enabled"
            51-> loggedString += "Logger: Disabled chat logging"
            52-> loggedString += "Logger: Enabled chat logging"
            53-> loggedString += "Logger: Chat log file location changed"
            54-> loggedString += "CLI: Global restart initiated"
            55-> loggedString += "CLI: shutdown initiated"
            56-> loggedString += "MISC: Restarting module"
            57-> loggedString += "CLI: User entering CLI Chat mode"
            58-> loggedString += "CLI: User left CLI Chat mode"
            59-> loggedString += "CLI: Attempting a reload of the whole module"
            60-> loggedString += "CLI: Module stopped"
            61-> loggedString += "BOX: Module stopped"
            62-> loggedString += "Plugins: Module stopped"
            63-> loggedString += "Plugins: All plugins loaded"
            64-> loggedString += "Plugins: Attempting to reload the subsystem"
            65-> loggedString += "CLI: Failed to reload. CLI Disabled."
            66-> loggedString += "Plugins: Failed to reload. Plugins disabled."
            67-> loggedString += "Plugins: Disabling subsystem"
            68-> loggedString += "Plugins: Enabling subsystem"
            69-> loggedString += "Logger: plugins log file location changed"
            70-> loggedString += "Plugins: Failure in plugin loop"
            else -> {
                loggedString += "Error: Unspecified error ("+logMessage.toString()+")"
            }
        }
        if (logData != "") {
            loggedString += " ("+logData+")"
        }
        if(consoleLog) println(loggedString)
        if(fileLog) log.appendText("\n"+loggedString, StandardCharsets.UTF_8)

        /*if(logLineRotate) {
            lineCount--
        };*/
    }
    public fun LogBoringMessage(logMessage :Int, logData :String = "")
    {
        if(logLevel > 1) {
            var loggedString = "["+DateFormat.format(Date())+"] "
            when(logMessage) {
                1-> loggedString += "SettingsManager: Updated settings and saved them to file"
                2-> loggedString += "SettingsManager: Changed setting"
                3-> loggedString += "SettingsManager: Settings not changed - no changes found"
                else -> {
                    loggedString += "Error: Unspecified message ("+logMessage.toString()+")"
                }
            }
            if (logData != "") {
                loggedString += " ("+logData+")"
            }
            if(consoleLog) println(loggedString)
            if(fileLog) log.appendText("\n"+loggedString, StandardCharsets.UTF_8)
            //if(logLineRotate) lineCount++;
        }
    }
    public fun LogCommands(command :String, params :String = "")
    {
        var loggedString = "["+DateFormat.format(Date())+"] "
        loggedString += "CLI: Received Command '"+command+"'"

        if(params != "")loggedString += " with parameters ("+params+")"
        if(consoleLog) println(loggedString)
        if(fileLog) log.appendText("\n"+loggedString)
        //if(logLineRotate) lineCount++;
    }
    public fun LogChat(message: Message)
    {
        var loggedString = "{"+message.id+"} "
        loggedString += "["+message.humanReadableDate+":("+message.postDate+")]"
        loggedString += " <"+message.username+":"+message.userID+":"+message.avatarURL+">"
        loggedString += " "+message.message
        loggedString += " :("+message.userLevel+","+message.privLevel2+")(" + (if(message.userLevel == "1" && message.privLevel2 == "0") "guest" else if (message.userLevel == "2" && message.privLevel2 == "1") "user" else if (message.userLevel == "3" && message.privLevel2 == "2") "mod" else "unknown") + ")"

        if(consoleLog) println(loggedString)
        if(logChat) chatLog.appendText("\n"+loggedString)
        //if(logLineRotate) chatLineCount++;
    }
    public fun LogConnection(logMessage :Int, logData :String = "")
    {
        if(logLevel > 1)
        {
            var loggedString = "["+DateFormat.format(Date())+"] "
            when(logMessage) {
                0-> loggedString += "Box: PING"
                1-> loggedString += "CBOX.WS: PONG"
                2-> loggedString += "CBOX.WS: NOPE"
                3-> loggedString += "WebManager: Doing a POST request"
                4-> loggedString += "WebManager: Doing a GET request"
                else -> {
                    loggedString += "Unknown connection type"
                }
            }
            if (logData != "") {
                loggedString += " ("+logData+")"
            }
            if(consoleLog) println(loggedString)
            if(fileLog) log.appendText("\n"+loggedString, StandardCharsets.UTF_8)
            //if(logLineRotate) lineCount++;
        }
    }
    public fun LogPlugin(pluginName :String, pluginMessage :String)
    {
        if(this.logPlugins) {
            var loggedString = "["+DateFormat.format(Date())+"] {" + pluginName + "}: " + pluginMessage
            if(consoleLog) println(loggedString)
            if(fileLog) pluginLog.appendText("\n"+loggedString, StandardCharsets.UTF_8)
            //if(logLineRotate) pluginLineCount++;
        }

    }
    public fun LogBoringPluginData(pluginName: String, pluginMessage: String)
    {
        if(logLevel > 1) this.LogPlugin(pluginName, pluginMessage)
        //if(logLineRotate) pluginLineCount++;
    }

    //Setting changing API
    public fun ChangeConsoleLoggingState(state :Boolean)
    {
        if(state == false) this.LogMessage(22)
        this.consoleLog = state
        if(state == true) this.LogMessage(23)
    }
    public fun ChangeFileLoggingState(state :Boolean)
    {
        if(state == false) this.LogMessage(24)
        this.fileLog = state
        if(state == true) this.LogMessage(25)
    }
    public fun ChangeChatLoggingState(state :Boolean)
    {
        if(state == false) this.LogMessage(51)
        this.logChat = state
        if(state == true) this.LogMessage(52)
    }
    public fun ChangeLogFile(fileName :String = "log.txt", folder :String = "logs") :Boolean
    {
        try {
            loadedFile = true
            logFolder = folder
            logFileName = fileName
            log = File(logFolder+"/"+logFileName)

            this.LogMessage(21, folder+"/"+fileName)
            return true
        } catch (e :Exception) {
            loadedFile = false
            return false
        }
    }
    public fun ChangeChatLogFile(fileName: String = "chat.txt", folder :String = "logs") :Boolean
    {
        try {
            logFolder = folder
            chatLogFile = fileName
            chatLog = File(logFolder+"/"+logFileName)
            this.LogMessage(53, folder+"/"+fileName)
            return true
        } catch (e :Exception) {
            return false
        }
    }
    public fun ChangePluginLogFile(fileName: String = "plugins.txt", folder :String = "logs") :Boolean
    {
        try {
            logFolder = folder
            pluginLogFile = fileName
            pluginLog = File(logFolder+"/"+pluginLogFile)
            this.LogMessage(69, folder+"/"+fileName)
            return true
        } catch(e :Exception){
            return false
        }
    }
    public fun Disable(file :Boolean = true, console: Boolean = true, chat :Boolean = true) :Boolean
    {
        if(enableStatus) {
            this.LogMessage(18)
            enableStatus = false
            if(file)fileLog = false;
            if(console)consoleLog = false;
            if(chat)logChat = false;
            return true
        }
        return false
    }
    public fun Enable(File :Boolean = true, Console: Boolean = true, chat :Boolean = true) :Boolean
    {
        if(!enableStatus) {
            enableStatus = true
            if(File && loadedFile)fileLog = true;
            else if (!loadedFile && Console) this.LogMessage(3)
            if(Console)consoleLog = true;
            if(chat)logChat = true;
            this.LogMessage(19)
            return true
        }
        return false
    }
    public fun ArchiveOldLogs(log :Boolean = true)
    {
        val appZip = ZipUtil(logFolder, Settings.getCurrentTime().toString()+"_logs.zip", Settings.GetSetting("logFile"), Settings.GetSetting("chatLogFile"), Settings.GetSetting("pluginLogFile"), if (log) this else null)
        appZip.generateFileList()
        appZip.zipIt()
        //Wipe the folder
        appZip.clearOriginalFolder()
        this.LogMessage(20)
    }
    public fun Reload() :Boolean
    {
        this.LogMessage(26)
        this.logFolder = Settings.GetSetting("logFolder")
        this.logFileName = Settings.GetSetting("logFile")
        this.chatLogFile = Settings.GetSetting("chatLogFile")
        this.pluginLogFile = Settings.GetSetting("pluginLogFile")
        this.consoleLog = Settings.GetSetting("consoleLog").toBoolean()
        this.fileLog = Settings.GetSetting("fileLog").toBoolean()
        this.logChat = Settings.GetSetting("logChat").toBoolean()
        this.logPlugins = Settings.GetSetting("enablePlugins").toBoolean()
        this.logLineRotate = Settings.GetSetting("logLineRotate").toBoolean()

        this.log = File(logFolder+"/"+logFileName)
        this.chatLog = File(logFolder+"/"+chatLogFile)
        this.pluginLog = File(logFolder+"/"+pluginLogFile)


        if(this.logLineRotate) {
            this.lineCount = 0
            this.chatLineCount = 0
            this.pluginLineCount = 0
        }
        if(this.loginit()) {
            this.loadedFile = true
            this.LogMessage(28)
            return true
        } else {
            this.LogMessage(27)
            return false
        }
    }

    //Rotate the log files
    private fun checkLogExtensions(fileName: String)  :String {
        var tmpLogLocation = ""
        for(i in 1..maxLogs) {
            val tmpLog = File(logFolder+"/"+fileName+"."+i.toString())
            if(!tmpLog.exists()) {
                tmpLogLocation = tmpLog.toString()
                break;
            }
        }
        return tmpLogLocation
    }

    private fun logRotate() :Boolean {
        var shouldArchive = false
        if(log.exists()) {
            //copy old logs
            var tmpLogLocation = checkLogExtensions("log");

            if (tmpLogLocation == "") {
                //Archive old logs
                log.copyTo(File(logFolder+"/log."+maxLogs.toString()))
                shouldArchive = true
            } else {
                //Copy old log to a new place and delete it afterwards
                log.copyTo(File(tmpLogLocation))
            }
            log.delete()
        } else {
            File(logFolder).mkdirs()
        }

        if(chatLog.exists()) {
            var tmpLogLocation = checkLogExtensions("chat");

            if (tmpLogLocation == "") {
                //Archive old logs
                chatLog.copyTo(File(logFolder+"/chat."+maxLogs.toString()))
                shouldArchive = true
            } else {
                //Copy old log to a new place and delete it afterwards
                chatLog.copyTo(File(tmpLogLocation))
            }
            chatLog.delete()
        }
        return shouldArchive
    }
    private fun chatLogRotate() :Boolean {
        var shouldArchive = false
        if(chatLog.exists()) {
            var tmpLogLocation = checkLogExtensions("chat");

            if (tmpLogLocation == "") {
                //Archive old logs
                chatLog.copyTo(File(logFolder+"/chat."+maxLogs.toString()))
                shouldArchive = true
            } else {
                //Copy old log to a new place and delete it afterwards
                chatLog.copyTo(File(tmpLogLocation))
            }
            chatLog.delete()
        }
        return shouldArchive
    }
    private fun pluginLogRotate() :Boolean {
        var shouldArchive = false
        if(pluginLog.exists()) {
            var tmpLogLocation = checkLogExtensions("plugins");

            if (tmpLogLocation == "") {
                //Archive old logs
                chatLog.copyTo(File(logFolder+"/plugins."+maxLogs.toString()))
                shouldArchive = true
            } else {
                //Copy old log to a new place and delete it afterwards
                chatLog.copyTo(File(tmpLogLocation))
            }
            pluginLog.delete()
        }
        return shouldArchive
    }

    //Initializer function, separated so that it can be properly called from Reload()
    private fun loginit() :Boolean
    {
        try {
            if(logRotate) {

                var shouldArchive = logRotate()
                var shouldChatArchive = chatLogRotate()
                var shouldPluginArchive = pluginLogRotate()

                //TODO: Archive only the specific log files
                if((shouldArchive || shouldPluginArchive || shouldChatArchive) && logArchiving) this.ArchiveOldLogs(false)
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }
}

