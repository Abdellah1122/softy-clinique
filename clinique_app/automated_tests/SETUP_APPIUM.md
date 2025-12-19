# Appium Setup Guide

To run the automated iOS login tests, follow these steps.

## 1. Prerequisites
You need **Node.js** and **Python** installed.

### Install Appium Server
```bash
npm install -g appium
appium driver install xcuitest
```

### Install Python Client
```bash
pip3 install Appium-Python-Client
```

## 2. Prepare the App
Ensure your Flutter app is built for the simulator (Debug mode is fine for local testing).
```bash
cd clinique_app
flutter build ios --simulator
```

## 3. Run the Test
1.  Start the Appium Server in a terminal:
    ```bash
    appium
    ```
2.  Run the Python script in another terminal:
    ```bash
    python3 automated_tests/appium_login_test.py
    ```

## 4. Reports
-   Screenshots will be saved in `clinique_app/automated_tests/test_reports/screenshots`.
-   You can attach these images to your report as proof of testing.
