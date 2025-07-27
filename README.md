# Webtube Backend

This is the **backend** of the Webtube FullStack project – a Spring Boot and MySQL-powered REST API that supports the campus media website of **De La Salle Araneta University**.

The backend manages user authentication, content creation, comment moderation, and integrates AI capabilities to enhance the content workflow and user interaction.

---

## ⚙️ Technologies Used

- **Java 17**  
- **Spring Boot** (Spring MVC, Spring Security, Spring Data JPA)  
- **MySQL**  
- **OpenAI API** – For content summarization and AI-assisted post generation  
- **Sentiment Analysis** – For comment moderation and bad word filtering

---

## 🧠 AI-Powered Features

- **Content Summarization**  
  Automatically summarizes long articles using OpenAI to improve readability and user engagement.

- **AI-Assisted Content Creation**  
  Generates suggested **titles** and **descriptions** for posts using GPT.

- **Comment Moderation**  
  Filters and analyzes comments with sentiment analysis or bad word detection to maintain a safe and respectful media platform.

---

## 📁 Project Structure (Simplified)

<img width="486" height="880" alt="image" src="https://github.com/user-attachments/assets/03649d8b-2a6c-4af5-b053-6b19d7aa96a0" />

---

## 🔐 Authentication & Roles

Webtube uses **JWT-based authentication** and role-based authorization.

### Supported Roles:
- `ROLE_ADMIN` – Full access to manage users, approve or reject content, and configure system settings.
- `ROLE_AUTHOR` – Can create, edit, and manage their own content, and view engagement on their posts.

---

## 🚀 Running the Backend Locally

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/Webtube-FullStack.git
   cd Webtube-FullStack/backend
Configure the database and API keys
Edit src/main/resources/application.properties:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/webtube_db
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password

openai.api.key=YOUR_OPENAI_API_KEY
Run the application
Use your IDE or run via terminal:


./mvnw spring-boot:run
📬 Sample API Endpoints
Method	Endpoint	Description
POST	/api/auth/signup	Register a new user
POST	/api/auth/signin	Login and receive JWT
GET	/api/posts	Retrieve all published posts
POST	/api/posts	Create a new post (with AI support)
POST	/api/comments	Submit a comment (with moderation)

🛡️ Security
JWT-based stateless authentication

Role-based access control (ROLE_ADMIN, ROLE_AUTHOR)

CORS configured to allow frontend communication
