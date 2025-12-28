# Security Policy

## Supported Versions

We actively support the following versions with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability in Limitless Companion, please help us by reporting it responsibly.

### How to Report

**Please do NOT report security vulnerabilities through public GitHub issues.**

Instead, please report security vulnerabilities by emailing:
- **Email**: security@limitless-companion.org

### What to Include

When reporting a vulnerability, please include:

- **Description**: A clear description of the vulnerability
- **Steps to reproduce**: Detailed steps to reproduce the issue
- **Impact**: What an attacker could achieve by exploiting this vulnerability
- **Affected versions**: Which versions are affected
- **Environment**: Server setup, Android version, etc.
- **Proof of concept**: If available (please don't include actual exploits)

### Response Timeline

We will acknowledge your report within 48 hours and provide a more detailed response within 7 days indicating our next steps.

We will keep you informed about our progress throughout the process of fixing the vulnerability.

### Our Commitment

- We will investigate all legitimate reports
- We will keep you informed about our progress
- We will credit you (if desired) once the issue is resolved
- We will not pursue legal action against security researchers who follow this policy

## Security Considerations

### For Users

**Self-Hosted Architecture**: Limitless Companion is designed to be self-hosted, meaning you control your data. However, this also means you are responsible for securing your server.

**Recommended Security Practices**:

1. **Network Security**:
   - Deploy behind a reverse proxy (nginx/caddy) with HTTPS
   - Use firewall rules to restrict access to necessary ports only
   - Consider VPN access for remote administration

2. **Server Hardening**:
   - Keep Docker and host system updated
   - Use strong passwords for database and admin access
   - Enable Docker security features (AppArmor, seccomp)
   - Regular backups of your data

3. **API Security**:
   - Rotate API keys regularly
   - Monitor server logs for suspicious activity
   - Use HTTPS for all client-server communication

4. **Data Privacy**:
   - Understand that transcripts are stored on your server
   - Consider encryption at rest for sensitive data
   - Regular cleanup of old transcripts (default: 90 days)

### For Contributors

**Secure Coding Practices**:

- All API endpoints require authentication
- Input validation on all user inputs
- SQL injection prevention through proper ORM usage
- Secure storage of sensitive configuration
- Regular dependency updates and security scans

**Reporting Security Issues in Code**:

If you find a security issue in the codebase:

1. Do not commit the fix directly to a public branch
2. Contact maintainers privately first
3. We will coordinate a proper disclosure and fix

## Known Security Considerations

### Current Limitations

1. **API Key Storage**: API keys are stored in Android EncryptedSharedPreferences, which provides device-level security but not perfect protection against determined attackers with physical access.

2. **Network Transmission**: While HTTPS is enforced, the security depends on proper certificate configuration on the server side.

3. **Self-Hosted Responsibility**: As a self-hosted application, security ultimately depends on the user's server configuration and maintenance.

4. **Bluetooth Audio**: Audio capture over Bluetooth SCO is encrypted by the Bluetooth protocol, but the implementation should be reviewed for potential eavesdropping vulnerabilities.

### Future Security Enhancements

- Certificate pinning for API connections
- End-to-end encryption for sensitive data
- Multi-factor authentication for admin operations
- Automated security scanning in CI/CD pipeline

## Contact

For security-related questions or concerns:
- **Email**: security@limitless-companion.org
- **GitHub**: Create a private security advisory (maintainers only)

Thank you for helping keep Limitless Companion secure!