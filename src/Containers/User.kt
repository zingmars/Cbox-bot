/**
 * User credentials holder
 * Created by zingmars on 03.10.2015.
 */
package Containers

public class User(public var Username :String = "", public var Password :String = "", public var AvatarURL :String = "")
{
    public var Key = ""
    public fun SetCredentials(newUsername: String, newPassword: String, newAvatarURL: String = "")
    {
        this.SetUsername(newUsername)
        this.SetPassword(newPassword)
        this.SetAvatarURL(newAvatarURL)
    }
    //API Functions
    public fun SetUsername(newUsername :String)
    {
        Username = newUsername
    }
    public fun SetPassword(newPassword :String)
    {
        Password = newPassword
    }
    public fun SetAvatarURL(newAvatarURL :String)
    {
        AvatarURL = newAvatarURL
    }
}
