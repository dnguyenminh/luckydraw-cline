#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Setting up Lucky Draw frontend application...${NC}\n"

# Check if node is installed
if ! command -v node &> /dev/null; then
    echo -e "${RED}Node.js is not installed. Please install Node.js and try again.${NC}"
    exit 1
fi

# Check node version
NODE_VERSION=$(node -v)
REQUIRED_VERSION="v18.0.0"

if [[ $NODE_VERSION < $REQUIRED_VERSION ]]; then
    echo -e "${RED}Node.js version must be >= ${REQUIRED_VERSION}. Current version: ${NODE_VERSION}${NC}"
    exit 1
fi

# Install dependencies
echo -e "${YELLOW}Installing dependencies...${NC}"
npm install

# Check if environment files exist
if [ ! -f "src/environments/environment.ts" ]; then
    echo -e "${YELLOW}Creating environment.ts file...${NC}"
    cp src/environments/environment.example.ts src/environments/environment.ts
fi

if [ ! -f "src/environments/environment.prod.ts" ]; then
    echo -e "${YELLOW}Creating environment.prod.ts file...${NC}"
    cp src/environments/environment.example.ts src/environments/environment.prod.ts
    
    # Update production settings
    sed -i 's/production: false/production: true/' src/environments/environment.prod.ts
    sed -i 's|http://localhost:8080/api|/api|' src/environments/environment.prod.ts
    sed -i 's/enableDebugTools: true/enableDebugTools: false/' src/environments/environment.prod.ts
    sed -i 's/logLevel: .debug./logLevel: "error"/' src/environments/environment.prod.ts
fi

# Create .gitignore if it doesn't exist
if [ ! -f ".gitignore" ]; then
    echo -e "${YELLOW}Creating .gitignore file...${NC}"
    cat > .gitignore << EOL
# See http://help.github.com/ignore-files/ for more about ignoring files.

# Compiled output
/dist
/tmp
/out-tsc
/bazel-out

# Node
/node_modules
npm-debug.log
yarn-error.log

# IDEs and editors
.idea/
.project
.classpath
.c9/
*.launch
.settings/
*.sublime-workspace
.vscode/*
!.vscode/settings.json
!.vscode/tasks.json
!.vscode/launch.json
!.vscode/extensions.json

# Visual Studio Code
.vscode/*
!.vscode/settings.json
!.vscode/tasks.json
!.vscode/launch.json
!.vscode/extensions.json
.history/*

# Miscellaneous
/.angular/cache
.sass-cache/
/connect.lock
/coverage
/libpeerconnection.log
testem.log
/typings

# System files
.DS_Store
Thumbs.db

# Environment files
/src/environments/environment.ts
/src/environments/environment.prod.ts
EOL
fi

# Make scripts executable
chmod +x setup.sh

echo -e "\n${GREEN}Setup completed successfully!${NC}"
echo -e "${YELLOW}You can now start the development server with:${NC} npm start"
echo -e "${YELLOW}To build for production:${NC} npm run build --prod"