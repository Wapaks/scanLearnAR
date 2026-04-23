# Grade 3 QA And Release Validation Checklist

## Purpose

Use this checklist before expanding beyond the current Grade 3 Quarter 1 pilot.

The goal is to confirm that the LMS backbone is stable, teacher-controlled, and section-aware before Quarter 2 content is added.

## Current Phase

We are in:

`Pilot Stabilization and Rollout Readiness`

This means the priority is not adding more capability first. The priority is proving the Grade 3 pilot is safe to release and easy to manage.

## Release Rules That Must Hold

- Students should only see published lessons that are released to their section.
- Students should only see active missions that are released to their section.
- A mission linked to a lesson should not be active in sections where that lesson is not released.
- Direct intents or stale navigation should not let students open unreleased lessons or missions.
- Teacher tools can still open draft and review content for editing.

## Teacher Flow QA

### Curriculum Dashboard

- Open Curriculum Dashboard as a teacher.
- Confirm Grade 3 Quarter 1 loads correctly.
- Confirm lesson counts appear correctly.
- Confirm published lessons show release metadata.
- Confirm draft and review filters still work.

### Lesson Studio

- Open a published lesson.
- Toggle release sections and save.
- Confirm publish is blocked if no release section is selected.
- Confirm the saved release sections persist after reopening the lesson.
- Confirm a draft lesson can still be edited without being student-visible.

### Mission Editor

- Create a mission linked to a published lesson.
- Confirm mission creation works when the selected sections match the lesson release.
- Try to activate a mission for a section where the linked lesson is not released.
- Confirm the app blocks that save with a clear message.
- Reopen the mission and confirm release metadata persists.

### Review Queue

- Confirm draft or review lessons still appear.
- Confirm curriculum-linked intervention missions appear when applicable.
- Confirm the queue still opens lesson and mission editors correctly.

### Analytics And Intervention

- Open Teacher Analytics.
- Confirm mission outcome summary appears.
- Confirm the action buttons open the linked lesson, mission, and learner dashboard.
- Open a learner detail view from the section dashboard.
- Confirm the learner-level intervention buttons work:
  - open linked lesson
  - send note
  - assign follow-up mission
  - relaunch retry path

## Student Flow QA

### Home And Learning Plan

- Open student home.
- Confirm the main path is Learning Plan first.
- Confirm Quarter Explorer opens the Quarter Hub instead of bypassing straight into scanner flow.
- Confirm Quarter Missions opens the quarter-aware mission flow.

### Quarter Hub And Units

- Confirm Quarter Hub shows only released lessons indirectly through its unit completion data.
- Open each unit.
- Confirm only released lessons for the student section appear.
- Confirm unreleased lessons do not appear in the list.

### Lesson Player

- Open a released lesson.
- Confirm it loads normally.
- Attempt to open an unreleased lesson through a stale or manual path if possible.
- Confirm the app blocks access with a release message.

### Test Knowledge

- Confirm Test Knowledge only lists released lessons.
- Confirm released lessons still launch in assessment-only mode.

### Mission Center

- Confirm only released missions for the student section appear.
- Confirm mission cards still show progress correctly.
- Try opening a mission no longer released for the section.
- Confirm the app blocks scanner entry with a release message.

### Progress

- Confirm lesson completion counts still load.
- Confirm progress reflects only the released quarter lesson set the student is allowed to see.

## Section Release Validation

Run this scenario for at least two sections:

1. Release a lesson only to `Manga`.
2. Link a mission to that lesson and release the mission only to `Manga`.
3. Log in as a `Manga` student and confirm the lesson and mission are visible.
4. Log in as a `Melon` or `Guyabano` student and confirm the lesson and mission are hidden.
5. Try opening old mission or lesson routes directly and confirm access is blocked.

## Regression Watchlist

- Explorer still works as an optional enrichment path.
- Scanner flow still works for valid released missions.
- Chat flow still opens correctly for teacher-student notes.
- Progress and mastery still save after lesson completion.
- AI draft, AI mission, and AI analytics still work for teachers.

## Release Decision Gate

Quarter 2 should not start until these are true:

- All teacher QA items pass.
- All student QA items pass.
- Section release scenario passes.
- No unreleased lesson or mission can be opened by students.
- The teacher intervention loop works end to end.
- Grade 3 Quarter 1 can be demonstrated as one stable classroom cycle.

## What Comes After This

Once this checklist passes, the next safest move is:

`Expand Grade 3 to Quarter 2`

After Quarter 2 is stable, continue the same pattern for Quarters 3 and 4 before scaling to a new grade.
