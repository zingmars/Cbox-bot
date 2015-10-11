/**
 * Base plugin class to be inherited from
 * Created by zingmars on 04.10.2015.
 */
package BotPlugins
import Containers.PluginBufferItem
import Settings
import Logger
import Plugins
import ThreadController

open public class BasePlugin() {
    public var settings    :Settings? = null
    public var logger      :Logger?   = null
    public var handler     :Plugins?  = null
    public var pluginName  :String?   = null
    public var controller  :ThreadController? = null

    init {
        // This is the base class for all CBot plugins
        // To make a plugin just extend this class and put it in src/BotPlugins/ (or whatever is defined in your settings file) directory
        // Note - your filename must match your class name and it must be inside the BotPlugins package
        // To initiate just override pubInit() (you can override this initializer too, but you won't have access to any variables at that point), and to do your logic just override connector.
        // Note that your connector override will need to have the PluginBufferItem input for it to receive any messages.
        // To send data back just add an element to any of the buffers available in ThreadController, (i.e. BoxBuffer will output anything send to it)
        // Note that pubInit and connector both return a boolean value that indicates whether or not the plugin was successful
    }
    //non-overridable classes
    final public fun initiate(settings :Settings, logger: Logger, pluginsHandler :Plugins, controller :ThreadController,pluginName :String) :Boolean
    {
        this.settings = settings
        this.logger = logger
        this.handler = pluginsHandler
        this.pluginName = pluginName
        this.controller = controller
        if(this.pubInit()) this.logger?.LogPlugin(pluginName, "Started!")
        else {
            this.stop()
            this.logger?.LogPlugin(pluginName, "failed to load!")
            return false
        }
        return true
    }
    //overridable classes
    open public fun pubInit() :Boolean { return true } //Initializer
    open public fun connector(buffer :PluginBufferItem) :Boolean { return true } //Receives data from Plugin controller
    open public fun stop() {} //This is run when the plugin is unloaded
    open public fun executeCommand(command :String) :String { return "Plugin not configured" } //Execute a command
}