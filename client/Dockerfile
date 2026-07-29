# ---- Build stage ----
FROM node:20-alpine AS build

WORKDIR /app

# Copy only the manifest files first so `npm install` is cached as its own
# layer - it only re-runs when package.json/package-lock.json actually
# change, not on every source edit.
COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build

# ---- Run stage ----
FROM nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
