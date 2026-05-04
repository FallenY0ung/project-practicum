#!/bin/bash

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
