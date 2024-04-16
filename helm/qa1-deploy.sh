CURRENT_CTX=$(cat ~/.kube/config | grep "current-context:" | sed "s/current-context: //");

if [ "$CURRENT_CTX" == "qa-eks" ]; then
  if [[ $# != 0 && $1 == "--dry-run" ]]; then
    helm upgrade business-service business-chart -f ./envs/qa1.yaml -n qa1 --dry-run;
  else
    echo "Deploying to qa1";
    helm upgrade business-service business-chart -f ./envs/qa1.yaml -n qa1;
  fi
else
    echo "Can't deploy to 'qa1' because you're using kube context: ${CURRENT_CTX} - are you deploying to the right environment?";
    exit 1;
fi
