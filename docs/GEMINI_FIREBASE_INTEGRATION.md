# Gemini Integration Guide For ScanLearn

## Recommended Integration Path

For this Android app, the best path is:

- use Firebase AI Logic
- keep Gemini teacher-facing first
- keep teacher review and publishing in control

This fits ScanLearn because the project already uses Firebase Authentication, Firebase Realtime Database, and the teacher-reviewed curriculum flow.

## Why Firebase AI Logic

Use Firebase AI Logic instead of embedding a raw Gemini API key directly in the Android app.

Why:

- it is the recommended mobile/web integration path for Android
- it fits the current Firebase-based app architecture
- it helps keep the Gemini API key off the client app
- it supports App Check and runtime controls for safer production rollout

## What We Added In The App

The current codebase now includes a first teacher copilot layer:

- AI lesson draft in Lesson Studio
- AI lesson simplification in Lesson Studio
- AI quiz/activity generation in Lesson Studio
- AI mission draft support in Mission Builder
- AI analytics summary suggestion in Teacher Analytics

Code entry point:

- [GeminiTeacherCopilotService.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/GeminiTeacherCopilotService.kt>)

Current abstraction layer:

- [TeacherCopilotService.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/TeacherCopilotService.kt>)
- [TeacherCopilotServiceFactory.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/TeacherCopilotServiceFactory.kt>)

This means the app is now ready to keep Firebase AI as fallback while a backend Gemini gateway is built later.

## Firebase Console Setup

You still need to complete the Firebase-side setup for Gemini to work.

### 1. Open Firebase Console

Open your Firebase project for ScanLearn.

### 2. Enable Firebase AI Logic

In Firebase Console:

- open the AI Logic section
- choose the Gemini API provider

Recommended for this project now:

- Gemini Developer API for faster prototyping

Later for school deployment:

- consider Vertex AI Gemini API if you need stricter enterprise controls

### 3. Confirm Android App Registration

Make sure this Android app is correctly registered in the Firebase project and that your current `google-services.json` matches that project.

### 4. Enable App Check

Before production use, enable App Check for the app.

Recommended:

- Play Integrity for release builds
- Debug App Check provider for local testing

### 5. Keep The Model Configurable

Right now the code uses:

- `gemini-2.5-flash`

Later, move the model name to Firebase Remote Config so you can switch models without shipping a new APK.

## Current Teacher AI Features

### Lesson Studio

AI actions now available:

- `AI Draft`
- `Simplify`
- `AI Quiz`

These features help teachers:

- draft a lesson title, objective, and summary
- simplify language for elementary learners
- generate short formative activities

### Mission Builder

AI action now available:

- `Generate Mission With AI`

This creates a mission draft from the selected category, objects, and sections.

### Teacher Analytics

AI action now available:

- `Generate AI Summary`

This turns raw analytics trends into:

- a short overview
- a likely concern
- a suggested next classroom action

## Important Product Rules

Keep these rules fixed:

- AI is an assistant, not the curriculum source of truth
- teachers must review AI output before saving or publishing
- student-facing AI should come later, after teacher-side AI is stable
- lesson quality matters more than speed of generation

## Recommended Next AI Steps

After this first Gemini teacher copilot phase, the next best improvements are:

1. save AI suggestions as draft variants in Firebase
2. add Remote Config for model name and prompt versioning
3. add App Check for abuse protection
4. add usage logging for which teacher feature triggered AI
5. add explicit publish review states for AI-generated content

## Official References

Use these official docs when you continue the setup:

- Firebase AI Logic overview: [firebase.google.com/docs/ai-logic](https://firebase.google.com/docs/ai-logic)
- Firebase AI Logic Android setup: [firebase.google.com/docs/ai-logic/get-started](https://firebase.google.com/docs/ai-logic/get-started)
- Supported Firebase AI Logic models: [firebase.google.com/docs/ai-logic/models](https://firebase.google.com/docs/ai-logic/models)
- Firebase AI Kotlin reference: [firebase.google.com/docs/reference/kotlin/com/google/firebase/ai/FirebaseAI](https://firebase.google.com/docs/reference/kotlin/com/google/firebase/ai/FirebaseAI)
- Google Gemini API libraries: [ai.google.dev/gemini-api/docs/downloads](https://ai.google.dev/gemini-api/docs/downloads)
