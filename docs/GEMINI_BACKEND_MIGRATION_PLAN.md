# Gemini Backend Migration Plan

## Goal
Move ScanLearn teacher AI from direct Android-to-Firebase AI calls to a safer backend gateway that uses the Gemini API via Google AI Studio without exposing provider keys in the APK.

Target architecture:

`Android app -> ScanLearn AI gateway -> Gemini API (Google AI Studio) -> chosen model`

This keeps the teacher workflow stable:
- AI Draft
- Simplify
- AI Quiz
- Generate Mission
- Analytics Summary

Only the provider path changes underneath.

## Why We Should Migrate This Way
The current app calls Firebase AI Logic directly from Android in [GeminiTeacherCopilotService.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/GeminiTeacherCopilotService.kt>). That is fine for early prototyping, but a backend gateway gives us cleaner provider control, request validation, usage limits, and future rollout flexibility.

Google AI Studio is a better fit for this next phase because it gives us:
- direct Gemini API access
- stable model selection without provider routing changes
- easier backend-side logging and rate limiting
- a cleaner path if we later want to move from prototype traffic to managed production traffic

Important note:
Google AI Studio API keys must stay off the Android client. We should not ship a Gemini API key inside the app.

## What You Should Do Now
Create a Gemini API key in Google AI Studio, but use it only for the backend gateway.

Recommended key setup:
- Name: `ScanLearn Dev Gateway`
- Restrict it if practical for your backend environment
- Store it outside the Android project
- Rotate any previously exposed key before using the backend gateway

Do not:
- hardcode it in Kotlin
- put it in `BuildConfig`
- commit it to the repo
- call Google AI Studio directly from the app

## Current Code Impact
Current direct provider entry point:
- [GeminiTeacherCopilotService.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/GeminiTeacherCopilotService.kt>)

Current app-side abstraction scaffold:
- [TeacherCopilotService.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/TeacherCopilotService.kt>)
- [TeacherCopilotServiceFactory.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/TeacherCopilotServiceFactory.kt>)
- [BackendTeacherCopilotService.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/BackendTeacherCopilotService.kt>)

Current teacher features already wired to the abstraction:
- Lesson Studio
- Mission editor
- Teacher analytics

That is good news, because we already have one app-facing contract. We should preserve that contract and replace only the implementation behind it.

## Recommended Migration Phases
### Phase A: Keep App Behavior Stable
Do not change teacher UI flows first.

Keep these actions exactly the same:
- teacher taps a button
- app receives generated content
- teacher reviews before publish
- AI governance logging still runs

### Phase B: Keep The App-Side AI Abstraction
The app should continue using:

- `TeacherCopilotService`
- `generateLessonDraft(...)`
- `simplifyLesson(...)`
- `generateQuizActivities(...)`
- `generateMissionDraft(...)`
- `summarizeAnalytics(...)`

Implementations should be:
- `GeminiTeacherCopilotService` for the current Firebase fallback path
- `BackendTeacherCopilotService` for the Gemini backend gateway path

This lets us switch provider plumbing without rewriting activities.

### Phase C: Build The Backend Gateway
Create a small backend service with endpoints like:

- `POST /teacher-ai/lesson-draft`
- `POST /teacher-ai/simplify`
- `POST /teacher-ai/quiz`
- `POST /teacher-ai/mission`
- `POST /teacher-ai/analytics-summary`

The backend should:
- authenticate the app or teacher session
- validate request payloads
- inject the Gemini API key server-side
- call the Gemini API
- normalize the response into ScanLearn's expected format
- log provider/model usage
- return only the needed result to the app

### Phase D: Connect Backend To Gemini API
Use Gemini API from the backend with your server-side key and the Gemini model you choose first, such as:

- `gemini-2.5-flash`

This should happen only on the server.

### Phase E: Cut Over By Environment
Suggested rollout:
- debug/dev: backend Gemini path
- fallback: current Firebase AI path
- production later: backend Gemini path only

This gives us a safe rollback while testing.

## Suggested Backend Tech Choices
Pick the lightest backend you can maintain. Good options:
- Firebase Cloud Functions
- Cloud Run
- a small Node/Express service
- a small FastAPI service

Best fit for the current app:
- Firebase Functions if you want to stay near the current Firebase stack
- Cloud Run if you want cleaner service separation and more control

## Request/Response Shape
Normalize provider output at the backend so the Android app stays simple.

Example lesson draft response:

```json
{
  "title": "Properties of Rocks",
  "objective": "Describe simple properties of rocks found around us.",
  "summary": "Rocks have different colors, sizes, and textures. Some rocks are smooth while others are rough.",
  "rawText": "..."
}
```

Example quiz response:

```json
{
  "activities": [
    {
      "type": "multiple_choice",
      "prompt": "Which one is rough?",
      "instructions": "Choose the best answer.",
      "options": ["Pebble", "Sand", "Rock", "Leaf"],
      "answer": "Rock"
    }
  ]
}
```

## Security Rules
Minimum security baseline:
- Gemini API key only on backend
- rate limit teacher AI endpoints
- basic auth/session validation
- request size validation
- response size limits
- provider timeout and retry policy
- usage logging per feature

## Governance Compatibility
The current governance work should remain in place:
- draft variants
- usage logs
- model tracking
- prompt version tracking

We should extend logging with:
- `provider = firebase_ai` or `provider = gemini_backend`
- `provider_model = actual model id`
- latency
- request status

## Best Initial Model Strategy
Do not over-optimize model choice on day one.

Start with:
- one default model for all teacher tasks
- maybe one fallback later

First priority is stable integration, not perfect model routing.

## Recommended Migration Order
1. Create a new Gemini API key in Google AI Studio.
2. Decide backend host: Firebase Functions or Cloud Run.
3. Keep the app-side `TeacherCopilotService` abstraction.
4. Keep Firebase implementation as fallback.
5. Build one backend endpoint first: `lesson-draft`.
6. Test Lesson Studio end to end.
7. Add `simplify`, `quiz`, `mission`, and `analytics-summary`.
8. Switch teacher features one by one.
9. Remove direct app dependency on Firebase AI when stable.

## What We Should Not Do
- Do not embed Gemini API keys in Android.
- Do not switch every AI feature at once.
- Do not remove the existing Firebase path before backend testing passes.
- Do not change teacher UI flows during provider migration.

## Recommendation For This Project
Because we are still finishing the Grade 3 rollout, the safest path is:

1. create the Gemini API key now
2. keep LMS release verification moving
3. build the backend gateway in parallel
4. migrate `AI Draft` first
5. keep Firebase as fallback until the backend path is stable

That gives us safer provider control without destabilizing the core LMS rollout.
