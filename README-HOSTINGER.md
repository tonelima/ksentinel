# Deploy on Hostinger

This project includes a production-oriented Docker Compose file for VPS deployment:

- `docker-compose.hostinger.yml`
- `.env.hostinger.example`
- `scripts/deploy-hostinger.sh`

The compose stack starts PostgreSQL, the Spring Boot API and the Vite/React frontend.

## 1. Prepare the VPS

Install Docker and the Docker Compose plugin on the Hostinger VPS.

## 2. Copy the project

Clone the repository on the server:

```bash
git clone https://github.com/tonelima/ksentinel.git
cd ksentinel
```

Clone the frontend repository next to this directory. The default compose setting expects:

```text
../KSentinel-Web
```

If you use another path, change `FRONTEND_CONTEXT` in `.env.hostinger`.

## 3. Configure environment variables

Create the production env file:

```bash
cp .env.hostinger.example .env.hostinger
```

Change at least:

- `JASYPT_PASSWORD`
- `JWT_SECRET`
- `DB_PASSWORD`

Optional frontend settings:

- `FRONTEND_PORT`: public port for the web UI, defaults to `80`
- `FRONTEND_CONTEXT`: path to the frontend project, defaults to `../KSentinel-Web`

## 4. Run deployment

```bash
chmod +x scripts/deploy-hostinger.sh
./scripts/deploy-hostinger.sh
```

## 5. Check status

```bash
docker compose --env-file .env.hostinger -f docker-compose.hostinger.yml ps
docker compose --env-file .env.hostinger -f docker-compose.hostinger.yml logs -f app
docker compose --env-file .env.hostinger -f docker-compose.hostinger.yml logs -f web
```

## Notes

- The Hostinger compose file does not expose PostgreSQL publicly.
- The API is exposed on `APP_PORT`.
- The frontend is exposed on `FRONTEND_PORT` and proxies `/api` to the API container.
- Put Nginx, Traefik or Hostinger reverse proxy in front of the app if you need HTTPS and domain routing.
