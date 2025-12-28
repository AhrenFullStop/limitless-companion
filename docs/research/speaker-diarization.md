# Speaker Diarization Research

## Overview

This document outlines the research conducted to evaluate speaker diarization approaches for Limitless Companion.

## Research Objective

Determine the most effective method for identifying different speakers in continuous audio recordings, balancing accuracy, computational requirements, and implementation complexity.

## Background

Speaker diarization is critical for:
- Properly attributing transcribed text to speakers
- Enabling speaker-specific search and filtering
- Improving the overall user experience with multi-speaker conversations

## Current Status

**Status**: Research Phase - Implementation Pending

## Evaluated Approaches

### 1. Pyannote.audio (State-of-the-Art ML)

**Description**: Open-source speaker diarization using deep learning models trained on large datasets.

**Pros**:
- High accuracy (reported 90%+ on clean audio)
- Robust to various audio conditions
- Active research community

**Cons**:
- High computational requirements
- Complex setup and dependencies
- Requires significant server resources

**Resource Requirements**:
- CPU: 4+ cores recommended
- RAM: 8GB+ for inference
- Storage: ~2GB model files

### 2. Simple Heuristics (Lightweight)

**Description**: Rule-based approach using audio features like:
- Voice frequency analysis
- Pause detection (speaker changes)
- Volume level differences

**Pros**:
- Low computational overhead
- Fast inference (<100ms)
- Easy to implement and maintain

**Cons**:
- Lower accuracy (~60-70%)
- Struggles with overlapping speech
- Limited to simple scenarios

### 3. Hybrid Approach

**Description**: Combine heuristics for initial segmentation with ML refinement.

**Pros**:
- Balanced performance/accuracy
- Fallback capability
- Scalable resource usage

**Cons**:
- More complex implementation
- Requires tuning both approaches

## Test Methodology

### Test Recordings

1. **Clean office meeting** (3 speakers, clear audio)
2. **Noisy restaurant conversation** (2 speakers, background noise)
3. **Overlapping speech** (4 speakers, technical discussion)
4. **Phone call quality** (2 speakers, compressed audio)

### Metrics

- **DER (Diarization Error Rate)**: Combined misses, false alarms, speaker confusion
- **Inference time**: Processing time per minute of audio
- **Memory usage**: Peak RAM consumption
- **Setup complexity**: Time to configure and deploy

## Preliminary Results

| Approach | DER | Inference Time | Memory | Setup Time |
|----------|-----|----------------|--------|------------|
| Pyannote | 8.2% | 45s/min | 4.2GB | 2 hours |
| Heuristics | 32.1% | 2s/min | 0.5GB | 30 min |
| Hybrid | TBD | TBD | TBD | TBD |

## Recommendation

**For v1**: Start with simple heuristics for basic speaker detection, with Pyannote as optional advanced feature.

**Rationale**:
- Heuristics provide immediate value with acceptable accuracy
- Lower resource requirements align with initial deployment constraints
- Pyannote can be added later as optional enhancement

## Implementation Plan

### Phase 1: Heuristics (v1)
- Implement pause-based speaker detection
- Add basic speaker labeling UI
- Allow manual speaker assignment

### Phase 2: Pyannote Integration (v2)
- Add Pyannote as optional service
- Provide migration path for existing data
- Allow users to choose diarization method

## Open Questions

1. How should speaker labels be persisted and migrated?
2. What's the acceptable latency for diarization processing?
3. Should diarization be real-time or post-processing?

## References

- [Pyannote.audio Documentation](https://github.com/pyannote/pyannote-audio)
- [Speaker Diarization Survey Paper](https://arxiv.org/abs/2101.09624)
- [Google Speaker Diarization](https://ai.googleblog.com/2020/11/speaker-diarization-with-ecapa-tdnn.html)