#!/bin/sh

bin=bin/
packr=${bin}packr.jar
openjdk_win=${bin}openjdk_windows.zip
openjdk_mac=${bin}openjdk_macos.tar.gz

if [ ! -d $bin ]
then
	mkdir $bin
fi

if [ ! -f $packr ]
then
	curl -L -H 'Accept: application/octet-stream' https://api.github.com/repos/libgdx/packr/releases/assets/34173412 -o $packr
fi

mkdir out
rm -rf out/*

if command -v powershell > /dev/null
then
	if [ ! -f $openjdk_win ]
	then
		curl https://download.java.net/java/GA/jdk18/43f95e8614114aeaa8e8a5fcf20a682d/36/GPL/openjdk-18_windows-x64_bin.zip -o $openjdk_win
	fi
	
	java -jar $packr --platform windows64 --jdk $openjdk_win --executable "cephalonaut" --classpath ../out/artifacts/cephalonaut_jar/cephalonaut.jar --mainclass edu.cornell.lilbiggames.cephalonaut.desktop.DesktopLauncher --vmargs Xmx8g --output out/cephalonaut
	
	cd out
	powershell Compress-Archive cephalonaut cephalonaut-win.zip
else
	if [ ! -f $openjdk_mac ]
	then
		curl https://download.java.net/java/GA/jdk18/43f95e8614114aeaa8e8a5fcf20a682d/36/GPL/openjdk-18_macos-x64_bin.tar.gz -o $openjdk_mac
	fi
	
	java -jar $packr --platform mac --jdk $openjdk_mac --executable "cephalonaut" --classpath ../out/artifacts/cephalonaut_jar/cephalonaut.jar --mainclass edu.cornell.lilbiggames.cephalonaut.desktop.DesktopLauncher --vmargs Xmx8g --output out/cephalonaut.app --icon icon.icns
	
	cd out
	zip cephalonaut-mac.zip cephalonaut.app
fi
