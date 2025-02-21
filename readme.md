# Lucky Draw Application

This is a Spring Boot application for managing lucky draw events.

## Prerequisites

- Java 17 or higher
- PostgreSQL 15.0 or higher
- Gradle 8.0 or higher
- Node.js 18.0 or higher (for frontend)

## Database Setup

### Initial Setup

```bash
# Clean and reset database (Development only)
./gradlew flywayClean -Dspring.profiles.active=dev

# Apply all migrations
./gradlew flywayMigrate -Dspring.profiles.active=dev
```

### Handling Migration Issues

If you encounter the "relation already exists" error or missing columns:

```bash
# 1. Clean the database (Development only)
./gradlew flywayClean -Dspring.profiles.active=dev

# 2. Repair any broken migrations
./gradlew flywayRepair -Dspring.profiles.active=dev

# 3. Apply migrations again
./gradlew flywayMigrate -Dspring.profiles.active=dev
```

For Windows, use `gradlew.bat` instead of `./gradlew`.

⚠️ Never use `flywayClean` on production environment!

### Migration Status Check

To check the current status of migrations:

```bash
./gradlew flywayInfo -Dspring.profiles.active=dev
```

## Building and Running

1. Build the application:

```bash
./gradlew build
```

2. Run the application:

```bash
# Development profile
./gradlew bootRun -Dspring.profiles.active=dev

# Test profile
./gradlew bootRun -Dspring.profiles.active=test
```

## Testing

Run tests with:

```bash
./gradlew test -Dspring.profiles.active=test
```

## API Documentation

Swagger UI is available at:
- Development: `http://localhost:8080/swagger-ui.html`
- Production: Disabled by default

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests
4. Submit a pull request

## Troubleshooting

### Common Database Issues

1. Missing columns or relations:
   ```bash
   ./gradlew flywayClean flywayMigrate -Dspring.profiles.active=dev
   ```

2. Corrupted migration history:
   ```bash
   ./gradlew flywayRepair -Dspring.profiles.active=dev
   ```

3. Migration checksum mismatch:
   ```bash
   ./gradlew flywayRepair flywayMigrate -Dspring.profiles.active=dev
   ```

### Environment-Specific Configuration

- Development: `application-dev.properties`
- Test: `application-test.properties`
- Production: `application-prod.properties`

## License

This project is licensed under the MIT License - see the LICENSE file for details.