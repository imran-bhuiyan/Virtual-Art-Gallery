# 🎨 Virtual Art Gallery

A feature-rich desktop application built with **JavaFX** and **MySQL** that simulates a complete virtual art marketplace. Users can browse, purchase, and auction traditional paintings and NFTs, while artists can manage their portfolios and engage with buyers through real-time chat.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture & OOP Design](#architecture--oop-design)
- [User Roles](#user-roles)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [Running the Chat Server](#running-the-chat-server)
- [Usage Guide](#usage-guide)
- [Known Issues & Notes](#known-issues--notes)

---

## 🖼️ Project Overview

The **Virtual Art Gallery** is an Object-Oriented Programming (AOOP course) capstone project that models a real-world online art marketplace. It supports four distinct user roles — **Guest**, **Customer**, **Artist**, and **Admin** — each with a dedicated dashboard and tailored functionality. The system handles painting sales, live auctions, NFT trading, a real-time messaging system, event management, credit-based payments, and more.

---

## ✨ Features

### 🔓 Guest
- Browse the gallery homepage without login
- View all paintings with category filters
- View top-rated artworks (by reaction count)
- Browse upcoming gallery events
- Submit inquiries via the **Contact Us** form
- Register as a new user (Customer or Artist)
- Login to an existing account

### 🛒 Customer
- Personalized home dashboard showing account balance
- Browse paintings by category (Oil, Acrylic, Watercolor, etc.)
- Add paintings to cart and checkout
- View top-rated artworks
- Participate in **live auctions** with real-time bidding
- Browse and purchase **NFTs** directly
- View and download owned NFTs from profile
- React (like) paintings — notifies the artist
- Direct **chat with artists** from any painting card
- Receive push notifications for auction results, reactions, etc.
- Manage profile: update name, email, password
- Add credits to wallet
- View gallery events

### 🎨 Artist
- Dashboard with artwork count, sold items, and total revenue
- Add new paintings (with image upload, category, price, year, stock)
- View and manage their painting portfolio
- Edit or manage individual paintings
- Create auctions for their paintings (with start/end date validation)
- View auction status and results
- Add NFTs (name, edition, blockchain, price, payment address, image)
- View NFT orders and manage NFT listings
- View painting orders placed by customers
- Real-time **message inbox** with customers
- Receive notifications for likes, orders, and auction wins
- Full profile management & password change
- View balance and submit **withdrawal requests**
- View withdrawal history

### 🛠️ Admin
- Central dashboard: total artists, artworks, customers, events, inquiries, and platform revenue
- **Automatic auction lifecycle management** on login (sets auctions to active/ended, transfers credits, sends notifications)
- Add and manage Artists
- Add and manage Customers
- Add and manage Artworks
- Add and manage Events (with full CRUD + editing)
- Handle and approve **artist withdrawal requests**
- Handle **customer credit top-up requests**
- Approve or reject **auction requests**
- View and respond to **customer inquiries** (Contact Us submissions)
- View all platform orders with order details
- Platform-wide messaging

---

## 🛠️ Technology Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Language     | Java 22                           |
| UI Framework | JavaFX 22-ea+11 (FXML)            |
| Database     | MySQL / MariaDB (port 3307)       |
| JDBC Driver  | MySQL Connector/J 8.0.33          |
| UI Controls  | ControlsFX 11.1.2                 |
| Forms        | FormsFX 11.6.0                    |
| Game Engine  | FXGL 17.3 (for notifications/FX)  |
| Build Tool   | Apache Maven 3.x                  |
| Testing      | JUnit Jupiter 5.10.0              |
| Chat Server  | Java Sockets (TCP, port 12345)    |

---

## 🏛️ Architecture & OOP Design

The project follows a clean **MVC (Model-View-Controller)** pattern using JavaFX's built-in FXML support and applies several core OOP principles:

### Key Design Patterns & Principles

| Pattern / Principle | Where Used |
|---------------------|------------|
| **Inheritance** | All role controllers extend `BaseController` |
| **Interface (Contract)** | `UserIdAware` interface — all controllers that receive a session `userId` implement it |
| **Abstraction** | `BaseController` is abstract; defines shared navigation and session-passing logic |
| **Polymorphism** | `setUserId()` is overridden in every controller to trigger role-specific data loading |
| **Encapsulation** | Private DB queries, private helper methods, FXML-injected fields |
| **Singleton-like DB** | `DataBaseConnection.getConnection()` is a static method used across all controllers |
| **Concurrency** | `ChatServer` uses `ExecutorService` with a fixed thread pool (500 threads) for concurrent client handling |
| **Observer-like** | Notification system automatically notifies artists on reactions and auction results |

### Class Hierarchy
```
UserIdAware (interface)
    └── BaseController (abstract)
            ├── adminDashboardController
            ├── ArtistDashboardController
            ├── ArtistAddPaintController
            ├── ArtistAddNFTController
            ├── ArtistAddAuctionController
            ├── customerHomePageController
            ├── customerPaintingController
            ├── CustomerProfileController
            ├── CustomerPaintingCheckoutController
            └── ... (all other role controllers)
```

### Session / User ID Propagation
When a user logs in, `UserLoginController` reads the `user_id` and `role` from the database and passes the `userId` to the next controller using `setUserId()`. Each page transition via `BaseController.loadPage()` or the local `loadPageWithUserId()` method automatically forwards the session ID, ensuring every page has access to the logged-in user's identity.

---

## 👥 User Roles

| Role     | Login Required | Access Level                                    |
|----------|----------------|------------------------------------------------|
| Guest    | ❌ No           | Read-only: view paintings, events, contact us  |
| Customer | ✅ Yes          | Buy, bid, chat, react, manage profile & NFTs   |
| Artist   | ✅ Yes          | Upload art, manage auctions, NFTs, withdraw    |
| Admin    | ✅ Yes          | Full platform management and oversight          |

---

## 🗄️ Database Schema

The application connects to a MySQL database named **`art_gallery`** at `localhost:3307`.

### Core Tables

| Table          | Description                                         |
|----------------|-----------------------------------------------------|
| `users`        | Stores all users: id, name, email, password, role, dob, credits |
| `paintings`    | Artworks: id, name, artist_id, year, category, price, stock, status, image_url |
| `nfts`         | NFT entries: id, name, artist_id, edition, blockchain, price, image_url, payment_address, owner_id |
| `auctions`     | Auction data: id, painting_id, starting_bid, current_bid, start_date, ends_time, status |
| `bids`         | Bid records: id, auction_id, user_id, bid_amount |
| `orders`       | Customer orders: id, user_id, order_status |
| `orderitems`   | Order line items: id, order_id, painting_id, price, quantity |
| `cart`         | Cart state: user_id, painting_id, quantity |
| `reactions`    | Painting likes: user_id, painting_id |
| `notifications`| System notifications: id, user_id, type, title, message, created_at |
| `ChatMessages` | Persistent chat: id, sender_id, receiver_id, content, sent_at |
| `events`       | Gallery events: id, title, description, date, etc. |
| `contactus`    | Guest contact form submissions: id, name, email, message |

---

## 📁 Project Structure

```
Virtual-Art-Gallery-main/
├── pom.xml                          # Maven build configuration
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java     # Java module descriptor
│       │   └── com/example/test_project/
│       │       ├── HelloApplication.java           # App entry point
│       │       ├── DataBaseConnection.java          # JDBC connection singleton
│       │       ├── BaseController.java              # Abstract base controller
│       │       ├── UserIdAware.java                 # Session interface
│       │       ├── ChatServer.java                  # TCP chat server
│       │       ├── UserLoginController.java         # Login & routing by role
│       │       ├── userRegistrationController.java  # User sign-up
│       │       │
│       │       ├── -- Admin --
│       │       ├── adminDashboardController.java
│       │       ├── adminAddArtController.java
│       │       ├── adminAddEventController.java
│       │       ├── adminArtistAddController.java
│       │       ├── adminCustomerAddController.java
│       │       ├── adminManageArtController.java
│       │       ├── adminManageArtistController.java
│       │       ├── adminManageCustomerController.java
│       │       ├── adminHandleAuctionController.java
│       │       ├── adminWithdrawRequestController.java
│       │       ├── AdminCreditRequestController.java
│       │       ├── adminInquriesController.java
│       │       ├── adminOrderDetailsController.java
│       │       ├── AdminManageEventController.java
│       │       ├── AdminEditEventController.java
│       │       ├── adminEditArtistController.java
│       │       ├── adminEditArtworkController.java
│       │       ├── adminEditCustomerController.java
│       │       │
│       │       ├── -- Artist --
│       │       ├── ArtistDashboardController.java
│       │       ├── ArtistAddPaintController.java
│       │       ├── ArtistAddNFTController.java
│       │       ├── ArtistAddAuctionController.java
│       │       ├── ArtistMyPaintingsController.java
│       │       ├── ArtistEditPaintingController.java
│       │       ├── ArtistPaintingPageController.java
│       │       ├── ArtistNFTPageController.java
│       │       ├── ArtistNFTordersController.java
│       │       ├── ArtistOrdersController.java
│       │       ├── ArtistSeeAuctionsController.java
│       │       ├── ArtistMessageController.java
│       │       ├── ArtistNotificationController.java
│       │       ├── ArtistProfileController.java
│       │       ├── ArtistPasswordChangeController.java
│       │       ├── ArtistBalanceController.java
│       │       ├── ArtistWithdrawMoneyController.java
│       │       ├── ArtistWithdrawHistoryController.java
│       │       │
│       │       ├── -- Customer --
│       │       ├── customerHomePageController.java
│       │       ├── customerPaintingController.java
│       │       ├── CustomerPaintingCheckoutController.java
│       │       ├── customerAuctionController.java
│       │       ├── customerNFTcontroller.java
│       │       ├── CustomerNFTCheckoutController.java
│       │       ├── CustomerEventController.java
│       │       ├── CustomerMessageController.java
│       │       ├── CustomerNotificationController.java
│       │       ├── CustomerProfileController.java
│       │       ├── CustomerAddCreditController.java
│       │       ├── customerPageTopArtController.java
│       │       │
│       │       └── -- Guest --
│       │           ├── guestPageController.java
│       │           ├── guestPaintingController.java
│       │           ├── GuestEventController.java
│       │           ├── guestTopArtController.java
│       │           └── GuestContactUsController.java
│       │
│       └── resources/
│           └── com/example/test_project/
│               ├── LoginStudentWindow.fxml
│               ├── Admin/          # Admin FXML layouts (19 files)
│               ├── Artist/         # Artist FXML layouts (18 files)
│               ├── Customer/       # Customer FXML layouts (14 files)
│               └── Guest/          # Guest FXML layouts (6 files)
│
└── uploads/                        # Runtime image storage
    └── nfts/                       # NFT image uploads
```

---

## ✅ Prerequisites

- **Java 22** (JDK 22 or later)
- **Apache Maven 3.6+**
- **MySQL or MariaDB** running on port **3307**
- A MySQL database named **`art_gallery`** with the required schema tables

---

## ⚙️ Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Nahin197/Virtual-Art-Gallery-AOOP-Course-project.git
cd Virtual-Art-Gallery-AOOP-Course-project
```

### 2. Set Up the Database

1. Start your MySQL/MariaDB server on **port 3307**.
2. Create the database:
   ```sql
   CREATE DATABASE art_gallery;
   ```
3. Import the schema (create the tables listed in the [Database Schema](#database-schema) section) using your preferred SQL client (MySQL Workbench, DBeaver, HeidiSQL, etc.).
4. Ensure the MySQL user `root` with an **empty password** can connect. Alternatively, update the credentials in `DataBaseConnection.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3307/art_gallery";
   private static final String USER = "root";
   private static final String PASSWORD = "";
   ```

### 3. Create Required Directories

The application stores uploaded images in a local `uploads/` directory. Ensure it exists at the project root:

```bash
mkdir -p uploads/nfts
```

### 4. Install Dependencies

Maven will download all dependencies automatically:

```bash
mvn clean install -DskipTests
```

---

## ▶️ Running the Application

```bash
mvn clean javafx:run
```

The application will launch on the **Guest Home Page**. From there, you can log in or register.

> **Default Admin Credentials:** Set these manually in your database (`role = 'admin'`).

---

## 💬 Running the Chat Server

The real-time messaging feature requires a separate **Chat Server** to be running before users start chatting.

```bash
# Compile first if not already done
mvn compile

# Run the chat server (in a separate terminal)
java -cp target/classes com.example.test_project.ChatServer
```

The server listens on **TCP port 12345**. It handles:
- User authentication by ID
- Message routing between sender and receiver
- Persistence of messages to `ChatMessages` table
- Online/offline status broadcasting

---

## 📖 Usage Guide

### For Customers
1. Launch the app → Click **Login** → Enter credentials.
2. From the **Home** page, view your wallet balance and navigate via the menu.
3. Go to **Paintings** → Browse by category → Click **Add to Cart**.
4. Go to **Cart** → Review items → **Checkout**.
5. Go to **Auctions** → View active auctions → Place bids.
6. Go to **NFTs** → Browse available NFTs → Purchase directly.
7. Go to **Profile** → View purchased NFTs → **Download** them.
8. Click **Chat with Artist** on any painting card to open a direct message.

### For Artists
1. Login → Land on **Artist Dashboard** (shows artwork count, sold items, revenue).
2. Go to **Add Painting** → Fill in details → Upload image.
3. Go to **Add Auction** → Enter a Painting ID you own → Set start/end dates.
4. Go to **Add NFT** → Fill in blockchain, edition, price → Upload image.
5. Go to **My Paintings** → Edit or remove listings.
6. Check **Orders** and **NFT Orders** for sales.
7. Go to **Withdraw Money** → Request a payout (processed by Admin).
8. Check **Notifications** for reactions and auction results.

### For Admin
1. Login → **Admin Dashboard** automatically processes all pending/expired auctions.
2. Use the menu to manage **Artists**, **Customers**, **Artworks**, and **Events**.
3. Review **Credit Requests**, **Withdrawal Requests**, and **Auction Requests**.
4. View **Orders** and platform **Inquiries**.

---

## ⚠️ Known Issues & Notes

- **Hardcoded image paths**: Some controllers contain absolute Windows paths (e.g., `D:\\Trimester\\8th\\AOOP\\...`) for icon/placeholder images. Update these to relative paths or use classpath resources if running on a different machine.
- **Password storage**: Passwords are currently stored in **plain text**. For any real-world deployment, use a hashing algorithm such as BCrypt.
- **Database port**: The default MySQL port is 3306, but this project uses **3307**. Ensure your server is configured accordingly.
- **Chat authentication**: The `ChatServer` currently authenticates by user ID without token verification (TODO noted in code). Do not expose this server publicly.
- **Image upload paths**: Uploaded paintings/NFT images are saved relative to the working directory (`uploads/`). Ensure the app is launched from the project root.

---

## 👨‍💻 Authors

Developed as an **Advanced Object-Oriented Programming (AOOP)** course project.

- **Developer** — [@Nahin](https://github.com/Nahin197)
- **Developer** — [@Montasir](https://github.com/Irfanuzzaman-Montasir)
- **Developer** — [@Imran](https://github.com/imran-bhuiyan)

- **Repository**: [Nahin197/Virtual-Art-Gallery-AOOP-Course-project](https://github.com/imran-bhuiyan/Virtual-Art-Gallery)

---

## 📄 License

This project is for academic purposes. All rights reserved by the original authors.
