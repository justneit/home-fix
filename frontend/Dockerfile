# syntax=docker/dockerfile:1

FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY . .
RUN npm run build

FROM nginx:1.29-alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Rendered by the nginx image's envsubst entrypoint (BACKEND_URL).
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
ENV BACKEND_URL=http://backend:8080
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --retries=5 CMD wget -qO- http://127.0.0.1/healthz || exit 1
