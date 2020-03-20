# Twitter Stream Stats

Consumes twitter's sample status stream and calculates stats. Stats are printed in the terminal via the `StatsConsolePrinter`.

## Running the app

To run the application first make sure the environment contains the following environment variables:
* TWITTER_CONSUMER_KEY
* TWITTER_CONSUMER_SECRET
* TWITTER_ACCESS_TOKEN
* TWITTER_ACCESS_TOKEN_SECRET
 
After environment variables are made available run `sbt run` from the repository root to start the application.

Tests can be run via `sbt test`.

## Design considerations
* 


## Path to production

As it stands this application is not production ready. In order to get to a production ready state at least the following concerns should be accounted for:
* improve test coverage
* handle stream disconnects & warnings/errors
* implement graceful shutdown
* distribute processing and serving of stats across multiple nodes
* load test to ensure correct configuration and capacity for processing full twitter stream
* allow access via JMX and integrate metrics into APM software
* ensure logging and performance metrics can be consumed by monitoring software
