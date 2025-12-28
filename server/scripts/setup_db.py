#!/usr/bin/env python3
"""
Limitless Companion - Database Setup Script
Initializes the PostgreSQL database with required extensions and tables.
"""

import sys
import asyncio
from pathlib import Path

# Add app to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy import text
import structlog

from app.config.settings import settings


async def setup_database():
    """Set up the database with required extensions and initial data."""

    # Create database URL
    database_url = (
        f"postgresql://{settings.postgres_user}:{settings.postgres_password}"
        f"@{settings.postgres_host}:{settings.postgres_port}/{settings.postgres_db}"
    )

    logger = structlog.get_logger()
    logger.info("Setting up database", database_url=database_url)

    # Create async engine
    engine = create_async_engine(database_url, echo=settings.debug)

    try:
        async with engine.begin() as conn:
            # Enable required extensions
            logger.info("Enabling PostgreSQL extensions")

            # pgvector for embeddings
            await conn.execute(text("CREATE EXTENSION IF NOT EXISTS vector;"))

            # uuid-ossp for UUID generation (if needed)
            await conn.execute(text("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"))

            # Create indexes for better performance
            logger.info("Creating performance indexes")

            # Note: Actual table creation will be handled by Alembic migrations
            # This script just sets up the database environment

            logger.info("Database setup completed successfully")

    except Exception as e:
        logger.error("Database setup failed", error=str(e))
        raise
    finally:
        await engine.dispose()


async def verify_setup():
    """Verify that the database setup is correct."""

    database_url = (
        f"postgresql://{settings.postgres_user}:{settings.postgres_password}"
        f"@{settings.postgres_host}:{settings.postgres_port}/{settings.postgres_db}"
    )

    logger = structlog.get_logger()
    logger.info("Verifying database setup")

    engine = create_async_engine(database_url, echo=False)

    try:
        async with engine.begin() as conn:
            # Check pgvector extension
            result = await conn.execute(text(
                "SELECT * FROM pg_extension WHERE extname = 'vector';"
            ))
            if not result.fetchone():
                raise RuntimeError("pgvector extension not found")

            # Test vector operations
            await conn.execute(text(
                "CREATE TEMP TABLE test_vector (id SERIAL, embedding VECTOR(384));"
            ))
            await conn.execute(text(
                "INSERT INTO test_vector (embedding) VALUES ('[0.1,0.2,0.3]');"
            ))

            logger.info("Database verification completed successfully")

    except Exception as e:
        logger.error("Database verification failed", error=str(e))
        raise
    finally:
        await engine.dispose()


async def main():
    """Main entry point."""
    logger = structlog.get_logger()

    try:
        await setup_database()
        await verify_setup()
        logger.info("Database setup completed successfully")
        print("✅ Database setup completed successfully")

    except Exception as e:
        logger.error("Database setup failed", error=str(e))
        print(f"❌ Database setup failed: {e}")
        sys.exit(1)


if __name__ == "__main__":
    # Configure structlog for CLI output
    structlog.configure(
        processors=[
            structlog.stdlib.filter_by_level,
            structlog.stdlib.add_logger_name,
            structlog.stdlib.add_log_level,
            structlog.stdlib.PositionalArgumentsFormatter(),
            structlog.processors.TimeStamper(fmt="iso"),
            structlog.processors.StackInfoRenderer(),
            structlog.processors.format_exc_info,
            structlog.processors.UnicodeDecoder(),
            structlog.processors.JSONRenderer()
        ],
        context_class=dict,
        logger_factory=structlog.stdlib.LoggerFactory(),
        wrapper_class=structlog.stdlib.BoundLogger,
        cache_logger_on_first_use=True,
    )

    asyncio.run(main())