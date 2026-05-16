#!/bin/bash

echo "🚀 Starting deployment..."

# Build React
echo "📦 Building React app..."
cd src/main/frontend
npm install
npm run build

# Build Spring Boot
echo "📦 Building Spring Boot app..."
cd ../../../
mvn clean package -DskipTests

# Run tests
echo "🧪 Running tests..."
mvn test

# Stop existing container
echo "🛑 Stopping existing container..."
docker stop project-app || true
docker rm project-app || true

# Run new container
echo "🚀 Starting new container..."
docker run -d \
  --name project-app \
  -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/project_management_db \
  -e JWT_SECRET=$JWT_SECRET \
  project-management-app:latest

echo "✅ Deployment complete!"