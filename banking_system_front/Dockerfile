# syntax=docker/dockerfile:1

# ---- Build stage -----------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /app

# Install deps first so this layer is cached unless package*.json changes
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite inlines VITE_* variables into the bundle at build time (not runtime),
# so the backend URL has to be baked in here rather than passed at
# container startup. Default matches the .env already committed in this
# repo (http://localhost:8080) - override with
# --build-arg VITE_API_BASE_URL=... to point a build at a different backend.
ARG VITE_API_BASE_URL=http://localhost:8080
RUN echo "VITE_API_BASE_URL=${VITE_API_BASE_URL}" > .env

RUN npm run build

# ---- Runtime stage ----------------------------------------------------------
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=5s --start-period=15s --retries=10 \
    CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]