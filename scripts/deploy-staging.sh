echo "Deploying app to staging."
kubectl create namespace $1

echo "
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: myapp
  namespace: $1
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
" > deployment/dep-my-app.yaml

echo "
apiVersion: v1
kind: Service
metadata:
  labels:
    name: myapp
    app: myapp
  name: myapp
  namespace: $1
spec:
  selector:
    app: myapp
  type: ClusterIP
  ports:
  - name: myapp
    protocol: TCP
    port: 5000

" > deployment/srv-my-app.yaml

echo "
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: myapp
  namespace: $1
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

kubectl create -f deployment/dep-my-app.yaml --namespace=$1
kubectl create -f deployment/srv-my-app.yaml --namespace=$1
kubectl create -f deployment/ing-my-app.yaml --namespace=$1
