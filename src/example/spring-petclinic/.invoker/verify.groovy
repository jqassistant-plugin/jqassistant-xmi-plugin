// This file is used by the Maven Invoker Plugin to verify the result of the jQAssistant plugin
def xmlReportFile = new File(basedir, '../target/jqassistant/jqassistant-report.xml')
assert xmlReportFile.exists()
def report = new XmlSlurper().parse(xmlReportFile)
def defaultGroup = report.group.find { it.@id = 'default' }

[
        'petclinic:UndefinedJavaComponent',
        'petclinic:UndefinedJavaComponentDependency',
        'petclinic:UnusedUMLComponent',
        'petclinic:UnusedUMLComponentDependency'
].each { constraintId ->
    def constraint = defaultGroup.constraint.find { it.@id == constraintId }
    assert constraint != null && constraint.status == 'success'
}

def htmlReportFile = new File(basedir, '../target/jqassistant/report/asciidoc/index.html')
assert htmlReportFile.exists();


