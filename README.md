# App Instructions:

1. **Install Android Studio**:
   - If not already installed, download and install Android Studio from the [official website](https://developer.android.com/studio).

2. **Clone the Repository**:
   - Open Android Studio.
   - Click on “Get from Version Control” or go to File > New > Project from Version Control.
   - Paste the URL of the GitHub repository into the URL field.
   - Choose a directory where you want to save the project.
   - Click on “Clone” or “OK” to clone the repository.

3. **Open the Project in Android Studio**:
   - Once the repository is cloned, Android Studio will automatically open the project.
   - If it doesn’t, go to File > Open and select the directory where you cloned the repository.

4. **Wait for Gradle Sync**:
   - Android Studio will start syncing the project with Gradle. This process may take some time depending on the project’s size and dependencies.

5. **Run the App**:
   - After the Gradle sync is complete, you can run the app by clicking on the green play button in the toolbar or by going to Run > Run 'app'.
   - Before running the app, ensure that your preferred connected device (emulator or physical device) is selected from the dropdown menu.

6. **Enabling Notifications**:
   - When the app prompts for permission to enable notifications on first run, tap on “Enable Notifications” or a similar option.
   - If prompted by the system, grant the app permission to send notifications by tapping “Allow” or “Grant.”

7. **Entering Raspberry Pi IP Address and Port Number**:
   - After enabling notifications, the app will prompt you to enter the IP address of your Raspberry Pi.
   - Tap on the input field labeled “Raspberry Pi IP Address” or similar.
   - Using the on-screen keyboard, enter the IP address provided by your Raspberry Pi.
   - Example format: “xxx.xxx.xxx.xxx”
   - Tap on the confirmation button or “Done” on the keyboard.
   - Next, the app will prompt you to enter the port number that was exposed when running the Docker image on your Raspberry Pi.
   - Tap on the input field labeled “Port Number” or similar.
   - Enter the port number provided during the Docker image run command.
   - Tap on the confirmation button or “Done” on the keyboard to proceed.

8. **Using Controls and Dashboards**:
   - Upon successful connection, all controls and dashboards within the app should function as expected.
   - Test various features and functionalities to ensure proper communication with the Raspberry Pi.
   - If you encounter any issues, verify the connection settings and consult the [main project README](https://github.com/ET0735-DevOps-AIoT-AY2320/DCPE_2A_24_Group2) or Raspberry Pi instructions for troubleshooting assistance.
