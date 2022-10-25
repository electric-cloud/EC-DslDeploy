import java.io.File

def propertyContent = new File(propsDir, 'prop1.txt').text

property 'prop1', value: """$propertyContent""", {
  description = 'prop1'
}
