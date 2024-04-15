# How to Deploy via helm

Please follow the following steps for releasing this service.

1. always use `email-service` as the helm release name
2. always increment the Chart Version in `email-chart/Chart.yaml`

## Release commands

Always `cd` into the `/helm` folder of this repo.

    helm upgrade email-service email-chart -f ./envs/dev.yaml -n dev
    helm upgrade email-service email-chart -f ./envs/qa1.yaml -n qa1
    helm upgrade email-service email-chart -f ./envs/prod.yaml -n prod

You can add `--dry-run` to the above commands to generate a yaml file output

## Flags to NOT USE when doing releases

1. `--create-namespace` - we dont want to accidentally deploy a wrong-versioned environment to the wrong cluster
2. `--install` (`-i`) - this will prevent typos in release names from creating a second copy of services in the cluster

# Release 'diff'ing

    helm upgrade email-service email-chart -f ./envs/dev.yaml -n dev --dry-run > diff.yaml
    [Delete the top of the file helm info and bottom "Notes" section, save]
    kubectl diff -f diff.yaml

# Rolling back

1. Abort the argo rollout first
2. checkout the tag of the last release, and do another `helm upgrade`

# Helpful commands

    helm ls

Show a list of helm releases. Recommend `--reverse` to list releases is desc order, and `-f "email"` to filter for just
email-service releases.

    helm history email-service

Show a list of the release history for just email-service

    helm install email-service email-chart -f ./envs/dev.yaml -n dev

Install the release to the cluster. This is only needed on a first deployment of a service into a cluster,
and `helm upgrade` should be used after that.

# NOT recommended commands

    helm rollback email-service [REVISION NUMBER]

This will rollback to the specified revision number (you can retrieve this from `helm ls`). If you omit `[REVISION]`,
this will rollback to the last release. You can add `--dry-run` to this command to have it print out all the yaml that
would be executed
It is NOT recommended due to a bug in helm regarding resources with the `"helm.sh/resource-policy": keep` annotation -
helm thinks the desired objects do not exist and therefore the rollback fails. Until this is fixed, we should always
rollback via argo or explicitly 'installing' the old version.
