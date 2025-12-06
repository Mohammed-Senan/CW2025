## GitHub Repo

[https://github.com/Mohammed-Senan/CW2025](https://github.com/Mohammed-Senan/CW2025)

## Compilation Instructions

- **Step 1:** Ensure all Java classes are organized in the `com.comp2042` package structure (`model`, `controller`, `view`, `main`).

- **Step 2:** Make sure JavaFX is configured. This project uses Maven to handle JavaFX dependencies via the `javafx-maven-plugin`.

- **Step 3:** Compile and run the application:

    - **Via Terminal:**
      ```bash
      mvn clean javafx:run
      ```
    - **Via IntelliJ:**
      Open the Maven sidebar, go to **Plugins** > **javafx** > **javafx:run**.

## Requirements

- Java 17 or compatible version
- Maven 3.x
- JavaFX SDK (managed automatically via Maven)

## Implemented and Working Properly

- **Main Menu:**
  A polished, arcade-style start screen featuring a "Matrix" digital rain background. Includes animated neon buttons for:
  * **Start Game:** Launches the classic endless mode.
  * **Levels:** Opens the level selection screen.
  * **Settings:** Opens the audio and gameplay configuration.
  * **Controls:** Shows the tutorial overlay.
  * **Quit:** Exits the application.

- **Pause Menu:**
  Pressing `P` instantly pauses the game loop and displays a semi-transparent overlay with specific options:
  * **Resume:** Continues the game instantly.
  * **Settings:** Allows adjusting volume or ghost mode mid-game.
  * **Back to Menu:** Returns to the Main Menu (resetting the current session).
  * **Quit:** Exits the application to the desktop.

- **Ghost Mode (Shadow Piece):**
  A semi-transparent "ghost" block shows exactly where the active piece will land. This helps players make accurate drops.

- **Neon Visuals & Particle Effects:**
  The game features a complete "Cyberpunk" aesthetic. When lines are cleared, they don't just vanish; they trigger a particle explosion animation ("shards") for better game feel.

- **Hard Drop:**
  Implemented a Hard Drop feature (Spacebar) that instantly locks the piece at the Ghost position, speeding up gameplay.

- **Strategic Leveling System:**
  Unlike standard Tetris, this game uses a "Target Score" system. To pass a level, the player must achieve a specific score within a limited number of blocks. If blocks run out, the level is failed.

- **Sound System (Singleton):**
  A centralized audio manager handles looping background music and specific sound effects for movement, rotation, line clears, and game over.

- **Settings Menu:**
  Users can toggle Ghost Mode on/off and adjust Music and SFX volumes independently via sliders.

- **Tutorial Overlay (How to Play):**
  A dedicated "How to Play" screen accessible from the main menu or in-game via the `!` button. It visualizes controls using graphical neon keycaps for easy learning.

- **Next Block Preview:**
  A dedicated HUD panel on the right side displays the upcoming tetromino, allowing players to plan their strategy in advance.

- **High Scores:**
  The game tracks the highest score achieved. This data is persistent (saved locally) and is displayed prominently on the main game HUD to motivate players.

## Implemented but Not Working Properly

- **Window Resizing:**
  While the game window is resizable, the background image (brick wall) currently uses a fixed aspect ratio and may not stretch perfectly on ultra-wide monitors.

## Features Not Implemented

- **Hold Piece:**
  The mechanics for "Holding" a piece (swapping with a saved block) were planned but not implemented in the final version due to time constraints.

- **Online Multiplayer:**
  Real-time multiplayer using WebSockets is listed in the roadmap but not yet active.

## New Java Classes

- **TetrominoFactory.java (Factory Pattern):**
  Handles the creation logic for new blocks. It encapsulates the random number generation and switch statements, removing clutter from the GameController.

- **SoundManager.java (Singleton Pattern):**
  Manages all audio resources. It ensures only one instance of the audio engine exists and allows sound to be triggered from any class (Menu, Game, Board).

- **GameConfig.java:**
  A dedicated class to store global settings like Volume levels and Ghost Mode status, separating configuration data from game logic.

- **SettingsPanel.java:**
  Handles the logic for the new Settings overlay, including the volume sliders and toggle switches.

## Modified Java Classes

- **GameController.java:**
  Extensively refactored. The original monolithic class was split to follow the MVC pattern. It now handles only game logic and delegates rendering to the view components.

- **Board.java:**
  Updated to support the new "Leveling System" logic (tracking blocks used vs. score needed) and to trigger the new Particle Effects on line clear.

- **Main.java:**
  Updated to support the new FXML loading structure and set the "Tetris NEW VERSION" window title.

## Unexpected Problems

- **Ghost Block Misalignment:**
  Initially, the Ghost Block appeared slightly wider than the real blocks due to how JavaFX renders borders (`strokeRect`).
  **Solution:** I implemented a rendering offset calculation to draw the stroke "inside" the grid cell rather than centered on the line.

- **FXML Controller Crash:**
  After refactoring the packages to MVC (`model`/`controller`), the game crashed because the FXML files were still pointing to the old `logic` package.
  **Solution:** I updated the `fx:controller` attribute in `gameLayout.fxml` and `mainMenu.fxml` to point to the new package locations.

- **Audio File Extensions:**
  During development, some audio files were named `sfx.mp3.mp3` by mistake, causing loading errors.
  **Solution:** Renamed all resources to standard naming conventions and verified paths in `SoundManager`.