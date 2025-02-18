#!/bin/bash

# Database connection parameters
DB_NAME="dev_lucky_draw"
DB_USER="postgres"
DB_PASSWORD="postgres"
DB_HOST="localhost"
DB_PORT="5432"

echo "Resetting database..."

# Run the reset SQL script
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f src/main/resources/db/clean/reset_flyway.sql

echo "Database reset complete. You can now restart the application."