# Grade 3 Quarter 2 Curriculum Pack

## Purpose

This document drafts the next curriculum-ready content slice for ScanLearn after the Grade 3 Quarter 1 pilot.

This pack is intended to be:

- DepEd-aligned
- LMS-first
- implementation-ready
- narrow enough for the next safe expansion step

This is not the signal to seed Quarter 2 immediately.

Quarter 2 should only be implemented after the current Grade 3 QA and release validation pass is cleared.

## Quarter Identity

### Grade

- Grade 3

### Quarter

- Quarter 2

### Proposed Quarter Title

- Rocks, Soil, and Materials Around Us

### Quarter Description

This quarter helps learners observe and classify earth materials and common materials in their environment. It also introduces the basic states of matter through familiar, concrete examples that work well with object observation and guided comparison.

## DepEd Alignment

This quarter supports the Grade 3 standard areas related to:

- rocks and soil
- things found in the surroundings
- classifying matter as solid, liquid, or gas
- describing observable properties of materials

## Product Goal For This Quarter

A Grade 3 learner should be able to:

1. identify common rocks and soil in simple ways
2. compare materials by observable properties
3. classify materials as solid, liquid, or gas
4. connect common materials to everyday use and safety
5. complete lesson-linked missions and quick checks

## Suggested Build Scope

Keep this quarter similar in size to the Quarter 1 pilot:

- 1 quarter
- 2 units
- 4 lessons
- 4 competencies
- 8 lesson activities
- 2 to 3 curriculum-linked missions

This keeps Quarter 2 realistic for the next expansion slice.

## Quarter Structure

## Unit 1: Earth Materials Around Us

### Unit Goal

Learners observe common rocks and soil and describe simple visible characteristics such as color, texture, size, and use.

### Suggested Lessons

#### Lesson 1: Common Rocks Around Home And School

Objective:

- Identify common rocks found in everyday surroundings and describe simple visible features.

Summary:

- Rocks can be found in roads, gardens, riverbanks, and school grounds. They may differ in size, color, shape, and texture.

Scan-supported potential:

- medium to strong

Possible linked object clusters:

- rock
- stone
- gravel
- pebbles

#### Lesson 2: Soil And What It Is Used For

Objective:

- Describe soil as an important earth material and tell simple uses of soil.

Summary:

- Soil helps plants grow and is found in gardens, fields, and places where living things depend on the land.

Scan-supported potential:

- medium

Possible linked object clusters:

- soil
- garden soil
- potted plant soil
- clay-like earth samples

## Unit 2: Materials And States Of Matter

### Unit Goal

Learners classify common materials and describe them as solid, liquid, or gas using familiar objects and situations.

### Suggested Lessons

#### Lesson 3: Solids, Liquids, And Gases

Objective:

- Classify everyday examples as solid, liquid, or gas.

Summary:

- Solids keep their shape, liquids take the shape of containers, and gases spread to fill space.

Scan-supported potential:

- strong for solids, medium for liquids, weak for gases

Possible linked object clusters:

- table
- bottle
- glass
- water container
- balloon as gas prompt

#### Lesson 4: Materials In Daily Life

Objective:

- Compare common materials by their simple properties and relate them to everyday use.

Summary:

- Different materials such as wood, metal, plastic, glass, soil, and stone are useful in different ways because of their observable properties.

Scan-supported potential:

- strong

Possible linked object clusters:

- table
- spoon
- plastic bottle
- glass container
- classroom and household items

## Competency Draft

### Competency 1

Code:

- G3-Q2-C1

Statement:

- Identify common rocks and describe simple visible properties such as size, color, and texture.

### Competency 2

Code:

- G3-Q2-C2

Statement:

- Describe soil as an earth material and explain simple uses of soil in everyday life.

### Competency 3

Code:

- G3-Q2-C3

Statement:

- Classify common examples as solid, liquid, or gas using observable clues.

### Competency 4

Code:

- G3-Q2-C4

Statement:

- Compare common materials and relate their properties to how people use them safely and effectively.

## Lesson Activity Draft

Keep the same pilot pattern as Quarter 1:

- 2 quick checks per lesson
- mostly multiple choice
- one short answer where helpful

### Suggested Activity Set

#### Lesson 1 Activities

- Which material is most likely a rock found on the ground?
- Describe one visible property of a rock.

#### Lesson 2 Activities

- Why is soil important for plants?
- Where do we usually find soil?

#### Lesson 3 Activities

- Which example is a liquid?
- Which example is a gas?

#### Lesson 4 Activities

- Which material is best for a drinking glass?
- Why is wood useful for tables or chairs?

## Assessment Direction

Test Knowledge for Quarter 2 should measure:

- earth material recognition
- matter-state classification
- observable property comparison
- simple use-and-safety reasoning

Recommended pattern:

- 4 lesson-level quizzes
- one combined quarter review later after full Grade 3 stabilization

## Mission Draft

Missions should stay lesson-linked and section-released.

### Mission 1: Rock And Soil Spotting

Mission type:

- curriculum_scan

Linked lesson:

- Common Rocks Around Home And School

Goal:

- Find one rock-like material and one soil-related example, then compare them.

Possible object targets:

- rock
- stone
- soil
- potted plant soil

### Mission 2: Matter State Hunt

Mission type:

- curriculum_scan

Linked lesson:

- Solids, Liquids, And Gases

Goal:

- Identify one solid and one liquid in the environment, then explain how they are different.

Possible object targets:

- table
- bottle
- water container
- spoon

### Mission 3: Material Use Check

Mission type:

- guided_review

Linked lesson:

- Materials In Daily Life

Goal:

- Look for useful materials in school or home and connect each material to one reason it is used.

Possible object targets:

- table
- spoon
- plastic container
- glass item

## Scan-Supported Object Strategy

Quarter 2 should expand the object library carefully.

Recommended object priority:

- rock
- stone
- gravel
- soil
- water
- bottle
- spoon
- glass
- plastic container

If exact recognition is hard, the app can still support this quarter well through:

- teacher-curated object lists
- manual object selection fallback
- lesson-linked mission prompts

## Release Strategy

Recommended first release approach:

- release Quarter 2 to the same pilot sections:
  - Manga
  - Melon
  - Guyabano
  - Kasoy
  - Santol

But keep rollout staged:

1. seed Quarter 2 as draft
2. QA in teacher workflows
3. release to one section first if needed
4. then open to the rest of the pilot sections

## Suggested Firebase / Model Shape

When this is implemented, it should follow the same pattern already used by Quarter 1:

- 1 new quarter record
- 2 units
- 4 lessons
- lesson activities
- competencies
- linked missions
- released section IDs

Suggested IDs:

- `g3_q2`
- `g3_q2_unit_earth_materials`
- `g3_q2_unit_states_of_matter`
- `g3_q2_lesson_rocks`
- `g3_q2_lesson_soil`
- `g3_q2_lesson_states_of_matter`
- `g3_q2_lesson_material_uses`

## Definition Of Ready

Quarter 2 should be considered ready for seeding only if:

- Grade 3 Quarter 1 QA checklist is passed
- lesson release control is stable
- mission release control is stable
- direct-access guards for unreleased content are working
- teacher publish flow is stable

## Recommended Next Step After This Document

Once the Grade 3 QA pass is cleared, the next implementation slice should be:

`Phase: Seed Grade 3 Quarter 2 pilot content`

Scope:

- add Quarter 2 models and content data
- seed units, lessons, activities, and competencies
- seed 2 to 3 linked missions
- verify release control
- test student flow end to end

## Final Note

Quarter 2 should be treated as the first true proof that the ScanLearn architecture can scale beyond a single pilot quarter.

If Quarter 2 works cleanly with:

- curriculum structure
- lesson release control
- mission release control
- teacher intervention loop
- student progress and mastery

then the app will be in a strong position to complete Grade 3 Quarters 3 and 4 next.
