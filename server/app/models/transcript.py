import uuid
from sqlalchemy import Column, String, BigInteger, Text, DateTime, Float
from sqlalchemy.dialects.postgresql import UUID
from app.database.connection import Base
import datetime

class Transcript(Base):
    __tablename__ = "transcripts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    session_id = Column(String, index=True)
    text = Column(Text, nullable=False)
    start_time = Column(BigInteger, nullable=False)  # epoch ms
    duration_ms = Column(BigInteger, nullable=False)
    source = Column(String, default="on_device")
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    embedding = Column(Text, nullable=True) # For future pgvector integration in M6

class Device(Base):
    __tablename__ = "devices"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    device_id = Column(String, unique=True, index=True)
    name = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
