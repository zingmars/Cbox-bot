/**
 * Helper class to help with HTTP requests
 * Created by zingmars on 03.10.2015.
 */
import Containers.POST
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

public class HTTP(private var Logger :Logger? = null)
{
    public fun GET(URL :String) :String
    {
        Logger?.LogConnection(3, URL)
        var site = URL(URL)
        return site.readText(StandardCharsets.UTF_8)
    }
    public fun POST(URL :String, Data :String) :POST
    {
        //I swear to god, this might just be the only place on the internets that has HTTP POST code adapted for use with Kotlin.
        Logger?.LogConnection(4, URL)
        var userAgent = "cbox bot (https://github.com/zingmars/Cbox-bot) POST"

        var site = URL(URL)
        var postData = Data.toByteArray(StandardCharsets.UTF_8)
        var postDataLength = postData.size

        var connection= site.openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.instanceFollowRedirects = false
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.setRequestProperty("charset", "utf-8")
        connection.setRequestProperty("User-Agent", userAgent)
        connection.setRequestProperty("Content-Length", postDataLength.toString())
        connection.useCaches = false

        try {
            //Send the request
            var outputStream = DataOutputStream(connection.outputStream)
            outputStream.write(postData)

            //Get the response
            var input = connection.inputStream
            var inputBuffer = BufferedReader(InputStreamReader(input))
            var line :String
            var response = StringBuffer()
            while(true) {
                try {
                    line = inputBuffer.readLine()
                } catch (e: Exception) {
                    break;
                }
                response.append(line+"\n")
            }
            inputBuffer.close()
            connection.disconnect()

            return Containers.POST(connection.headerFields, response.toString())
        }
        catch (e: Exception) {
            return Containers.POST(connection.headerFields, "null")
        }
    }
    public fun PassLogger(logger :Logger?)
    {
        this.Logger = logger
    }
}