# How to use this Templated Helm Chart

1. use this template to create a new microservice
2. pick a name for your service. Find-replace `TEMPLATE_SVC_BASE` in these files with your service name, but do-not-include '-service'
   1. also rename the `-chart` folder to include this value
3. pick a DB name for your service. Find-replace `TEMPLATE_DB` in these files with your db name.
4. whatever you use for the package subpath of your service (ex, in `co.adhoclabs.business_service`, `business_service`), find-replace `TEMPLATE_PKG_PATH` with that value

Example:

    TEMPLATE_SVC_BASE = identity
    TEMPLATE_DB = identity_db
    TEMPLATE_PKG_PATH = identity_service

After doing find-replace, and releasing a 1.0.0 chart, you'd get :

    Service: 
        name: identity-service
        name: identity-service
    Configmaps:
         nginx-identity-service-1-0-0
            assumes dns entry {dev|qa1|qa2|prod}-identity.burnerapp.com
         identity-service-env-1-0-0
         identity-service-app-dot-conf-1-0-0
    Rollout:
         identity-service
            containers assumed to come from ECR repo named identity-service in the same region as our other ECR instances

Next steps for you:
1. edit dev/qa1/qa2/prod.yaml to include proper keys/secrets/db credentials/etc
   1. local.yaml should include dev creds unless there is some other config
   2. values.yaml can include dev defaults where sensible

You can delete this file after finishing this work.