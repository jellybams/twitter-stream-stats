# Twitter Stream Stats

Consumes twitter's sample status stream and calculates stats. Stats are printed in the terminal every three seconds via the `StatsConsolePrinter`. Sample console output:


    =========================================================
    total tweets processed: 88248
    total run time (seconds): 1267
    average tweets (second): 69
    average tweets (minute): 4179
    average tweets (hour): 250744
    top emojis: 😂 -> 3558  😭 -> 2759  ❤ -> 1589
    tweets with emoji: 17061.0
    tweets with emoji (%): 19.33%
    top hashtags: WHYRUep9 -> 620  คั่นกูEP5 -> 1185  준면이와의_평생을_약속해 -> 3709
    tweets with url (%): 27.51%
    tweets with image url (%): 22.35%
    top domains: pbs.twimg.com -> 26747  twitter.com -> 575  youtu.be -> 360
    =========================================================
    

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
