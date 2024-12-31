# iNoteGames

iNoteGames is a Spring Boot application that provides a RESTful API for managing interactive games, such as Tic-Tac-Toe. The application supports turn-based multiplayer gameplay, bot opponents, and game state tracking with a robust exception handling mechanism. It also leverages WebSocket for real-time game updates and Docker for easy deployment.

---

## Features

- **Tic-Tac-Toe**: Fully functional multiplayer Tic-Tac-Toe with bot support.
- **Real-Time Gameplay**: WebSocket integration for real-time game state updates.
- **Custom Exceptions**: Handles errors like invalid tokens, duplicate players, and game state conflicts with descriptive exceptions.
- **Player Stats**: Tracks player stats and game histories.
- **Bot Integration**: AI-powered bot for single-player Tic-Tac-Toe matches.
- **Modular Design**: Cleanly separated concerns for models, controllers, services, and configurations.
- **Docker Support**: Includes a `Dockerfile` for seamless containerization.

---

## Project Structure

```
├── mvnw                 # Maven wrapper for build automation
├── mvnw.cmd             # Windows Maven wrapper
├── pom.xml              # Maven project configuration
├── Dockerfile           # Dockerfile for containerization
├── src/
│   ├── main/
│   │   ├── resources/                  # Configuration files and static resources
│   │   │   ├── application.properties  # Spring Boot application configuration
│   │   │   └── banner.txt              # Custom application banner
│   │   └── java/
│   │       └── com/example/iNoteGames/
│   │           ├── Exception/          # Custom exception classes
│   │           ├── Storage/            # Game state storage
│   │           ├── Model/              # Data models and responses
│   │           ├── Configuration/      # WebSocket and REST template configurations
│   │           ├── Controller/         # REST API controllers
│   │           ├── Service/            # Business logic and game services
│   │           └── INoteGamesApplication.java  # Application entry point
└── test/
    └── java/com/example/iNoteGames/    # Unit tests
```

---

## Installation

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Docker (optional for containerization)

### Build and Run Locally

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/iNoteGames.git
   cd iNoteGames
   ```

2. **Build the Project**:
   ```bash
   ./mvnw clean install
   ```

3. **Run the Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the API**:
   The application runs on `http://localhost:8080` by default.

### Run with Docker

1. **Build the Docker Image**:
   ```bash
   docker build -t inotegames .
   ```

2. **Run the Docker Container**:
   ```bash
   docker run -p 8080:8080 inotegames
   ```

---

## API Endpoints

- **`POST /games/start`**: Create a new game.
- **`POST /games/connect`**: Join an existing game.
- **`POST /games/gameplay`**: Make a move in the game.
- **`POST /games/getStatus`**: Get the current status of the game.
- **`POST /games/reset`**: Reset board and start a new game.

---

## Technologies Used

- **Framework**: Spring Boot
- **Build Tool**: Maven
- **WebSocket**: For real-time communication
- **Docker**: For containerized deployments

---

## Future Enhancements

- Add a leaderboard to track player rankings.
- Implement additional games, such as Connect Four or Chess.
- Improve the bot's AI for more challenging gameplay.
