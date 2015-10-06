/**
 * Class that holds all the cbox.ws messages
 * Created by zingmars on 03.10.2015.
 */
package Containers

public class Message(public val id :String, public val postDate :String, public val humanReadableDate :String, public val username :String, public val userLevel :String, public val avatarURL :String, public val message :String, public val unknownField :String, public val privLevel2 :String, public val userID :String) {
    // A couple of notes on this class:
    // The fields come from me reading the output of the requests to cbox's server
    // The format isn't actually documented anywhere and I'm too lazy to read the scripts file
    // Anything after message is just a guess from my observations. No, I have no idea why there are two userLevel strings.
    // As for the unknown field, it appears to be a something you can toggle in admin panel, something OverTheGun's cbox has disabled.
    // Because of this, it will pretty much always be empty. I will however leave the field, should someone use this for another cbox need it.
    // If User is a guest: userLevel = 1, userLevel2 = 0, user: userLevel = 2, userLevel2 = 1, mod: userLevel = 3, UserLevel2 = 2
}