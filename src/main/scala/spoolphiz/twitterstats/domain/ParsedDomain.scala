package spoolphiz.twitterstats.domain


case class ParsedDomain(domain: String, isImage: Boolean = false, fromMedia: Boolean = false)
