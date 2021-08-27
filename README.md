# microservice_template
This is a template for future microservices. New microservice repos should be copied from this one so that our basic infrastructure is consistent across services.

This template has a number of examples of how to perform different database actions and should not necessarily be taken as gospel for how to design an api - but rather as a resource for how to handle scenarios that may arise as we construct a new service.  

## postman collection
https://www.getpostman.com/collections/e7a9243edc39f2ab5ffd

##monitoring for your new service
###RDS
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
2. Open the script in the `misc` repo, located at `[repo]/scripts/cloudwatch/create_rds_db_cloudwatch_alarms.py`
3. edit the service name, rds instance name, and threshold values at the top of the script.
4. WARN THE `#prod-alerts` Slack channel that they will see a bunch of incoming alerts for the new service
5. run the script. (Note - if you mess anything up, re-running the script will properly update the alarms as long as you keep the service name the same)