def summaryString (def counters) {
  String summary=""

  counters.each { k,v ->
    summary += v? "$v $k\n" : "no $k\n"
  }
  return summary
}
