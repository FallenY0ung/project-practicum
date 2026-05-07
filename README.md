# HomeApp

Spring Boot приложение для умного дома и погодных рекомендаций.

Проект использует PostgreSQL, Kafka, Liquibase, Telegram Bot API, OpenRouter AI, Prometheus, Grafana и Kafka UI.
Приложение может запускаться локально через Docker Compose и в Kubernetes через Minikube + Helm.

---

## Стек технологий

- Java 25
- Spring Boot
- PostgreSQL
- Apache Kafka
- Liquibase
- Docker / Docker Compose
- Kubernetes / Minikube
- Helm
- Prometheus
- Grafana
- Kafka UI
- Telegram Bot API
- OpenRouter AI API
- Lens для визуального просмотра Kubernetes-кластера

---



# Запуск через Docker Compose

## 1. Создать `.env` файл

В корне проекта необходимо создать файл `.env`:

```env
POSTGRES_DB=homeapp
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

OPENROUTER_API_KEY=your_openrouter_api_key
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
```


## 2. Запустить проект

```bash
docker compose up --build
```

После запуска будут доступны:

```text
Приложение: http://localhost:8080
Kafka UI:   http://localhost:8081
Prometheus: http://localhost:9090
Grafana:    http://localhost:3000
```

Данные для входа в Grafana по умолчанию:

```text
login: admin
password: admin
```

## 3. Остановить Docker Compose

```bash
docker compose down
```

Если нужно удалить volumes:

```bash
docker compose down -v
```

---

# Запуск в Kubernetes через Minikube

## 1. Установить зависимости

Для macOS:

```bash
brew install minikube kubectl helm
```

Также должен быть установлен и запущен Docker Desktop.

Для визуального просмотра Kubernetes-кластера можно использовать Lens.

---

## 2. Запустить Minikube

Рекомендуемый запуск Minikube через Docker driver:

```bash
minikube start --driver=docker --cpus=4 --memory=8192
```

Проверить, что кластер запущен:

```bash
kubectl get nodes
```

Ожидаемый результат:

```text
NAME       STATUS   ROLES           AGE   VERSION
minikube   Ready    control-plane    ...   ...
```

Проверить текущий контекст:

```bash
kubectl config current-context
```

Должно быть:

```text
minikube
```

Если активен другой контекст:

```bash
kubectl config use-context minikube
```

---

## 3. Подключить Lens

Lens использует kubeconfig из файла `~/.kube/config`.

После запуска Minikube в Lens должен появиться кластер `minikube`.

В Lens полезно смотреть:

```text
Workloads -> Pods
Workloads -> Deployments
Workloads -> Jobs
Network   -> Services
Config    -> Secrets
Config    -> ConfigMaps
Storage   -> Persistent Volume Claims
Events
Logs
```

После деплоя проекта нужно выбрать namespace:

```text
homeapp
```

---

## 4. Собрать Docker-образы

В проекте используются два собственных Docker-образа:

```text
homeapp:latest              — Spring Boot приложение
homeapp-migrations:latest   — Liquibase миграции
```

Для MacBook на Apple Silicon лучше собирать под `linux/arm64`:

```bash
docker buildx build --platform linux/arm64 -t homeapp:latest --load .
docker buildx build --platform linux/arm64 -t homeapp-migrations:latest -f Dockerfile.migrations --load .
```

Если используется обычная архитектура, можно собрать так:

```bash
docker build -t homeapp:latest .
docker build -t homeapp-migrations:latest -f Dockerfile.migrations .
```

---

## 5. Загрузить образы в Minikube

Локальные Docker-образы  нужно загрузить командой `minikube image load`.

```bash
minikube image load homeapp:latest
minikube image load homeapp-migrations:latest
```

Также нужно загрузить сторонние образы, которые используются в Kubernetes:

```bash
docker pull postgres:18
docker pull apache/kafka:4.1.2
docker pull kafbat/kafka-ui:main
docker pull prom/prometheus:latest
docker pull grafana/grafana:latest

minikube image load postgres:18
minikube image load apache/kafka:4.1.2
minikube image load kafbat/kafka-ui:main
minikube image load prom/prometheus:latest
minikube image load grafana/grafana:latest
```

Проверить загруженные образы:

```bash
minikube image ls | grep homeapp
minikube image ls | grep postgres
minikube image ls | grep kafka
```

---

## 6. Передать секреты

Секреты не нужно хранить в `values.yaml`.

Перед запуском Helm chart нужно задать переменные окружения в терминале:

```bash
export OPENROUTER_API_KEY="your_openrouter_api_key"
export TELEGRAM_BOT_TOKEN="your_telegram_bot_token"
```

Проверить, что переменные заданы:

```bash
echo $OPENROUTER_API_KEY
echo $TELEGRAM_BOT_TOKEN
```

Эти значения попадут в Kubernetes Secret через Helm.


---

## 7. Запустить Helm chart

Запуск проекта в Kubernetes:

```bash
helm upgrade --install homeapp deploy/helm/homeapp \
  --namespace homeapp \
  --create-namespace \
  --set secrets.openrouterApiKey="$OPENROUTER_API_KEY" \
  --set secrets.telegramBotToken="$TELEGRAM_BOT_TOKEN" \
  --timeout 10m
```

Проверить pod-ы:

```bash
kubectl get pods -n homeapp
```

Ожидаемое состояние:

```text
homeapp-app          1/1 Running
homeapp-kafka        1/1 Running
homeapp-postgres     1/1 Running
homeapp-kafka-ui     1/1 Running
homeapp-prometheus   1/1 Running
homeapp-grafana      1/1 Running
homeapp-migrations   0/1 Completed
```

`homeapp-migrations` со статусом `Completed` — это нормально. Это Kubernetes Job, который выполняет Liquibase-миграции и завершает работу.

---

# Доступ к сервисам


```text
Приложение
Kafka UI
Prometheus
Grafana
```

## Вариант 1. Через `minikube service`

Посмотреть список сервисов:

```bash
kubectl get svc -n homeapp
```

Посмотреть URL сервисов Minikube:

```bash
minikube service list -n homeapp
```

Открыть сервисы:

```bash
minikube service homeapp-app -n homeapp
minikube service homeapp-kafka-ui -n homeapp
minikube service homeapp-prometheus -n homeapp
minikube service homeapp-grafana -n homeapp
```

Получить только URL:

```bash
minikube service homeapp-app -n homeapp --url
minikube service homeapp-kafka-ui -n homeapp --url
minikube service homeapp-prometheus -n homeapp --url
minikube service homeapp-grafana -n homeapp --url
```

На macOS с Docker driver такие команды могут держать туннель, поэтому терминал лучше не закрывать.

---

## Вариант 2. Через port-forward

Этот вариант больше похож на Docker Compose, потому что сервисы доступны на привычных localhost-портах.

Создать папку для скриптов:

```bash
mkdir -p scripts
```

Создать файл `scripts/k8s-forward.sh`:

```bash
cat > scripts/k8s-forward.sh <<'EOF'
#!/bin/bash

set -e

echo "App:        http://localhost:8080"
echo "Kafka UI:   http://localhost:8081"
echo "Prometheus: http://localhost:9090"
echo "Grafana:    http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop all forwards"

kubectl port-forward -n homeapp svc/homeapp-app 8080:8080 &
kubectl port-forward -n homeapp svc/homeapp-kafka-ui 8081:8080 &
kubectl port-forward -n homeapp svc/homeapp-prometheus 9090:9090 &
kubectl port-forward -n homeapp svc/homeapp-grafana 3000:3000 &

wait
EOF
```

Сделать скрипт исполняемым:

```bash
chmod +x scripts/k8s-forward.sh
```

Запустить:

```bash
./scripts/k8s-forward.sh
```

После этого сервисы будут доступны:

```text
Приложение: http://localhost:8080
Kafka UI:   http://localhost:8081
Prometheus: http://localhost:9090
Grafana:    http://localhost:3000
```

Чтобы остановить port-forward, нажать:

```text
Ctrl + C
```

---

# Проверка приложения

Healthcheck:

```text
http://localhost:8080/actuator/health
```

Prometheus-метрики приложения:

```text
http://localhost:8080/actuator/prometheus
```

Кастомная метрика количества запросов к ИИ:

```text
openrouter_ai_requests_total
```

Пример PromQL-запроса в Prometheus/Grafana:

```promql
sum(openrouter_ai_requests_total)
```

По статусам:

```promql
sum by (status) (openrouter_ai_requests_total)
```

Успешные запросы:

```promql
sum(openrouter_ai_requests_total{status="success"})
```

Ошибочные запросы:

```promql
sum(openrouter_ai_requests_total{status="error"})
```

---

# Grafana

После запуска открыть:

```text
http://localhost:3000
```

Данные для входа:

```text
login: admin
password: admin
```

Datasource для Prometheus:

```text
http://homeapp-prometheus:9090
```

Даже если Grafana открыта через `localhost:3000`, внутри Grafana нужно указывать Kubernetes service name, потому что Grafana работает внутри кластера и обращается к Prometheus по внутреннему DNS имени.

---

# Kafka UI

Kafka UI доступен по адресу:

```text
http://localhost:8081
```

Bootstrap server внутри Kubernetes:

```text
homeapp-kafka:9092
```

Если Kafka UI запущен через Helm chart, bootstrap server уже должен быть передан через переменную окружения:

```text
KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=homeapp-kafka:9092
```

---

# Просмотр логов

Раньше логи смотрелись через Docker Compose. В Kubernetes аналог — `kubectl logs`.

## Логи Spring Boot приложения

```bash
kubectl logs -f -n homeapp deploy/homeapp-app
```

## Логи предыдущего запуска приложения

Полезно, если pod перезапускался:

```bash
kubectl logs -n homeapp deploy/homeapp-app --previous
```

## Логи PostgreSQL

```bash
kubectl logs -f -n homeapp deploy/homeapp-postgres
```

## Логи Kafka

```bash
kubectl logs -f -n homeapp deploy/homeapp-kafka
```

## Логи Liquibase миграций

```bash
kubectl logs -n homeapp -l job-name=homeapp-migrations
```

## Логи Kafka UI

```bash
kubectl logs -f -n homeapp deploy/homeapp-kafka-ui
```

## Логи Prometheus

```bash
kubectl logs -f -n homeapp deploy/homeapp-prometheus
```

## Логи Grafana

```bash
kubectl logs -f -n homeapp deploy/homeapp-grafana
```

---

# Просмотр логов через Lens

В Lens:

```text
minikube -> namespace homeapp -> Workloads -> Pods -> выбрать pod -> Logs
```

Также в Lens можно смотреть:

```text
Events
Shell
Environment
Containers
Service
Deployment
Job
PVC
Secrets
ConfigMaps
```

---

# Полезные команды диагностики

Посмотреть pod-ы:

```bash
kubectl get pods -n homeapp
```

Посмотреть pod-ы в режиме наблюдения:

```bash
kubectl get pods -n homeapp -w
```

Посмотреть сервисы:

```bash
kubectl get svc -n homeapp
```

Посмотреть deployments:

```bash
kubectl get deployments -n homeapp
```

Посмотреть jobs:

```bash
kubectl get jobs -n homeapp
```

Посмотреть PVC:

```bash
kubectl get pvc -n homeapp
```

Подробное описание pod-а:

```bash
kubectl describe pod <pod-name> -n homeapp
```

События namespace:

```bash
kubectl get events -n homeapp --sort-by=.metadata.creationTimestamp
```

Проверить Helm release:

```bash
helm list -n homeapp
```

Посмотреть, что Helm сгенерирует из chart-а:

```bash
helm template homeapp deploy/helm/homeapp
```

Проверить Helm chart:

```bash
helm lint deploy/helm/homeapp
```

---



# Удаление приложения из Minikube

Удалить Helm release:

```bash
helm uninstall homeapp -n homeapp
```

Удалить namespace полностью:

```bash
kubectl delete namespace homeapp
```

Остановить Minikube:

```bash
minikube stop
```

Полностью удалить Minikube-кластер:

```bash
minikube delete
```

---

# Деплой на реальный сервер

Minikube — это локальный Kubernetes-кластер для разработки и проверки.

После переноса проекта в Helm chart его стало проще перенести на реальный Kubernetes-кластер, например:

```text
k3s
microk8s
kubeadm
managed Kubernetes в облаке
```

Но для реального сервера нужно сделать несколько дополнительных шагов.

## 1. Запушить Docker-образы в registry

В Minikube образы загружались так:

```bash
minikube image load homeapp:latest
```

На реальном сервере так не делают. Обычно используется Docker Registry:

```text
Docker Hub
GitHub Container Registry
GitLab Container Registry
Yandex Container Registry
```

Пример имён образов:

```text
ghcr.io/username/homeapp:1.0.0
ghcr.io/username/homeapp-migrations:1.0.0
```

После этого в `values.yaml` нужно указать registry-образы:

```yaml
app:
  image:
    repository: ghcr.io/username/homeapp
    tag: "1.0.0"

migrations:
  image:
    repository: ghcr.io/username/homeapp-migrations
    tag: "1.0.0"
```

## 2. Настроить внешний доступ

В Minikube использовались:

```text
minikube service
kubectl port-forward
```

На реальном сервере обычно используется:

```text
Ingress
домен
HTTPS
TLS-сертификат
```

Например:

```text
https://homeapp.example.com
https://grafana.example.com
https://kafka-ui.example.com
```

## 3. Передать секреты безопасно

Для учебного проекта можно передавать секреты через `--set`.

Для production лучше использовать:

```text
Kubernetes Secret
Sealed Secrets
External Secrets
Vault
CI/CD variables
```

## 4. Подумать о PostgreSQL и Kafka

Для учебного проекта PostgreSQL и Kafka можно держать внутри Kubernetes.

В production часто используют managed-сервисы или отдельные кластеры:

```text
PostgreSQL -> managed database / отдельный сервер
Kafka      -> managed Kafka / отдельный Kafka-кластер
```

---

# Итог

Проект может запускаться двумя способами:

```text
Docker Compose       -> удобно для локальной разработки
Minikube + Helm      -> production-like запуск в Kubernetes
```

В Kubernetes были перенесены:

```text
Spring Boot приложение
PostgreSQL
Kafka
Liquibase миграции
Kafka UI
Prometheus
Grafana
Secrets
PVC
ConfigMap
```

Проект теперь можно описывать как приложение, адаптированное под Kubernetes-окружение с использованием Helm.

Для локальной разработки Docker Compose остаётся самым удобным вариантом, а Kubernetes/Minikube показывает готовность проекта к более серьёзному способу развёртывания.
