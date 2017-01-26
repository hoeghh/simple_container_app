echo "Building commit $1"
docker build -t hoeghh/flash-hello:$1 -f simple-container-app/Dockerfile
