#!/bin/bash

if [ -z "$GITHUB_TOKEN" ]; then
  echo "Error: GITHUB_TOKEN environment variable is not set"
  echo "Set it with: export GITHUB_TOKEN=ghp_your_token_here"
  exit 1
fi

./gradlew clean
./gradlew droid-dex:assemble

echo -e "\n\nPublishing to GitHub Packages (lokal-app/device-telemetry-toolkit). Waiting for 5 seconds...\n\n"
sleep 5

./gradlew droid-dex:publishMavenPublicationToLokalGitHubPackagesRepository
