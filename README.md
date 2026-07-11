# 🏦 Libarty Bank

[![Java](https://img.shields.io/badge/Java-11+-orange?style=flat-square&logo=java)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue?style=flat-square&logo=react)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)

Full-stack banking application built with **Java Spring Boot** (backend) and **JavaScript React** (frontend) as an OOP final project.

---

## ✨ Features

- 🔐 User authentication & authorization
- 💳 Account management with multiple accounts
- 💸 Fund transfers between accounts
- 📊 Transaction history
- 👤 Role-based access control
- 📱 Responsive web interface

---

## 🛠️ Tech Stack

### Backend
- **Language**: Java 11+
- **Framework**: Spring Boot
- **Build**: Maven

### Frontend
- **Framework**: React 18+
- **Styling**: CSS
- **Package Manager**: npm

### DevOps
- **Containerization**: Docker & Docker Compose

---

## 📁 Project Structure

```
├── Banking_System/              # Backend (Java Spring Boot)
│   ├── src/main/java/          # Backend source code
│   ├── src/main/resources/     # Configuration files
│   └── pom.xml                 # Maven dependencies
│
├── banking_system_front/        # Frontend (React)
│   ├── src/                    # React components & pages
│   ├── public/                 # Static assets
│   └── package.json            # npm dependencies
│
├── docker-compose.yml           # Docker compose configuration
└── .github/workflows/           # CI/CD configuration
```

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose installed

### Setup & Run

```bash
# Start both backend and frontend
docker compose up --build

# Application will be available at:
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
```

---

## 📡 Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/login` | User login |
| `GET` | `/accounts` | List all accounts |
| `POST` | `/transactions/transfer` | Transfer funds between accounts |
| `GET` | `/transactions` | View transaction history |

---

## 🧪 Testing

```bash
cd Banking_System
./mvnw test
```

---

## 💡 Used Paradigms

### 🎯 Object-Oriented Programming (OOP)
- **Encapsulation** - Data hiding with private/public access modifiers
- **Inheritance** - Code reuse through class hierarchies
- **Polymorphism** - Method overriding and interface implementation
- **Abstraction** - Interfaces and abstract classes for contracts
- **SOLID Principles** - Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion

### ⚡ Aspect-Oriented Programming (AOP)
- **Transactions** - Cross-cutting concern for data consistency
- **Permissions** - Aspect-based security checks
- **Security** - Authentication and authorization aspects
- **Activation Check** - Validation before account operations

---





## 👥 Contributors

Created as a final project for the Object-Oriented Programming course at **Free University of Tbilisi**.

---

<div align="center">

Made by the Grade-Oriented-Programming Team: 
  Giga Beradze,
  Davit Enukidze,
  Zaqaria Beridze,
  Guga Gugutishvili,
  Giorgi Michitashvili
  

</div>
