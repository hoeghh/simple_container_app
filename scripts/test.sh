
responsecode="$(curl -s -k -I --header "Host: $1.example.com" https://kubemaster.kubernetes | head -n 1|cut -d$' ' -f2)"
if [ $responsecode == "200" ]; then
  echo "worked: $responsecode"
  exit 0
else
  echo "failed: $responsecode"
  exit 1
fi
# A very simple test
