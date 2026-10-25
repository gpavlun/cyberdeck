#!/bin/bash

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p dev.cyberdeck 1
