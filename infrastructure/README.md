# Infrastructure

- Local and single-host environments are defined in the root [`docker-compose.yml`](../docker-compose.yml).
- The frontend container's nginx configuration lives with the frontend
  ([`frontend/nginx.conf.template`](../frontend/nginx.conf.template)).
- Deployment procedure: [`docs/deployment.md`](../docs/deployment.md).

This folder is reserved for future deployment assets (TLS proxy config, backup scripts,
monitoring) as they become necessary.
