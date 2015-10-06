/**
 * cbox.ws chat manager class
 * Created by zingmars on 03.10.2015.
 */
import Containers.Message
import Containers.PluginBufferItem
import Containers.User
import java.util.*

public class Box(private val Settings :Settings, private val Logger :Logger, private val ThreadController :ThreadController)
{
    private var active              = false
    private var lastMessageID       = "0"
    private var server              = Settings.GetSetting("server")
    private var id                  = Settings.GetSetting("boxid")
    private var tag                 = Settings.GetSetting("boxtag")
    private var loggedIn            = false
    private var isAdmin             = false
    private var user :User          = User()
    private var HTTPUtility         = HTTP(Logger)
    private var Daemon :Thread      = Thread()
    private var refreshRate         = Settings.GetSetting("refreshRate").toLong()
    private var adminFailRate       = 0
    private var pingRate            = Math.floor((30000.toLong()/refreshRate).toDouble()).toInt()
    public  var toCLI                = false

    init
    {
        // Get the latest messages
        Logger.LogMessage(31)
        var messages = GetMessages(lastMessageID)

        if(messages == null) {
            Logger.LogMessage(32)
        } else {
            active = true

            // Log in
            if(Settings.GetSetting("username") != "") {
                user.SetCredentials(Settings.GetSetting("username"), SetPassword(), Settings.GetSetting("avatar"))
                this.LogIn()
            }

            // Start a thread that monitors the box
            Logger.LogMessage(30)
            this.start()
        }
    }

    //Thread functions
    public fun isActive() :Boolean
    {
        return active
    }
    public fun start(restarted :Boolean = false)
    {
        active = true
        Daemon = Thread(Runnable () { this.Demon() })
        Daemon.isDaemon = true
        Daemon.start()
        Logger.LogMessage(45)
        ThreadController.ConnectBox(Daemon)
        if(restarted) ThreadController.AddToCLIBuffer("Box restarted successfully")
    }
    public fun stop()
    {
        this.active = false;
        ThreadController.DisconnectBox()
        Daemon.stop()
        Logger.LogMessage(61)
    }
    private fun Demon()
    {
        var pingcounter = 0
        while(active) {
            try {
                Thread.sleep(this.refreshRate)
                var boxMessages = this.GetMessages()
                //Send to plugin controller
                if(boxMessages != null) {
                    for(message in boxMessages) {
                        // If User is a guest: userLevel = 1, userLevel2 = 0, user: userLevel = 2, userLevel2 = 1, mod: userLevel = 3, UserLevel2 = 2
                        var privlvl = if(message.userLevel == "3" && message.privLevel2 == "2") "mod" else if (message.userLevel == "2" && message.privLevel2 == "1") "user" else "guest"
                        var extra = if(isAdmin) {getIP(message.id) as String} else ""
                        ThreadController.AddToPluginBuffer(PluginBufferItem(message.id, message.userID, message.postDate,message.username, message.message, privlvl, extra))
                        if(toCLI) ThreadController.AddToCLIBuffer("{"+extra+":"+message.id+"}["+message.humanReadableDate+"]<"+message.userID+":"+message.username+">("+privlvl+") "+message.message)
                    }
                }

                //Load plugin response
                var pluginMessages = ThreadController.GetBoxBuffer()
                if(pluginMessages.count() > 0) {
                    for(message in pluginMessages) {
                        SendMessage(message)
                    }
                } else {
                    pingcounter++
                    if(pingcounter == pingRate) {
                        KeepAlive()
                        pingcounter = 0
                    }
                }
            } catch (e: Exception) {
                ThreadController.AddToCLIBuffer("Box module failed: " + e.toString())
                Logger.LogMessage(999, "Unknown box loop error")
            }
        }
    }
    public fun Reload() :Boolean
    {
        this.stop()
        if(Settings.GetSetting("username") != "" && user.Username != "") {
            Logger.LogMessage(41)
            this.LogOut()
            this.ChangeCredentials(user.Username, user.Password, user.AvatarURL)
            this.LogIn()
        }
        refreshRate = Settings.GetSetting("refreshRate").toLong()
        pingRate = Math.floor((30000.toLong()/refreshRate).toDouble()).toInt()
        this.start(true)
        return true;
    }
    public fun changeRefreshRate(newRate :Long)
    {
        this.refreshRate = newRate
        this.pingRate = Math.floor((30000.toLong()/refreshRate).toDouble()).toInt()
    }

    //Message sending and receiving API
    public fun GetMessages(lastID :String = lastMessageID) :ArrayList<Message>?
    {
        try {
            var messages = HTTPUtility.GET("http://www"+server+".cbox.ws/box/?sec=ar&boxid="+id+"&boxtag="+tag+"&_v=857&p="+lastID+"&c="+(System.currentTimeMillis() / 1000L).toString()).split("\n").reversed()
            var sortedMessages = ArrayList<Message>()
            if(messages.size() > 0) {
                for (i in 0..messages.size()-2)  {
                    var splitMessage = messages[i].split("\t")
                    lastMessageID = splitMessage[0]

                    var tmpMessage = parseMessage(splitMessage)
                    sortedMessages.add(tmpMessage as Message)

                    // Log the message
                    Logger.LogChat(tmpMessage)
                }
            }

            return sortedMessages
        } catch ( e: Exception) {
            Logger.LogMessage(32, e.toString())
            return null
        }
    }
    public fun SendMessage(message :String) :Boolean
    {
        //TODO: for some reason during POST the '+' character will go missing.
        Logger.LogMessage(42, message)
        if(loggedIn) {
            var POST = HTTPUtility.POST("http://www"+server+".cbox.ws/box/?sec=submit&boxid="+id+"&boxtag="+tag+"&_v=857","aj=857&lp="+lastMessageID+"&pst="+message+"&nme="+user.Username+"&eml="+user.AvatarURL+"&key="+user.Key)
            var response = POST.Content.split("\n")
            try {
                if (parseMessage(response[2].split("\t")) == null) {
                    Logger.LogMessage(44)
                    return false
                } else {
                    return true
                }
            } catch (e: Exception) {
                Logger.LogMessage(44)
                return false
            }
        } else {
            return false
        }
    }
    private fun parseMessage(message :List<String>) :Message?
    {
        try {
            var post :String

            // Turn links back to normal text
            post = message[6].replace("<a class=\"autoLink\" href=\"", "")
            post = post.replace("\" target=\"_blank\">[link]</a>", "")
            //TODO: Turn HTML entities into their respective characters.
            // I remember that there was a prettier way to turn an array into arguments, yet I can't remember it right now.
            return Message(message[0], message[1], message[2], message[3], message[4], message[5], post, message[7], message[8], message[9])
        } catch (e: Exception) {
            return null
        }
    }

    //Credential related functions
    public fun ChangeCredentials(Username :String, Password :String, Avatar :String = "")
    {
        user.SetCredentials(Username, Password, Avatar)
    }
    private fun checkAdmin()
    {
        var test = getIP()
        if(test != null) {
            isAdmin = true
            Settings.SetSetting("isAdmin", "true")
            Logger.LogMessage(50)
        }
    }
    private fun LogIn() :Boolean
    {
        if(loggedIn) LogOut()
        Logger.LogMessage(36, user.Username)
        try {
            var POST = HTTPUtility.POST("http://www"+server+".cbox.ws/box/?boxid="+id+"&boxtag="+tag+"&sec=profile&n=" + user.Username + "&k=","pword="+user.Password+"&sublog=+Log+in+")
            if(POST.Content != "null") {
                var newCookies = POST.Headers.get("Set-Cookie")
                var newCookiesLength = if(newCookies != null) newCookies.size() else 0
                if(newCookiesLength != 0) {
                    for (i in 0..newCookiesLength-1) {
                        val cookie = newCookies?.get(i).toString();
                        if (cookie.startsWith("key_"+id)) {
                            user.Key = cookie.substring(cookie.indexOf("=")+1, cookie.indexOf(";")-1)
                            if(user.Key != "delete") {
                                loggedIn = true
                                checkAdmin()
                                Logger.LogMessage(43)
                            }
                            return true
                        }
                    }
                    return false
                } else {
                    loggedIn = false
                    return false
                }
            }
            else {
                return false
            }
        } catch (e: Exception) {
            Logger.LogMessage(33)
            loggedIn = false
            return false
        }
    }
    private fun LogOut() :Boolean
    {
        Logger.LogMessage(37)
        loggedIn = false
        isAdmin = false
        Settings.SetSetting("isAdmin", "false")
        user.Key = ""
        try {
            var GET = HTTPUtility.GET("http://www"+server+".cbox.ws/box/?boxid="+id+"&boxtag="+tag+"&sec=profile&n="+user.Username+"&l=1&k="+user.Key) //fire and forget
        } catch (e: Exception) {
        }
        return true
    }
    private fun SetPassword() :String
    {
        var password = Settings.GetSetting("password")
        if(password == "") {
            println("Password not defined, please enter user's password")
            password = readLine().toString()
        }
        return password
    }

    //Miscellaneous functions
    public fun getIP(ID :String = lastMessageID) :String?
    {
        var GET = HTTPUtility.GET("http://www"+server+".cbox.ws/box/?sec=getip&boxid="+id+"&boxtag="+tag+"&_v=857&n="+user.Username+"&k="+user.Key+"&i="+ID)
        var state = GET[0]
        if(state == '0') {
            adminFailRate++
            if(adminFailRate == 20) {
                isAdmin = false
                Settings.SetSetting("isAdmin", "false")
            }
            return null
        } else return GET.substring(1)
    }
    private fun KeepAlive() :Boolean
    {
        Logger.LogConnection(0)
        try {
            var POST = HTTPUtility.POST("http://www"+server+".cbox.ws/box/?sec=users&boxid="+id+"&boxtag="+tag+"&_v=857 ", "state=online&k="+user.Key+"&rcid=9999_99999999") //WTF is RCID?
            if(POST.Content == "1\n") {
                Logger.LogConnection(1)
                return true
            }
            else {
                Logger.LogConnection(2)
                return false
            }
        } catch (e: Exception) {
            return false
        }
    }

}
