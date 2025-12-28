#!/usr/bin/env python3
"""
Limitless Companion - API Key Generation Script
Generates API keys for device registration.
"""

import sys
import uuid
from pathlib import Path

# Add app to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from app.config.settings import settings


def generate_api_key() -> str:
    """Generate a new API key."""
    return f"{settings.api_key_prefix}{uuid.uuid4().hex}"


def generate_device_id() -> str:
    """Generate a new device ID."""
    return str(uuid.uuid4())


def main():
    """Main entry point."""
    device_id = generate_device_id()
    api_key = generate_api_key()

    print("Device created successfully!")
    print(f"Device ID: {device_id}")
    print(f"API Key: {api_key}")
    print()
    print("Use these credentials in your mobile app settings.")


if __name__ == "__main__":
    main()