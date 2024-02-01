# Go Game
Starting point for the Go game

* Usage of Java 21 is required
  * Make sure your IDE uses the correct version and/or set $JAVA_HOME if you use the commandline
* Also usage of JavaFX 21.0.1 and Junit 5.10 or newer are required. 
* This process will be the instructions of running the GoServer and GoClient through the IntelliJ IDE (ver. 2023.3.2) 
  use of the command line or another IDE environment to run it might need some more stepts.
  1. Download the final release.
  2. Import the project to your intelliJ IDE.
  3. Important is to mark the src/main/java directory as Source folder, the src/test/java as Test Source folder and
     src/main/resources as Resources folder.
  4. For the server you need to build and run the GoServer.java through IntelliJ IDE. After that follow the instruction of the Tui.
  5. For the client you need to build and run the GoClient.java through IntelliJ IDE. After that follow the instruction of the Tui.
 * For running multiple clients go Run -> Edit Configurations... coose the GoClient on the application window and then -> Modify options -> Allow multiple instances.
 * Some times JavaFX might throw some warnings, for that go Run -> Edit Configurations... coose the GoClient on the application window and then -> Modify options
      Add VM Options and on the run options box type "--module-path /<path to java fx installation>/javafx-sdk-<javafx version>/lib --add-modules javafx.controls,javafx.fxml"
