# ScanLearn App Overview

## What This App Is

ScanLearn is a learning app for elementary students.

Its main goal is to help students learn about real-world objects by combining scanning, simple lessons, quizzes, teacher-made content, and teacher guidance.

The app is not meant to be just an object detector. The scanner is only the starting point. The real purpose is learning.

## Tech Stack

The app is built with the following main technologies:

- Android Studio for development
- Kotlin for the app code
- XML layouts for the user interface
- View Binding for connecting layouts to activities
- Firebase Authentication for login and account management
- Firebase Realtime Database for app data and chat persistence
- Google ML Kit for image labeling
- ML Kit Object Detection for object selection and recognition support
- CameraX for camera access
- Glide for image loading
- Gson for local object serialization where needed
- Material Components for Android UI elements

In simple terms, this means the app is a native Android project that combines Firebase, camera tools, and on-device machine learning.

## Main Purpose

The app helps students:

- discover objects around them
- learn simple facts about those objects
- answer short quizzes
- complete teacher-guided missions
- build progress that teachers can review

The app helps teachers:

- create and manage learning objects
- make missions for students
- monitor student performance
- review scan quality and quiz results
- communicate with students through chat

## Main Roles

There are two main account types in the app.

### Student

A student can:

- log in
- open the student home screen
- use Explorer Mode
- open Mission Mode
- use Challenge Mode
- view progress
- chat with teachers

A student cannot chat with another student.

### Teacher

A teacher can:

- log in
- open the teacher dashboard
- manage objects
- manage missions
- view students
- view analytics
- chat with teachers and students

## Core Idea Of The Learning Flow

The app follows a guided learning flow:

1. The student sees something in real life.
2. The student scans it or selects an image.
3. The app suggests possible matches.
4. The student confirms the correct object.
5. The app shows a short learning lesson.
6. The student answers a quiz.
7. The result is saved for teacher review and analytics.

This design is important because it reduces wrong results from the scanner. The app does not want to blindly trust recognition.

## Main Student Screens

### 1. Authentication

Users first enter the app through the authentication flow.

Flow:

`Splash -> Login/Register -> Role-based Home`

If the account is a teacher, the app opens the teacher dashboard.

If the account is a student, the app opens the student home screen.

### 2. Student Home

The student home is the main starting point for learning.

It currently gives access to:

- Explorer Mode
- Mission Mode
- Challenge Mode
- Progress
- Chat

The chat button is a floating button. If there are unread messages, it shows a count like `Chat 1`.

### 3. Explorer Mode

Explorer Mode is for open learning and discovery.

Flow:

`Home -> Scanner -> Recognition Result -> Confirmation -> Object Lesson -> Quiz -> Result`

This mode is best when a student wants to freely scan and learn.

### 4. Mission Mode

Mission Mode is for teacher-guided learning tasks.

Flow:

`Home -> Missions -> Mission Details -> Learning Task -> Quiz -> Progress Update`

Teachers can create missions and assign them to sections.

### 5. Challenge Mode

Challenge Mode is used for a faster or more quiz-like experience.

It supports more active practice and can feel more game-like compared to Explorer Mode.

### 6. Progress

Students can open the progress screen to review their activity and performance.

This helps them see what they have completed and how they are doing over time.

## Main Teacher Screens

### Teacher Dashboard

The teacher dashboard is the teacher home screen.

It gives quick access to:

- learning objects
- missions
- students
- analytics
- chat

The teacher also has a floating chat button with unread message count.

### Teacher Objects

Teachers can create and manage the object library.

An object usually includes:

- object name
- category
- image
- description
- facts
- quiz questions
- status such as published or archived

### Teacher Missions

Teachers can:

- create missions
- edit missions
- archive or reactivate missions
- assign mission goals to students or sections

Missions help turn the app into a classroom tool instead of only a scanner app.

### Teacher Students

Teachers can view students and inspect individual student progress.

This helps teachers see:

- quiz performance
- submissions
- scan history
- weak topics
- low-confidence scans

### Teacher Analytics

The analytics side gives teachers a clearer view of how students and content are performing.

This includes:

- object-level analytics
- category-level analytics
- scan confidence trends
- quiz score trends

## Chat System

The app includes a built-in chat system for safe school communication.

### Chat Rules

- teacher to teacher is allowed
- teacher to student is allowed
- student to teacher is allowed
- student to student is not allowed

This rule is important for safety because the app is meant for elementary students.

### How Chat Works

- Chat is opened from a floating button on the home screen.
- Students see teachers available in the database.
- Teachers see users they are allowed to message.
- Chats are one-to-one only.
- Messages are stored in Firebase so they remain even after the app is closed.
- Unread messages increase the floating chat count.
- Opening a conversation marks that conversation as read.

## Firebase Use In The App

Firebase Realtime Database is the main online data source.

Firebase Authentication is also used for account login and registration.

The app stores things like:

- users
- objects
- missions
- student mission progress
- scan attempts
- quiz attempts
- submissions
- chat conversations
- chat messages

This allows the app to keep student and teacher data synchronized across sessions.

## Technical Structure

The current app is mostly activity-based.

That means each major screen is handled by its own Android activity.

The structure currently relies on:

- activities for screen logic
- adapters for list-based UI
- model classes for app data
- service classes for Firebase and storage access
- XML layout files for UI screens

This structure is practical for the current stage of the project, even if it may later be refactored into a more layered architecture.

## Why The App Uses Confirmation

The scanner is useful, but it is not always perfect.

Because of that, the app uses a confirmation-based flow.

That means:

- the app can suggest matches
- the student still chooses the correct object
- the lesson opens only after confirmation

This improves trust, reduces wrong lessons, and creates cleaner learning records.

## What Makes This App Different

ScanLearn is different from a basic scanner app because it combines:

- recognition
- lesson content
- quizzes
- missions
- analytics
- teacher control
- safe teacher-student communication

The goal is not only to identify objects.

The goal is to support real classroom learning.

## Short Summary

In simple terms, ScanLearn is a classroom learning app where:

- students scan and learn
- teachers manage content and missions
- results are saved
- progress is measurable
- communication stays focused on learning

It is designed to be safe, guided, and useful for elementary education.
