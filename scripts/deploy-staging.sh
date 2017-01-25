echo "Deploying app to staging."
kubectl create namespace $1
kubectl create -f deployment/dep-my-app.yaml --namespace=$1
kubectl create -f deployment/srv-my-app.yaml --namespace=$1

echo "
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: myapp
spec:
  rules:
  - host: $1.example.com
    http:
      paths:
      - path: /
        backend:
          serviceName: myapp
          servicePort: 5000

" > deployment/ing-my-app.yaml
kubectl create -f deployment/ing-my-app.yaml --namespace=$1
