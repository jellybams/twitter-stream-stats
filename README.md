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
* handling of stats for retweets and quoted tweets needs clarification, this implementation ignores those types and only aggregates stats for original tweets - this can easily be expanded on based on requirements clarification
* `top domains in tweets` stat takes into account domains from media that has been attached to the tweet, clarification is needed on whether this behavior is correct
* consumption / processing semantics - does reprocessing of tweets already seen matter? do we need exactly once processing? current implementation does not guard for duplicate processing
* stats implementation is naive and continually aggregates data for the entire run time, could be improved with a sliding window implementation
* horizontal scalability of tweet processing can be achieved by distributing the `StatsProcessorActor` instances across multiple nodes
* even though this implementation is basic most actors are still written as finite state machines so as to be easier to expand on 

## Path to production

As it stands this application is not production ready. In order to get to a production ready state at least the following concerns should be accounted for:
* improve test coverage
* handle stream disconnects & warnings/errors
* implement graceful shutdown
* load test to ensure correct configuration and capacity for processing full twitter stream
* integrate metrics into APM software
* ensure logging can be consumed by monitoring software
