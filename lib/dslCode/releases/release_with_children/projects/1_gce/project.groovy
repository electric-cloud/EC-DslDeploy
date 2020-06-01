/**
1. The dsl was generated using GCE plugin.
2. Update properties on the generated dsl so that it is considered as a project rather than plugin
3. Empty out procedure steps(except for deployService which calls flowserver) in the generated dsl so that procedure steps are all dummy placeholders
4. run evalDsl on this dsl
*/
project '1_gce', {
  description = 'Integrates with the Google Container Engine to run Docker containers on the Google Cloud Platform.'
  resourceName = null
  workspaceName = null

  credential 'gce_test_config', userName: 'serviceAcct', {
    passwordRecoveryAllowed = null
  }

  procedure 'CreateConfiguration', {
    description = 'Creates a configuration for Google Container Engine'
    jobNameTemplate = null
    resourceName = null
    timeLimitUnits = null
    workspaceName = null

    formalParameter 'config', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Configuration:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'credential', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'JSON containing Service Account key pair:'
      orderIndex = null
      required = '1'
      type = 'credential'
    }

    formalParameter 'desc', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Description:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    step 'createConfiguration', {
      alwaysRun = '0'
      broadcast = '0'
      command = '''#
#  Copyright 2016 Electric Cloud, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

#########################
## createcfg.pl
#########################

use ElectricCommander;
use ElectricCommander::PropDB;

use constant {
               SUCCESS => 0,
               ERROR   => 1,
             };

my $opts;

## get an EC object
my $ec = new ElectricCommander();
$ec->abortOnError(0);

## load option list from procedure parameters
my $x       = $ec->getJobDetails($ENV{COMMANDER_JOBID});
my $nodeset = $x->find(\'//actualParameter\');
foreach my $node ($nodeset->get_nodelist) {
    my $parm = $node->findvalue(\'actualParameterName\');
    my $val  = $node->findvalue(\'value\');
    $opts->{$parm} = "$val";
}

if (!defined $opts->{config} || "$opts->{config}" eq "") {
    print "config parameter must exist and be non-blank\\n";
    exit ERROR;
}

# check to see if a config with this name already exists before we do anything else
my $xpath    = $ec->getProperty("/myProject/gce_cfgs/$opts->{config}");
my $property = $xpath->findvalue("//response/property/propertyName");

if (defined $property && "$property" ne "") {
    my $errMsg = "A configuration named \'$opts->{config}\' already exists.";
    $ec->setProperty("/myJob/configError", $errMsg);
    print $errMsg;
    exit ERROR;
}

my $cfg = new ElectricCommander::PropDB($ec, "/myProject/gce_cfgs");
my $errors = $ec->checkAllErrors($xpath);

# add all the options as properties
foreach my $key (keys %{$opts}) {
    if ("$key" eq "config") {
        next;
    }
    $cfg->setCol("$opts->{config}", "$key", "$opts->{$key}");


}

exit SUCCESS;
'''
      condition = null
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = null
      parallel = '0'
      postProcessor = 'postp'
      precondition = null
      releaseMode = 'none'
      resourceName = null
      shell = 'ec-perl'
      subprocedure = null
      subproject = null
      timeLimitUnits = 'minutes'
      workingDirectory = null
      workspaceName = null
    }

    step 'createAndAttachCredential', {
      alwaysRun = '0'
      broadcast = '0'
      command = '''#
#  Copyright 2016 Electric Cloud, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

##########################
# createAndAttachCredential.pl
##########################
use ElectricCommander;

use constant {
	SUCCESS => 0,
	ERROR   => 1,
};

## get an EC object
my $ec = new ElectricCommander();
$ec->abortOnError(0);

my $credName = "$[/myJob/config]";

my $xpath = $ec->getFullCredential("credential");
my $errors = $ec->checkAllErrors($xpath);
my $clientID = $xpath->findvalue("//userName");
my $clientSecret = $xpath->findvalue("//password");

my $projName = "$[/myProject/projectName]";
print "Creating credential $credName in project $projName with client $clientID\\n";

# Create credential
$ec->deleteCredential($projName, $credName);
$xpath = $ec->createCredential($projName, $credName, $clientID, $clientSecret);
$errors .= $ec->checkAllErrors($xpath);

# Give config the credential\'s real name
my $configPath = "/projects/$projName/gce_cfgs/$credName";
$xpath = $ec->setProperty($configPath . "/credential", $credName);
$errors .= $ec->checkAllErrors($xpath);

# Give job launcher full permissions on the credential
my $user = "$[/myJob/launchedByUser]";
$xpath = $ec->createAclEntry("user", $user,
    {projectName => $projName,
     credentialName => $credName,
     readPrivilege => allow,
     modifyPrivilege => allow,
     executePrivilege => allow,
     changePermissionsPrivilege => allow});
$errors .= $ec->checkAllErrors($xpath);

# Attach credential to steps that will need it
$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => "Provision Cluster",
     stepName => "provisionCluster"});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => "Deploy Service",
     stepName => "createOrUpdateDeployment"});
$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => "Undeploy Service",
     stepName => "undeployService"});
$errors .= $ec->checkAllErrors($xpath);

if ("$errors" ne "") {
    # Cleanup the partially created configuration we just created
    $ec->deleteProperty($configPath);
    $ec->deleteCredential($projName, $credName);
    my $errMsg = "Error creating configuration credential: " . $errors;
    $ec->setProperty("/myJob/configError", $errMsg);
    print $errMsg;
    exit 1;
}
'''
      condition = null
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = null
      parallel = '0'
      postProcessor = null
      precondition = null
      releaseMode = 'none'
      resourceName = null
      shell = 'ec-perl'
      subprocedure = null
      subproject = null
      timeLimitUnits = 'minutes'
      workingDirectory = null
      workspaceName = null
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'config', {

          // Custom properties
          formType = 'standard'
        }

        property 'credential', {

          // Custom properties
          formType = 'standard'
        }

        property 'desc', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
    ec_parameterForm = '''<!--

     Copyright 2016 Electric Cloud, Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<editor>
    <formElement>
        <type>entry</type>
        <label>Configuration:</label>
        <property>config</property>
        <required>1</required>
        <documentation>Unique name for the plugin configuration.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Description:</label>
        <property>desc</property>
        <required>0</required>
        <documentation>Description for the plugin configuration.</documentation>
    </formElement>
    <formElement>
        <type>credential</type>
        <label>JSON containing Service Account key pair:</label>
        <property>credential</property>
        <required>1</required>
        <credentialType>key</credentialType>
        <documentation>The contents of the JSON file generated at the time of the Service Account key creation. The service account identified by the key pair in the JSON will be used for accessing Google APIs for managing containers and clusters.</documentation>
        <attachedAsParameterToStep>createAndAttachCredential</attachedAsParameterToStep>
    </formElement>
</editor>
'''
  }

  procedure 'Define Container', {
    description = 'Helper procedure for generating a container spec'
    jobNameTemplate = null
    resourceName = null
    timeLimitUnits = null
    workspaceName = null

    formalParameter 'additionalAttributes', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Additional Attributes:'
      orderIndex = null
      required = '0'
      type = 'textarea'
    }

    formalParameter 'applicationName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Application Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'applicationProjectName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Application Project Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'containerName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Container Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'serviceName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Service Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'serviceProjectName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Service Project Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    step 'generateSpec', {
      alwaysRun = '0'
      broadcast = '0'
      command = '''
// Gather input parameters
def containerName = $[containerName]

// The following information will be retrieved from the Deploy container instance
//imageName: Docker image name. Use image-name:version-tag format to refer to a specific version of the image.
//ports
//env variables
//entry point

'''
      condition = null
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = null
      parallel = '0'
      postProcessor = 'postp'
      precondition = null
      releaseMode = 'none'
      resourceName = null
      shell = 'ec-groovy'
      subprocedure = null
      subproject = null
      timeLimitUnits = 'minutes'
      workingDirectory = null
      workspaceName = null
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'additionalAttributes', {

          // Custom properties
          formType = 'standard'
        }

        property 'applicationName', {

          // Custom properties
          formType = 'standard'
        }

        property 'applicationProjectName', {

          // Custom properties
          formType = 'standard'
        }

        property 'containerName', {

          // Custom properties
          formType = 'standard'
        }

        property 'serviceName', {

          // Custom properties
          formType = 'standard'
        }

        property 'serviceProjectName', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
    containerMappingsForm = '''<!--

     Copyright 2016 Electric Cloud, Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<editor>
    <formElement>
        <type>textarea</type>
        <label>Additional Attributes:</label>
        <property>additionalAttributes</property>
        <required>0</required>
        <documentation>Pass-through attributes for the container definition in JSON format. Supports container attributes defined in http://kubernetes.io/docs/api-reference/v1/definitions/#_v1_container.</documentation>
    </formElement>
</editor>
'''
    ec_parameterForm = '''<!--

     Copyright 2016 Electric Cloud, Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<editor>
    <formElement>
        <type>entry</type>
        <label>Container Name:</label>
        <property>containerName</property>
        <required>0</required>
        <documentation>The name of the container.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Service Name:</label>
        <property>serviceName</property>
        <required>0</required>
        <documentation>The name of the service in Deploy that encapsulates the service to be deployed on the Google container cluster.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Service Project Name:</label>
        <property>serviceProjectName</property>
        <required>0</required>
        <documentation>The name of the project that the service belongs to.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Application Name:</label>
        <property>applicationName</property>
        <required>0</required>
        <documentation>The name of the application that the service belongs to. Not applicable for a top-level service.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Application Project Name:</label>
        <property>applicationProjectName</property>
        <required>0</required>
        <documentation>The name of the project that the application belongs to. Not applicable for a top-level service.</documentation>
    </formElement>
    <formElement>
        <type>textarea</type>
        <label>Additional Attributes:</label>
        <property>additionalAttributes</property>
        <required>0</required>
        <documentation>Pass-through attributes for the container definition in JSON format. Supports container attributes defined in http://kubernetes.io/docs/api-reference/v1/definitions/#_v1_container.</documentation>
    </formElement>
</editor>
'''
  }

  procedure 'Define Service', {
    description = 'Helper procedure for generating a service spec'
    jobNameTemplate = null
    resourceName = null
    timeLimitUnits = null
    workspaceName = null

    formalParameter 'applicationName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Application Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'applicationProjectName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Application Project Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'loadBalancerIP', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'LoadBalancer IP:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'loadBalancerSourceRanges', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'LoadBalancer Source Ranges:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'serviceName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Service Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'serviceProjectName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Service Project Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'sessionAffinity', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Enable Client-IP based Session Affinity?'
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    step 'generateSpec', {
      alwaysRun = '0'
      broadcast = '0'
      command = '''
// Gather input parameters
def serviceName = $[serviceName]

// The following information will be retrieved from the Deploy container instance
//imageName: Docker image name. Use image-name:version-tag format to refer to a specific version of the image.
//ports
//env variables
//entry point

'''
      condition = null
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = null
      parallel = '0'
      postProcessor = 'postp'
      precondition = null
      releaseMode = 'none'
      resourceName = null
      shell = 'ec-groovy'
      subprocedure = null
      subproject = null
      timeLimitUnits = 'minutes'
      workingDirectory = null
      workspaceName = null
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'applicationName', {

          // Custom properties
          formType = 'standard'
        }

        property 'applicationProjectName', {

          // Custom properties
          formType = 'standard'
        }

        property 'loadBalancerIP', {

          // Custom properties
          formType = 'standard'
        }

        property 'loadBalancerSourceRanges', {

          // Custom properties
          formType = 'standard'
        }

        property 'serviceName', {

          // Custom properties
          formType = 'standard'
        }

        property 'serviceProjectName', {

          // Custom properties
          formType = 'standard'
        }

        property 'sessionAffinity', {

          // Custom properties
          checkedValue = 'true'
          formType = 'standard'
          initiallyChecked = '0'
          uncheckedValue = 'false'
        }
      }
    }
    ec_parameterForm = '''<!--

     Copyright 2016 Electric Cloud, Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<editor>
    <formElement>
        <type>entry</type>
        <label>Service Name:</label>
        <property>serviceName</property>
        <required>0</required>
        <documentation>The name of the service in Deploy that encapsulates the service to be deployed on the Google container cluster.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Service Project Name:</label>
        <property>serviceProjectName</property>
        <required>0</required>
        <documentation>The name of the project that the service belongs to.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Application Name:</label>
        <property>applicationName</property>
        <required>0</required>
        <documentation>The name of the application that the service belongs to. Not applicable for a top-level service.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Application Project Name:</label>
        <property>applicationProjectName</property>
        <required>0</required>
        <documentation>The name of the project that the application belongs to. Not applicable for a top-level service.</documentation>
    </formElement>

    <formElement>
        <type>entry</type>
        <label>LoadBalancer IP:</label>
        <property>loadBalancerIP</property>
        <required>0</required>
        <documentation>IP to use for the load balancer for \'LoadBalancer\' service type.</documentation>
    </formElement>

    <formElement>
        <type>entry</type>
        <label>LoadBalancer Source Ranges:</label>
        <property>loadBalancerSourceRanges</property>
        <required>0</required>
        <documentation>Comma-separated list of IP CIDR ranges to specify the IP ranges that are allowed to access the load balancer.</documentation>
    </formElement>

    <formElement>
        <type>checkbox</type>
        <label>Enable Client-IP based Session Affinity?</label>
        <property>sessionAffinity</property>
        <required>0</required>
        <checkedValue>ClientIP</checkedValue>
        <uncheckedValue>None</uncheckedValue>
        <initiallyChecked>0</initiallyChecked>
        <documentation>Whether to enable client-IP based session affinity. Defaults to round robin if not checked.</documentation>
    </formElement>

    <!--Not needed for the demo. To be considered post demo.
    <formElement>
        <type>textarea</type>
        <label>Additional Service Attributes:</label>
        <property>additionalServiceAttributes</property>
        <required>0</required>
        <documentation>Pass-through attributes for the service in JSON format. Supports service attributes defined in http://kubernetes.io/docs/api-reference/v1/definitions/#_v1_service.</documentation>
    </formElement>

    <formElement>
        <type>textarea</type>
        <label>Additional Pod Attributes:</label>
        <property>additionalPodAttributes</property>
        <required>0</required>
        <documentation>Pass-through attributes for the service pods in JSON format. Supports pod attributes defined in http://kubernetes.io/docs/api-reference/v1/definitions/#_v1_pod.</documentation>
    </formElement>
-->
</editor>
'''
    serviceMappingsForm = '''<!--

     Copyright 2016 Electric Cloud, Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<editor>
    <formElement>
        <type>entry</type>
        <label>LoadBalancer IP:</label>
        <property>loadBalancerIP</property>
        <required>0</required>
        <documentation>IP to use for the load balancer for \'LoadBalancer\' service type.</documentation>
    </formElement>

    <formElement>
        <type>entry</type>
        <label>LoadBalancer Source Ranges:</label>
        <property>loadBalancerSourceRanges</property>
        <required>0</required>
        <documentation>Comma-separated list of IP CIDR ranges to specify the IP ranges that are allowed to access the load balancer.</documentation>
    </formElement>

    <formElement>
        <type>checkbox</type>
        <label>Enable Client-IP based Session Affinity?</label>
        <property>sessionAffinity</property>
        <required>0</required>
        <checkedValue>ClientIP</checkedValue>
        <uncheckedValue>None</uncheckedValue>
        <initiallyChecked>0</initiallyChecked>
        <documentation>Whether to enable client-IP based session affinity. Defaults to round robin if not checked.</documentation>
    </formElement>
<!--Not needed for the demo. To be considered post demo.
    <formElement>
        <type>textarea</type>
        <label>Additional Service Attributes:</label>
        <property>additionalServiceAttributes</property>
        <required>0</required>
        <documentation>Pass-through attributes for the service in JSON format. Supports service attributes defined in http://kubernetes.io/docs/api-reference/v1/definitions/#_v1_service.</documentation>
    </formElement>

    <formElement>
        <type>textarea</type>
        <label>Additional Pod Attributes:</label>
        <property>additionalPodAttributes</property>
        <required>0</required>
        <documentation>Pass-through attributes for the service pods in JSON format. Supports pod attributes defined in http://kubernetes.io/docs/api-reference/v1/definitions/#_v1_pod.</documentation>
    </formElement>
-->
</editor>
'''
  }

  procedure 'DeleteConfiguration', {
    description = 'Deletes an existing plugin configuration'
    jobNameTemplate = null
    resourceName = null
    timeLimitUnits = null
    workspaceName = null

    formalParameter 'config', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Configuration:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    step 'deleteConfiguration', {
      alwaysRun = '0'
      broadcast = '0'
      command = '''#
#  Copyright 2016 Electric Cloud, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

use ElectricCommander;

my $ec = new ElectricCommander();

$ec->deleteProperty("/myProject/gce_cfgs/$[config]");
$ec->deleteCredential("$[/myProject/projectName]", "$[config]");
exit 0;
'''
      condition = null
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = null
      parallel = '0'
      postProcessor = null
      precondition = null
      releaseMode = 'none'
      resourceName = null
      shell = 'ec-perl'
      subprocedure = null
      subproject = null
      timeLimitUnits = 'minutes'
      workingDirectory = null
      workspaceName = null
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'config', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
    ec_parameterForm = '''<!--

     Copyright 2016 Electric Cloud, Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<editor>
    <formElement>
        <type>entry</type>
        <label>Configuration:</label>
        <property>config</property>
        <required>1</required>
        <documentation>The name of the configuration to delete.</documentation>
    </formElement>
</editor>
'''
  }

  procedure 'Deploy Service', {
    description = 'Creates or updates a Deployment to bring up a Replica Set and Pods.'
    jobNameTemplate = null
    resourceName = null
    timeLimitUnits = null
    workspaceName = null

    formalParameter 'applicationName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Application Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'applicationRevisionId', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Application Revision ID:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'clusterName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Cluster Name:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'clusterOrEnvProjectName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Cluster Or Environment Project Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'environmentName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Environment Name:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'serviceName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Service Name:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'serviceProjectName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Project Name:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    step 'setup', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = ''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = 'postp'
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = 'ec-perl'
      subprocedure = null
      subproject = null
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }

    step 'createOrUpdateDeployment', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = '''def serviceName = \'$[serviceName]\'
def serviceProjectName = \'$[serviceProjectName]\'
def applicationName = \'$[applicationName]\'
def clusterName = \'$[clusterName]\'
def clusterOrEnvProjectName = \'$[clusterOrEnvProjectName]\'
// default cluster project name if not explicitly set
if (!clusterOrEnvProjectName) {
    clusterOrEnvProjectName = \'$[serviceProjectName]\'
}
def environmentName = \'$[environmentName]\'
def applicationRevisionId = \'$[applicationRevisionId]\'
println """ectool evalDsl "getServiceDeploymentDetails serviceName:'$serviceName', projectName:'$serviceProjectName',clusterName:'$clusterName', applicationEntityRevisionId:'$applicationRevisionId', applicationName:'$applicationName', clusterProjectName:'$clusterOrEnvProjectName', environmentName:'$environmentName'" """.execute().text'''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = 'postp'
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = 'ec-groovy'
      subprocedure = null
      subproject = null
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'applicationName', {

          // Custom properties
          formType = 'standard'
        }

        property 'applicationRevisionId', {

          // Custom properties
          formType = 'standard'
        }

        property 'clusterName', {

          // Custom properties
          formType = 'standard'
        }

        property 'clusterOrEnvProjectName', {

          // Custom properties
          formType = 'standard'
        }

        property 'environmentName', {

          // Custom properties
          formType = 'standard'
        }

        property 'serviceName', {

          // Custom properties
          formType = 'standard'
        }

        property 'serviceProjectName', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
    ec_parameterForm = '''<!--

     Copyright 2016 Electric Cloud, Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<editor>
    <formElement>
        <type>entry</type>
        <label>Service Name:</label>
        <property>serviceName</property>
        <required>1</required>
        <documentation>The name of the service in Deploy that encapsulates the service to be deployed on the Google container cluster.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Project Name:</label>
        <property>serviceProjectName</property>
        <required>1</required>
        <documentation>The name of the project that the service belongs to. In case of an application-level service it also owns the application.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Application Name:</label>
        <property>applicationName</property>
        <required>0</required>
        <documentation>The name of the application that the service belongs to. Not applicable for a top-level service.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Application Revision ID:</label>
        <property>applicationRevisionId</property>
        <required>0</required>
        <documentation>Revision Id of the application version that the service belongs to.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Cluster Name:</label>
        <property>clusterName</property>
        <required>1</required>
        <documentation>The name of the cluster in Deploy that encapsulates the Google container cluster on which the service is to be deployed.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Cluster Or Environment Project Name:</label>
        <property>clusterOrEnvProjectName</property>
        <required>0</required>
        <documentation>The name of the project that the cluster belongs to if it is a top-level project cluster. Or the name of the project that the environment belongs to if it is an environment-scoped cluster.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Environment Name:</label>
        <property>environmentName</property>
        <required>0</required>
        <documentation>The name of the environment that the cluster belongs to. Not applicable for a top-level project cluster.</documentation>
    </formElement>
</editor>
'''
  }

    procedure 'Undeploy Service', {
      description = 'Undeploy service'
      jobNameTemplate = null
      resourceName = null
      timeLimitUnits = null
      workspaceName = null

      formalParameter 'applicationName', defaultValue: '', {
        description = ''
        expansionDeferred = '0'
        label = 'Application Name:'
        orderIndex = null
        required = '0'
        type = 'entry'
      }

      formalParameter 'applicationRevisionId', defaultValue: '', {
        description = ''
        expansionDeferred = '0'
        label = 'Application Revision ID:'
        orderIndex = null
        required = '0'
        type = 'entry'
      }

      formalParameter 'clusterName', defaultValue: '', {
        description = ''
        expansionDeferred = '0'
        label = 'Cluster Name:'
        orderIndex = null
        required = '1'
        type = 'entry'
      }

      formalParameter 'clusterOrEnvProjectName', defaultValue: '', {
        description = ''
        expansionDeferred = '0'
        label = 'Cluster Or Environment Project Name:'
        orderIndex = null
        required = '0'
        type = 'entry'
      }

      formalParameter 'environmentName', defaultValue: '', {
        description = ''
        expansionDeferred = '0'
        label = 'Environment Name:'
        orderIndex = null
        required = '0'
        type = 'entry'
      }

      formalParameter 'serviceName', defaultValue: '', {
        description = ''
        expansionDeferred = '0'
        label = 'Service Name:'
        orderIndex = null
        required = '1'
        type = 'entry'
      }

      formalParameter 'serviceProjectName', defaultValue: '', {
        description = ''
        expansionDeferred = '0'
        label = 'Project Name:'
        orderIndex = null
        required = '1'
        type = 'entry'
      }

      step 'setup', {
        description = ''
        alwaysRun = '0'
        broadcast = '0'
        command = ''
        condition = ''
        errorHandling = 'failProcedure'
        exclusiveMode = 'none'
        logFileName = ''
        parallel = '0'
        postProcessor = 'postp'
        precondition = ''
        releaseMode = 'none'
        resourceName = ''
        shell = 'ec-perl'
        subprocedure = null
        subproject = null
        timeLimit = ''
        timeLimitUnits = 'minutes'
        workingDirectory = ''
        workspaceName = ''
      }

      step 'undeployService', {
        description = ''
        alwaysRun = '0'
        broadcast = '0'
        command = '''def serviceName = \'$[serviceName]\'
  def serviceProjectName = \'$[serviceProjectName]\'
  def applicationName = \'$[applicationName]\'
  def clusterName = \'$[clusterName]\'
  def clusterOrEnvProjectName = \'$[clusterOrEnvProjectName]\'
  // default cluster project name if not explicitly set
  if (!clusterOrEnvProjectName) {
      clusterOrEnvProjectName = \'$[serviceProjectName]\'
  }
  def environmentName = \'$[environmentName]\'
  def applicationRevisionId = \'$[applicationRevisionId]\'
  println """ectool evalDsl "getServiceDeploymentDetails serviceName:'$serviceName', projectName:'$serviceProjectName',clusterName:'$clusterName', applicationEntityRevisionId:'$applicationRevisionId', applicationName:'$applicationName', clusterProjectName:'$clusterOrEnvProjectName', environmentName:'$environmentName'" """.execute().text'''
        condition = ''
        errorHandling = 'failProcedure'
        exclusiveMode = 'none'
        logFileName = ''
        parallel = '0'
        postProcessor = 'postp'
        precondition = ''
        releaseMode = 'none'
        resourceName = ''
        shell = 'ec-groovy'
        subprocedure = null
        subproject = null
        timeLimit = ''
        timeLimitUnits = 'minutes'
        workingDirectory = ''
        workspaceName = ''
      }

      // Custom properties

      property 'ec_customEditorData', {

        // Custom properties

        property 'parameters', {

          // Custom properties

          property 'applicationName', {

            // Custom properties
            formType = 'standard'
          }

          property 'applicationRevisionId', {

            // Custom properties
            formType = 'standard'
          }

          property 'clusterName', {

            // Custom properties
            formType = 'standard'
          }

          property 'clusterOrEnvProjectName', {

            // Custom properties
            formType = 'standard'
          }

          property 'environmentName', {

            // Custom properties
            formType = 'standard'
          }

          property 'serviceName', {

            // Custom properties
            formType = 'standard'
          }

          property 'serviceProjectName', {

            // Custom properties
            formType = 'standard'
          }
        }
      }
      ec_parameterForm = '''<!--

       Copyright 2016 Electric Cloud, Inc.

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

  -->
  <editor>
      <formElement>
          <type>entry</type>
          <label>Service Name:</label>
          <property>serviceName</property>
          <required>1</required>
          <documentation>The name of the service in Deploy that encapsulates the service to be deployed on the Google container cluster.</documentation>
      </formElement>
      <formElement>
          <type>entry</type>
          <label>Project Name:</label>
          <property>serviceProjectName</property>
          <required>1</required>
          <documentation>The name of the project that the service belongs to. In case of an application-level service it also owns the application.</documentation>
      </formElement>
      <formElement>
          <type>entry</type>
          <label>Application Name:</label>
          <property>applicationName</property>
          <required>0</required>
          <documentation>The name of the application that the service belongs to. Not applicable for a top-level service.</documentation>
      </formElement>
      <formElement>
          <type>entry</type>
          <label>Application Revision ID:</label>
          <property>applicationRevisionId</property>
          <required>0</required>
          <documentation>Revision Id of the application version that the service belongs to.</documentation>
      </formElement>
      <formElement>
          <type>entry</type>
          <label>Cluster Name:</label>
          <property>clusterName</property>
          <required>1</required>
          <documentation>The name of the cluster in Deploy that encapsulates the Google container cluster on which the service is to be deployed.</documentation>
      </formElement>
      <formElement>
          <type>entry</type>
          <label>Cluster Or Environment Project Name:</label>
          <property>clusterOrEnvProjectName</property>
          <required>0</required>
          <documentation>The name of the project that the cluster belongs to if it is a top-level project cluster. Or the name of the project that the environment belongs to if it is an environment-scoped cluster.</documentation>
      </formElement>
      <formElement>
          <type>entry</type>
          <label>Environment Name:</label>
          <property>environmentName</property>
          <required>0</required>
          <documentation>The name of the environment that the cluster belongs to. Not applicable for a top-level project cluster.</documentation>
      </formElement>
  </editor>
  '''
    }

  procedure 'Provision Cluster', {
    description = 'Provisions a Google container cluster which is the foundation of a Container Engine application. Pods, services, and replication controllers all run on top of a cluster.'
    jobNameTemplate = null
    resourceName = null
    timeLimitUnits = null
    workspaceName = null

    formalParameter 'additionalZones', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Additional Zones:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'clusterDescription', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Description:'
      orderIndex = null
      required = '0'
      type = 'textarea'
    }

    formalParameter 'clusterName', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Container Cluster Name:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'clusterProjectID', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Google Cloud Platform Project ID:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'config', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Configuration:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'diskSize', defaultValue: '100', {
      description = ''
      expansionDeferred = '0'
      label = 'Disk Size:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'enableAutoscaling', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Enable Autoscaling for Node Pool?'
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'imageType', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Image Type:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'machineType', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Machine Type:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'masterZone', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Master Zone:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'maxNodeCount', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Maximum Node Pool Size:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'minNodeCount', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Minimum Node Pool Size:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    formalParameter 'network', defaultValue: 'default', {
      description = ''
      expansionDeferred = '0'
      label = 'Network:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'nodePoolName', defaultValue: 'default-pool', {
      description = ''
      expansionDeferred = '0'
      label = 'Node Pool Name:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'nodePoolSize', defaultValue: '3', {
      description = ''
      expansionDeferred = '0'
      label = 'Initial Node Pool Size:'
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    formalParameter 'subnetwork', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      label = 'Subnetwork:'
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    step 'setup', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = ''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = 'postp'
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = 'ec-perl'
      subprocedure = null
      subproject = null
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }

    step 'provisionCluster', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = ''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = 'postp'
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = 'ec-groovy'
      subprocedure = null
      subproject = null
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'additionalZones', {

          // Custom properties
          formType = 'standard'
        }

        property 'clusterDescription', {

          // Custom properties
          formType = 'standard'
        }

        property 'clusterName', {

          // Custom properties
          formType = 'standard'
        }

        property 'clusterProjectID', {

          // Custom properties
          formType = 'standard'
        }

        property 'config', {

          // Custom properties
          formType = 'standard'
        }

        property 'diskSize', {

          // Custom properties
          formType = 'standard'
        }

        property 'enableAutoscaling', {

          // Custom properties
          checkedValue = 'true'
          formType = 'standard'
          initiallyChecked = '0'
          uncheckedValue = 'false'
        }

        property 'imageType', {

          // Custom properties
          formType = 'standard'
        }

        property 'machineType', {

          // Custom properties
          formType = 'standard'
        }

        property 'masterZone', {

          // Custom properties
          formType = 'standard'
        }

        property 'maxNodeCount', {

          // Custom properties
          formType = 'standard'
        }

        property 'minNodeCount', {

          // Custom properties
          formType = 'standard'
        }

        property 'network', {

          // Custom properties
          formType = 'standard'
        }

        property 'nodePoolName', {

          // Custom properties
          formType = 'standard'
        }

        property 'nodePoolSize', {

          // Custom properties
          formType = 'standard'
        }

        property 'subnetwork', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
    ec_parameterForm = '''<!--

     Copyright 2016 Electric Cloud, Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<editor>
    <formElement>
        <type>entry</type>
        <label>Configuration:</label>
        <property>config</property>
        <required>1</required>
        <documentation>The name of an existing configuration which holds all the connection information for Google Container Engine.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Container Cluster Name:</label>
        <property>clusterName</property>
        <required>1</required>
        <documentation>The name of the cluster that needs to be provisioned.</documentation>
    </formElement>
    <formElement>
        <type>textarea</type>
        <label>Description:</label>
        <property>clusterDescription</property>
        <required>0</required>
        <documentation>Description of the cluster that needs to be provisioned. The cluster description is set at creation time only and cannot be updated so this parameter will be ignored if the cluster already exists.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Google Cloud Platform Project ID:</label>
        <property>clusterProjectID</property>
        <required>1</required>
        <documentation>The Project ID for the Google Cloud Platform project that the cluster belongs to. If the cluster does not exist in the project identified by the Project ID, then it will be created in this project.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Master Zone:</label>
        <property>masterZone</property>
        <required>1</required>
        <documentation>The primary zone for the cluster. The master zone for a cluster is set at creation time and cannot be updated so this parameter will be ignored if the cluster already exists.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Node Pool Name:</label>
        <property>nodePoolName</property>
        <required>1</required>
        <value>default-pool</value>
        <documentation>The node pool to be created or updated.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Initial Node Pool Size:</label>
        <property>nodePoolSize</property>
        <required>1</required>
        <value>3</value>
        <documentation>The number of nodes to be created in each of the cluster\'s zones. Defaults to 3.</documentation>
    </formElement>
    <formElement>
        <type>checkbox</type>
        <label>Enable Autoscaling for Node Pool?</label>
        <property>enableAutoscaling</property>
        <required>0</required>
        <checkedValue>true</checkedValue>
        <uncheckedValue>false</uncheckedValue>
        <initiallyChecked>0</initiallyChecked>
        <documentation>Whether to enable autoscaling for the node pool.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Minimum Node Pool Size:</label>
        <property>minNodeCount</property>
        <required>0</required>
        <documentation>Minimum number of nodes in the NodePool. Must be &gt;= 1 and &lt;= \'Maximum Node Pool Size\'. Applicable if Autoscaling is enabled.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Maximum Node Pool Size:</label>
        <property>maxNodeCount</property>
        <required>0</required>
        <documentation>Maximum number of nodes in the NodePool. Must be &gt;= \'Minimum Node Pool Size\'. Applicable if Autoscaling is enabled.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Machine Type:</label>
        <property>machineType</property>
        <required>1</required>
        <documentation>The type of machine to use for nodes.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Image Type:</label>
        <property>imageType</property>
        <required>1</required>
        <documentation>Image Type specifies the base OS that the nodes in the cluster will run on.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Disk Size:</label>
        <property>diskSize</property>
        <required>1</required>
        <value>100</value>
        <documentation>Size in GB for node VM boot disks. Defaults to 100 GB.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Additional Zones:</label>
        <property>additionalZones</property>
        <required>0</required>
        <documentation>The set of additional zones in which the specified node footprint should be replicated. All zones must be in the same region as the cluster\'s primary zone. If additional-zones is not specified, all nodes will be in the cluster\'s primary zone.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Network:</label>
        <property>network</property>
        <required>1</required>
        <value>default</value>
        <documentation>The Compute Engine Network that the cluster will connect to. Google Container Engine will use this network when creating routes and firewalls for the clusters. Defaults to the \'default\' network.</documentation>
    </formElement>
    <formElement>
        <type>entry</type>
        <label>Subnetwork:</label>
        <property>subnetwork</property>
        <required>0</required>
        <documentation>The name of the Google Compute Engine subnetwork to which the cluster is connected.</documentation>
    </formElement>
</editor>
'''
  }

  // Custom properties

  property 'ec_container_service_plugin', {

    // Custom properties

    property 'operations', {

      // Custom properties

      property 'createConfiguration', {

        // Custom properties

        property 'parameterRefs', {

          // Custom properties
          configuration = 'config'
        }

        property 'ui_formRefs', {

          // Custom properties
          parameterForm = 'ec_parameterForm'
        }
      }

      property 'defineContainerMappings', {

        // Custom properties

        property 'ui_formRefs', {

          // Custom properties
          parameterForm = 'containerMappingsForm'
        }
      }

      property 'defineServiceMappings', {

        // Custom properties

        property 'ui_formRefs', {

          // Custom properties
          parameterForm = 'serviceMappingsForm'
        }
      }

      property 'deleteConfiguration', {

        // Custom properties

        property 'ui_formRefs', {
          propertyType = 'sheet'
        }
      }

      property 'deployService', {

        // Custom properties

        property 'parameterRefs', {

          // Custom properties

          applicationRevisionId = 'applicationRevisionId'
          clusterName = 'clusterName'
          clusterOrEnvironmentProjectName = 'clusterOrEnvProjectName'
          serviceName = 'serviceName'
        }

        property 'ui_formRefs', {

          // Custom properties
          parameterForm = 'ec_parameterForm'
        }
      }

        property 'undeployService', {

          // Custom properties

          property 'parameterRefs', {

            // Custom properties

            applicationRevisionId = 'applicationRevisionId'
            clusterName = 'clusterName'
            clusterOrEnvironmentProjectName = 'clusterOrEnvProjectName'
            serviceName = 'serviceName'
          }

          property 'ui_formRefs', {

            // Custom properties
            parameterForm = 'ec_parameterForm'
          }
        }

      property 'provisionCluster', {

        // Custom properties

        property 'parameterRefs', {

          // Custom properties
          configuration = 'config'
          platformClusterName = 'clusterName'
          platformProjectReference = 'clusterProjectID'
        }

        property 'ui_formRefs', {

          // Custom properties
          parameterForm = 'ec_parameterForm'
        }
      }
    }
    configurationLocation = 'gce_cfgs'
    displayName = 'Google Container Engine'
    hasConfiguration = '1'
  }
  ec_container_service_plugin.operations.createConfiguration.procedureName='CreateConfiguration'
  ec_container_service_plugin.operations.defineContainerMappings.procedureName='Define Container'
  ec_container_service_plugin.operations.defineServiceMappings.procedureName='Define Service'
  ec_container_service_plugin.operations.deleteConfiguration.procedureName='DeleteConfiguration'
  ec_container_service_plugin.operations.deployService.procedureName='Deploy Service'
  ec_container_service_plugin.operations.undeployService.procedureName='Undeploy Service'
  ec_container_service_plugin.operations.provisionCluster.procedureName='Provision Cluster'
  ec_container_service_plugin.operations.deployService.parameterRefs.applicationName='applicationName'
  ec_container_service_plugin.operations.deployService.parameterRefs.environmentName='environmentName'
  ec_container_service_plugin.operations.deployService.parameterRefs.projectName='serviceProjectName'
  ec_container_service_plugin.operations.deployService.parameterRefs.serviceName='serviceName'
  ec_container_service_plugin.operations.deployService.parameterRefs.clusterName='clusterName'
    ec_container_service_plugin.operations.undeployService.parameterRefs.applicationName='applicationName'
    ec_container_service_plugin.operations.undeployService.parameterRefs.environmentName='environmentName'
    ec_container_service_plugin.operations.undeployService.parameterRefs.projectName='serviceProjectName'
    ec_container_service_plugin.operations.undeployService.parameterRefs.serviceName='serviceName'
    ec_container_service_plugin.operations.undeployService.parameterRefs.clusterName='clusterName'



  property 'gce_cfgs', {

    // Custom properties

    property 'gce_test_config', {

      // Custom properties
      credential = 'gce_test_config'
      desc = ''
    }
  }

  property 'logs', {

    // Custom properties

    property 'Thu Oct 27 15:34:45 2016', value: '''Plugin Name: EC-GoogleContainerEngine-1.0.0
Plugin Directory: C:/ProgramData/Electric Cloud/ElectricCommander/plugins/EC-GoogleContainerEngine-1.0.0
<responses version="2.3" dispatchId="11" nodeId="192.168.16.252">
  <response requestId="9" nodeId="192.168.16.252">
    <value>
Result: [resources, workspaces, projects]
Console output:
Line 0020: [OUT] Processing procedure DSL file C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\createConfiguration\\procedure.dsl
Line 0020: [OUT] Processing form XML C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\createConfiguration\\form.xml
Line 0020: [OUT] Processing procedure DSL file C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\defineContainer\\procedure.dsl
Line 0020: [OUT] Processing form XML C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\defineContainer\\form.xml
Line 0020: [OUT] Processing procedure DSL file C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\defineService\\procedure.dsl
Line 0020: [OUT] Processing form XML C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\defineService\\form.xml
Line 0020: [OUT] Processing procedure DSL file C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\deleteConfiguration\\procedure.dsl
Line 0020: [OUT] Processing form XML C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\deleteConfiguration\\form.xml
Line 0020: [OUT] Processing procedure DSL file C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\deployService\\procedure.dsl
Line 0020: [OUT] Processing form XML C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\deployService\\form.xml
Line 0020: [OUT] Processing procedure DSL file C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\provisionCluster\\procedure.dsl
Line 0020: [OUT] Processing form XML C:\\ProgramData\\Electric Cloud\\ElectricCommander\\plugins\\EC-GoogleContainerEngine-1.0.0\\dsl\\procedures\\provisionCluster\\form.xml
Line 0020: [OUT] /projects/EC-GoogleContainerEngine-1.0.0/procedures/CreateConfiguration/standardStepPicker: \'false\'
Line 0020: [OUT] /projects/EC-GoogleContainerEngine-1.0.0/procedures/Define Container/standardStepPicker: \'true\'
Line 0020: [OUT] /projects/EC-GoogleContainerEngine-1.0.0/procedures/Define Service/standardStepPicker: \'true\'
Line 0020: [OUT] /projects/EC-GoogleContainerEngine-1.0.0/procedures/DeleteConfiguration/standardStepPicker: \'false\'
Line 0020: [OUT] /projects/EC-GoogleContainerEngine-1.0.0/procedures/Deploy Service/standardStepPicker: \'true\'
Line 0020: [OUT] /projects/EC-GoogleContainerEngine-1.0.0/procedures/Provision Cluster/standardStepPicker: \'true\'
</value>  </response></responses><artifactVersion>
      <artifactVersionId>8beca2f5-9c95-11e6-8490-346895edae65</artifactVersionId>
      <artifactVersionName>com.electriccloud:EC-GoogleContainerEngine-Grapes:1.0.0</artifactVersionName>
      <artifactKey>EC-GoogleContainerEngine-Grapes</artifactKey>
      <artifactName>com.electriccloud:EC-GoogleContainerEngine-Grapes</artifactName>
      <artifactVersionState>available</artifactVersionState>
      <buildNumber>0</buildNumber>
      <createTime>2016-10-27T22:34:39.097Z</createTime>
      <description>JARs that EC-GoogleContainerEngine plugin procedures depend on</description>
      <groupId>com.electriccloud</groupId>
      <lastModifiedBy>admin</lastModifiedBy>
      <majorMinorPatch>1.0.0</majorMinorPatch>
      <modifyTime>2016-10-27T22:34:39.097Z</modifyTime>
      <owner>admin</owner>
      <propertySheetId>8beca2f7-9c95-11e6-8490-346895edae65</propertySheetId>
      <repositoryName>default</repositoryName>
      <version>1.0.0</version>
    </artifactVersion>

Details:

''', {
      expandable = '1'
    }
  }

  property 'scripts', {

    // Custom properties
    helperClasses = '''@Grab(\'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1\' )
@Grab(group=\'com.google.api-client\', module=\'google-api-client\', version=\'1.22.0\')

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static groovyx.net.http.Method.PUT

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class GCEClient extends BaseClient {

    // Constants
    final String RESOURCE_MGR_API =\'https://cloudresourcemanager.googleapis.com\'
    final String CONTAINER_ENGINE_API = \'https://container.googleapis.com\'

    String retrieveAccessToken(def pluginConfig) {

        if (OFFLINE) return "BearerFOO"
        String keyPairJson = pluginConfig.credential.password

        GoogleCredential credential = GoogleCredential.fromStream(
                new ByteArrayInputStream(keyPairJson.bytes))
                .createScoped(Arrays.asList(
                    \'https://www.googleapis.com/auth/compute\',
                    \'https://www.googleapis.com/auth/cloud-platform\'));
        credential.refreshToken();
        def accessToken = credential.getAccessToken();
        return \'Bearer \' + accessToken
    }

    Object getProject(String projectId, String accessToken) {
        doHttpGet (RESOURCE_MGR_API, "/v1/projects/$projectId", accessToken, /*failOnErrorCode*/ false)
    }

    /**
     * Retrieves the cluster from GCE and returns null if not found
     *
     */
    Object getCluster(String projectId, String zone, String clusterName, String accessToken ){
        if (OFFLINE) return [endpoint:\'http://foo\']

        def response = doHttpGet("/v1/projects/${projectId}/zones/${zone}/clusters/${clusterName}",
                                  accessToken, /*failOnErrorCode*/ false)
       response.status == 200 ? response.data : null

    }

    Object getNodePool(String projectId, String zone, String clusterName,String nodePoolName, String accessToken ){
      def response = doHttpGet("/v1/projects/${projectId}/zones/${zone}/clusters/${clusterName}/nodePools/${nodePoolName}",
                                  accessToken, /*failOnErrorCode*/ false)
      println("Nodepool response: ${response.data}")
      response.status == 200 ? response.data : null
    }

    Object doHttpGet(String requestUri, String accessToken, boolean failOnErrorCode = true) {

        doHttpGet(/*requestUrl*/ CONTAINER_ENGINE_API,
                requestUri,
                accessToken,
                failOnErrorCode)
    }

    Object doHttpGet(String requestUrl, String requestUri, String accessToken, boolean failOnErrorCode = true) {

        doHttpRequest(GET,
                requestUrl,
                requestUri,
                [\'Authorization\' : accessToken],
                failOnErrorCode)
    }

    Object doHttpPost(String requestUri, String accessToken, Object requestBody, boolean failOnErrorCode = true) {

        doHttpRequest(POST,
                /*requestUrl*/ CONTAINER_ENGINE_API,
                requestUri,
                [\'Authorization\' : accessToken],
                failOnErrorCode,
                requestBody)
    }

    Object doHttpPut(String requestUri, String accessToken, Object requestBody, boolean failOnErrorCode = true) {

        doHttpRequest(PUT,
                /*requestUrl*/ CONTAINER_ENGINE_API,
                requestUri,
                [\'Authorization\' : accessToken],
                failOnErrorCode,
                requestBody)
    }

    String buildNodepoolPayload(Map args){
        def nodePoolTemplate = [nodePool: [
                name: args.nodePoolName,
                initialNodeCount: args.initialNodeCount,
                config: [
                    machineType: args.machineType,
                    diskSizeGb: args.diskSizeGb,
                    imageType: args.imageType,
                    //TODO The oAuthScope is a manadatory field, for now just adding a value, need to get ir right
                    oauthScopes: ["https://www.googleapis.com/auth/compute"]
                ]
          ]
        ]

        def json = new JsonBuilder(nodePoolTemplate)
        return json.toPrettyString()
    }

    String buildClusterPayload(Map args){

        boolean enableAutoScaling = toBoolean(args.autoscalingEnabled)
        def clusterTemplate = [ cluster: [
            name: args.clusterName,
            description: args.clusterDescription,
            // __DONT__ copy the nodepool from above method directly, there is slight difference
            nodePools:[[
                name: args.nodePoolName,
                initialNodeCount: args.initialNodeCount,
                config: [
                    machineType: args.machineType,
                    diskSizeGb: args.diskSizeGb,
                    imageType: args.imageType,
                    //TODO The oAuthScope is a manadatory field, for now just adding a value, need to get ir right
                    oauthScopes: ["https://www.googleapis.com/auth/compute"]
                ],
                autoscaling: [
                  enabled: enableAutoScaling,
                  minNodeCount: args.minNodeCount,
                  maxNodeCount: args.maxNodeCount
                ]
              ]
            ],
            network: args.network,
            subnetwork: args.subnetwork
          ]
        ]

        def json = new JsonBuilder(clusterTemplate)
        return json.toPrettyString()
    }

    String buildClusterUpdatePayload(Map args){
        boolean enableAutoScaling = toBoolean(args.autoscalingEnabled)
        def json = new JsonBuilder()
        def clusterUpdate = enableAutoScaling ?
            json {
                update {
                    desiredNodePoolId args.nodePoolId
                    desiredNodePoolAutoscaling {
                        enabled true
                        minNodeCount args.minNodeCount
                        maxNodeCount args.maxNodeCount
                    }
                }
            }
         :
            json {
                update {
                    desiredNodePoolId args.nodePoolId
                    desiredNodePoolAutoscaling {
                        enabled false
                    }
                }
            }

        return json.toPrettyString()
    }


}

public class KubernetesClient extends GCEClient {

    /**
     * Retrieves the Deployment instance from GCE Kubernetes cluster.
     * Returns null if no Deployment instance by the given name is found.
     */
    def getDeployment(String clusterEndPoint, String deploymentName, String accessToken) {

        if (OFFLINE) return null

        def response = doHttpGet(clusterEndPoint,
                "/apis/extensions/v1beta1/namespaces/default/deployments/$deploymentName",
                accessToken, /*failOnErrorCode*/ false)
        response.status == 200 ? response.data : null
    }

    /**
     * Retrieves the Service instance from GCE Kubernetes cluster.
     * Returns null if no Service instance by the given name is found.
     */
    def getService(String clusterEndPoint, String serviceName, String accessToken) {

        if (OFFLINE) return null

        def response = doHttpGet(clusterEndPoint,
                "/api/v1/namespaces/default/services/$serviceName",
                accessToken, /*failOnErrorCode*/ false)
        response.status == 200 ? response.data : null
    }

    def createOrUpdateService(String clusterEndPoint, def serviceDetails, String accessToken) {

        String serviceName = serviceDetails.serviceName
        def deployedService = getService(clusterEndPoint, serviceName, accessToken)

        def serviceDefinition = buildServicePayload(serviceDetails, deployedService)

        if (OFFLINE) return null

        if(deployedService){
            doHttpRequest(PUT,
                    clusterEndPoint,
                    "/api/v1/namespaces/default/services/$serviceName",
                    [\'Authorization\' : accessToken],
                    /*failOnErrorCode*/ true,
                    serviceDefinition)

        } else {
            doHttpRequest(POST,
                    clusterEndPoint,
                    \'/api/v1/namespaces/default/services\',
                    [\'Authorization\' : accessToken],
                    /*failOnErrorCode*/ true,
                    serviceDefinition)
        }
    }

    def createOrUpdateDeployment(String clusterEndPoint, def serviceDetails, String accessToken) {

        // Use the same name as the service name to create a Deployment in Kubernetes
        // that will drive the deployment of the service pods.
        def deploymentName = serviceDetails.serviceName
        def existingDeployment = getDeployment(clusterEndPoint, deploymentName, accessToken)
        def deployment = buildDeploymentPayload(serviceDetails, existingDeployment)

        if (OFFLINE) return null

        if (existingDeployment) {
            doHttpRequest(PUT,
                    clusterEndPoint,
                    "/apis/extensions/v1beta1/namespaces/default/deployments/$deploymentName",
                    [\'Authorization\' : accessToken],
                    /*failOnErrorCode*/ true,
                    deployment)

        } else {
            doHttpRequest(POST,
                    clusterEndPoint,
                    \'/apis/extensions/v1beta1/namespaces/default/deployments\',
                    [\'Authorization\' : accessToken],
                    /*failOnErrorCode*/ true,
                    deployment)
        }

    }

    String buildDeploymentPayload(def args, def existingDeployment){

        def json = new JsonBuilder()
        //Get the message calculation out of the way
        int maxSurgeValue = args.maxCapacity ? (args.maxCapacity.toInteger() - args.defaultCapacity.toInteger()) : 1
        int maxUnavailableValue =  args.minCapacity ?
                (args.defaultCapacity.toInteger() - args.minCapacity.toInteger()) : 1

        def result = json {
            kind "Deployment"
            apiVersion "extensions/v1beta1"
            metadata {
                name args.serviceName
            }
            spec {
                replicas args.defaultCapacity.toInteger()
                strategy {
                    rollingUpdate {
                        maxUnavailable maxUnavailableValue
                        maxSurge maxSurgeValue
                    }
                }
                selector {
                    matchLabels {
                        "ec-svc" args.serviceName
                    }
                }
                template {
                    metadata {
                        name args.serviceName
                        labels {
                            "ec-svc" args.serviceName
                        }
                    }
                    spec{
                        containers(args.container.collect { svcContainer ->
                            [
                                    name: svcContainer.containerName.toLowerCase(),
                                    image: "${svcContainer.imagePath}:${svcContainer.imageVersion}",
                                    ports: svcContainer.port?.collect { port ->
                                        [
                                                name: port.portName,
                                                containerPort: port.targetPort.toInteger(),
                                                protocol: port.protocol?: "TCP"
                                                //TODO: hostPort
                                        ]
                                    },
                                    //TODO: handle null
                                    env: svcContainer.environmentVariable?.collect { envVar ->
                                        [
                                                name: envVar.environmentVariableName,
                                                value: envVar.value
                                        ]
                                    }
                            ]
                        })
                    }
                }

            }
        }

        def payload = existingDeployment
        if (payload) {
            payload = mergeObjs(payload, result)
        } else {
            payload = result
        }
        return ((new JsonBuilder(payload)).toPrettyString())
    }

    def addServiceParameters(def json, Map args) {

        def value = getServiceParameter(args, \'loadBalancerIP\')
        if (value != null) {
            json.loadBalancerIP value
        }

        value = getServiceParameter(args, \'sessionAffinity\', \'None\')
        if (value != null) {
            json.sessionAffinity value
        }

        value = getServiceParameterArray(args, \'loadBalancerSourceRanges\')
        if (value != null) {
            json.loadBalancerSourceRanges value
        }
    }

    def getServiceParameter(Map args, String parameterName, def defaultValue = null) {
        def result = args.parameterDetail?.find {
            it.parameterName == parameterName
        }?.parameterValue

        return result != null ? result : defaultValue
    }

    def getServiceParameterArray(Map args, String parameterName, String defaultValue = null) {
        def value = getServiceParameter(args, parameterName, defaultValue)
        value?.toString()?.tokenize(\',\')
    }

    String buildDeploymentPayloadOld(Map args){
        // TODO Right now this template is inside method, would be ideal to be inside class
        // For that we need to find a way to pass arguments to list (Aka template)
        def deploymentTemplate = [
          kind: "Deployment",
          apiVersion: "extensions/v1beta1",
          labels: (new JsonBuilder(args.lableList)).getContent(),
          metadata: [
            name: args.deployName,
            labels: (new JsonBuilder(args.lableList)).getContent()
          ],
          spec: [
            replicas: args.replicas,
            selector: (new JsonBuilder(args.selectorList)).getContent(),
            template: [
                metadata: [
                    name: args.deployName,
                    labels: (new JsonBuilder(args.lableList)).getContent()
                ],
                spec: [
                        containers: (new JsonBuilder(args.containerList)).getContent(),
                        volumes: (new JsonBuilder(args.volumeList)).getContent()
                ]
            ]
          ]
        ]
        def json = new JsonBuilder(deploymentTemplate)
        return json.toPrettyString()
      }

      String getUpdatedDeployment(Map args){
        def updatedDeployment = args.deployData
        updatedDeployment.spec.replicas = args.replicas
        def json = new JsonBuilder(updatedDeployment)
        return json.toPrettyString()
      }

    String buildServicePayload(Map args, def deployedService){

        def json = new JsonBuilder()
        def result = json {
            kind "Service"
            apiVersion "v1"

            metadata {
                name args.serviceName
            }
            //GCE plugin injects this service selector
            //to link the service to the pod that this
            //Deploy service encapsulates.
            spec {
                //service type is currently hard-coded to LoadBalancer
                type "LoadBalancer"
                this.addServiceParameters(delegate, args)

                selector {
                    "ec-svc" args.serviceName
                }
                ports(args.port.collect { svcPort ->
                    [
                            port: svcPort.hostPort.toInteger(),
                            //name is required for Kubernetes if more than one port is specified so auto-assign
                            name: svcPort.portName,
                            targetPort: svcPort.subport?:svcPort.hostPort.toInteger(),
                            // default to TCP which is the default protocol if not set
                            protocol: svcPort.protocol?: "TCP"
                    ]
                })
            }
        }

        def payload = deployedService
        if (payload) {
            payload = mergeObjs(payload, result)
        } else {
            payload = result
        }
        return (new JsonBuilder(payload)).toPrettyString()
    }

}

public class EFClient extends BaseClient {

    def getServerUrl() {
        def commanderServer = System.getenv(\'COMMANDER_SERVER\')
        def commanderPort = System.getenv("COMMANDER_HTTPS_PORT")
        def secure = Integer.getInteger("COMMANDER_SECURE", 1).intValue()
        def protocol = secure ? "https" : "http"

        return "$protocol://$commanderServer:$commanderPort"
    }

    Object doHttpGet(String requestUri, boolean failOnErrorCode = true, def query = null) {
        def sessionId = System.getenv(\'COMMANDER_SESSIONID\')
        doHttpRequest(GET, getServerUrl(), requestUri, [\'Cookie\': "sessionId=$sessionId"],
                failOnErrorCode, /*requestBody*/ null, query)
    }

    Object doHttpPost(String requestUri, Object requestBody, boolean failOnErrorCode = true) {
        def sessionId = System.getenv(\'COMMANDER_SESSIONID\')
        doHttpRequest(POST, getServerUrl(), requestUri, [\'Cookie\': "sessionId=$sessionId"], failOnErrorCode, requestBody)
    }

    def getConfigValues(def configPropertySheet, def config, def pluginProjectName) {

        // Get configs property sheet
        def result = doHttpGet("/rest/v1.0/projects/$pluginProjectName/$configPropertySheet", /*failOnErrorCode*/ false)

        def configPropSheetId = result.data?.property?.propertySheetId
        if (!configPropSheetId) {
            throw new RuntimeException("No plugin configurations exist!")
        }

        result = doHttpGet("/rest/v1.0/propertySheets/$configPropSheetId", /*failOnErrorCode*/ false)
        // Get the property sheet id of the config from the result
        def configProp = result.data.propertySheet.property.find{
            it.propertyName == config
        }

        if (!configProp) {
            throw new RuntimeException("Configuration $config does not exist!")
        }

        result = doHttpGet("/rest/v1.0/propertySheets/$configProp.propertySheetId")

        def values = result.data.propertySheet.property.collectEntries{
            [(it.propertyName): it.value]
        }

        println("Config values: " + values)

        def cred = getCredentials(config)
        values << [credential: [userName: cred.userName, password: cred.password]]

        println("After Config values: " + values)

        values
    }

    def getProvisionClusterParameters(String clusterName,
                                      String clusterOrEnvProjectName,
                                      String environmentName) {

        def partialUri = environmentName ?
                "projects/$clusterOrEnvProjectName/environments/$environmentName/clusters/$clusterName" :
                "projects/$clusterOrEnvProjectName/clusters/$clusterName"

        def result = doHttpGet("/rest/v1.0/$partialUri")

        def params = result.data.cluster?.provisionParameters?.parameterDetail

        if(!params) {
            handleError("No provision parameters found for cluster $clusterName!")
        }

        def provisionParams = params.collectEntries {
            [(it.parameterName): it.parameterValue]
        }

        println "Cluster params from Deploy: $provisionParams"

        return provisionParams
    }

    def getServiceDeploymentDetails(String serviceName,
                                    String serviceProjectName,
                                    String applicationName,
                                    String applicationRevisionId,
                                    String clusterName,
                                    String clusterProjectName,
                                    String environmentName) {

        def partialUri = applicationName ?
                "projects/$serviceProjectName/applications/$applicationName/services/$serviceName" :
                "projects/$serviceProjectName/services/$serviceName"
        def queryArgs = [
                request: \'getServiceDeploymentDetails\',
                clusterName: clusterName,
                clusterProjectName: clusterProjectName,
                environmentName: environmentName,
                applicationEntityRevisionId: applicationRevisionId
        ]
        def result = doHttpGet("/rest/v1.0/$partialUri", /*failOnErrorCode*/ true, queryArgs)

        def svcDetails = result.data.service
        println("Service Details: " + JsonOutput.toJson(svcDetails))

        svcDetails
    }

    def getServiceDeploymentDetailsOld(String serviceName,
                                    String serviceProjectName,
                                    String applicationName,
                                    String applicationRevisionId,
                                    String clusterName,
                                    String getServiceDeploymentDetails,
                                    String environmentName) {
        // TODO: Retrieve service details including containers and mapping overrides
        // set for deploying the service to the given cluster
        [
                //base service attributes defined in deploy
                //(assumed to be merged with any overrides done during service mapping/deployment)
                serviceName: serviceName,
                defaultCapacity: 3,
                minCapacity: 2,
                maxCapacity: 5,
                ports: [
                        [
                                // This is the port that the service will listen on.
                                // Depending on the platform, the necessary wiring to
                                // the container will be done by the plugin.
                                // E.g., on GCE, if the service contains multiple containers
                                // with the same containerPort, then the containerReference
                                // will be used map the service port to the container.
                                // Similarly, on ECS, the container reference will be used
                                // for routing to the appropriate container port with appropriate
                                // ELB configuration.
                                port: \'80\',
                                containerPort: \'8080\',
                                // protocol is optional and will default to \'TCP\' if not specified.
                                protocol: \'TCP\',
                                containerReference: \'container1\'
                        ],
                        [
                                port: \'5000\',
                                containerReference: \'container2\'
                        ]
                ],
                containers:[
                        [
                                //base container attributes defined in deploy
                                name: \'container1\',
                                image:\'gcr.io/microservices-poc-143218/user-ms\',
                                version:\'v4\',
                                entryPoint: \'test entry point\',
                                command: \'echo "hello world"\',
                                cpuLimit: \'cpu limit\',
                                cpuRequest: \'mem req\',
                                memoryLimit: \'mem limit\',
                                memoryRequest: \'cpu req\',
                                environmentVariables:[
                                        [name:\'dbpassword\', value:\'pwd\'],
                                        [name:\'dburl\', value:\'jdbc:mysql://104.198.137.39:3306/mspoc\']
                                ],
                                ports:[
                                        // Typically only container port is required
                                        // for port definitions in containers.
                                        [containerPort: \'5000\'],
                                        [containerPort: \'8080\'],
                                ],
                                //Platform-specific container attributes
                                //defined in defineContainer plugin procedure
                                additionalAttributes: \'[attr1: \\\'pass-thru attr1\\\', attr1: \\\'pass-thru attr1\\\']\'
                        ]
                ],
                //Platform-specific service attributes
                //defined in defineService plugin procedure
                loadBalancerIP: \'load.balancer.I.P\',
                loadBalancerSourceRanges: \'load.balancer.source.ranges\',
                sessionAffinity: \'None\',
                additionalServiceAttributes: \'\',
                additionalPodAttributes: \'\'
        ]
    }

    def getCredentials(def credentialName) {
        def jobStepId = \'$[/myJobStep/jobStepId]\'
        def result = doHttpGet("/rest/v1.0/jobsSteps/$jobStepId/credentials/$credentialName")
        result.data.credential
    }

}

public class BaseClient {

    //Meant for use during development if there is no internet access
    //in which case Google/GCE API calls will become no-ops.
    final boolean OFFLINE = false

    public Integer logLevel
    public static Integer DEBUG = 1
    public static Integer INFO = 2
    public static Integer ERROR = 3


    Object doHttpRequest(Method method, String requestUrl,
                         String requestUri, def requestHeaders,
                         boolean failOnErrorCode = true,
                         Object requestBody = null,
                         def queryArgs = null) {

        println "requestUrl: $requestUrl"
        println "method: $method"
        println "URI: $requestUri"
        if (queryArgs) {
            println "queryArgs: \'$queryArgs\'"
        }
        println "URL: \'$requestUrl$requestUri\'"
        if (requestBody) println "Payload: $requestBody"

        def http = new HTTPBuilder(requestUrl)
        http.ignoreSSLIssues()

        http.request(method, JSON) {
            uri.path = requestUri
            if (queryArgs) {
                uri.query = queryArgs
            }
            headers = requestHeaders
            body = requestBody

            response.success = { resp, json ->
                println "request was successful $resp.statusLine.statusCode $json"
                [statusLine: resp.statusLine,
                 status: resp.status,
                 data      : json]
            }

            response.failure = { resp, reader ->
                println "Error details: $reader"
                if (failOnErrorCode) handleError("Request failed with $resp.statusLine")
                [statusLine: resp.statusLine,
                 status: resp.status]
            }
        }
    }

    def mergeObjs(def dest, def src) {
        //Converting both object instances to a map structure
        //to ease merging the two data structures
        println("\\nSource to merge: " + JsonOutput.toJson(src))
        def result = mergeJSON((new JsonSlurper()).parseText((new JsonBuilder(dest)).toString()),
                (new JsonSlurper()).parseText((new JsonBuilder(src)).toString()))
        println("\\nAfter merge: " + JsonOutput.toJson(result))
        return result
    }

    def mergeJSON(def dest, def src) {
        src.each { prop, value ->
            println "Has property $prop? value:" + dest[prop]
            if(dest[prop] != null && dest[prop] instanceof Map) {
                mergeJSON(dest[prop], value)
            } else {
                dest[prop] = value
            }
        }
        return dest
    }

    /**
     * Based on plugin parameter value truthiness
     * True if value == true or value == \'1\'
     */
    boolean toBoolean(def value) {
        return value != null && (value == true || value == \'true\' || value == 1 || value == \'1\')
    }

    def handleError (String msg) {
        println "ERROR: $msg"
        System.exit(-1)
    }
}'''
    retrieveGrapeDependencies = '''#
#  Copyright 2016 Electric Cloud, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

=head1 NAME

retrieveGrapeDependencies.pl

=head1 DESCRIPTION


Retrieves artifacts published as artifact EC-GoogleContainerEngine-Grapes
to the grape root directory configured with ec-groovy.

=head1 METHODS

=cut

use strict;
use warnings;

use File::Copy::Recursive qw(rcopy);
use File::Path;

use ElectricCommander;

$|=1;

main();

sub main {
    my $ec = ElectricCommander->new();
    $ec->abortOnError(1);

    my $xpath = $ec->retrieveArtifactVersions({
        artifactVersionName => \'com.electriccloud:EC-GoogleContainerEngine-Grapes:1.0.0\'
    });

    # copy to the grape directory ourselves instead of letting
    # retrieveArtifactVersions download to it directly to give
    # us better control over the over-write/update capability.
    # We want to copy only files the retrieved files leaving
    # the other files in the grapes directory unchanged.
    my $dataDir = $ENV{COMMANDER_DATA};
    die "ERROR: Data directory not defined!" unless ($dataDir);

    my $grapesDir = $ENV{COMMANDER_DATA} . \'/grape/grapes\';
    my $dir = $xpath->findvalue("//artifactVersion/cacheDirectory");

    mkpath($grapesDir);
    die "ERROR: Cannot create target directory" unless( -e $grapesDir );

    rcopy( $dir, $grapesDir) or die "Copy failed: $!";
    print "Retrieved and copied grape dependencies to $grapesDir";

}'''
  }
  ec_setup = '''use Cwd;

use File::Spec;
use ElectricCommander::ArtifactManagement;
use ElectricCommander::ArtifactManagement::ArtifactVersion;

use POSIX;

my $dir = getcwd;

my $logfile ="";

my $pluginDir;

if(defined $ENV{QUERY_STRING}) { # Promotion through UI

       $pluginDir = $ENV{COMMANDER_PLUGINS} . "/$pluginName";



} else {

       $pluginDir = $dir;

}



$commander->setProperty("/plugins/$pluginName/project/pluginDir",{value=>$pluginDir});

$logfile .= "Plugin Name: $pluginName\\n";

$logfile .= "Plugin Directory: $pluginDir\\n";



# Evaluate promote.groovy or demote.groovy based on whether plugin is being promoted or demoted ($promoteAction)

local $/ = undef;

# If env variable QUERY_STRING exists:

if(defined $ENV{QUERY_STRING}) { # Promotion through UI

       open FILE, $ENV{COMMANDER_PLUGINS} . "/$pluginName/dsl/$promoteAction.groovy" or die "Couldn\'t open file: $!";

} else {  # Promotion from the command line

       open FILE, "dsl/$promoteAction.groovy" or die "Couldn\'t open file: $!";

}

my $dsl = <FILE>;

close FILE;

my $dslReponse = $commander->evalDsl($dsl,

              {parameters=>qq(

                     {

                           "pluginName":"$pluginName"

                     }

              ),
              debug=>"false",
			  serverLibraryPath=>"$pluginDir/dsl"}

);

$logfile .= $dslReponse->findnodes_as_string("/");

my $errorMessage = $commander->getError();

if (!$errorMessage) {

    # This is here because we cannot do publishArtifactVersion in dsl today

    # delete artifact if it exists first
    $commander->deleteArtifactVersion("com.electriccloud:EC-GoogleContainerEngine-Grapes:1.0.0");

    if ($promoteAction eq "promote") {

        #publish jars to the repo server if the plugin project was created successfully
        my $am = new ElectricCommander::ArtifactManagement($commander);
        my $artifactVersion = $am->publish({
            groupId=>"com.electriccloud",
            artifactKey=>"EC-GoogleContainerEngine-Grapes",
            version=>"1.0.0",
            includePatterns=>"*",
            fromDirectory=>"$pluginDir/lib/grapes",
            description=>"JARs that EC-GoogleContainerEngine plugin procedures depend on"
        });

        # Print out the xml of the published artifactVersion.
        $logfile .=  $artifactVersion->xml() . "\\n";

        if ($artifactVersion->diagnostics()) {
            $logfile .= "\\nDetails:\\n" . $artifactVersion->diagnostics();
        }
    }
}

# Create output property



my $nowString = localtime;

$commander->setProperty("/plugins/$pluginName/project/logs/$nowString",{value=>$logfile});

'''
  pluginDir = 'C:/ProgramData/Electric Cloud/ElectricCommander/plugins/EC-GoogleContainerEngine-1.0.0'
}
