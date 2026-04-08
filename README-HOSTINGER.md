# Deploy on Hostinger

This project includes a production-oriented Docker Compose file for VPS deployment:

- `docker-compose.hostinger.yml`
- `.env.hostinger.example`
- `scripts/deploy-hostinger.sh`

## 1. Prepare the VPS

Install Docker and the Docker Compose plugin on the Hostinger VPS.

## 2. Copy the project

Clone the repository on the server:

```bash
git clone https://github.com/tonelima/ksentinel.git
cd ksentinel
```

## 3. Configure environment variables

Create the production env file:

```bash
cp .env.hostinger.example .env.hostinger
```

Change at least:

- `APP_PASSWORD`
- `JASYPT_PASSWORD`
- `DB_PASSWORD`

## 4. Run deployment

```bash
chmod +x scripts/deploy-hostinger.sh
./scripts/deploy-hostinger.sh
```

## 5. Check status

```bash
docker compose --env-file .env.hostinger -f docker-compose.hostinger.yml ps
docker compose --env-file .env.hostinger -f docker-compose.hostinger.yml logs -f app
```

## Notes

- The Hostinger compose file does not expose PostgreSQL publicly.
- The application is exposed on `APP_PORT`.
- Put Nginx, Traefik or Hostinger reverse proxy in front of the app if you need HTTPS and domain routing.
