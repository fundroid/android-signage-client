debug:
	ant debug
	adb uninstall camp.pixels.signage
	adb install bin/Signage-debug.apk

release:
	ant release
	adb uninstall camp.pixels.signage
	adb install bin/Signage-release.apk
