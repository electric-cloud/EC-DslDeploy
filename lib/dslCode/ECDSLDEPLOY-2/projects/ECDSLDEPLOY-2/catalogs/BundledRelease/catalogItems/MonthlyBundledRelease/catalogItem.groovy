def itemName='MonthlyBundledRelease'

 catalogItem itemName,
   iconUrl: 'icon-release.svg',
   description: '''<xml>
 <title>
   Template to create release for bundled releases.
 </title>

 <htmlData>
   <![CDATA[
   ]]>
 </htmlData>
</xml>''',
   buttonLabel: 'Create',
   dslParamForm: '''{
 "sections": {
   "section": [
     {
       "name": "Release",
       "instruction": "Provide details on the release to be created",

       "ec_parameterForm": "<editor><formElement> <label>Release Name</label> <property>relName</property> <documentation>Name of the release to be created.</documentation> <type>entry</type> <required>1</required><value>jan2019</value> </formElement><formElement> <label>Release Description</label> <property>relDes</property> <documentation>Description of the release to be created.</documentation> <type>entry</type> <required>1</required> <value>Bundled release for Jan 2019</value></formElement><formElement> <label>Release Start Date</label> <property>startDate</property> <documentation>Planned start date for this release.</documentation> <type>entry</type> <required>0</required><value>2019-10-01</value> </formElement><formElement> <label>Release End Date</label> <property>endDate</property> <documentation>Planned end date for this release.</documentation> <type>entry</type> <required>0</required><value>2019-01-31</value> </formElement></editor>"
     }
   ],
   "endTarget": {
     "object": "release",
     "formValue": "relName"
   }
 }
}''',
 dslString: new File(projectDir, "catalogs/$catalogName/catalogItems/$itemName/code/${itemName}.groovy").text
