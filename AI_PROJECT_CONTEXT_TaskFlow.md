# AI PROJECT CONTEXT -- TaskFlow Backend

## Project Overview

Project Name: TaskFlow

Goal: Build a backend project management system to learn enterprise
backend development with Spring Boot.

Main Objectives: - Understand Spring Boot architecture - Learn
JPA/Hibernate database modeling - Implement REST APIs - Use Redis for
caching - Use Docker for containerized development - Implement
authentication using JWT

------------------------------------------------------------------------

# Technology Stack

Backend - Java 23 - Spring Boot - Spring Data JPA (Hibernate)

Database - MySQL

Caching - Redis

Security - Spring Security - JWT Authentication

DevOps - Docker - Docker Compose

API Documentation - Swagger / OpenAPI

------------------------------------------------------------------------

# Backend Architecture

Layered Architecture:

Client ↓ Controller Layer ↓ Service Layer ↓ Repository Layer ↓ Database

Explanation:

Controller Handles HTTP requests and responses.

Service Contains business logic.

Repository Handles database communication.

Entity Represents database tables.

DTO Transfers data between client and server.

------------------------------------------------------------------------

# Project Folder Structure

src/main/java/com/taskflow

controller service repository entity dto config security exception util

------------------------------------------------------------------------

# Database Design

Tables

users projects tasks comments

Relationships

User 1 ---- n Project Project 1 ---- n Task Task 1 ---- n Comment User 1
---- n Task

Example Schema

Users - id - username - email - password - role

Projects - id - name - description - created_at - owner_id

Tasks - id - title - description - status - priority - project_id -
assigned_user_id

Comments - id - content - task_id - created_at

------------------------------------------------------------------------

# Core Features

User Management - register - login - role management - profile
management

Project Management - create project - update project - delete project -
list projects

Task Management - create task - assign task - update status - update
priority - list tasks by project

Comment System - add comment - edit comment - delete comment - list
comments

------------------------------------------------------------------------

# Redis Requirement

Redis should be used for caching.

Cache targets: - project list - task list

Annotations: @Cacheable @CacheEvict

------------------------------------------------------------------------

# Docker Setup

Containers:

Spring Boot App MySQL Database Redis Cache

Use docker-compose to run all services.

------------------------------------------------------------------------

# Instructions for AI Assistant

You are a senior backend engineer.

Help implement the TaskFlow backend step by step.

Rules: - Follow layered architecture - Use DTO instead of returning
entities directly - Write clean production-style code - Use REST best
practices - Include validation and exception handling

------------------------------------------------------------------------

# Development Roadmap

Day 1 Project setup Spring Boot initialization Docker setup MySQL +
Redis containers

Day 2 Design database schema Create JPA entities Setup relationships

Day 3 Create repositories Create services Implement CRUD APIs

Day 4 Add DTO layer Add validation Implement global exception handling

Day 5 Integrate Redis caching Cache project and task APIs

Day 6 Implement authentication JWT token generation Spring Security
configuration

Day 7 Dockerize Spring Boot application Run full system with Docker
Compose

------------------------------------------------------------------------

# Example AI Request

Example Prompt:

Based on the TaskFlow backend architecture above, please implement the
Project module including:

Entity Repository Service Controller

Follow clean architecture and best practices.
