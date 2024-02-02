import com.sun.jna.Library;
import com.sun.jna.Native;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class PreMain {
    public static void main(String[] args) throws IOException {
        System.out.println("Premain called with args: " + Arrays.toString(args));
        var osName = System.getProperty("os.name").toLowerCase();

        Path appData;
        if (osName.startsWith("windows")) appData = Path.of(System.getenv("APPDATA"));
        else if (osName.startsWith("linux")) appData = Path.of("~/.config/");
        else if (osName.startsWith("mac")) appData = Path.of("~/Library/Applications Support");
        else throw new UnsupportedOperationException("Unsupported OS: " + System.getProperty("os.name"));

        var dataDir = appData.resolve("UltreonBrowser");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException ignored) {

        }

        if (osName.startsWith("windows")) Kernel32.INSTANCE.SetCurrentDirectoryW(dataDir.toString().toCharArray());
        else if (osName.startsWith("linux") || osName.startsWith("mac")) C.INSTANCE.chdir(dataDir.toString());
        else throw new UnsupportedOperationException("Unsupported OS: " + System.getProperty("os.name"));

        Files.createDirectories(dataDir.resolve("logs"));

        MainKt.main(args);
    }

    /**
     * Posix API wrapper for the PreMain.C library.
     * Used for setting the current working directory.
     */
    @SuppressWarnings({"UnusedReturnValue", "SpellCheckingInspection"})
    interface C extends Library {
        C INSTANCE = Native.load("c", C.class);

        /**
         * Sets the current working directory to the specified path.
         * <p>
         * <p>
         * Original Posix API signature:
         * ```c
         * int chdir(const char *path);
         * ```
         */
        int chdir(@NotNull String path);
    }

    /**
     * Windows API wrapper for the PreMain.Kernel32 library.
     * Used for setting the current working directory.
     */
    @SuppressWarnings("UnusedReturnValue")
    interface Kernel32 extends Library {
        /**
         * The PreMain.Kernel32 instance.
         */
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        /**
         * Sets the current working directory to the specified path.
         * <p>
         * <p>
         * Original Win32 API signature:
         * ```c
         * BOOL SetCurrentDirectory( LPCTSTR lpPathName );
         * ```
         */
        int SetCurrentDirectoryW(char @NotNull [] pathName);
    }
}
