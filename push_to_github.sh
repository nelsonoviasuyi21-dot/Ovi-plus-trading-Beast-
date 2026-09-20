#!/data/data/com.termux/files/usr/bin/bash

set -e

cd "$(dirname "$0")"

echo
echo "============================================================"
echo " OVI PLUS TRADING BEAST - GITHUB UPLOAD"
echo "============================================================"
echo

if ! command -v git >/dev/null 2>&1; then
    echo "Installing git..."
    pkg install git -y
fi

echo
read -p "Enter your GitHub repository URL: " REPO

if [ -z "$REPO" ]; then
    echo "No repository URL supplied."
    exit 1
fi

git init

git config user.name "Ovi Plus Trading Beast"
git config user.email "ovi-plus-trading-beast@users.noreply.github.com"

git branch -M main

git add .

git commit -m "Build advanced Ovi Plus Trading Beast APK"

if git remote get-url origin >/dev/null 2>&1; then
    git remote set-url origin "$REPO"
else
    git remote add origin "$REPO"
fi

git push -u origin main

echo
echo "============================================================"
echo " UPLOAD COMPLETE"
echo "============================================================"
echo
echo "GitHub Actions should now start the Android build."
echo
echo "Open the repository on GitHub."
echo
echo "Then:"
echo "Actions"
echo "  -> Build Ovi Plus Trading Beast APK"
echo "  -> latest successful run"
echo "  -> Artifacts"
echo "  -> Ovi-Plus-Trading-Beast-ADVANCED"
echo
