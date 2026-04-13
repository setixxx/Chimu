FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon

FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/

COPY --from=builder /app/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]