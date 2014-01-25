# RAMP Server

A server application that applies equalizer modifications on up to 24 tracks simultaneously and then streams the results back to a client application in realtime.

The web-based client application can be found [here](https://github.com/mattprice/RAMP-Webapp).

## Requirements

### Linux

* Git, GCC, and Make
* [OpenJDK 6+](http://openjdk.java.net/install/index.html) or [Oracle JDK 6+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

#### Debian, Ubuntu

```bash
sudo apt-get install git gcc make default-jdk
```

#### CentOS, Red Hat, Fedora

```bash
sudo yum install git gcc make java-1.7.0-openjdk-devel
sudo ln -s /etc/alternatives/java_sdk /usr/lib/jvm/default-java
```

Note that libpd searches for the Java libraries in `$JAVA_HOME` and falls back to `/usr/lib/jvm/default-java`. If you have installed Java using another method you will need to set `$JAVA_HOME`, or create a symbolic link, to the correct location.

### Mac OS X

* [Xcode](http://itunes.apple.com/us/app/xcode/id497799835) or the [Command Line Tools for Xcode](https://developer.apple.com/downloads)
* [Oracle JDK 6+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Instructions

### NetBeans

1. Install the "Gradle Support" plugin:
    * Go to **Tools**, **Plugins**.
    * Click the **Available Plugins** tab.
    * Search and install "Gradle Support".
2. Go to **File**, **Open Project**.
3. Wait for NetBeans to finish scanning the project. When it has completed, you can use Run Project like normal.

### Commands

* `./gradlew run` — Builds and runs the RAMP application.
* `./gradlew build` — Builds the RAMP application.
* `./gradlew clean` — Deletes all compiled files. This can be useful if you think one of the intermediate files is corrupt. The next time you build RAMP it will build all files from scratch instead of ignoring unchanged files.

## License

This project is licensed under the terms of the [MIT License](/LICENSE).