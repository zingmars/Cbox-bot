Cbox.ws Bot in Kotlin 
====

# This is a very overengineered "simple" bot that...

1) Connects to a specified cbox.ws chat box and reads/logs messages

2) Allows moderator-only commands and that kind of stuff

3) Provides a CLI (trough, say a telnet client) access that allows quick reconfiguring and reloading if needed (admittedly)

4) Has a plugin API that lets you inject any compiled kotlin class (technically at any time too) and run it, as long as it inherits the base plugin class and functions

5) Does it in a multi-threaded fashion

And probably much more...

-----
# Why?

When looking around the webs I found this language, Kotlin. Now, I have a project coming up that involves building an Android app, and not being on good terms with Java myself, I decided to build something simple to understand it's basics (this is why you'll find stuff like classloader, socket server etc. in this project). I've also wanted to build something that connects to a chat box that a community I frequent uses; I've tried making a desktop client before before, but I didn't finish it out of laziness. It should however in theory work with any cbox.ws based chatbox, although I haven't tested it, so I can't vouch for it.

-----
# Setup guide (using IntelliJ IDEA):

1) Open File->Settings->Plugins, look for Kotlin, download and install it

2) Open project's folder using the Open function

3) On the Project view open the src directory and open the app.kt file

4) There should now be a bar asking your to set up Project SDK. If you haven't already, point it to your jdk's directory.

5) I have included kotlin's runtimes with the project, so you should now be able to make and run this project.

------
# TL;DR - Files

/src:
	
	/BotPlugins - contains plugins for the bot

	/Containers - some cross communication model classes

app.kt - main entry point for this application. Loads and manages every other class

The rest should be obvious - ZipUtil handles file zipping (for log file archiving), CLI handles command line input, Logger Logs, HTTP handles HTTP connections etc.  

------
# Plugins

Just throw them into /src/BotPlugins. All plugins must follow this base example:

```
package BotPlugins
import Containers.PluginBufferItem

public class PluginName : BasePlugin()
{
	override fun pubInit() :Boolean
	{
		return true
	}
	override fun connector(buffer : PluginBufferItem) :Boolean
	{
		return true
	}
	override fun stop()
	{
	}
}
```

There are couple of notes though:

1. Plugins are using a really weird ClassLoader implementation, so it it's a bit buggy. The reason for this is that I wanted to see if dynamic class loading is possible with Java, and although it is, I don't really think it's worth the hassle I went through.

2. Plugin name MUST be the same as the filename. If it's not, it won't be loaded. The files in BotPlugins folder can be empty however, but they need to have a compiled version.

3. Yes, it must be a class.

4. pubInit() is the class that's executed once it has been given proper context (namely - logger, a settings file, threadmanager etc). More info on that is available as a comment on BasePlugin.kt

5. connector() is called for every message

6. stop() is called when the plugin is turned off. This is for when you have threads or something like that running.

7. Even though you can use pretty much the whole logger class, it would be saner to use plugin related log commands. The boring one's for when the loglevel is over 2 and it's there to avoid spamming the log with pointless stuff.

8. Yes, you can reload your class while it's running (using CLI, or if you make it to - through an user command), but you'll need to replace the compiled version in the jar file. It works when run from an IDE, not so much when you have a portable jar file.

------
# TODO?

A full refactor to make the code consistent would be nice. Plenty of TODO's scattered through the code as well. Even so, this is project is, for all intents and purposes, finished. Feel free to fork and do whatever.

------
# License

Please view LICENSE (BSD 2-clause). TL;DR - Do what you want, just include the original license and don't blame me when something breaks