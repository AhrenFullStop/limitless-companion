from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List, Optional
from pydantic import BaseModel
import uuid
import datetime

from app.database.connection import get_db
from app.models.transcript import Transcript, Device

router = APIRouter(prefix="/transcripts", tags=["transcripts"])

# --- Pydantic Schemas ---
class TranscriptCreate(BaseModel):
    session_id: str
    text: str
    start_time: int
    duration_ms: int
    source: Optional[str] = "on_device"

class TranscriptRead(BaseModel):
    id: uuid.UUID
    session_id: str
    text: str
    start_time: int
    duration_ms: int
    source: str
    created_at: datetime.datetime

    class Config:
        orm_mode = True

class DeviceRegister(BaseModel):
    device_id: str
    name: Optional[str] = None

# --- Routes ---

@router.post("/", response_model=TranscriptRead)
async def upload_transcript(transcript_in: TranscriptCreate, db: AsyncSession = Depends(get_db)):
    transcript = Transcript(
        session_id=transcript_in.session_id,
        text=transcript_in.text,
        start_time=transcript_in.start_time,
        duration_ms=transcript_in.duration_ms,
        source=transcript_in.source
    )
    db.add(transcript)
    await db.commit()
    await db.refresh(transcript)
    return transcript

@router.get("/", response_model=List[TranscriptRead])
async def list_transcripts(session_id: Optional[str] = None, db: AsyncSession = Depends(get_db)):
    query = select(Transcript)
    if session_id:
        query = query.where(Transcript.session_id == session_id)
    query = query.order_by(Transcript.created_at.desc())
    result = await db.execute(query)
    return result.scalars().all()

@router.post("/register-device")
async def register_device(device_in: DeviceRegister, db: AsyncSession = Depends(get_db)):
    # Simple upsert
    query = select(Device).where(Device.device_id == device_in.device_id)
    result = await db.execute(query)
    device = result.scalar_one_or_none()
    
    if not device:
        device = Device(device_id=device_in.device_id, name=device_in.name)
        db.add(device)
    else:
        device.name = device_in.name

    await db.commit()
    await db.refresh(device)
    return {"status": "success", "device_id": device.device_id}
