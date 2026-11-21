@echo off
title NET E-Commerce - Live Cloudflare Tunnel
echo ================================================================
echo    Starting Next Era Technologies (NET) Grid + Cloudflare Tunnel
echo ================================================================
echo.
echo [1/2] Starting Spring Boot Server on port 8081 in a new window...
start "NET Grid Server (Port 8081)" cmd /k "cd /d "%~dp0" && gradlew.bat bootRun"

echo [2/2] Waiting 12 seconds for the server to initialize...
timeout /t 12 /nobreak >nul

echo.
echo ================================================================
echo    Launching Cloudflare Tunnel (Your public HTTPS link is below)
echo ================================================================
echo.
npx --yes cloudflared tunnel --url http://localhost:8081
pause
