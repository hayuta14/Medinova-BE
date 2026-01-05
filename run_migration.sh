#!/bin/bash

# Script to run database migration for adding department column
# This script assumes PostgreSQL is running via Docker Compose

echo "🚀 Starting database migration..."

# Check if docker-compose is available
if command -v docker-compose &> /dev/null; then
    echo "📦 Using docker-compose..."
    
    # Get the postgres service name from docker-compose
    POSTGRES_SERVICE="postgres"
    
    # Check if container is running
    if docker-compose ps | grep -q "$POSTGRES_SERVICE.*Up"; then
        echo "✅ PostgreSQL container is running"
        
        # Run migration script
        echo "📝 Executing migration script..."
        docker-compose exec -T postgres psql -U medinova_user -d medinova < migration_add_department_column.sql
        
        if [ $? -eq 0 ]; then
            echo "✅ Migration completed successfully!"
            echo "🔍 Verifying migration..."
            docker-compose exec -T postgres psql -U medinova_user -d medinova -c "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'doctors' AND column_name = 'department';"
        else
            echo "❌ Migration failed!"
            exit 1
        fi
    else
        echo "❌ PostgreSQL container is not running. Please start it first:"
        echo "   docker-compose up -d"
        exit 1
    fi
else
    echo "📦 Docker Compose not found. Trying direct psql connection..."
    
    # Try direct psql connection
    if command -v psql &> /dev/null; then
        echo "📝 Executing migration script via psql..."
        psql -h localhost -U medinova_user -d medinova -f migration_add_department_column.sql
        
        if [ $? -eq 0 ]; then
            echo "✅ Migration completed successfully!"
        else
            echo "❌ Migration failed!"
            exit 1
        fi
    else
        echo "❌ Neither docker-compose nor psql found."
        echo "Please run the migration manually:"
        echo "   psql -h localhost -U medinova_user -d medinova -f migration_add_department_column.sql"
        exit 1
    fi
fi

echo "✨ Done!"


