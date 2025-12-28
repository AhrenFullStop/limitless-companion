"""
Health check endpoint for monitoring server status.
"""

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
import httpx
import asyncio

from app.database.connection import get_db
from app.config.settings import settings

router = APIRouter()


@router.get("/health")
async def health_check(db: AsyncSession = Depends(get_db)):
    """
    Comprehensive health check endpoint.

    Checks database connectivity, Ollama service status, and overall system health.
    """
    health_status = {
        "status": "healthy",
        "database": "unknown",
        "ollama": "unknown",
        "models_loaded": False,
        "version": "1.0.0",
    }

    # Check database connectivity
    try:
        await db.execute("SELECT 1")
        health_status["database"] = "connected"
    except Exception as e:
        health_status["status"] = "unhealthy"
        health_status["database"] = f"error: {str(e)}"

    # Check Ollama service
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(f"{settings.ollama_host}/api/tags")
            if response.status_code == 200:
                models_data = response.json()
                available_models = [model["name"] for model in models_data.get("models", [])]

                if settings.ollama_model in available_models:
                    health_status["ollama"] = "ready"
                    health_status["models_loaded"] = True
                else:
                    health_status["ollama"] = f"model_not_found: {settings.ollama_model}"
            else:
                health_status["ollama"] = f"http_error: {response.status_code}"

    except (httpx.TimeoutException, httpx.ConnectError) as e:
        health_status["ollama"] = f"connection_error: {str(e)}"
    except Exception as e:
        health_status["ollama"] = f"error: {str(e)}"

    # Set overall status
    if health_status["database"] != "connected" or not health_status["models_loaded"]:
        health_status["status"] = "unhealthy"

    return health_status


@router.get("/health/database")
async def database_health(db: AsyncSession = Depends(get_db)):
    """Database-specific health check."""
    try:
        await db.execute("SELECT 1")
        return {"status": "healthy", "database": "connected"}
    except Exception as e:
        return {"status": "unhealthy", "database": f"error: {str(e)}"}


@router.get("/health/ollama")
async def ollama_health():
    """Ollama service health check."""
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(f"{settings.ollama_host}/api/tags")
            if response.status_code == 200:
                return {"status": "healthy", "ollama": "ready"}
            else:
                return {"status": "unhealthy", "ollama": f"http_error: {response.status_code}"}
    except Exception as e:
        return {"status": "unhealthy", "ollama": f"error: {str(e)}"}