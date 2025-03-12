#!/bin/bash

# Script to refresh demo data for the Lucky Draw application

echo "Starting demo data refresh..."

# Get directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Database connection details - default for local development
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-luckydraw}
DB_USER=${DB_USER:-postgres}
DB_PASS=${DB_PASS:-postgres}

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Using database configuration:"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"

# Function to execute SQL
execute_sql() {
    PGPASSWORD=$DB_PASS psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -v ON_ERROR_STOP=1 -q -f "$1"
    return $?
}

# Clean up existing data
echo -n "Cleaning existing data... "
cat << EOF | PGPASSWORD=$DB_PASS psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -v ON_ERROR_STOP=1 -q
DELETE FROM spin_histories;
DELETE FROM participant_events;
DELETE FROM golden_hours;
DELETE FROM rewards;
DELETE FROM event_locations;
DELETE FROM events;
DELETE FROM participant_roles;
DELETE FROM participants;
DELETE FROM provinces;
DELETE FROM regions;
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Done${NC}"
else
    echo -e "${RED}Failed${NC}"
    exit 1
fi

# Start application with demo profile to reload data
echo "Starting application with demo profile..."
cd "$PROJECT_ROOT"
./mvnw spring-boot:run -Dspring.profiles.active=demo

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Demo data refresh completed successfully${NC}"
else
    echo -e "${RED}Failed to refresh demo data${NC}"
    exit 1
fi
