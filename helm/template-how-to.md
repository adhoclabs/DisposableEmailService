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


1. create IAM Roles as needed (see below section)
2. edit dev/qa1/qa2/prod.yaml to include proper keys/secrets/db credentials/etc
3. local.yaml should include dev creds unless there is some other config
4. values.yaml can include dev defaults where sensible
5. After the first deployment (helm install) in each cluster, you will need to create a Route53 A-record alias to the loadbalancer that is autocreated for the k8s Service.

You can delete this file after finishing this work.

### Setting up IAM Roles for your Service
In order to make use of "service account" auth for your service when deployed into our kubernetes clusters, you need to create a properly made IAM Role in AWS.

1. Set up an IAM _policy_ as you normally would - you probably want 3 policies, something like: `dev-TEMPLATE_SVC_BASE-service-policy`, `qa1-TEMPLATE_SVC_BASE-service-policy`, `prod-TEMPLATE_SVC_BASE-service-policy`
2. Edit and run the script located in the misc repo at `[root]/scripts/iam/create-service-account-iam-role.py`

   Note: The script assumes the role name will be `[dev/qa1/prod]-TEMPLATE_SVC_BASE-sa`, and that the name of the service account (see `templates/service-account.yaml`) will be `TEMPLATE_SVC_BASE-service`. Be aware if you do something different.
