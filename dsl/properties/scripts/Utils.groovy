
class Const {

  static Map<String, String> ENCODE_MAP = [
      "/": "%2F", "\\": "%5C", ":": "%3A", "*": "%2A", "?": "%3F", "\"": "%22",
      "<": "%3C", ">": "%3E", "|": "%7C"
    ].asUnmodifiable()

  static Map<String, String> DECODE_MAP = ENCODE_MAP
      .collectEntries {key, value -> [(value) : key]}.asUnmodifiable()

}

static String encode(String arg)
{
  String result = arg
  Const.ENCODE_MAP.each {key, value ->
    result = result.replace(key, value)
  }
  return result
}

static String decode(String arg)
{
  String result = arg
  Const.DECODE_MAP.each {key, value ->
    result = result.replace(key, value)
  }
  return result
}

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
