# Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Data Architecture](#data-architecture)
4. [Demo Data Management](#demo-data-management)

## Overview

The Lucky Draw application is designed with a modular, maintainable architecture following Domain-Driven Design principles. The system supports event management, participant tracking, and reward distribution through a secure, scalable platform.

## System Architecture

### Layers
1. Presentation Layer
   - REST Controllers
   - DTOs
   - Request/Response mapping

2. Business Layer
   - Services
   - Domain logic
   - Event processing

3. Data Layer
   - Repositories
   - Entity mapping
   - Data access

### Key Components
- Security: Spring Security with JWT
- Persistence: Spring Data JPA
- Validation: Bean Validation
- Testing: JUnit 5, AssertJ

## Data Architecture

### Database Design
1. User Management
   - Users
   - Roles
   - Permissions

2. Event Management
   - Events
   - Locations
   - Golden Hours

3. Participant Management
   - Participants
   - Event Registrations
   - Spin History

### Data Flow
1. Event Creation
2. Participant Registration
3. Reward Distribution
4. History Tracking

## Demo Data Management

### Overview
The application includes a comprehensive demo data setup that provides a realistic testing and demonstration environment. The demo data includes historical, current, and future records to demonstrate the full system lifecycle.

### Enabling Demo Data

1. Configure application profile:
```yaml
spring:
  profiles:
    active: demo
```

2. The demo data will be automatically populated by DemoDataInitializer when the application starts with the "demo" profile.

### Demo Data Structure

The demo data includes:

1. User Roles and Access Levels:
   - ADMIN: System administrators
   - OPERATOR: System maintenance staff
   - MANAGER: Event managers
   - USER: Basic system users
   - PARTICIPANT: Regular event participants
   - VIP: Premium participants

2. Events Timeline:
   - Past events (2022-2023): Historical data with complete spin histories
   - Current events (2024): Active events for testing
   - Future events (2024-2025): Planned events
   
3. Geographic Coverage:
   - 3 regions (North, Central, South)
   - 6 key provinces
   - 1200+ event locations

4. Participant Data:
   - 1500+ participants
   - Mix of regular and VIP users
   - Distributed across regions
   - Various registration dates

5. Event Activity:
   - 5000+ spin histories
   - Different reward types
   - Golden hour participation
   - Points and rewards tracking

### Data Time Periods

The demo data spans multiple time periods to demonstrate system features:

- Past (Historical):
  - 3 years ago: Initial events
  - 2 years ago: Historical activities
  - 1 year ago: Recent history
  
- Current:
  - Active events
  - Ongoing participation
  - Current golden hours
  
- Future:
  - Upcoming events
  - Scheduled activities
  - Future promotions

### Using Demo Data

1. For Development:
   ```bash
   # Start application with demo profile
   ./mvnw spring-boot:run -Dspring.profiles.active=demo
   ```

2. For Testing:
   - Demo data provides comprehensive test scenarios
   - Covers all business cases
   - Includes edge cases and special conditions

3. For Demonstration:
   - Shows full event lifecycle
   - Demonstrates participant progression
   - Illustrates VIP features
   - Displays historical analysis capabilities

### Refreshing Demo Data

The demo data can be refreshed in several ways:

1. Using the Provided Script (Recommended):
   ```bash
   # Make script executable
   chmod +x scripts/refresh-demo-data.sh
   
   # Run with default configuration
   ./scripts/refresh-demo-data.sh
   
   # Or with custom database settings
   DB_HOST=custom-host DB_PORT=5433 ./scripts/refresh-demo-data.sh
   ```
   The script will:
   - Clean existing data
   - Start application with demo profile
   - Reload all demo data
   - Use environment variables for configuration

2. Application Restart:
   ```bash
   # Start with demo profile - data will be refreshed
   ./mvnw spring-boot:run -Dspring.profiles.active=demo
   ```

3. Manual Database Reset:
   ```sql
   -- Clear all data
   DELETE FROM spin_histories;
   DELETE FROM participant_events;
   -- ... [other tables]
   
   -- Restart application with demo profile
   ```

### Data Volumes

The demo data includes:
- Users: 1500+
- Events: 10+
- Locations: 1200+
- Participants: 1500+
- Spins: 5000+
- Rewards: 100+
- Golden Hours: 200+

This provides sufficient data volume for:
- Performance testing
- Data analysis
- UI pagination
- Report generation

### Maintenance

The demo data is maintained through:

1. SQL Template:
   - Location: `src/main/resources/db/demo/demo-data-template.sql`
   - Contains all data generation logic
   - Uses timestamp placeholders

2. Data Initializer:
   - Class: `DemoDataInitializer`
   - Handles timestamp updates
   - Manages data population

3. Version Control:
   - Template is versioned with application
   - Changes tracked in Git
   - Documentation updated with changes

## Security Architecture
[Security documentation continues...]
