CLI Commands (casing doesn't matter):
:quit - Disconnects you from the CLI
:shutdowncli - Turns off the CLI interface. If you do this, be warned that there's no way to turn it back on without restarting it at the moment.
:ping - Check if the connection is still alive
:shutdown - Turns off the bot
:reload - Restarts the CLI module
:help - Tells you to see this file.

Global commands:
global.reload - Reloads EVERYTHING. Honestly, it's been mostly untested, so you're probably better of just restarting the bot.

Settings commands:
settings.savesettings - Save any settings in memory to disk
settings.loadsetitngs - Reload settings from the settings file
settings.getsetting <name> - Get the value of a setting
settings.setsetting <name> <value> - Set the value of a setting. Note that the value must not include spaces.
settings.changesettingsfile <filename> - Change the active settings file and load settings from it.
settings.getSettings - Outputs all the active settings

Logger commands:
logger.disable - Stop logging
logger.enable <optional: log to file> <optional: log to console> <optional: log chat> - Start logging module (note - If you want to specify an optional parameter, you must specify the ones before it as well. E.g. - if you want to turn off chat logging, your command will look like logger.enable true true false).
logger.disable <optional: log to file> <optional: log to console> <optional: log chat> - Start logging module (note - If you want to specify an optional parameter, you must specify the ones before it as well. E.g. - if you want to turn off chat logging, your command will look like logger.false true true false).
logger.archive - Zips all the logs in the /logs folder and puts the file in the same directory the binary is in.
logger.toconsole <true:false> - Change whether the bot will log to the console it's running in
logger.tofile <true:false> - Change whether the bot will log to a file
logger.chattofile <true:false> - Change whether the bot will log the chat to a file
logger.changelogfile <optional: log file name> <optional: log folder name> - Change the active log file
logger.changechatlogfile <optional: log file name> <optional: log folder name> - Change the active chat log file
logger.changepluginlogfile <optional: log file name> <optional: log folder name> - Change the active chat log file
logger.reload - Reload the logger plugin. Might take a few seconds.

Cbox handler commands:
box.reload - reloads the cbox handler module
box.tocli - Starts the CLI chat. See CLI chat commands.
box.relog <username> <password> <optional: avatar URL> - Relog to cbox with given credentials.
box.refreshrate <miliseconds> - Change how often the bot polls cbox.ws for new posts. My reccommendation is around 1-2 seconds (1000-2000) to avoid flooding their servers with requests.
box.getip <optional: messageid> - Get the IP of the message's owner. Will only work if your account is admin & logged in. If not specified, it will grab the latest poster's IP.
box.send <message> - Sends a message to the box as the bot.

CLI chat commands:
:quit - quits the CLI chat mode
:write - Pauses incoming messages to let you write a message.

Plugin commands:
plugins.reload - Reload the plugins
plugins.disable - Disable the plugins module
plugins.enable - Enables the plugins module
plugins.refreshrate <miliseconds> - Change how often the bot will go over any messages passed to the plugins. By default it's the same as Cbox refreshrate, and there's no benefit to changing it to be any different.
plugins.unload <plugin name> - Unloads the specified plugin.
plugins.load <plugin name> - Loads the specified plugin (see README).
plugins.reload <plugin name> - Reloads the specified plugin.
plugins.savesettings - Save plugin-specific settings to file
plugins.getsettings - Get all plugin-specific settings
plugins.setsetting - Set the value of a setting. Note that the value must not include spaces.
plugins.command <plugin name> <command> - Send a command to a plugin. 