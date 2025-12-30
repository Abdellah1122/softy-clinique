#!/bin/bash
# Script to setup and run the ML Service
# It handles the virtual environment creation to avoid "externally-managed-environment" errors on Mac.

# Ensure we are in the script's directory
cd "$(dirname "$0")"

echo "Stopping any existing uvicorn processes..."
pkill -f uvicorn || true

echo "Removing old virtual environment..."
rm -rf venv

echo "Creating new virtual environment..."
python3 -m venv venv

echo "Activating virtual environment..."
source venv/bin/activate

echo "Upgrading pip..."
pip install --upgrade pip

echo "Installing dependencies from requirements.txt..."
pip install -r requirements.txt

echo "Starting FastAPI server on port 8000..."
uvicorn main:app --reload --port 8000

#!/bin/bash
# Script to setup and run the ML Service
# It handles the virtual environment creation to avoid "externally-managed-environment" errors on Mac.

# Ensure we are in the script's directory
cd "$(dirname "$0")"

echo "Stopping any existing uvicorn processes..."
pkill -f uvicorn || true

echo "Removing old virtual environment..."
rm -rf venv

echo "Creating new virtual environment..."
python3 -m venv venv

echo "Activating virtual environment..."
source venv/bin/activate

echo "Upgrading pip..."
pip install --upgrade pip

echo "Installing dependencies from requirements.txt..."
pip install -r requirements.txt

echo "Starting FastAPI server on port 8000..."
uvicorn main:app --reload --port 8000
