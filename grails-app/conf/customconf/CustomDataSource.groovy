package customconf

environments {
    development {
        dataSource {
            dbCreate = 'update' // one of 'create', 'create-drop','update'
            driverClassName = 'com.mysql.jdbc.Driver'
            url = 'jdbc:mysql://localhost/project_guide2'
            username = 'project_guide'
            password = 'project_guide'
            logSql = true
        }
        hibernate {
            dialect = 'org.hibernate.dialect.MySQL5InnoDBDialect'
            show_sql = true
        }
    }
}

