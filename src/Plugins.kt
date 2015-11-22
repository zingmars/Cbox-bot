/**
 * Class that manages anything not directly related to cbox.ws
 * Created by zingmars on 03.10.2015.
 */
import BotPlugins.BasePlugin
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

public class Plugins(private val Settings :Settings, private val Logger :Logger, private val ThreadController :ThreadController)
{
    private var active                          = false
    private var enabled                         = Settings.GetSetting("enablePlugins").toBoolean()
    private var pluginDirectory                 = Settings.GetSetting("pluginDirectory")
    private var Daemon                          = Thread()
    private var pluginSettings                  = Settings("PluginSettings.cfg", true)
    private val pluginList :MutableList<String> = ArrayList()
    private val plugins                         = ArrayList<BasePlugin>()
    private var refreshRate                     = Settings.GetSetting("refreshRate").toLong()
    private var abuserList                      = ""
    public  var username                        = ""

    init
    {
        if(enabled) {
            if(Settings.checkSetting("username")) username = Settings.GetSetting("username")
            if(!Settings.checkSetting("isAdmin")) Settings.SetSetting("isAdmin", "false")
            if(pluginSettings.checkSetting("AbuserList", true)) abuserList = pluginSettings.GetSetting("AbuserList")
            pluginSettings.checkSetting("AbuserList", true)
            //Add plugin admin
            //TODO: Manager plugin that allows to do the same things you can do from CLI
            if(pluginSettings.GetSetting("HonourableUsers") == "") {
                println("Plugin admin not configured. Please enter new admin(s - comma seperated no spaces) (or leave blank to have no admin, in which case you'll need to manually enter comma seperated nicknames in plugin conf file)")
                var input = readLine()
                if(input == "") input = " "
                pluginSettings.SetSetting("HonourableUsers", input.toString())
            }

            pluginSettings.SetLogger(Logger)
            this.start()
            Logger.LogMessage(34)
        } else {
            Logger.LogMessage(35)
        }
    }

    //Daemon functions
    public fun isActive() :Boolean
    {
        return active
    }
    public fun start(restarted :Boolean = false)
    {
        if(enabled) {
            this.active = true
            Daemon = Thread(Runnable () { this.Demon() })
            Daemon.isDaemon = true
            reloadAllPlugins()
            Daemon.start()
            ThreadController.ConnectPlugins(Daemon)
            if(restarted) ThreadController.AddToCLIBuffer("Plugin container restarted successfully")
            Logger.LogMessage(63)
        }
    }
    public fun stop()
    {
        this.active = false
        for(plugin in plugins) {
            unloadPlugin(plugin.pluginName.toString())
        }
        ThreadController.DisconnectPlugins()
        Daemon.stop()
        Logger.LogMessage(62)
    }
    public fun Reload() :Boolean
    {
        try {
            Logger.LogMessage(64)
            this.stop()

            reloadAllPlugins()
            refreshRate = Settings.GetSetting("refreshRate").toLong()

            if(this.enabled) {
                this.start(true)
                return true
            } else return false
        } catch (e: Exception) {
            this.active = false
            this.enabled = false
            Logger.LogMessage(66)
            return false
        }
    }
    public fun Disable()
    {
        Logger.LogMessage(67)
        enabled = false
        this.stop()
    }
    public fun Enable()
    {
        Logger.LogMessage(68)
        enabled = true
        this.start()
    }
    private fun Demon()
    {
        while (active) {
            try {
                Thread.sleep(this.refreshRate)
                //Run data to plugins
                for (message in ThreadController.GetPluginBuffer()) {
                    //run through loaded plugin's checklist
                    //TODO: There's a bug which causes the ignore list to not work
                    if(message.userName != username && !pluginSettings.GetSetting("AbuserList").contains(message.userName)) {
                        for(plugin in plugins) {
                            plugin.connector(message)
                        }
                    }
                }
                pluginSettings.SaveSettings()
            } catch (e: Exception) {
                Logger.LogMessage(70, e.toString())
            }
        }
    }

    //Plugin management
    public fun unloadPlugin(name :String) :String
    {
        // We need to go through all of the plugins since we don't keep an index :(
        var i = 0
        for (plugin in plugins) {
            if(name.toLowerCase() == plugin.pluginName?.toLowerCase()) {
                plugin.stop()
                plugins.removeAt(i)
                Logger.LogPlugin(plugin.pluginName.toString(), "Unloaded.")
                return "Success"
            }
            i++
        }
        return "Plugin not found or already disabled."
    }
    public fun executeFunction(pluginName :String, command :String)
    {
        for (plugin in plugins) {
            if(pluginName.toLowerCase() == plugin.pluginName?.toLowerCase()) {
                plugin.executeCommand(command)
            }
        }
    }
    public fun LoadPlugin(name :String) :Boolean
    {
        var source = pluginDirectory+name
        var pluginFile = File(source)

        try {
            var fileURL = ArrayList<URL>()
            fileURL.add(pluginFile.toURI().toURL())

            var cl = URLClassLoader(fileURL.toTypedArray())
            var pluginName = name.substring(0, name.lastIndexOf(".")).replace("\\", "")
            var cls = Class.forName("BotPlugins."+pluginName, true, cl)

            var instance = cls.newInstance() as BasePlugin
            if (instance.initiate(pluginSettings, Logger, this, ThreadController, pluginName)) {
                plugins.add(instance)
                return true
            }
            return true
        } catch (e: Exception) {
            Logger.LogMessage(999, e.toString())
            return false
        }
    }
    public fun reloadPlugin(name :String) :Boolean
    {
        try {
            this.unloadPlugin(name)
            this.LoadPlugin(name+".kt")
            return true
        } catch (e: Exception) {
            return false
        }
    }
    private fun reloadAllPlugins()
    {
        pluginList.clear()
        plugins.clear()
        generateFileList()

        for(plugin in pluginList) {
            this.LoadPlugin(plugin)
        }
    }

    //CLI API
    public fun changeRefreshRate(newRate :Long)
    {
        this.refreshRate = newRate
    }
    public fun SavePluginSettings()
    {
        pluginSettings.SaveSettings()
    }
    public fun SetSetting(setting :String, value :String)
    {
        pluginSettings.SetSetting(setting, value)
    }
    public fun GetAllSettings() :String
    {
        return pluginSettings.GetAllSettings()
    }

    //Misc
    private fun generateFileList(node: File = File(pluginDirectory))
    {
        if (node.isFile) {
            pluginList.add(node.toString().substring(pluginDirectory.length, node.toString().length))
        }

        if (node.isDirectory) {
            val subNote = node.list()
            for (filename in subNote) {
                generateFileList(File(node, filename))
            }
        }
    }
    public fun isAdmin() :Boolean
    {
        return Settings.GetSetting("isAdmin").toBoolean()
    }
    public fun isPluginAdmin(name :String) :Boolean
    {
        return pluginSettings.GetSetting("HonourableUsers").toString().toLowerCase().toString().contains(name.toLowerCase())
    }
}