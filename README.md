# Probability Puzzles

This is the repo for https://play.google.com/store/apps/details?id=atorch.statspuzzles.

Nearly all of the puzzles in this app can be solved by hand, with pen and paper.
For code that checks some of the answers using simulations (Monte Carlo), see https://github.com/atorch/probability_puzzles_solutions.

Are you looking for the puzzles?
They're defined in [app/src/main/res/values/strings.xml](app/src/main/res/values/strings.xml).

Several contributors have translated the app into other languages:
- Thank you [Roland Illig](https://github.com/rillig)
for [translating the app into German](app/src/main/res/values-de/strings.xml)
- Thank you Roberto Espiño for [translating the app into Spanish](app/src/main/res/values-es/strings.xml)
- Thank you [Ahmad Alhour](https://github.com/aalhour) for [translating the app into Arabic](app/src/main/res/values-ar/strings.xml)

Thank you [Mariusz Gromada](https://github.com/mariuszgromada)
for publishing [mXparser](https://github.com/mariuszgromada/MathParser.org-mXparser),
which is used in [SolvePuzzle.java](app/src/main/java/atorch/statspuzzles/SolvePuzzle.java).

Thank you Andrew Gelman for
[featuring the app on your blog](https://statmodeling.stat.columbia.edu/2015/05/05/hes-looking-for-probability-puzzles/),
and thank you to everyone on stackoverflow who [helped me](https://stackoverflow.com/questions/48960080/empty-space-above-the-app-bar)
[figure out](https://stackoverflow.com/questions/27443006/setshareintent-when-a-new-fragment-is-displayed)
[how to](https://stackoverflow.com/questions/26495084/how-do-i-add-an-action-bar-to-a-swipe-view)
write an Android app, back in the dark ages before LLMs!

I first released this app in July 2014.
The app has been rated 6,594 times as of October 2020, and has around 26,000 active users.

## Running Tests

To run the unit tests for this project, execute the following command:

```bash
./gradlew test
```

## Building the App

To build the app and run all checks, including linting, execute the following command:

```bash
./gradlew build
```

This is the same command that is run in the GitHub Actions CI. Running this locally will help you find and fix issues before pushing your changes.

## Running Instrumented Tests (Locally from the Terminal)

Running the instrumented tests (`./gradlew connectedAndroidTest`) requires a connected Android device or an emulator. The following steps describe how to set up and run an Android emulator on Ubuntu entirely from the command line.

### 1. KVM Hardware Acceleration Setup

For better performance, the Android emulator should use KVM hardware acceleration.

First, check if your CPU supports virtualization:
```bash
sudo apt-get update
sudo apt-get install -y cpu-checker
kvm-ok
```
If the output includes `KVM acceleration can be used`, you are good to go. Otherwise, you may need to enable virtualization (e.g., VT-x or AMD-V) in your computer's BIOS/UEFI settings.

Next, install required KVM packages and add your user to the `kvm` group.
```bash
sudo apt-get install -y qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils
sudo adduser $USER kvm
```
You will need to **reboot your system** for the group change to take effect.

### 2. Install Java (JDK)

The Android command-line tools require a Java Development Kit.
```bash
sudo apt-get install -y openjdk-17-jdk
```

### 3. Install Android SDK Command-Line Tools

1.  **Download and set up the SDK:**
    ```bash
    # Download the latest command-line tools from Google
    wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

    # Create the necessary directory structure and unzip the tools
    mkdir -p ~/android_sdk/cmdline-tools
    unzip commandlinetools-linux-*.zip -d ~/android_sdk/cmdline-tools
    mv ~/android_sdk/cmdline-tools/cmdline-tools ~/android_sdk/cmdline-tools/latest
    rm commandlinetools-linux-*.zip
    ```

2.  **Set environment variables:**
    Add the following to your `~/.bashrc` or `~/.zshrc` file:
    ```bash
    export ANDROID_HOME=$HOME/android_sdk
    export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
    export PATH=$PATH:$ANDROID_HOME/platform-tools
    export PATH=$PATH:$ANDROID_HOME/emulator
    ```
    Then, source the file to apply the changes (e.g., `source ~/.bashrc`).

### 4. Download System Image and Create AVD

1.  **Accept SDK licenses:**
    ```bash
    sdkmanager --licenses
    ```
    Accept all licenses by typing `y` and pressing Enter.

2.  **Install the system image and other tools:**
    The CI uses API level 33. We will install the corresponding system image.
    ```bash
    sdkmanager "platform-tools" "build-tools;33.0.2" "system-images;android-33;default;x86_64" "emulator"
    ```

3.  **Create the Android Virtual Device (AVD):**
    ```bash
    avdmanager create avd -n pixel_33 -k "system-images;android-33;default;x86_64" --device "pixel_6"
    ```
    This creates an AVD named `pixel_33` using the `"pixel_6"` device profile, which simulates the screen size and characteristics of a Google Pixel 6 phone. The GitHub Actions CI is configured to use the same device profile to ensure that tests run in a consistent environment.

### 5. Run the Tests

1.  **Start the emulator in the background:**
    ```bash
    emulator -avd pixel_33 -no-window &
    ```
    Wait a minute or two for the emulator to fully boot up.

2.  **Run the connected tests:**
    ```bash
    ./gradlew connectedAndroidTest
    ```
    The Gradle script will automatically detect the running emulator and execute the tests on it.

### 6. Running Tests for a Different Locale

To reproduce the CI environment for a specific locale, you need to start the emulator with the desired language settings. The CI runs tests against English, German, Spanish, and Arabic.

First, make sure the emulator is not running (`adb emu kill`).

Then, start the emulator with the desired locale. For example, to start the emulator with the German locale, run:

```bash
emulator -avd pixel_33 -no-window -prop persist.sys.language=de -prop persist.sys.country=DE &
```

Wait for the emulator to boot up, and then run the tests:

```bash
./gradlew connectedAndroidTest
```

### 7. Shut down the emulator when you're done:
    ```bash
    adb emu kill
    ```

## Running the App on a Physical Device

You can also install and run the app on a physical Android device using the command line.

### 1. Enable USB Debugging on Your Device

1.  On your Android device, open **Settings**.
2.  Go to **About phone**.
3.  Tap **Build number** seven times to enable **Developer options**.
4.  Go back to the main **Settings** menu, then go to **System** > **Developer options**.
5.  Enable **USB debugging**.

### 2. Connect Your Device via USB

Connect your Android device to your computer using a USB cable. A prompt might appear on your device asking you to authorize the computer for debugging. Accept it.

### 3. Build and Install the App

1.  **Navigate to the project directory:**
    ```bash
    cd /home/adrian/probability_puzzles
    ```

2.  **Build the debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
    Note: Gradle uses an incremental build process. If you haven't made any code changes, Gradle may not rebuild the APK. To force a rebuild, you can run `./gradlew clean assembleDebug`.

3.  **Install the APK on your device:**
    ```bash
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    ```

The `-r` flag reinstalls the app, keeping its data. After the command completes, the app will be installed on your device, and you can run it by tapping its icon.

## Running the App on a Physical Device (Wireless Debugging)

You can also connect to your device wirelessly. This is especially useful if you have issues with USB drivers or cables.

### 1. Enable Wireless Debugging

1.  On your Android device, open **Settings** > **System** > **Developer options**.
2.  Enable **Wireless debugging**.
3.  Make sure your device and your computer are on the same Wi-Fi network.

### 2. Pair Your Device

You only need to pair your device once.

**Option A: Pairing with QR Code (Recommended)**

1.  In Android Studio, go to the **Device Manager** and select **Pair devices using Wi-Fi**.
2.  A QR code will be displayed.
3.  On your device, under **Wireless debugging**, select **Pair device with QR code** and scan the QR code on your computer.

**Option B: Pairing with Command Line**

1.  On your device, under **Wireless debugging**, select **Pair device with pairing code**.
2.  You will see a pairing code and an IP address with a port (e.g., `192.168.1.100:41234`).
3.  On your computer, run the following command, replacing the IP, port, and pairing code with the ones from your device:
    ```bash
    adb pair 192.168.1.100:41234 123456
    ```

### 3. Connect to Your Device

After pairing, you need to connect to your device.

1.  On your device, under **Wireless debugging**, you will see an IP address and port for the connection (e.g., `192.168.1.100:38765`). This port is different from the pairing port.
2.  On your computer, run the following command, replacing the IP and port with the ones from your device:
    ```bash
    adb connect 192.168.1.100:38765
    ```
3.  You can verify the connection by running `adb devices`. You should see your device listed.

### 4. Troubleshooting Wireless Debugging

Wireless debugging can sometimes be tricky. Here are some common issues and how to solve them:

*   **`adb devices` shows `offline`:**
    *   On your device, go to **Settings** > **System** > **Developer options** and tap **Revoke USB debugging authorizations**.
    *   Toggle **Wireless debugging** off and on again.
    *   Reconnect to your device.

*   **`adb pair` fails with `protocol fault`:**
    *   This can be a network issue. Make sure you are not on a VPN.
    *   Check if your firewall is blocking the connection.
    *   Some routers have an "AP Isolation" feature that prevents devices from communicating. Make sure it's disabled.
    *   Restart the ADB server with `adb kill-server`.

*   **Build fails with `IOException: Unable to delete directory`:**
    *   This is likely a file ownership issue caused by running Android Studio with `sudo`. **Do not run Android Studio with `sudo`!**
    *   To fix this, you need to change the ownership of the project files back to your user. Run the following command, replacing `your_user` with your username:
        ```bash
        sudo chown -R your_user:your_user /path/to/your/project
        ```
    *   You might also need to stop the Gradle daemon with `./gradlew --stop`.

*   **Installation fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`:**
    *   This means the app is already installed with a different signature (e.g., from the Play Store).
    *   Uninstall the app from your device and try again. You can do this from the command line:
        ```bash
        adb uninstall atorch.statspuzzles
        ```
