import Containers.PluginBufferItem
import java.util.*

/**
 * Simple class to theoretically control threads from one place
 * in reality it's basically where all meet to exchange information
 * Created by zingmars on 03.10.2015.
 */
public class ThreadController(private val Logger :Logger)
{
    private var CLI :Thread? = null
    private var BOX :Thread? = null
    private var PLUGINS :Thread? = null

    private var CLIBuffer= ArrayList<String>()
    private var BoxBuffer= ArrayList<String>()
    private var PluginBuffer = ArrayList<PluginBufferItem>()
    private var MainThreadBuffer = ArrayList<String>()

    init
    {
        Logger.LogMessage(46)
    }

    // Connect threads
    public fun ConnectCLI(CLI :Thread)
    {
        Logger.LogMessage(47)
        this.CLI = CLI
    }
    public fun ConnectBox(BOX :Thread)
    {
        Logger.LogMessage(49)
        this.BOX = BOX
    }
    public fun ConnectPlugins(PLUGINS :Thread)
    {
        Logger.LogMessage(48)
        this.PLUGINS = PLUGINS
    }

    // Disconnect threads
    public fun DisconnectCLI()
    {
        Logger.LogMessage(38)
        CLI = null
    }
    public fun DisconnectBox()
    {
        Logger.LogMessage(39)
        BOX = null
    }
    public fun DisconnectPlugins()
    {
        Logger.LogMessage(40)
        PLUGINS = null
    }

    // Inter-thread communication
    public fun AddToCLIBuffer(data :String)
    {
        if(CLI != null) CLIBuffer.add(data)
    }
    public fun AddToMainBuffer(data :String)
    {
        MainThreadBuffer.add(data)
    }
    public fun AddToBoxBuffer(data :String)
    {
        if(BOX != null) BoxBuffer.add(data)
    }
    public fun AddToPluginBuffer(data :PluginBufferItem)
    {
        if(PLUGINS != null) PluginBuffer.add(data)
    }

    public fun GetMainBuffer(clear :Boolean = true) :ArrayList<String>
    {
        var buffer = ArrayList<String>(MainThreadBuffer)
        if(clear) MainThreadBuffer.clear()
        return buffer
    }
    public fun GetCLIBuffer(clear :Boolean = true) :ArrayList<String>
    {
        var buffer = ArrayList<String>(CLIBuffer)
        if(clear) CLIBuffer.clear()
        return buffer
    }
    public fun GetBoxBuffer(clear :Boolean = true) :ArrayList<String>
    {
        var buffer = ArrayList<String>(BoxBuffer)
        if(clear) BoxBuffer.clear()
        return buffer
    }
    public fun GetPluginBuffer(clear :Boolean = true) :ArrayList<PluginBufferItem>
    {
        var buffer = ArrayList<PluginBufferItem>(PluginBuffer)
        if(clear) PluginBuffer.clear()
        return buffer
    }
}