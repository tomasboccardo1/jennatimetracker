grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.war.file = "target/${appName}.war"


grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn"
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        /* Specify dependencies here under either
           build', 'compile', 'runtime', 'test' or 'provided' scopes eg.*/
        compile 'javax.activation:activation:1.1'
        compile 'commons-digester:commons-digester:2.0'
        compile 'commons-logging:commons-logging:1.1.1'
        compile 'javax.mail:mail:1.4'
        compile 'org.apache.poi:poi:3.2-FINAL'
        compile 'ar.com.fdvs:DynamicJasper:3.0.13'
        compile 'org.markdownj:markdownj:0.3.0-1.0.2b4'
        compile 'jivesoftware:smack:3.0.4'
        compile 'jivesoftware:smackx:3.0.4'
        runtime 'mysql:mysql-connector-java:5.1.10'
        compile 'com.googlecode.json-simple:json-simple:1.1.1'
    }

    plugins {
        compile ":hibernate:$grailsVersion"
        compile ":avatar:0.6.3"
        compile ':spring-security-oauth:2.0.1.1'
        compile ":hibernate-filter:0.3.2"
        compile ":i18n-templates:1.1.0.1"
        compile ":quartz:0.4.2"
        compile ":quartz-monitor:0.3-RC2"
        compile ":session-temp-files:1.0"
        compile ":export:1.5"
        compile ":mail:1.0.1"
        compile ':database-migration:1.3.6'
        runtime ":resources:1.2.1"
        runtime ":yui-minify-resources:0.1.5"
        build ":tomcat:$grailsVersion"
    }
}
