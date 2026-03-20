from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import os

from app.api.routes.health import router as health_router
from app.api.routes.transcripts.transcripts import router as transcripts_router
from app.database.connection import engine, Base
from app.config.settings import settings

app = FastAPI(title="Limitless Companion API", version="1.0.0")

# Setup CORS
if settings.cors_enabled:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

@app.on_event("startup")
async def startup():
    # Create tables locally if not exist (not for production with migrations)
    # Since it's async, we use this helper
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

# Register routers
app.include_router(health_router)
app.include_router(transcripts_router)

@app.get("/")
async def root():
    return {"message": "Limitless Companion API is online", "docs": "/docs"}
