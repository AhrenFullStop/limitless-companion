---
name: Research Task
description: Create a research task for technical investigation
title: "[RESEARCH] "
labels: ["research", "documentation"]
assignees: []
body:
  - type: markdown
    attributes:
      value: |
        Research tasks help us make informed technical decisions. Use this template for investigating new technologies, comparing alternatives, or validating assumptions.

        **Examples:**
        - Comparing embedding models for RAG search
        - Evaluating speaker diarization accuracy
        - Testing LLM performance for action detection
        - Investigating on-device ML options

  - type: textarea
    id: objective
    attributes:
      label: Research Objective
      description: What question are we trying to answer?
      placeholder: "What is the most accurate speaker diarization approach for our use case?"
    validations:
      required: true

  - type: textarea
    id: background
    attributes:
      label: Background & Context
      description: Why is this research needed? What problem are we solving?
      placeholder: |
        Current implementation uses [current approach]. We need to evaluate if [alternative]
        would provide better [accuracy/performance/privacy/etc.].
    validations:
      required: true

  - type: textarea
    id: scope
    attributes:
      label: Research Scope
      description: What specifically should be investigated?
      placeholder: |
        - Test accuracy on 5+ sample recordings
        - Measure latency and CPU usage
        - Compare results with current implementation
        - Document trade-offs and recommendations
    validations:
      required: true

  - type: dropdown
    id: component
    attributes:
      label: Component
      description: Which part of the system does this research affect?
      options:
        - Audio Processing (Whisper)
        - Speaker Diarization
        - Action Detection (LLM)
        - Search (RAG/Embeddings)
        - Database (PostgreSQL/pgvector)
        - Mobile Architecture
        - Server Architecture
        - Deployment/Docker
        - Other
    validations:
      required: true

  - type: textarea
    id: success_criteria
    attributes:
      label: Success Criteria
      description: How will we know if the research was successful?
      placeholder: |
        - Clear recommendation with pros/cons documented
        - Performance benchmarks completed
        - Implementation feasibility assessed
        - Decision documented in project-plan.md
    validations:
      required: true

  - type: dropdown
    id: priority
    attributes:
      label: Priority
      description: How urgent is this research?
      options:
        - High (blocks current development)
        - Medium (should do before next milestone)
        - Low (nice to know, future consideration)
    validations:
      required: true

  - type: input
    id: timeline
    attributes:
      label: Estimated Timeline
      description: How long should this research take?
      placeholder: "e.g., 1-2 weeks, 3-5 days"
    validations:
      required: false

  - type: textarea
    id: resources
    attributes:
      label: Resources Needed
      description: What resources or access do you need?
      placeholder: |
        - Access to test recordings
        - Cloud credits for API testing
        - Specific hardware for benchmarking
        - Research papers or documentation links
    validations:
      required: false

  - type: textarea
    id: deliverables
    attributes:
      label: Expected Deliverables
      description: What should be produced from this research?
      placeholder: |
        - Document in docs/research/ with findings
        - Code samples or proof-of-concepts
        - Performance benchmarks
        - Recommendation for implementation
    validations:
      required: true

  - type: checkboxes
    id: checklist
    attributes:
      label: Checklist
      description: Research preparation checklist
      options:
        - label: Research question is clearly defined
          required: true
        - label: Success criteria are measurable
          required: true
        - label: Resources needed are identified
          required: true
        - label: Timeline is realistic
          required: true