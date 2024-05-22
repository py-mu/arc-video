package com.pymu.arc.video.basic;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class LibraryLoader {
    public static final String NATIVE_LIBRARY_NAME = "arc-video-desktop";
    private static boolean loaded = false;
    private static String libraryPath;

    /**
     * This will set the path in which it tries to find the native library.
     *
     * @param path The path on which the library can be found. If it is null or an empty string, the default location will be used.
     *             This is usually a SteamJavaNatives folder inside the jar.
     */
    public static void setLibraryFilePath(String path) {
        libraryPath = path;
    }

    /**
     * This method will load the libraries from the path given with setLibraryFilePath.
     *
     * @return whether loading was successful
     */
    public static boolean loadLibraries() {
        if (loaded) {
            return true;
        }

        SharedLibraryLoader libLoader;
        if (libraryPath == null) {
            libLoader = new SharedLibraryLoader();
        } else {
            libLoader = new SharedLibraryLoader(libraryPath);
        }

        try {
            libLoader.load(NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            loaded = false;
            return false;
        }
        loaded = true;
        return true;
    }

    /**
     * This tells whether the native libraries are already loaded.
     *
     * @return Whether the native libraries are already loaded.
     */
    public static boolean isLoaded() {
        return loaded;
    }

    public static void setDebugLogging(boolean debugLogging) {
        if (!loaded) {
            if (!loadLibraries()) {
                return;
            }
        }
        setDebugLoggingNative(debugLogging);
    }
    /*
     * Native functions
     * @off
     */

	/*JNI
		#include "Utilities.h"
	 */

    /**
     * This function can be used to turn on/off debug logging of the native code
     *
     * @param debugLogging whether logging should be turned on or off
     */
    private native static void setDebugLoggingNative(boolean debugLogging);/*
	 	debug(debugLogging);
	 */
}
