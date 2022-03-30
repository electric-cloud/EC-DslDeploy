/* ###########################################################################
#
#  Utils: Utility groovy functions accessible throughout the plugin
#
#  Author: known
#
#  Copyright 2017-2022 Electric-Cloud Inc.
#
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.
#
# History
# ---------------------------------------------------------------------------
# 2022-03-25 mayasse  Add functions to get DSL parameters,
#                     modify and delete commands from export file/folder paths
# 2019-??-?? unknown  Initial version
############################################################################ */

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

def singularForm(String pluralForm) {
    def result = "";
    if (pluralForm.endsWith("ies")) {
        result = pluralForm[0..-4] + "y"
    } else if (pluralForm.endsWith("sses")) {
        result = pluralForm[0..-3]
    } else if (pluralForm.endsWith("s")) {
        result = pluralForm[0..-2]
    } else {
        result = pluralForm
    }
    return result
}
def pluralToParameterName(String pluralForm) {
    def result = singularForm((String)pluralForm) + "Name"
    //  There are always exceptions to the rules
    //   EmailNotifier uses "notifierName"
    if (pluralForm == "emailNotifiers") {
        result = "notifierName"
    }
    return result
}
def pathToParameterList(String filePath) {
    def result = [];
    def pathParts = [];
    pathParts = filePath.tokenize(File.separatorChar)
    def isProperty = false;
    for (def i = 0; i < pathParts.size() - 1; i = i+2) {
        // For properties do NOT include nested property sheets
        //   But DO include the property name - which is the last piece
        if (pathParts[i] == "properties") {
            isProperty = true;
            result.add(pluralToParameterName(pathParts[i]) + ":'" + pathParts[-1].take(pathParts[-1].lastIndexOf('.')) + "'")
            break;
        }
        // For resources and resource Pools the *.dsl file is the object name
        if (pathParts[i] == "resources" || pathParts[i] == "resourcePools") {
            result
                .add(pluralToParameterName(pathParts[i]) + ":'" + pathParts[-1]
                    .take(pathParts[-1]
                        .lastIndexOf('.')) + "'")
        } else if (filePath.endsWith("/deployerApplication.dsl") && pathParts[i] == "deployerApplications") {
            result.add("applicationName:'" + pathParts[i + 1] + "'")
        } else {
            result.add(pluralToParameterName(pathParts[i]) + ":'" + pathParts[i + 1] + "'")
        }
    }
    // For Properties add the path parameter to properly locate the property in nested sheets
    if (isProperty) {
        result.add("path:'" + (!filePath.startsWith("/") ? "/" : "") + filePath.take(filePath.lastIndexOf('.')) + "'")
    }
    return result
}
def  pathToObjectName(String filePath) {
    def result = "";
    def pathParts = [];
    pathParts = filePath.tokenize(File.separatorChar)
    /* Properties values are stored the leaf-node which is a text file (*.txt)
       And the text file name is the property name.
       In all case not *.dsl (including porperties) the leaf-node also names the object.

       Resources and ResourcePools use the .dsl file to determine the object's name

       Some steps have 2 files a .dsl file and a .cmd, .groovy, .pl or .sh file.
       In the case of such steps we don't mind if we stumble upon the same object name and delete it twice.
     */
    if (!filePath.endsWith(".dsl") || pathParts[1] == "resources" || pathParts[1] == "resourcePools") {
        result = pathParts[-1]
        result = result
            .take(result
                .lastIndexOf('.')) // Strip file extension
    } else {
        // All other objects derive their name from the folder above the DSL file:
        //   .../an_object_name/object.dsl
        result = pathParts[-2]
    }
    return result;
}
def pathToDeleteCommand(String filePath){
    return pathToCommand(filePath, "delete")
}
def pathToModifyCommand(String filePath){
    return pathToCommand(filePath, "modify")
}

def pathToCommand(String filePath, String command) {
    def result = command
    def pathParts = [];
    pathParts = pathParts + filePath.tokenize(File.separatorChar)
    if (filePath.contains("/properties/")) {
        result = result + "Property"
    } else if (!filePath.endsWith(".dsl")) {
        result = ""
    } else if (filePath.contains("/steps/")) {
        if (filePath.contains("/processes/")) {
            result = result + "ProcessStep"
        }
        else if (filePath.contains("/procedures/")) {
            result = result + "Step"
        }
        else {
            result = ""
        }
    } else if (filePath.contains("/resources/") || filePath.contains("/resourcePools/")) {
        result = result + singularForm((String) pathParts[-2]).capitalize()
    } else if (filePath.contains("/deployerApplications/") && command == "delete") {
        result = "removeDeployerApplication"
    } else {
        result = result + singularForm((String) pathParts[-3]).capitalize()
    }
    return result
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