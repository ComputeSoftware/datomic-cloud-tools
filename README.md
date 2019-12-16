# Datomic Cloud Tools

Some CLI tools to make working with Datomic Cloud easier.

## Prerequisites

Install [Clojure CLI tools](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools) and [AWS CLI](https://aws.amazon.com/cli/).

## Installation

The installation simply creates a bash script called `dc-tools.sh` that calls this library via the Clojure CLI tools.

```shell script
echo "clojure -Sdeps '{:deps {compute/datomic-cloud-tools {:git/url \"https://github.com/ComputeSoftware/datomic-cloud-tools.git\" :sha \"d129439d84987b46928d7b93d1db6a280e694834\"}}}' -m dc-tools \$@" > dc-tools.sh
```

## Deleting Datomic Cloud System Durable Storage

Datomic Cloud provides documentation on how to [delete a system](https://docs.datomic.com/cloud/operation/deleting.html). 
This process is, however, quite manual. This does not integrate well with infrastructure-as-code tooling and does not really
fit in with the automate-everything sort of mentality.

This script will automate the [steps to delete the durable storage](https://docs.datomic.com/cloud/operation/deleting.html#deleting-storage) 
a Datomic Cloud system uses. Running the below commandwill run through all the steps using the 

```shell script
./dc-tools.sh delete-storage [datomic-system]
```

## Listing Running Datomic Systems

This command will list all Datomic Systems that are current running in your AWS account.

```shell script
./dc-tools.sh list-running-systems
```