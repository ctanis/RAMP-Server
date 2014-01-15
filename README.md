# RAMP Server

A server application that applies equalizer modifications on up to 24 tracks simultaneously and then streams the results back to a client application in realtime.

The web-based client application can be found [here](https://github.com/mattprice/RAMP-Webapp).

## Requirements

**Linux**:

* Git
* GCC
* Make

**Mac OSX**:

* [Xcode](http://itunes.apple.com/us/app/xcode/id497799835) or the [Command Line Tools for Xcode](https://developer.apple.com/downloads)

**Windows:**

* Git; [GitHub for Windows](http://windows.github.com) is the recommended Git client and will handle installing Git for you.
* [Cygwin](http://www.cygwin.com) or the Visual Studio Command Prompt
	* If using Cygwin, you will need to install the Git, GCC-Core, and Make packages.

## Instructions

**Note:** Windows users should replace each instance of `./gradlew` below with `./gradlew.bat`.

### Commands

* `./gradlew run` — Builds and runs the RAMP application.
* `./gradlew build` — Builds the RAMP application.
* `./gradlew clean` — Deletes all pre-compiled files. This can be useful for solving some problems. The next time you build RAMP it will build all files from scratch.

## License

This project is licensed under the terms of the [MIT License](/LICENSE).