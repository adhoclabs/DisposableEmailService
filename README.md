# microservice_template
This is a template for future microservices. New microservice repos should be copied from this one so that our basic infrastructure is consistent across services.

This template has a number of examples of how to perform different database actions and should not necessarily be taken as gospel for how to design an api - but rather as a resource for how to handle scenarios that may arise as we construct a new service.  

## postman collection
https://www.getpostman.com/collections/e7a9243edc39f2ab5ffd

## monitoring for your new service
### RDS
If your service has an RDS instance that we will need to add monitoring to in Prod...
#### Create a "tactical" DB Dashboard
1. Log into the AWS console
2. Go to Cloudwatch
3. Go to Dashboards
4. Click an existing dashbaord (ex [store-service-prod-tactical](https://us-west-2.console.aws.amazon.com/cloudwatch/home?region=us-west-2#dashboards:name=store-service-prod-tactical))
5. in the "Actions" menu at the top, choose "Save As"
6. name your service (probably _your-service_-prod-tactical) and save
7. on your new dashboard, in the "Actions" menu, choose "View/edit source"
8. Do a text find/replace on the DB instance identifier (ex, Rename all instances of `prod-store-service` to `prod-your-rds-instance-name`)
9. Save.
#### Setting up initial Cloudwatch Alarms
1. authenticate with aws-mfa on the command line
2. Open the RDS script in the `misc` repo, located at `[repo]/scripts/cloudwatch`
3. edit the service name, rds instance name, and threshold values at the top of the script.
4. WARN THE `#prod-alerts` Slack channel that they will see a bunch of incoming alerts for the new service
5. run the script. (Note - if you mess anything up, re-running the script will properly update the alarms as long as you keep the service name the same)

#### Setting up pganalyze
1. From the main pganalyze site, click the server dropdown, then click "+ Add Server"
2. Follow steps 1 and 2 to:
    1. enable the extension
    2. create a user (save the password for a later step!)
    3. create a schema
    4. create or replace a function
3. Skip the Step 3 screen (adding IAM policy)
4. On the Step 4 screen, choose docker container
    1. You will want to pull the container image locally in order to test the connection with pganalyze
5. On the Step 5 screen
    1. In the Kubernetes repo/manifests/prod.burner.cc/pganalyze folder, copy an existing pganalyze file (ex, collector-telephony-prod.yml) and rename things to match your new service
    2. Copy the relevant details into the `env` file described on the pganalyze instructions - we will use this to run the container locally and verify everything works
    3. Run the test command
        1. If you get an error like 'no route to host' on the 'Testing statisctics collection...' or 'Testing activity snapshots...', check the postgres web site - it may have correctly worked anyways.
    4. Apply the yaml to the cluster.
6. Log Insights - these should be included as long as you use the RDS name as the host, or you set the AWS_INSTANCE_ID environment variable on the collector
