import org.apache.log4j.*

Logger.getLogger('org.apache.openjpa.enhance').level = Level.INFO

ant.taskdef(name: "openjpac", classname: "org.apache.openjpa.ant.PCEnhancerTask")
eventCompileEnd = {
    grailsConsole.updateStatus "Enhancing OpenJPA classes"
    def metaInf = new File("${grailsSettings.classesDir}/META-INF/persistence.xml")
    grailsConsole.updateStatus "Enhancing OpenJPA with persistence.xml ${metaInf.absolutePath}"
    ant.mkdir(dir: metaInf.parent)

    metaInf.text = '''
<persistence version="1.0" xmlns="http://java.sun.com/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence 
    http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
  <persistence-unit name="enhance" transaction-type="RESOURCE_LOCAL">
    <provider>org.apache.openjpa.persistence.PersistenceProviderImpl</provider>
    <properties>
        <property name="openjpa.Log" value="File=org.apache.openjpa.log, DefaultLevel=WARN, Tool=INFO, Enhance=INFO"/>
    </properties>
  </persistence-unit>
</persistence>    
    '''

    def commonPath = { delim, Object[] paths ->
        def pathParts = paths.collect { it.split(delim) }
        pathParts.transpose().inject([match:true, commonParts:[]]) { aggregator, part ->
            aggregator.match = aggregator.match && part.every { it == part [0] }
            if (aggregator.match) { aggregator.commonParts << part[0] }
            aggregator
        }.commonParts.join(delim)
    }

    commonClassesPath = commonPath('/', classesDir.getAbsolutePath(), pluginClassesDir.getAbsolutePath())
    classesDirRelative = classesDir.absolutePath - (commonClassesPath + '/')
    pluginsDirRelative = pluginClassesDir.absolutePath - (commonClassesPath + '/')

    ant.path(id: "jpa.enhancement.classpath") {
        pathelement location: grailsSettings.classesDir.absolutePath
        pathelement location: grailsSettings.pluginClassesDir.absolutePath
    }

    ant.openjpac {
        delegate.classpath(refid: "jpa.enhancement.classpath")
        fileset(dir: commonClassesPath) {
            exclude name: '**/domain/**/*closure*.class'
            include name: "$classesDirRelative/**/domain/**/*.class"
            include name: "$pluginsDirRelative/**/domain/**/*.class"
            include name: "$pluginsDirRelative/**/org/grails/plugins/settings/Setting.class"
        }
        delegate.config(propertiesFile: metaInf.absolutePath)
    }
    ant.delete(file: metaInf)
    grailsConsole.updateStatus "done Enhancing OpenJPA with persistence.xml ${metaInf.absolutePath}"
}