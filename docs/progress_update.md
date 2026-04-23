# ScanLearn Progress Update

## Current Product Direction

ScanLearn is now firmly moving as a:

`curriculum-first Grade 3 science LMS with teacher-managed AI support`

The app is no longer being shaped as a scan-first prototype. Scanning is now treated as enrichment and mission support inside a larger learning cycle:

`My Learning Plan -> Quarter Hub -> Unit -> Lesson -> Assessment -> Mastery -> Teacher Intervention`

This is still aligned with the ScanLearn AI roadmap and the DepEd-based scaling direction.

## Current Build Status

The Grade 3 pilot backbone is now in place.

Completed major phases:

- Phase 1A: LMS data model, Firebase schema support, repositories
- Phase 1B: Grade 3 Quarter 1 pilot seeding
- Phase 2A: My Learning Plan and Quarter Hub
- Phase 2B: Unit Detail, Lesson Player, Test Knowledge
- Phase 3: Teacher curriculum dashboard and Lesson Studio
- Phase 4: Gemini teacher copilot
- Phase 4.5: AI governance, draft logging, usage logging
- Phase 5: Review Queue and section/class curriculum dashboard
- Phase 6: mission-to-curriculum alignment
- Phase 6.5: mission analytics and teacher intervention loop
- Pilot stabilization: lesson release control, mission release control, runtime guards, legacy scan-first cleanup

## Grade 3 Curriculum Status

### Quarter 1

Quarter 1 is seeded and supports the current pilot learning cycle.

### Quarter 2

Quarter 2 is now seeded in the app and connected to teacher tools.

Quarter 2 includes:

- quarter: `Rocks, Soil, and Materials Around Us`
- 2 units
- 4 lessons
- 4 competencies
- 8 lesson activities
- 3 curriculum-linked draft missions

Quarter 2 content is still controlled through teacher release decisions.

That means:

- the data is already in Firebase seed logic
- lessons are reviewable in teacher curriculum tools
- missions are filterable in teacher mission tools
- release still happens lesson by lesson or section by section

## Teacher Workflow Status

Teacher-side core tools are now working across the Grade 3 pilot:

- Curriculum Dashboard
- Lesson Studio
- review / publish workflow
- section-based lesson release control
- mission editing with lesson-linked release checks
- Review Queue
- section curriculum snapshot
- learner intervention actions
- AI lesson/mission/quiz/analytics support

Recent teacher-side improvement:

- the Curriculum Dashboard is now quarter-aware instead of assuming Quarter 1 only
- the Missions screen is now quarter-filterable
- Quarter 2 is now practical to review and release from the teacher UI

## Student Workflow Status

Student-side LMS flow is working:

- My Learning Plan
- Quarter Hub
- Unit Detail
- Lesson Player
- Test Knowledge
- quarter-aware mission access
- progress and mastery saving

Release-gating is also in place:

- students only see lessons released to their section
- students only see missions released to their section
- stale direct opens are blocked in lesson and mission runtime flows

## AI Status

Teacher AI currently works through Firebase AI Logic and App Check.

Working teacher AI actions:

- AI Draft
- Simplify
- AI Quiz
- Generate Mission With AI
- Generate AI Summary

Governance already exists for:

- AI draft variants
- AI usage logs
- AI-assisted metadata on lessons and missions
- prompt version tracking

## Gemini Backend Migration Status

A Gemini backend migration plan is now documented in:

- [GEMINI_BACKEND_MIGRATION_PLAN.md](</D:/games/ScanLearn/docs/GEMINI_BACKEND_MIGRATION_PLAN.md>)

Target direction:

`Android app -> backend AI gateway -> Gemini API (Google AI Studio)`

Important note from this session:

- Gemini should remain the provider
- the Gemini API key should stay on the backend only
- do not place the replacement key in the Android app
- the backend cutover should happen feature by feature, starting with `AI Draft`

## DepEd Scaling Direction

DepEd-based scaling documentation is now in place:

- [DEPED_CURRICULUM_SCALING_BLUEPRINT.md](</D:/games/ScanLearn/docs/DEPED_CURRICULUM_SCALING_BLUEPRINT.md>)

This confirms the scaling rule:

`DepEd standard -> quarter -> units -> lessons -> assessments -> scan enrichment`

Current scope remains:

- Grade 3 first
- complete 4 quarters well before scaling to the next grade

## Current Priority Phase

We are currently in:

`Pilot Stabilization and Rollout Readiness`

Product reset note:

- the app is now being cleaned up as an LMS-first product
- student and teacher home flows should prioritize lessons, tasks, and progress
- grade and section setup is expanding from the old Grade 3-only assumptions to a full Grade 1 to 6 school structure
- teacher scope should be limited to the grade level they handle

More specifically, the app is now at:

`Quarter 2 teacher release setup and verification`

This means we should avoid rushing into more features until Quarter 2 is released and validated through the teacher-to-student cycle.

## Recommended Next Step

The next practical step is:

- review Quarter 2 lessons in Teacher Curriculum
- publish and release them gradually
- review Quarter 2 missions in Teacher Missions
- activate only missions whose linked lessons are already released
- validate the student experience for one pilot section

After that, the next engineering step should be:

- keep the new app-side AI provider abstraction
- keep Firebase AI as fallback
- start backend gateway work for Gemini API via Google AI Studio

## Short Session Summary

Today’s progress moved the project forward in three important ways:

1. Quarter 2 was made manageable in teacher tools, not just seeded in data.
2. The project direction was reinforced using the DepEd curriculum structure for Grades 1 to 6.
3. The next AI evolution was clarified: move to a backend Gemini gateway instead of putting provider keys in the mobile app.

## End-of-Day State

The project is on the right track.

The Grade 3 pilot now has a real LMS backbone, teacher control, AI assistance, release gating, and a clear path for Quarter 2 rollout and safer future AI provider migration.
