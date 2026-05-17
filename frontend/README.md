# Plane Battle Frontend

React + TypeScript client for the Plane Battle MVP.

## Local Development

```bash
npm install
npm run dev
```

The app reads `VITE_WS_URL` for the backend WebSocket URL. If it is not set, it uses `ws://localhost:8090/ws/game`.

For production deployments, configure `VITE_WS_URL` in the hosting platform environment variables. See `.env.production.example` for the expected format.
