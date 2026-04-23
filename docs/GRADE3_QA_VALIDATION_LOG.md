# Grade 3 QA Validation Log

## Purpose

This log records the current Grade 3 QA pass status before Quarter 2 seeding.

It is meant to answer:

- what has already been validated in code
- what issues were found and fixed
- what still requires manual in-app checking

## QA Pass Type

Current pass:

- code-and-flow validation
- compile validation
- release-gating review

Not yet completed:

- full manual device walkthrough across teacher and student roles

## Validation Date

- 2026-04-19

## Automated / Structural Validation Completed

### Release Gating

Validated in code:

- student-facing unit, quarter, test, and progress flows use released lessons
- mission lists use released missions for the student section
- direct lesson open in `LessonPlayerActivity` is blocked if the lesson is not published or not released to the section
- direct mission scan entry in `ScannerActivity` is blocked if the mission is not released to the section
- active mission creation is blocked if the linked lesson is not published or not released to the same section

### Navigation Cleanup

Validated in code:

- student home now routes `Quarter Explorer` through `QuarterHubActivity`
- student home now routes `Quarter Missions` through quarter-aware mission flow
- student LMS paths are no longer primarily bypassed through the old scan-first home shortcuts

### Section Release Control

Validated in code:

- lessons now carry `releasedSectionIds`
- missions now carry `releasedSectionIds`
- teacher lesson publishing requires at least one release section
- mission visibility depends on both targeting and release visibility

### Compile Validation

Validated:

- `.\gradlew.bat :app:compileDebugKotlin` succeeded after the QA-related fixes

## Issues Found During This QA Pass

### Issue 1: Learning Plan Mission Count Could Drift From Visible Lesson Set

Status:

- fixed

Problem:

- `MyLearningPlanActivity` counted quarter missions directly, which could over-report missions if a mission existed for a lesson outside the student’s actually released lesson set.

Fix:

- filtered mission count against the visible lesson IDs already loaded into the learning plan

Files:

- [MyLearningPlanActivity.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/activities/MyLearningPlanActivity.kt>)

### Issue 2: Direct Intent Risk For Unreleased Lessons

Status:

- fixed

Problem:

- a stale or manually triggered lesson intent could open `LessonPlayerActivity` even if the lesson was no longer released to the student section

Fix:

- added runtime release validation inside `LessonPlayerActivity`

Files:

- [LessonPlayerActivity.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/activities/LessonPlayerActivity.kt>)

### Issue 3: Direct Mission Scanner Entry Could Bypass Release Visibility

Status:

- fixed

Problem:

- a student could potentially enter mission-mode scanning with an old mission ID even if that mission was no longer visible to the section

Fix:

- added runtime mission visibility validation inside `ScannerActivity`

Files:

- [ScannerActivity.kt](</D:/games/ScanLearn/app/src/main/java/com/example/scanlearn/activities/ScannerActivity.kt>)

## Current Structural QA Status

### Pass

- lesson release control exists
- mission release control exists
- quarter-aware mission visibility exists
- direct lesson guard exists
- direct mission guard exists
- teacher release configuration exists
- student home no longer prioritizes the old scan-first route

### Needs Manual Validation

- teacher can successfully toggle lesson release sections in the UI
- teacher can create and edit active missions with correct release rules in the UI
- teacher analytics intervention buttons all navigate correctly on device
- student mission cards still show correct progress after release filtering
- student can complete a mission and still get proper progress updates after the new gating rules
- chat note flow from learner intervention works smoothly on device
- section-specific visibility behaves correctly across at least two student accounts

## Manual Validation Priority Order

Run these first:

1. lesson release scenario across `Manga` and `Melon`
2. mission release scenario across `Manga` and `Melon`
3. direct stale lesson and mission access attempt
4. teacher intervention actions from analytics and learner detail
5. end-to-end student cycle:
   - Learning Plan
   - Quarter Hub
   - Unit
   - Lesson
   - Mission
   - Progress

## Quarter 2 Readiness Assessment

Updated status:

- Quarter 2 seeding approved and completed

Reason:

- the manual Grade 3 walkthrough was completed after the structural validation pass
- the project proceeded into Quarter 2 seed implementation
- teacher quarter-aware release setup is now also in place

## Post-Seed Follow-up

Quarter 2 is now:

- seeded in the curriculum pack implementation
- visible in teacher curriculum review tools
- visible in teacher mission tools through quarter filtering
- ready for controlled teacher publish and section release

## Next Recommended Action

Next action:

- perform Quarter 2 teacher release verification
- publish and release Quarter 2 lessons gradually
- activate only the missions whose linked lessons are already released
- validate student visibility and progress flow for one pilot section
