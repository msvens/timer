echo $1
curl -i \
  --request POST \
  --header "Content-type: application/json" \
  --data '{"title":"'"$1"'"}' \
  http://localhost:9000/timers
echo
