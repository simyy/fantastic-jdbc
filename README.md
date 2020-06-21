# Fantastic-JDBC

`Fantastic-JDBC` is a simplified ORM for learning, which is based on `MyBatis`.

## Architecture

`Fantastic-JDBC` contains most of modules in `MyBatis`, such as

* Configuration
    * MapperRegistry
    * UnpooledDataSource
    * mappedStatements
    * SqlSource(Dynamic/Raw/Static)
    
* Builder
    * XMLConfigBuilder
    * XMLMapperBuilder
    * XMLStatementBuilder
    * XMLScriptBuilder
    
* SqlSession
    * Executor
    * StatementHandler
