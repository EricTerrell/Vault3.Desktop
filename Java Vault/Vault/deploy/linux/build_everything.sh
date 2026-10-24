#!/bin/bash

. build_linux_gtk_x86_64.sh
. build_linux_gtk_aarch_64.sh
. build_win_x86_64.sh
. build_win_aarch_64.sh

# Verify that the files are for the proper platform

file "/home/eric/Documents/software development/Vault3-build/Vault3LinuxGTK_aarch_64/custom-runtime/bin/java"
file "/home/eric/Documents/software development/Vault3-build/Vault3LinuxGTK_x86_64/custom-runtime/bin/java"
file "/home/eric/Documents/software development/Vault3-build/Vault3Win_aarch_64/custom-runtime/bin/javaw.exe"
file "/home/eric/Documents/software development/Vault3-build/Vault3Win_x86_64/custom-runtime/bin/javaw.exe"
