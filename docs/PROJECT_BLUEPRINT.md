# ScanLearn Project Blueprint

## Implementation Checklist

- [x] Write project blueprint and target architecture
- [x] Replace blind scanner auto-navigation with confirmation flow
- [x] Add suggested matches screen after scanning
- [x] Add manual object selection after weak or uncertain detection
- [x] Save scan attempts to Firebase
- [x] Pass `scan_attempt_id` through lesson, quiz, and result flow
- [x] Save submission analytics fields for confidence and manual correction
- [x] Save quiz attempts as a first-class Firebase node
- [x] Show first teacher analytics using scan attempts, quiz attempts, and submissions
- [x] Add a teacher student-detail insights screen
- [x] Add smart weak-topic insights by object and category
- [x] Add student-detail actions for follow-up mission planning and low-confidence scan review
- [x] Save teacher follow-up missions to Firebase and assign them by section
- [x] Move missions fully from local storage to Firebase
- [x] Make teacher-created Firebase objects the only active content source
- [x] Add teacher object library screen with published and archived filters
- [x] Add object detail screen with edit, publish or archive, and object-level analytics
- [x] Add teacher analytics screens for scan quality and object confusion
- [x] Expand teacher dashboard into objects, missions, students, analytics
- [x] Add teacher-side mission create, edit, archive, and section-assignment workflow
- [ ] Add mission assignment and tracking by section
- [ ] Add category-aware scanning rules before model execution
- [ ] Evaluate Pl@ntNet, Google Vision, and custom-model integration

## Recommendation To Proceed

Based on the current progress, these are the best next actions to keep the project aligned and moving forward.

### Things To Do Next

- Build `Test Knowledge` mode so the student side fully matches the intended three-mode structure
- Add a teacher review queue for low-confidence scans, manual corrections, and hard-to-match objects
- Improve mission tracking so teachers can see assignment progress by section and by mission
- Add mission editing details like required score, clearer completion rules, and optional difficulty
- Strengthen teacher object management with richer object metadata such as aliases, keywords, and multiple images
- Add section-aware or category-aware scanner restrictions before recognition runs
- Add better student progress summaries such as mastered objects, weak categories, and recent mission completions

### Things To Improve

- Improve recognition reliability by reducing dependence on raw ML Kit guesses
- Replace simple object matching with stronger matching based on aliases, keywords, and category context
- Improve content quality for elementary students by keeping lessons shorter, simpler, and more visual
- Improve mission design so missions feel like guided learning tasks instead of only checklists
- Improve analytics readability so teachers can act faster without interpreting too much raw data
- Improve teacher workflows with fewer clicks between insight, content editing, and mission assignment
- Improve consistency of text and UI polish by cleaning up remaining odd characters and older placeholder strings
- Improve technical structure over time by moving more logic from activities into repositories or use-case helpers

## 1. Product Direction

ScanLearn should become a guided learning app for elementary students, not just an image detector.

The scanner is a learning entry point, not the final source of truth.

The core learning loop should be:

1. Student sees something in the real world.
2. Student scans or selects an image.
3. App suggests likely matches.
4. Student confirms the correct object.
5. App teaches the object using short, age-appropriate content.
6. Student answers a short quiz.
7. Progress is saved for the teacher.

This structure protects the learning experience from false positives while still keeping the app fun and interactive.

## 2. Current State Summary

The current codebase already includes:

- Student and teacher authentication
- Firebase Realtime Database integration
- Scanner flow using ML Kit
- Object detail and quiz screens
- Teacher object creation
- Mission and progress screens

Main constraints in the current implementation:

- Scanner fallback can return unrelated objects when recognition is weak
- Learning content is split between Firebase and hardcoded templates
- Missions are stored locally in shared preferences instead of Firebase
- Student progress is tracked at a basic submission level only
- Teacher dashboard is focused on viewing counts, not managing structured learning

Relevant current files:

- `app/src/main/java/com/example/scanlearn/activities/ScannerActivity.kt`
- `app/src/main/java/com/example/scanlearn/utils/ObjectClassifier.kt`
- `app/src/main/java/com/example/scanlearn/models/LearningData.kt`
- `app/src/main/java/com/example/scanlearn/services/RealtimeDbService.kt`
- `app/src/main/java/com/example/scanlearn/activities/TeacherActivity.kt`
- `app/src/main/java/com/example/scanlearn/activities/MissionsActivity.kt`
- `app/src/main/java/com/example/scanlearn/services/StorageService.kt`

## 3. Final Product Structure

### Student Modes

The student side should have three clear modes:

#### Explorer Mode

Purpose:
Free discovery through scanning and learning.

Flow:

1. Student opens Explorer.
2. Student chooses a category or scans from all categories.
3. App analyzes the image.
4. App shows 1 to 3 suggested matches.
5. Student confirms the correct object.
6. Student reads short content.
7. Student takes a mini quiz.
8. Result is saved as learning evidence.

Explorer is best for curiosity and casual practice.

#### Mission Mode

Purpose:
Structured, teacher-guided learning.

Flow:

1. Student opens assigned missions.
2. Student chooses an active mission.
3. Mission shows objective, target objects, and completion rules.
4. Student scans or selects target objects one by one.
5. Each confirmed object unlocks its mission quiz step.
6. Mission progress updates after each completed object.
7. Mission is marked complete only when all required tasks are done.

Mission Mode is the academic backbone of the app.

#### Test Knowledge Mode

Purpose:
Assessment without depending on camera accuracy.

Flow:

1. Student chooses category, mission, or teacher-made quiz set.
2. Student answers questions directly.
3. App scores the result.
4. Result is stored and visible to the teacher.

This mode ensures the app remains useful even when recognition is poor or internet is weak.

### Teacher Responsibilities

Teacher responsibilities should be clearly scoped:

- Create and manage learning objects
- Upload image references for objects
- Write age-appropriate descriptions and facts
- Create quizzes for each object
- Create missions with clear objectives
- Assign missions by section or grade group
- Review student progress and quiz performance
- Review low-confidence scans or unresolved objects if needed

### Student Responsibilities

Student actions should stay simple:

- Scan or choose an image
- Confirm the correct object
- Read and listen to short lessons
- Take a mini quiz
- Complete missions
- Review progress and badges

## 4. Recommended Information Architecture

### Student Home

The student home should become:

- Explorer Mode
- Mission Mode
- Test Knowledge
- My Progress
- My Achievements

Optional later:

- Daily Challenge
- Recent Discoveries

### Teacher Home

The teacher dashboard should become:

- Learning Objects
- Missions
- Students
- Sections
- Results and Analytics
- Review Queue

This is a better long-term structure than keeping everything inside one student-count dashboard.

## 5. Screen Flow Blueprint

### Student Flow

#### Authentication

`Splash -> Auth -> Role Routing`

- Teacher goes to teacher dashboard
- Student goes to student home

#### Explorer Flow

`Home -> Explorer -> Scanner -> Recognition Result -> Confirmation -> Object Lesson -> Mini Quiz -> Result -> Progress Saved`

Important behavior:

- If confidence is low, do not auto-open a lesson
- Show suggestions instead
- Allow "Choose manually" from the current category

#### Mission Flow

`Home -> Mission List -> Mission Detail -> Start Mission -> Scanner -> Confirmation -> Object Lesson -> Mission Quiz Step -> Mission Progress Update -> Mission Complete`

#### Test Knowledge Flow

`Home -> Test Knowledge -> Select Category or Set -> Quiz -> Result -> Progress Saved`

#### Progress Flow

`Home -> My Progress -> Completed Missions / Quiz History / Skills Progress`

### Teacher Flow

#### Object Management

`Teacher Home -> Learning Objects -> Add Object / Edit Object / Archive Object`

#### Mission Management

`Teacher Home -> Missions -> Create Mission -> Add Required Objects -> Set Rules -> Assign to Sections`

#### Student Monitoring

`Teacher Home -> Students -> Section -> Student Detail -> Attempts / Scores / Missions / Scan History`

#### Review Queue

`Teacher Home -> Review Queue -> Low Confidence Detections / Unmatched Scans / Content Gaps`

## 6. Firebase as the Single Source of Truth

The app should stop using hardcoded learning content for production learning data.

`LearningData.kt` should now remain one of these only:

- seed data for development
- fallback defaults for first launch
- migration helper for populating Firebase

It should no longer be treated as the active source of classroom content once teacher-managed content is in place.

### Recommended Firebase Structure

Use Firebase Realtime Database with clearer top-level nodes.

```text
users/
  {userId}/
    id
    name
    email
    role
    sectionId
    gradeLevel
    status
    createdAt

sections/
  {sectionId}/
    id
    name
    gradeLevel
    adviserId
    active

objects/
  {objectId}/
    id
    name
    categoryId
    description
    facts[]
    imageUrls[]
    keywords[]
    aliases[]
    difficulty
    createdBy
    status
    createdAt
    updatedAt

object_quizzes/
  {objectId}/
    questions/
      {questionId}/
        prompt
        options[]
        correctIndex
        explanation

missions/
  {missionId}/
    id
    title
    description
    categoryId
    sectionIds[]
    objectIds[]
    passingScore
    requiredObjectCount
    active
    createdBy
    createdAt
    updatedAt

student_missions/
  {studentId}/
    {missionId}/
      status
      completedObjectIds[]
      progressPercent
      startedAt
      completedAt
      lastUpdatedAt

scan_attempts/
  {studentId}/
    {attemptId}/
      imageRef
      mode
      categoryContext
      predictionSource
      suggestions[]
      selectedObjectId
      confidence
      status
      createdAt

quiz_attempts/
  {studentId}/
    {attemptId}/
      objectId
      missionId
      mode
      score
      totalQuestions
      answers[]
      completedAt

learning_records/
  {studentId}/
    {recordId}/
      objectId
      mode
      reflection
      quizAttemptId
      scanAttemptId
      createdAt

analytics/
  sections/
  students/
  objects/
```

### Why This Structure Is Better

- Objects are independent from quizzes
- Missions are reusable and assignable
- Student mission progress is separate from mission definitions
- Scans are stored as attempts, not just raw counts
- Teachers can review recognition quality later
- Learning evidence becomes easier to analyze

## 7. Recommended Data Models

The current models should evolve into clearer domain models.

### Core Models

#### User

Add:

- `sectionId`
- `gradeLevel`
- `status`
- `createdAt`

#### LearningObject

Add:

- `aliases: List<String>`
- `keywords: List<String>`
- `imageUrls: List<String>`
- `difficulty: String`
- `status: String`
- `createdBy: String`
- `updatedAt: String`

#### Mission

Replace the current simple mission model with:

- `categoryId`
- `sectionIds`
- `objectIds`
- `requiredObjectCount`
- `passingScore`
- `active`
- `createdBy`

#### ScanAttempt

Create a new model:

- `id`
- `studentId`
- `mode`
- `categoryContext`
- `predictionSource`
- `suggestions`
- `selectedObjectId`
- `confidence`
- `status`
- `createdAt`

#### QuizAttempt

Create a new model:

- `id`
- `studentId`
- `objectId`
- `missionId`
- `mode`
- `score`
- `totalQuestions`
- `answers`
- `completedAt`

#### LearningRecord

This should replace the overloaded meaning of `Submission`.

- `id`
- `studentId`
- `objectId`
- `reflection`
- `mode`
- `scanAttemptId`
- `quizAttemptId`
- `createdAt`

## 8. Recognition Strategy

## Goal

Recognition should support learning without pretending to be perfectly accurate.

### Rules for the New Scanner

1. Never open a lesson from a random fallback.
2. Never silently map weak predictions to unrelated objects.
3. Always keep the student in control of final confirmation.
4. Save recognition confidence and suggestion history for analysis.
5. Filter suggestions by context whenever possible.

### Recommended Recognition Pipeline

#### Stage 1: Context Selection

Before scanning, define the recognition context:

- all categories
- animals
- plants
- classroom
- mission-restricted objects

This immediately reduces false positives.

#### Stage 2: Fast Recognition

Run a first-pass model:

- ML Kit on-device for fast labeling
- or a specialized online service depending on category

#### Stage 3: Candidate Matching

Convert raw labels into suggested `objectId` candidates using:

- teacher-defined aliases
- keywords
- category filters
- object image references

#### Stage 4: Confidence Rules

Recommended handling:

- High confidence: show top suggestion first, still require confirmation
- Medium confidence: show top 3 suggestions
- Low confidence: show "We are not sure" and manual selection

#### Stage 5: Student Confirmation

The student chooses:

- suggested object A
- suggested object B
- suggested object C
- none of these

Only after that should the lesson open.

### Recommended Provider Strategy

#### Phase 1

Keep ML Kit, but use it only for suggestions.

#### Phase 2

Plug in category-specific providers:

- Plants: Pl@ntNet
- Generic image labels: Google Cloud Vision
- Custom school content: custom model later

#### Phase 3

Train a custom classifier using your own approved object dataset.

Best long-term fit:

- Use a custom model for classroom objects and your exact curriculum objects
- Use specialized APIs only where they clearly outperform generic models

## 9. Teacher Content Strategy

Teacher-created content should drive the real learning database.

That means:

- Teacher uploads or creates an object
- Teacher attaches multiple sample images if possible
- Teacher writes short facts
- Teacher writes quiz questions
- Object becomes available to students after review or publish

### Content Writing Rules for Elementary Students

For every object:

- Description should be 2 to 4 short sentences
- Facts should be short, concrete, and age-appropriate
- Quiz questions should test understanding, not memorization only
- Avoid long paragraphs
- Use simple English or bilingual support later

### Suggested Object Content Template

- Name
- Category
- What is it?
- Where can we find it?
- Why is it important?
- 3 to 5 facts
- 3 quiz questions

## 10. Mission Design Strategy

Missions should teach with structure, not just scanning tasks.

### Good Mission Pattern

- Title: "Plants Around Our School"
- Objective: Find and learn 3 plant objects
- Required objects: `santan`, `oregano`, `makahiya`
- Completion rule: finish object quiz with at least 2 out of 3 correct
- Reward: badge, star, or completion mark

### Mission Types

- Find specific objects
- Find any 3 objects from a category
- Compare two similar objects
- Finish a quiz-only mission
- Teacher challenge for the week

### Mission Completion Rule

A mission should be complete only when:

- required objects were confirmed
- required quizzes were passed
- optional reflection was submitted if enabled

## 11. Student Progress Strategy

Progress should not just show scan count.

It should show:

- objects learned
- quizzes completed
- average score
- active missions
- completed missions
- weak topics
- recent activity

### Teacher View Should Show

- section performance
- student mission completion
- quiz accuracy by object
- low-confidence recognition cases
- objects that often cause confusion

## 12. Technical Architecture Recommendation

The current project is activity-driven and workable for now, but the next implementation should move toward clearer layers.

### Suggested App Layers

- UI layer: activities, adapters, view binding
- Domain layer: recognition logic, mission rules, scoring rules
- Data layer: Firebase services, local cache, repositories

### Practical Refactor Direction

You do not need a huge rewrite immediately.

Recommended gradual refactor:

1. Keep current activities.
2. Add repository classes for objects, missions, attempts, and users.
3. Move matching logic out of activities.
4. Keep `RealtimeDbService` as a low-level Firebase wrapper.
5. Create use-case style helpers for scan confirmation, mission progress, and quiz submission.

## 13. Phased Development Roadmap

### Phase 1: Stabilize the Learning Core

Goal:
Make the current prototype safe and structurally correct.

Tasks:

- Remove random scanner fallback behavior
- Add recognition result screen with suggestions
- Add manual object selection
- Keep teacher objects as the preferred data source
- Stop relying on hardcoded templates for active content
- Create Firebase mission nodes
- Save real scan attempts and quiz attempts

Definition of done:

- No wrong object lesson opens automatically from a weak scan
- Teacher content is loaded from Firebase
- Missions and progress are stored online

### Phase 2: Strengthen Teacher Workflows

Goal:
Make the app usable in real classroom management.

Tasks:

- Expand teacher dashboard into content, missions, and analytics sections
- Allow editing existing objects
- Allow mission creation and assignment by section
- Add student detail view
- Add review queue for poor recognition results

Definition of done:

- Teacher can run learning content without touching code
- Missions can be assigned and tracked by section

### Phase 3: Improve Recognition Accuracy

Goal:
Reduce false positives through context and specialization.

Tasks:

- Add category-aware scanning
- Add confidence thresholds
- Integrate Pl@ntNet for plants
- Evaluate Google Vision as generic backup
- Start collecting training images for a custom model

Definition of done:

- Suggestions are meaningfully better than current ML Kit-only behavior
- Recognition quality is measurable from saved attempt data

### Phase 4: Increase Learning Value

Goal:
Make the app genuinely effective for elementary learning.

Tasks:

- Add badges or achievements
- Add read-aloud support
- Add bilingual content if needed
- Add adaptive quizzes for weak topics
- Add teacher insights by topic and object

Definition of done:

- App is useful as both discovery and structured teaching material

## 14. Immediate Build Order

This is the recommended order for actual implementation from the current codebase.

1. Replace scanner auto-navigation with a recognition result screen.
2. Add suggestion and manual confirmation flow.
3. Add new Firebase models for missions, scan attempts, and quiz attempts.
4. Move missions out of local storage into Firebase.
5. Convert teacher-created objects into the only active object source.
6. Expand teacher dashboard to manage objects and missions separately.
7. Improve analytics and progress views.
8. Integrate specialized recognition providers.

## 15. Decisions to Keep Us Aligned

These decisions should stay fixed unless there is a strong reason to change them:

- The app teaches through confirmation, not blind detection
- Firebase is the production source of truth
- Mission Mode is the main structured-learning mode
- Test Knowledge exists so learning does not depend on camera accuracy
- Teacher-managed content is more important than model confidence
- Recognition quality must be measurable through saved attempt data

## 16. Recommended Next Implementation Ticket

The single best next ticket is:

`Build a confirmation-based scanner flow backed by Firebase objects`

That ticket should include:

- remove random fallback in scanner resolution
- return top suggestions instead of opening object details immediately
- add manual object picker
- save scan attempts with confidence and selected object
- continue to lesson only after confirmation

This one change will improve both product quality and classroom trust more than any other step right now.
