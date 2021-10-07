
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

static def isIncluded(
        def includeObjects,
        def excludeObjects,
        def patternToCheck) {

    if (!includeObjects && !excludeObjects) {
        return true
    }

    def include = false
    def mostSpecificIncludePath = null
    if (includeObjects) {
        for(path in includeObjects) {
            if (path == patternToCheck) {
                return true
            }

            def pathElements = patternToCheck.split("/")

            def includePathElements = path.split("/")

            def min = pathElements.size() <=
                    includePathElements.size() ? pathElements.size() :
                    includePathElements.size()

            def match = true
            for (int i = 0; i < min; i++) {
                def includePathElement = includePathElements[i]
                def pathElement = pathElements[i]

                if (pathElement != includePathElement
                        && !(i == 2 && "*" == includePathElement)) {
                    // wildcard allowed just for the name of project
                    match = false
                    break
                }
            }

            if (match) {
                def newPath = new LastMatchPath(path)
                if (newPath.moreSpecific(mostSpecificIncludePath)){
                    mostSpecificIncludePath = newPath
                }
            }
        }
    }

    if (!includeObjects || mostSpecificIncludePath) {
        include = true
    }

    if (include && excludeObjects) {
        for(path in excludeObjects) {
            if (path == patternToCheck) {
                return false
            }

            def pathElements = patternToCheck.split("/")

            def excludePathElements = path.split("/")

            if (excludePathElements.size() > pathElements.size()) {
                // do not take into account more specific exclude path
                continue;
            }

            def exclude = true
            for (int i = 0; i < excludePathElements.size(); i++) {
                def excludePathElement = excludePathElements[i]
                def pathElement = pathElements[i]

                if (pathElement != excludePathElement &&
                        !(i == 2 && "*" == excludePathElement)) {
                    // wildcard allowed just for the name of project
                    exclude = false
                    break
                }
            }

            if (exclude) {

                if (new LastMatchPath(path).moreSpecific(mostSpecificIncludePath))
                    return false
            }
        }
    }

    return include
}

class LastMatchPath  {
    def pathElements;
    def path;

    LastMatchPath(String path) {
        this.path = path
        this.pathElements = path.split("/")
    }

    boolean moreSpecific(LastMatchPath lastMatchPath) {
        if (!lastMatchPath) {
            return true
        }

        if (path.startsWith("/projects")) {
            if (pathElements.size() > 2 && lastMatchPath.pathElements
                    .size() > 2) {

                if (pathElements[2] != '*' && lastMatchPath
                        .pathElements[2] == '*') {
                    return true
                }

                if (pathElements[2] == '*' && lastMatchPath
                        .pathElements[2] != '*') {
                    return false
                }
            }
        }

        return pathElements.size() >= lastMatchPath.pathElements.size()
    }
}