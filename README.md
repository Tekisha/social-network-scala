# Social Network Application

This is a full-stack social networking application that includes basic functionalities such as registering, logging in, posting, liking, disliking, friend requests, and more. The project is implemented using **Scala (Play Framework)** on the backend, **ReactJS** on the frontend, and **MySQL** as the database. The application allows users to interact with each other by creating posts, adding friends, and liking/disliking posts.

## Features

### Authentication
- **Register**: Users can register by creating a new account.
- **Login**: Authentication is handled using JWT tokens.
- **JWT Token Authentication**: After logging in, a JWT token is stored and used for authenticating subsequent requests.

### Main Page
- **View Posts**: Users can view posts from their friends.
- **Like/Dislike**: Users can like or dislike posts (if already liked).
- **Post Ordering**: Posts are displayed in descending order with the most recent posts at the top.
- **Create Post**: Users can create new posts, which will be added to the feed.

### Profile Page
- **View Own Profile**: Users can view their own profile which includes personal information and posts.
- **Edit Profile**: Users can edit their personal information such as username, password, and profile picture.
- **View Friends**: Users can see a list of their friends.

### Other Users' Pages
- **View Other Users' Profiles**: Users can visit the profiles of other users. If they are friends, they can see the posts. If not, they will be given the option to send a friend request.
- **Add Friend**: If users are not friends, an "Add Friend" button is shown.

### Friend Requests
- **Send Friend Request**: Users can send friend requests to other users.
- **Accept/Reject Friend Request**: Users can accept or reject received friend requests.
- **Friendship Management**: Once a friend request is accepted, users become friends and can view each other’s posts.

### Search
- **Search Users**: Users can search for other users by their username.

### Commenting
- **Post Comments**: Users can comment on posts.
  
## Demo
You can view a demonstration of the project at the following link: [YouTube Demo](https://youtu.be/gdCzbpRTfcM).

## Technologies Used
### Backend:
- **Java 1.8**
- **Scala** (with Play Framework)
- **MySQL** (database)
- **Slick** (database query and management library)
  
### Frontend:
- **ReactJS**

### Tools:
- **JWT** for user authentication.
- **sbt** for building and managing Scala projects.

## Getting Started

### Backend Setup
1. **Install Java and Scala**: Ensure you have Java 1.8 and Scala installed on your system.
2. **Clone the repository**:
    ```bash
    git clone <[repository-url](https://github.com/Tekisha/social-network)>
    cd backend
    ```
3. **Configure Database**: Set up a MySQL database and configure the connection in the `application.conf` file.
4. **Run Backend**:
    ```bash
    sbt run
    ```

### Frontend Setup
1. **Install Node.js**: Make sure Node.js and npm are installed.
2. **Clone the repository**:
    ```bash
    cd frontend
    npm install
    ```
3. **Run Frontend**:
    ```bash
    npm start
    ```

### Database Configuration
1. Create a MySQL database.
2. Update the connection settings in the Play framework's `application.conf` file:
    ```hocon
    slick.dbs.default.db {
      url = "jdbc:mysql://localhost:3306/<your-database>"
      user = "<db-username>"
      password = "<db-password>"
    }
    ```

# API Endpoints

The following is a list of all API endpoints provided by the social network application.

## Static Resources
- **GET** `/assets/*file`: Serve static resources (such as images, CSS, and JS) from the `/public` folder, mapped to the `/assets` URL path.

## User Routes
- **GET** `/users`: Retrieve a list of all users.
- **GET** `/users/search`: Search for users by username.
- **GET** `/users/:id`: Retrieve a user's profile by their ID.
- **PATCH** `/users/me`: Update the logged-in user's basic profile information.
- **PUT** `/users/me/password`: Update the logged-in user's password.
- **DELETE** `/users`: Delete the logged-in user's account.
- **POST** `/register`: Register a new user.
- **POST** `/login`: Log in to the application and receive a JWT token.
- **POST** `/users/profile-photo`: Update the logged-in user's profile picture.

## Post Routes
- **POST** `/posts`: Create a new post.
- **PUT** `/posts/:postId`: Edit a post by its ID.
- **DELETE** `/posts/:postId`: Delete a post by its ID.
- **GET** `/posts/user/:userId`: Retrieve all posts by a specific user.
- **GET** `/posts`: Retrieve all posts.
- **GET** `/posts/friends`: Retrieve posts from friends.
- **GET** `/posts/:postId`: Retrieve a specific post by its ID.
- **POST** `/posts/:postId/comments`: Create a comment on a post.
- **GET** `/posts/:postId/comments`: Retrieve comments on a post.
- **DELETE** `/comments/:commentId`: Delete a comment by its ID.

## Friend Request Routes
- **POST** `/friendRequests`: Send a friend request to another user.
- **PUT** `/friendRequests/:requestId/respond`: Respond to a friend request (accept or reject).
- **GET** `/friendRequests/receivedPending`: Retrieve all pending friend requests for the logged-in user.
- **DELETE** `/friendRequests/:requestId`: Delete a specific friend request.
- **GET** `/friendRequests/:requestId`: Retrieve a specific friend request by its ID.
- **GET** `/friendRequests`: Retrieve all friend requests.
- **DELETE** `/friendRequests/user/:userId`: Delete a friend request by the user ID of the sender.

## Friendship Routes
- **GET** `/friendships`: Retrieve a list of the logged-in user's friends.
- **DELETE** `/friendships/:friendId`: Remove a friend by their ID.

## Like Routes
- **POST** `/posts/:postId/like`: Like a post by its ID.
- **DELETE** `/posts/:postId/unlike`: Unlike a post by its ID.

## Future Enhancements
- **Notifications**: Implement notifications for new friend requests, post likes, etc.
- **Real-Time Chat**: Add a messaging feature for friends.

