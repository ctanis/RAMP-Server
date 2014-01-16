# RAMP Server

A server application that applies equalizer modifications on up to 24 tracks simultaneously and then streams the results back to a client application in realtime.

The web-based client application can be found [here](https://github.com/mattprice/RAMP-Webapp).

## Requirements

### Linux

* Git, GCC, and Make
* [Oracle JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or [OpenJDK 7](http://openjdk.java.net/install/index.html)

Note that the libpd Makefile searches for the Java libraries in `/usr/lib/jvm/default-java`, but that's not where Oracle or OpenJDK installs them. You will need to create a symbolic link to the correct location:

* For Oracle: `sudo ln -s /usr/lib/jvm/java-7-oracle /usr/lib/jvm/default-java`
* For OpenJDK, 32-bit: `sudo ln -s /usr/lib/jvm/java-7-openjdk-i386 /usr/lib/jvm/default-java`
* For OpenJDK, 64-bit: `sudo ln -s /usr/lib/jvm/java-7-openjdk-amd64 /usr/lib/jvm/default-java`

For example, on Debian and Ubuntu (64-bit) using the OpenJDK:
```bash
# Install Git, GCC, and Make:
sudo apt-get install git gcc make

# Install the OpenJDK 7:
sudo apt-get install openjdk-7-jdk

# Create a symbolic link to the Java libraries:
sudo ln -s /usr/lib/jvm/java-7-openjdk-amd64 /usr/lib/java-default
```

### Mac OS X

* [Xcode](http://itunes.apple.com/us/app/xcode/id497799835) or the [Command Line Tools for Xcode](https://developer.apple.com/downloads)
* [Oracle JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Instructions

### Commands

* `./gradlew run` — Builds and runs the RAMP application.
* `./gradlew build` — Builds the RAMP application.
* `./gradlew clean` — Deletes all compiled files. This can be useful if you think one of the intermediate files is corrupt. The next time you build RAMP it will build all files from scratch instead of ignoring unchanged files.

## License

This project is licensed under the terms of the [MIT License](/LICENSE).