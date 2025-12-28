# Server Deployment Guide

## Overview

This guide covers deploying the Limitless Companion server using Docker Compose. The server provides the backend API for transcript storage, semantic search, and action detection.

## Prerequisites

### System Requirements
- **OS**: Linux (Ubuntu 22.04 LTS recommended), macOS, or Windows with WSL2
- **CPU**: 4 cores minimum (8+ recommended for Ollama)
- **RAM**: 16GB minimum (32GB recommended)
- **Storage**: 100GB available SSD storage
- **Network**: Public IP or domain name (required for mobile app connection)

### Software Requirements
- **Docker**: Version 24.0 or later
- **Docker Compose**: Version 2.0 or later
- **Git**: For cloning the repository

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/limitless-companion/limitless-companion.git
cd limitless-companion/server
```

### 2. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit configuration (see Configuration section below)
nano .env
```

### 3. Deploy Services

```bash
# Start all services
docker-compose up -d

# Monitor startup (wait for "Application startup complete")
docker-compose logs -f server

# Verify services are running
docker-compose ps
```

### 4. Create API Key

```bash
# Generate API key for your mobile device
docker exec limitless-server python manage.py create-device --name "My Phone"

# Save the output:
# Device ID: abc123-def456-ghi789
# API Key: sk_live_xyz789...
```

### 5. Test Server

```bash
# Health check
curl http://localhost:8000/api/health

# Expected response:
# {"status":"healthy","database":"connected","ollama":"ready"}
```

## Detailed Configuration

### Environment Variables

Edit `.env` with your deployment settings:

```bash
# ============================================
# Server Configuration
# ============================================

# Server host and port
SERVER_HOST=0.0.0.0
SERVER_PORT=8000

# Public URL (important for mobile app)
SERVER_URL=https://your-domain.com

# Enable HTTPS
HTTPS_ENABLED=true

# ============================================
# Database Configuration
# ============================================

POSTGRES_USER=limitless
POSTGRES_PASSWORD=your-secure-password-here
POSTGRES_DB=limitless_db

# ============================================
# LLM Configuration
# ============================================

OLLAMA_MODEL=llama3.1:8b
ACTION_DETECTION_CONFIDENCE=0.7

# ============================================
# Data Management
# ============================================

TRANSCRIPT_RETENTION_DAYS=90
```

### Domain Configuration (Production)

For production deployment with HTTPS:

1. **Get SSL Certificate**:
   ```bash
   # Using Let's Encrypt (recommended)
   sudo apt install certbot
   sudo certbot certonly --standalone -d your-domain.com
   ```

2. **Configure Reverse Proxy**:
   ```nginx
   # /etc/nginx/sites-available/limitless-companion
   server {
       listen 443 ssl http2;
       server_name your-domain.com;

       ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

       location / {
           proxy_pass http://localhost:8000;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```

3. **Update Environment**:
   ```bash
   SERVER_URL=https://your-domain.com
   HTTPS_ENABLED=true
   ```

## Troubleshooting

### Common Issues

**Port 8000 already in use**:
```bash
# Change port in .env
SERVER_PORT=8001

# Or find conflicting service
sudo lsof -i :8000
```

**Database connection failed**:
```bash
# Check database logs
docker-compose logs db

# Reset database
docker-compose down -v
docker-compose up -d db
```

**Ollama model not found**:
```bash
# Pull model manually
docker exec limitless-ollama ollama pull llama3.1:8b

# Check available models
docker exec limitless-ollama ollama list
```

**Out of memory**:
```bash
# Reduce model size
OLLAMA_MODEL=llama3.2:3b

# Or add swap space
sudo fallocate -l 8G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### Monitoring

**View logs**:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f server
docker-compose logs -f db
docker-compose logs -f ollama
```

**Check resource usage**:
```bash
# Container stats
docker stats

# Disk usage
docker system df
```

### Backup and Recovery

**Database backup**:
```bash
# Create backup
docker exec limitless-db pg_dump -U limitless limitless_db > backup.sql

# Restore backup
docker exec -i limitless-db psql -U limitless limitless_db < backup.sql
```

**Full server backup**:
```bash
# Stop services
docker-compose down

# Backup volumes
docker run --rm -v limitless_postgres-data:/data -v $(pwd):/backup alpine tar czf /backup/backup.tar.gz -C /data .

# Start services
docker-compose up -d
```

## Performance Tuning

### For High Load

**Increase resources**:
```yaml
# docker-compose.yml
services:
  server:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 4G
```

**Database optimization**:
```sql
-- Add to postgresql.conf
shared_buffers = 512MB
effective_cache_size = 2GB
maintenance_work_mem = 256MB
```

### For Low Resource Systems

**Use smaller models**:
```bash
OLLAMA_MODEL=llama3.2:3b
EMBEDDING_MODEL=all-MiniLM-L6-v2
```

**Reduce batch sizes**:
```bash
ACTION_DETECTION_BATCH_SIZE=3
```

## Security Hardening

### Network Security

**Firewall configuration**:
```bash
# Allow only necessary ports
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP (Let's Encrypt)
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 8000/tcp  # Direct access (if no reverse proxy)
sudo ufw --force enable
```

### API Security

**Rate limiting** (built-in):
- 1000 requests per hour per device
- Automatic IP blocking for abuse

**API key rotation**:
```bash
# Rotate existing key
docker exec limitless-server python manage.py rotate-key --device-id <device-id>

# Revoke compromised key
docker exec limitless-server python manage.py revoke-device --device-id <device-id>
```

### Data Security

**Encryption at rest**:
- Database files are encrypted using PostgreSQL's built-in encryption
- API keys stored hashed in database
- Consider full disk encryption for host system

## Maintenance Tasks

### Regular Updates

```bash
# Update Docker images
docker-compose pull

# Restart services
docker-compose up -d

# Clean up old images
docker image prune -f
```

### Data Cleanup

```bash
# Remove old transcripts (automatic)
docker exec limitless-server python manage.py cleanup --days 90

# Manual cleanup
docker exec limitless-server python manage.py cleanup --days 30 --dry-run
```

### Log Rotation

```bash
# Rotate application logs
docker-compose restart server

# Clean old logs
docker run --rm -v limitless_logs:/logs alpine find /logs -name "*.log" -mtime +30 -delete
```

## Production Checklist

- [ ] Domain name configured with SSL
- [ ] Firewall properly configured
- [ ] Regular backups scheduled
- [ ] Monitoring alerts set up
- [ ] API keys securely stored
- [ ] Resource limits configured
- [ ] Log rotation enabled
- [ ] Automatic updates configured