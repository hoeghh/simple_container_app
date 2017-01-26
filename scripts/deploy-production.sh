echo "Deploying app to production."
kubectl create namespace production

echo "
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      name: myapp
      labels:
        app: myapp
        run: myapp
    spec:
      containers:
      - name: myapp
        image: hoeghh/flash-hello:$1
        ports:
        - name: web
          containerPort: 5000
" > deployment/prod-dep-my-app.yaml

echo "
apiVersion: v1
kind: Service
metadata:
  labels:
    name: myapp
    app: myapp
  name: myapp
spec:
  selector:
    app: myapp
  type: ClusterIP
  ports:
  - name: myapp
    protocol: TCP
    port: 5000

" > deployment/prod-srv-my-app.yaml

echo "
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: myapp
spec:
  rules:
  - host: production.example.com
    http:
      paths:
      - path: /
        backend:
          serviceName: myapp
          servicePort: 5000

" > deployment/prod-ing-my-app.yaml

kubectl apply -f deployment/prod-dep-my-app.yaml --namespace=production --record
kubectl apply -f deployment/prod-srv-my-app.yaml --namespace=production --record
kubectl apply -f deployment/prod-ing-my-app.yaml --namespace=production --record
