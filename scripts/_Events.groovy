ant.taskdef( name:"openjpac", classname:"org.apache.openjpa.ant.PCEnhancerTask")
eventCompileEnd = {
    grailsConsole.updateStatus "Enhancing OpenJPA classes"
    def metaInf = new File("${grailsSettings.classesDir}/META-INF/persistence.xml")
    ant.mkdir(dir:metaInfo)
    metaInf.text = '''
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="1.0" xmlns="http://java.sun.com/xml/ns/persistence" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence 
    http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
  <persistence-unit name="enhance" transaction-type="RESOURCE_LOCAL">
    <provider>org.apache.openjpa.persistence.PersistenceProviderImpl</provider>
  </persistence-unit>
</persistence>    
    '''
    
    ant.path( id:"jpa.enhancement.classpath" ) {
        pathelement location:grailsSettings.classesDir
    }
    ant.openjpac(directory:grailsSettings.classesDir) {
        fileset(dir: grailsSettings.classesDir) {
            include name:"**/*.class"
            config log:"openjpa.Log"
        }
    }
    ant.delete(file:metaInfo)
}