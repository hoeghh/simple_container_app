echo "Building commit $1"
cd simple-container-app
docker build -t hoeghh/flash-hello:$1 .
