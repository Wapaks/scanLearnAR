# ScanLearn Grade 3 LMS Execution Plan

## Goal

Build the curriculum-first LMS foundation for one pilot grade first.

Pilot scope:

- Grade 3 only
- Quarter 1 first
- One to two units first
- Reuse the existing app foundation where possible
- Scale to other quarters and grades only after the pilot flow is stable

This plan follows the approved implementation sequence:

- Phase 1A: data model + Firebase schema + repository layer
- Phase 1B: seed Grade 3 Quarter 1 pilot content
- Phase 2A: My Learning Plan + Quarter Hub
- Phase 2B: Unit Detail + Lesson Player + Test Knowledge

## Product Rule For This Pilot

The app should now be structured around:

`Grade -> Quarter -> Unit -> Lesson -> Activity -> Assessment -> Mastery`

The scanner remains valuable, but only as a supporting feature inside the LMS.

## Success Criteria

At the end of these phases, a Grade 3 student should be able to:

1. Log in and open a Grade 3 learning plan
2. See the current quarter and available units
3. Open a unit and follow ordered lessons
4. Complete a lesson with a short activity and quick check
5. Take a quiz-only learning path through Test Knowledge
6. Save lesson progress and mastery data to Firebase

## Phase 1A: Data Model, Firebase Schema, Repository Layer

### Goal

Create the LMS foundation so the app can load curriculum-based academic content instead of relying mainly on object-driven flow.

### Priority Deliverables

- Add curriculum-first domain models
- Add Firebase schema support for curriculum and progress
- Add repository classes for curriculum loading and progress saving
- Keep `RealtimeDbService` as the low-level Firebase wrapper
- Avoid large architecture rewrites for now

### New Core Models

Recommended initial models:

- `CurriculumMap`
- `Quarter`
- `Unit`
- `Lesson`
- `LessonActivity`
- `Competency`
- `StudentLessonProgress`
- `MasteryRecord`

Recommended support fields:

- `gradeLevel`
- `quarterId`
- `unitId`
- `orderIndex`
- `status`
- `estimatedMinutes`
- `linkedObjectIds`
- `masteryStatus`
- `bestScore`
- `lastOpenedAt`

### Firebase Nodes To Add

Add support for these top-level nodes:

- `curriculum_maps`
- `quarters`
- `units`
- `lessons`
- `activities`
- `competencies`
- `student_lesson_progress`
- `mastery_records`

Keep existing nodes such as:

- `missions`
- `scan_attempts`
- `quiz_attempts`
- `objects` or `learning_objects`
- `chat_conversations`
- `chat_messages`

### Repository Layer To Add

Recommended repositories:

- `CurriculumRepository`
- `LessonRepository`
- `ProgressRepository`
- `AssessmentRepository`

Responsibilities:

- `CurriculumRepository`: load curriculum map, quarters, and units
- `LessonRepository`: load lessons, activities, linked objects
- `ProgressRepository`: save lesson completion, quick-check scores, mastery updates
- `AssessmentRepository`: support Test Knowledge question loading and submission

### File Direction

Likely touch points in the current codebase:

- [Models.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/models/Models.kt>)
- [RealtimeDbService.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/services/RealtimeDbService.kt>)
- `app/src/main/java/com/example/scanlearn/repositories/`
- `app/src/main/java/com/example/scanlearn/utils/`

### Definition Of Done

- Grade 3 curriculum entities can be read from Firebase
- Student lesson progress can be saved and loaded
- Lesson and curriculum data no longer depend on hardcoded object-only structure
- Existing student and teacher flows still compile and run

## Phase 1B: Seed Grade 3 Quarter 1 Pilot Content

### Goal

Seed only enough content to validate the LMS flow.

### Scope Rule

Do not build all Grade 3 quarters yet.

Start with:

- Grade 3
- Quarter 1
- One or two units
- A small lesson set with activities and assessments

### Recommended Pilot Theme

Based on the roadmap, Grade 3 Quarter 1 can focus on:

- body parts
- surroundings

Suggested vertical slice:

- 1 curriculum map
- 1 quarter
- 2 units
- 4 to 6 lessons
- 1 to 2 activities per lesson
- 3 to 5 quick-check items per lesson

### Seeding Targets

Seed these content types:

- grade metadata
- quarter metadata
- unit titles and order
- lesson objectives and summaries
- lesson activities
- competencies
- assessment items
- linked scan-supported objects where relevant

### Content Rule

Pilot content should be:

- short
- age-appropriate
- curriculum-aligned
- easy to expand later

### Definition Of Done

- Firebase contains a usable Grade 3 Quarter 1 pilot curriculum
- At least one complete student path exists from quarter to lesson completion
- Lessons can be linked to existing scan-support features when relevant

## Phase 2A: My Learning Plan And Quarter Hub

### Goal

Shift the student home experience from feature-first to curriculum-first.

### My Learning Plan

This should become the main academic dashboard for the student.

Recommended content:

- student grade level
- active quarter
- current progress percent
- next lesson
- active missions
- weak topics later

### Quarter Hub

This should be the entry point into quarter-based learning.

Recommended content:

- quarter title
- unit cards
- unit completion state
- required lessons
- optional explorer tasks later
- mission shortcuts
- test knowledge entry

### Integration Strategy

Do not remove the current app structure too early.

Safer approach:

- keep the current student home
- introduce `My Learning Plan` as the primary academic destination
- make Explorer, Missions, Progress, and Chat remain accessible
- gradually reduce dependence on the old Challenge-first structure

### File Direction

Likely new screens:

- `MyLearningPlanActivity`
- `QuarterHubActivity`

Likely current touch points:

- [HomeActivity.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/activities/HomeActivity.kt>)
- `app/src/main/res/layout/activity_home.xml`

### Definition Of Done

- A Grade 3 student can open My Learning Plan
- The app loads Quarter 1 curriculum data from Firebase
- The student can navigate from learning plan to quarter hub to unit list

## Phase 2B: Unit Detail, Lesson Player, Test Knowledge

### Goal

Build the reusable academic learning flow that will power later grades too.

### Unit Detail

Recommended content:

- unit objective
- ordered lesson list
- lesson status
- estimated learning time
- linked mission if one exists

### Lesson Player

The lesson player should be reusable across lessons.

Recommended sequence:

1. Hook
2. Teach
3. Guided activity
4. Quick check
5. Save progress
6. Offer remediation later if needed

### Test Knowledge

This should replace or evolve the current Challenge Mode.

Recommended behavior:

- quiz-only path
- based on lesson, unit, or quarter scope
- saves assessment results to Firebase
- contributes to mastery and progress

### Integration Rule

Do not break existing scan-based flows.

For now:

- keep Explorer as a support mode
- keep scan confirmation flow
- link scan-supported lessons to relevant objects
- treat Test Knowledge as the academic quiz mode

### File Direction

Likely new screens:

- `UnitDetailActivity`
- `LessonPlayerActivity`
- `TestKnowledgeActivity` or evolution of the current quiz-related flow

Likely current touch points:

- [MissionsActivity.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/activities/MissionsActivity.kt>)
- [QuizActivity.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/activities/QuizActivity.kt>)
- [ProgressActivity.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/activities/ProgressActivity.kt>)
- [AppConstants.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/utils/AppConstants.kt>)

### Definition Of Done

- A student can open a unit and enter an ordered lesson flow
- A lesson can save completion and score data
- Test Knowledge works independently of scanner accuracy
- Progress is tied to lessons and mastery, not only scan history

## Recommended Implementation Order

Build in this order:

1. Add new models to support curriculum and progress
2. Extend Firebase service methods for curriculum nodes
3. Add repositories for curriculum, lessons, progress, and assessments
4. Seed Grade 3 Quarter 1 pilot data
5. Build My Learning Plan
6. Build Quarter Hub
7. Build Unit Detail
8. Build Lesson Player
9. Evolve Challenge into Test Knowledge
10. Connect progress and mastery updates to the new lesson flow

## What We Are Intentionally Not Building Yet

To keep the pilot realistic, do not prioritize these yet:

- all Grades 1 to 6 content
- all Grade 3 quarters at once
- Gemini student-facing tutoring
- full AI remediation inside lessons
- advanced achievements and badges
- bilingual content
- major scanner-provider replacements

These should come after the Grade 3 LMS pilot proves the architecture and flow.

## Risks To Watch

### Risk 1: Building Too Much Content Too Early

If we seed too many lessons before the lesson player is stable, content cleanup will become expensive.

### Risk 2: Keeping Old And New Models Mixed For Too Long

The current code is still strongly object-driven. We should reuse what helps us, but avoid letting the old structure define the LMS architecture.

### Risk 3: UI Sprawl

If we add too many one-off screens, scaling later will be harder. The lesson player and quarter flow should be reusable from the start.

### Risk 4: Weak Progress Semantics

Progress should now mean lesson and mastery progress, not only scans or quiz counts.

## Recommended Next Phase After 1A To 2B

After these phases are complete, the best next move is:

## Phase 3: Teacher Curriculum And Publishing Tools

Why this should come next:

- Teachers need control over curriculum content before AI expands
- The LMS becomes much more useful when teachers can manage lessons, not just objects and missions
- This phase prepares the app for safe Gemini integration later

### Recommended Phase 3 Scope

- Curriculum Dashboard for grade and quarter management
- Lesson Studio for creating and editing lesson content
- Activity and assessment editing
- Teacher publishing workflow
- Review Queue for draft and pending content

## Recommended Phase After That

## Phase 4: Gemini Teacher Copilot

This should be the first AI-heavy phase.

Recommended AI features:

- lesson draft generation
- quiz generation
- mission generation
- lesson simplification
- analytics summary generation

This is safer and more valuable than student-facing AI first.

## Final Recommendation

Proceed now with:

- Phase 1A
- Phase 1B
- Phase 2A
- Phase 2B

Keep the pilot narrow, complete one full Grade 3 Quarter 1 student journey, and only then expand to teacher curriculum tools and Gemini support.
