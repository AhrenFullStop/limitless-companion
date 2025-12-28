---
name: Bug Report
description: Create a report to help us improve
title: "[BUG] "
labels: ["bug", "triage"]
assignees: []
body:
  - type: markdown
    attributes:
      value: |
        Thanks for taking the time to report a bug! This helps us improve Limitless Companion.

        **Before submitting:**
        - [ ] Check [existing issues](https://github.com/limitless-companion/limitless-companion/issues) to avoid duplicates
        - [ ] Ensure you're using the latest version
        - [ ] Include logs if available (see [Collecting Logs](https://github.com/limitless-companion/limitless-companion/blob/main/CONTRIBUTING.md#collecting-logs))

  - type: dropdown
    id: component
    attributes:
      label: Component
      description: Which part of the application is affected?
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

  - type: input
    id: version
    attributes:
      label: Version
      description: What version are you running? (Check app settings or server logs)
      placeholder: "e.g., v1.0.0, commit hash, or 'latest'"
    validations:
      required: true

  - type: textarea
    id: description
    attributes:
      label: Describe the Bug
      description: A clear and concise description of what the bug is.
      placeholder: "Describe what happened, what you expected to happen, and any error messages."
    validations:
      required: true

  - type: textarea
    id: reproduce
    attributes:
      label: Steps to Reproduce
      description: Provide detailed steps to reproduce the issue.
      placeholder: |
        1. Go to '...'
        2. Click on '....'
        3. Scroll down to '....'
        4. See error
      value: |
        1.
        2.
        3.
    validations:
      required: true

  - type: textarea
    id: expected
    attributes:
      label: Expected Behavior
      description: What should happen?
      placeholder: "A clear description of what you expected to happen."
    validations:
      required: true

  - type: textarea
    id: actual
    attributes:
      label: Actual Behavior
      description: What actually happened?
      placeholder: "Describe what actually happened instead."
    validations:
      required: true

  - type: textarea
    id: environment
    attributes:
      label: Environment
      description: Provide details about your setup.
      value: |
        **Mobile (if applicable):**
        - Device: [e.g., Pixel 7 Pro]
        - Android Version: [e.g., Android 14]
        - RAM: [e.g., 12GB]
        - Bluetooth headset: [e.g., Sony WH-1000XM4]

        **Server (if applicable):**
        - OS: [e.g., Ubuntu 22.04]
        - Docker version: [e.g., 24.0.5]
        - CPU: [e.g., Intel i7-8700K]
        - RAM: [e.g., 32GB]
        - Storage: [e.g., SSD]

        **Network:**
        - Connection type: [e.g., WiFi 5GHz, 4G]
        - Latency to server: [e.g., <50ms]
    validations:
      required: false

  - type: textarea
    id: logs
    attributes:
      label: Logs
      description: If applicable, add logs to help explain your problem.
      placeholder: |
        **Server logs:**
        ```
        Paste server logs here
        ```

        **Mobile logs:**
        ```
        Paste mobile logs here (see CONTRIBUTING.md for collection instructions)
        ```
      render: shell
    validations:
      required: false

  - type: textarea
    id: additional
    attributes:
      label: Additional Context
      description: Add any other context about the problem here.
      placeholder: "Screenshots, videos, or anything else that might help."
    validations:
      required: false

  - type: checkboxes
    id: checklist
    attributes:
      label: Checklist
      description: Please confirm the following
      options:
        - label: I have searched for similar issues and confirmed this is not a duplicate
          required: true
        - label: I have included all the required information above
          required: true
        - label: I have read the [Contributing Guidelines](https://github.com/limitless-companion/limitless-companion/blob/main/CONTRIBUTING.md)
          required: true