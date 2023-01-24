#!/usr/bin/env sh
git pull --rebase && ./mvnw clean package && di && bi && pi && -run
