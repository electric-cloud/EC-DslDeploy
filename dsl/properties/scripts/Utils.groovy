def pluralForm(String objType) {
  switch (objType) {
    case "process":
      return 'processes'
    case "personaCategory":
      return 'personaCategories'
    default:
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

static String decode(String arg)
{
  String result = arg
  ["%2F": "/", "%5C": "\\"] .each {key, value ->
    result = result.replace(key, value)
  }
  return result
}

static String encode(String arg)
{
  String result = arg
  ["/": "%2F", "\\": "%5C"].each {key, value ->
    result = result.replace(key, value)
  }
  return result
}
