CURRENT_CTX=$(cat ~/.kube/config | grep "current-context:" | sed "s/current-context: //");

if [ "$CURRENT_CTX" == "dev-eks" ]; then
  if [[ $# != 0 && $1 == "--dry-run" ]]; then
    helm upgrade email-service email-chart -f ./envs/dev.yaml -n dev --dry-run;
  else
    echo "Deploying to dev";
    helm upgrade email-service email-chart -f ./envs/dev.yaml -n dev;
  fi
else
    echo "Can't deploy to 'dev' because you're using kube context: ${CURRENT_CTX} - are you deploying to the right environment?";
    exit 1;
fi
