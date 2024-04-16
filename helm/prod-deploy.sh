CURRENT_CTX=$(cat ~/.kube/config | grep "current-context:" | sed "s/current-context: //");

if [ "$CURRENT_CTX" == "prod-eks" ]; then
  if [[ $# != 0 && $1 == "--dry-run" ]]; then
    helm upgrade business-service business-chart -f ./envs/prod.yaml -n prod --dry-run;
  else
    echo "Deploying to prod";
    helm upgrade business-service business-chart -f ./envs/prod.yaml -n prod;
  fi
else
    echo "Can't deploy to 'prod' because you're using kube context: ${CURRENT_CTX} - are you deploying to the right environment?";
    exit 1;
fi
