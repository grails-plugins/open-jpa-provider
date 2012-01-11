class OpenJpaProviderGrailsPlugin {
    // the plugin version
    def version = "1.0.0.M1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Open Jpa Provider Plugin" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/open-jpa-provider"

    // Extra (optional) plugin metadata


    // Details of company behind the plugin (if there is one)
    def organization = [ name: "SpringSource", url: "http://www.springsource.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Graeme Rocher", email: "grocher@vmware.com" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPGORMJPA" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/SpringSource/grails-data-mapping/tree/master/grails-plugins" ]
    

    def doWithSpring = {
        def domainClassPackages = application.domainClasses.collect { it.clazz.getPackage().name }.unique()
        if(!domainClassPackages) domainClassPackages = [application.metadata.getApplicationName()]
        
        entityManagerFactory(org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean) {
            packagesToScan = domainClassPackages                
            dataSource = ref("dataSource")
            jpaVendorAdapter = ref("jpaVendorAdapter")
            jpaPropertyMap = ref("jpaProperties")
        }
        
    	transactionManager(org.springframework.orm.jpa.JpaTransactionManager) {
  	        entityManagerFactory = entityManagerFactory
  	    }        
  	    
  	    addAlias("jpaTransactionManager", "transactionManager")
  	    
        def jpaConfig = application.config.jpa
        def ds = application.config.dataSource
        "jpaProperties"(org.springframework.beans.factory.config.PropertiesFactoryBean) { bean ->
              bean.scope = "prototype"
              properties = jpaConfig
        }        
        jpaVendorAdapter(org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter) {
            if(ds.dbCreate != null) {
                generateDdl = true
            }
            if(ds.logSql) {
                showSql = true
            }
            if(ds.driverClassName?.contains('h2')) {
                database = org.springframework.orm.jpa.vendor.Database.H2
            }
        }

    }
    def onChange = { event ->
        def beans = beans {
             def domainClassPackages = application.domainClasses.collect { it.clazz.getPackage().name }.unique()
             if(!domainClassPackages) domainClassPackages = [application.metadata.getApplicationName()]

             entityManagerFactory(org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean) {
                 packagesToScan = domainClassPackages                
                 dataSource = ref("dataSource")
                 jpaVendorAdapter = ref("jpaVendorAdapter")
                 jpaPropertyMap = ref("jpaProperties")
             }       
         	 transactionManager(org.springframework.orm.jpa.JpaTransactionManager) {
       	        entityManagerFactory = entityManagerFactory
       	     }        

       	     addAlias("jpaTransactionManager", "transactionManager")
         }

         beans.registerBeans(event.ctx)
    }

}
