"""
Limitless Companion - Application Settings
Pydantic settings configuration for environment variables.
"""

from typing import Optional
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # ============================================
    # Server Configuration
    # ============================================

    server_host: str = Field(default="0.0.0.0", description="Server bind host")
    server_port: int = Field(default=8000, description="Server bind port")
    server_url: str = Field(default="https://app-z0dhx-8000.tdc.zoskw.beta.runos.xyz", description="Public server URL for mobile app")

    # Security
    https_enabled: bool = Field(default=True, description="Enable HTTPS enforcement")
    cors_enabled: bool = Field(default=False, description="Enable CORS (development only)")

    # ============================================
    # Database Configuration
    # ============================================

    postgres_user: str = Field(default="limitless", description="PostgreSQL username")
    postgres_password: str = Field(..., description="PostgreSQL password")
    postgres_db: str = Field(default="limitless_db", description="PostgreSQL database name")
    postgres_host: str = Field(default="db", description="PostgreSQL host")
    postgres_port: int = Field(default=5432, description="PostgreSQL port")

    # ============================================
    # LLM Configuration (Ollama)
    # ============================================

    ollama_host: str = Field(default="http://ollama:11434", description="Ollama server URL")
    ollama_model: str = Field(default="llama3.1:8b", description="Ollama model name")

    # ============================================
    # Embedding Model (for RAG Search)
    # ============================================

    embedding_model: str = Field(
        default="all-MiniLM-L6-v2",
        description="Sentence transformer model for embeddings"
    )

    # ============================================
    # Action Detection Settings
    # ============================================

    action_detection_confidence: float = Field(
        default=0.7,
        description="Minimum confidence threshold for action detection",
        ge=0.0,
        le=1.0
    )

    action_detection_batch_size: int = Field(
        default=5,
        description="Number of transcripts to process in batch",
        gt=0
    )

    action_detection_rate_limit: int = Field(
        default=10,
        description="Maximum actions per minute",
        gt=0
    )

    # ============================================
    # Speaker Diarization Settings
    # ============================================

    speaker_diarization_enabled: bool = Field(
        default=True,
        description="Enable speaker diarization"
    )

    speaker_diarization_method: str = Field(
        default="pyannote",
        description="Diarization method (pyannote|heuristic)"
    )

    # ============================================
    # Data Retention & Storage
    # ============================================

    transcript_retention_days: int = Field(
        default=90,
        description="Days to retain transcripts",
        gt=0
    )

    max_transcript_length: int = Field(
        default=10000,
        description="Maximum transcript length in characters",
        gt=0
    )

    # ============================================
    # Security Settings
    # ============================================

    api_key_length: int = Field(default=32, description="API key length")
    api_key_prefix: str = Field(default="sk_live_", description="API key prefix")

    rate_limit_requests_per_hour: int = Field(
        default=1000,
        description="Rate limit per device per hour",
        gt=0
    )

    # ============================================
    # Logging Configuration
    # ============================================

    log_level: str = Field(
        default="INFO",
        description="Logging level",
        pattern="^(DEBUG|INFO|WARNING|ERROR|CRITICAL)$"
    )

    log_json_format: bool = Field(
        default=True,
        description="Use JSON logging format"
    )

    # ============================================
    # Development Settings
    # ============================================

    debug: bool = Field(default=False, description="Enable debug mode")

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )


# Global settings instance
settings = Settings()