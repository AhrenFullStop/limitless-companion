---
name: Feature Request
description: Suggest an idea for this project
title: "[FEATURE] "
labels: ["enhancement", "triage"]
assignees: []
body:
  - type: markdown
    attributes:
      value: |
        Thanks for suggesting a feature! We love hearing from our community about how to improve Limitless Companion.

        **Before submitting:**
        - [ ] Check [existing issues](https://github.com/limitless-companion/limitless-companion/issues) to avoid duplicates
        - [ ] Consider if this fits our [project scope](https://github.com/limitless-companion/limitless-companion/blob/main/project-plan.md#2-scope--objectives)

  - type: textarea
    id: summary
    attributes:
      label: Summary
      description: A brief description of the feature request.
      placeholder: "One or two sentences describing the feature."
    validations:
      required: true

  - type: textarea
    id: problem
    attributes:
      label: Problem/Use Case
      description: What problem would this feature solve? What use case does it address?
      placeholder: |
        Describe the problem you're trying to solve. For example:
        "As a [user type], I want [goal] so that [benefit]."
    validations:
      required: true

  - type: textarea
    id: solution
    attributes:
      label: Proposed Solution
      description: Describe your proposed solution.
      placeholder: |
        Describe how you think this should work. Include:
        - How users would interact with this feature
        - Any technical considerations
        - Mockups or examples if helpful
    validations:
      required: true

  - type: dropdown
    id: component
    attributes:
      label: Component
      description: Which part of the application would this affect?
      options:
        - Mobile App (Android)
        - Server (FastAPI)
        - Audio Processing (Whisper)
        - Action Detection (LLM)
        - Speaker Diarization
        - Search (RAG)
        - Database (PostgreSQL)
        - Docker/Deployment
        - Documentation
        - Other
    validations:
      required: true

  - type: dropdown
    id: priority
    attributes:
      label: Priority
      description: How important is this feature to you?
      options:
        - Nice to have
        - Would be helpful
        - Important for my use case
        - Critical/blocking my usage
    validations:
      required: false

  - type: dropdown
    id: complexity
    attributes:
      label: Estimated Complexity
      description: Your estimate of implementation difficulty
      options:
        - Simple (small change, <1 week)
        - Medium (moderate change, 1-4 weeks)
        - Complex (major change, 1-3 months)
        - Very Complex (architectural change, 3+ months)
        - Unknown
    validations:
      required: false

  - type: textarea
    id: alternatives
    attributes:
      label: Alternative Solutions
      description: Have you considered any alternative approaches?
      placeholder: "Describe any alternative solutions or workarounds you've considered."
    validations:
      required: false

  - type: textarea
    id: additional
    attributes:
      label: Additional Context
      description: Add any other context, screenshots, or examples.
      placeholder: "Links to similar features in other apps, research papers, etc."
    validations:
      required: false

  - type: checkboxes
    id: checklist
    attributes:
      label: Checklist
      description: Please confirm the following
      options:
        - label: I have searched for similar feature requests and confirmed this is not a duplicate
          required: true
        - label: This feature aligns with the project's privacy-first, self-hosted philosophy
          required: true
        - label: I understand this is an open-source project and implementation depends on community contributions
          required: true