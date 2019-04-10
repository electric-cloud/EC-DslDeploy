def pluralForm(String objType) {
  if (objType == "process") {
    return 'processes'
  } else {
    return objType + 's'
  }
}

def summaryString (def counters) {
  String summary=""

  counters.each { k,v ->
    summary += v? "$v " + pluralForm(k) + "\n" : "no $k\n"
  }
  return summary
}
