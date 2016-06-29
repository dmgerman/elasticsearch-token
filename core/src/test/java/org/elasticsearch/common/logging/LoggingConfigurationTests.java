begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.logging
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|StandardOpenOption
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|MDC
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|MockTerminal
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|internal
operator|.
name|InternalSettingsPreparer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|notNullValue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|LoggingConfigurationTests
specifier|public
class|class
name|LoggingConfigurationTests
extends|extends
name|ESTestCase
block|{
annotation|@
name|Before
DECL|method|before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|LogConfigurator
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
DECL|method|testResolveMultipleConfigs
specifier|public
name|void
name|testResolveMultipleConfigs
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|level
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getLevel
argument_list|()
decl_stmt|;
try|try
block|{
name|Path
name|configDir
init|=
name|getDataPath
argument_list|(
literal|"config"
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|configDir
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|LogConfigurator
operator|.
name|configure
argument_list|(
name|settings
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ESLogger
name|esLogger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|Logger
name|logger
init|=
name|esLogger
operator|.
name|getLogger
argument_list|()
decl_stmt|;
name|Appender
name|appender
init|=
name|logger
operator|.
name|getAppender
argument_list|(
literal|"console"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|appender
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|esLogger
operator|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"second"
argument_list|)
expr_stmt|;
name|logger
operator|=
name|esLogger
operator|.
name|getLogger
argument_list|()
expr_stmt|;
name|appender
operator|=
name|logger
operator|.
name|getAppender
argument_list|(
literal|"console2"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|appender
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|esLogger
operator|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"third"
argument_list|)
expr_stmt|;
name|logger
operator|=
name|esLogger
operator|.
name|getLogger
argument_list|()
expr_stmt|;
name|appender
operator|=
name|logger
operator|.
name|getAppender
argument_list|(
literal|"console3"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|appender
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setLevel
argument_list|(
name|level
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testResolveJsonLoggingConfig
specifier|public
name|void
name|testResolveJsonLoggingConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|tmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|loggingConf
init|=
name|tmpDir
operator|.
name|resolve
argument_list|(
name|loggingConfiguration
argument_list|(
literal|"json"
argument_list|)
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|loggingConf
argument_list|,
literal|"{\"json\": \"foo\"}"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tmpDir
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|LogConfigurator
operator|.
name|resolveConfig
argument_list|(
name|environment
argument_list|,
name|builder
argument_list|)
expr_stmt|;
name|Settings
name|logSettings
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|logSettings
operator|.
name|get
argument_list|(
literal|"json"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolvePropertiesLoggingConfig
specifier|public
name|void
name|testResolvePropertiesLoggingConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|tmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|loggingConf
init|=
name|tmpDir
operator|.
name|resolve
argument_list|(
name|loggingConfiguration
argument_list|(
literal|"properties"
argument_list|)
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|loggingConf
argument_list|,
literal|"key: value"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tmpDir
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|LogConfigurator
operator|.
name|resolveConfig
argument_list|(
name|environment
argument_list|,
name|builder
argument_list|)
expr_stmt|;
name|Settings
name|logSettings
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|logSettings
operator|.
name|get
argument_list|(
literal|"key"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveYamlLoggingConfig
specifier|public
name|void
name|testResolveYamlLoggingConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|tmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|loggingConf1
init|=
name|tmpDir
operator|.
name|resolve
argument_list|(
name|loggingConfiguration
argument_list|(
literal|"yml"
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|loggingConf2
init|=
name|tmpDir
operator|.
name|resolve
argument_list|(
name|loggingConfiguration
argument_list|(
literal|"yaml"
argument_list|)
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|loggingConf1
argument_list|,
literal|"yml: bar"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|loggingConf2
argument_list|,
literal|"yaml: bar"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tmpDir
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|LogConfigurator
operator|.
name|resolveConfig
argument_list|(
name|environment
argument_list|,
name|builder
argument_list|)
expr_stmt|;
name|Settings
name|logSettings
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|logSettings
operator|.
name|get
argument_list|(
literal|"yml"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|logSettings
operator|.
name|get
argument_list|(
literal|"yaml"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveConfigInvalidFilename
specifier|public
name|void
name|testResolveConfigInvalidFilename
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|tmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|invalidSuffix
init|=
name|tmpDir
operator|.
name|resolve
argument_list|(
name|loggingConfiguration
argument_list|(
name|randomFrom
argument_list|(
name|LogConfigurator
operator|.
name|ALLOWED_SUFFIXES
argument_list|)
argument_list|)
operator|+
name|randomInvalidSuffix
argument_list|()
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|invalidSuffix
argument_list|,
literal|"yml: bar"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|invalidSuffix
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|LogConfigurator
operator|.
name|resolveConfig
argument_list|(
name|environment
argument_list|,
name|builder
argument_list|)
expr_stmt|;
name|Settings
name|logSettings
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|logSettings
operator|.
name|get
argument_list|(
literal|"yml"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// tests that custom settings are not overwritten by settings in the config file
DECL|method|testResolveOrder
specifier|public
name|void
name|testResolveOrder
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|tmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|loggingConf
init|=
name|tmpDir
operator|.
name|resolve
argument_list|(
name|loggingConfiguration
argument_list|(
literal|"yaml"
argument_list|)
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|loggingConf
argument_list|,
literal|"logger.test_resolve_order: INFO, file\n"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|loggingConf
argument_list|,
literal|"appender.file.type: file\n"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
name|StandardOpenOption
operator|.
name|APPEND
argument_list|)
expr_stmt|;
name|Environment
name|environment
init|=
name|InternalSettingsPreparer
operator|.
name|prepareEnvironment
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tmpDir
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"logger.test_resolve_order"
argument_list|,
literal|"TRACE, console"
argument_list|)
operator|.
name|put
argument_list|(
literal|"appender.console.type"
argument_list|,
literal|"console"
argument_list|)
operator|.
name|put
argument_list|(
literal|"appender.console.layout.type"
argument_list|,
literal|"consolePattern"
argument_list|)
operator|.
name|put
argument_list|(
literal|"appender.console.layout.conversionPattern"
argument_list|,
literal|"[%d{ISO8601}][%-5p][%-25c] %m%n"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|MockTerminal
argument_list|()
argument_list|)
decl_stmt|;
name|LogConfigurator
operator|.
name|configure
argument_list|(
name|environment
operator|.
name|settings
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// args should overwrite whatever is in the config
name|ESLogger
name|esLogger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"test_resolve_order"
argument_list|)
decl_stmt|;
name|Logger
name|logger
init|=
name|esLogger
operator|.
name|getLogger
argument_list|()
decl_stmt|;
name|Appender
name|appender
init|=
name|logger
operator|.
name|getAppender
argument_list|(
literal|"console"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|appender
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|appender
operator|=
name|logger
operator|.
name|getAppender
argument_list|(
literal|"file"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|appender
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// tests that config file is not read when we call LogConfigurator.configure(Settings, false)
DECL|method|testConfigNotRead
specifier|public
name|void
name|testConfigNotRead
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|tmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|loggingConf
init|=
name|tmpDir
operator|.
name|resolve
argument_list|(
name|loggingConfiguration
argument_list|(
literal|"yaml"
argument_list|)
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|loggingConf
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"logger.test_config_not_read: INFO, console"
argument_list|,
literal|"appender.console.type: console"
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
name|Environment
name|environment
init|=
name|InternalSettingsPreparer
operator|.
name|prepareEnvironment
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tmpDir
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|MockTerminal
argument_list|()
argument_list|)
decl_stmt|;
name|LogConfigurator
operator|.
name|configure
argument_list|(
name|environment
operator|.
name|settings
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ESLogger
name|esLogger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"test_config_not_read"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|esLogger
argument_list|)
expr_stmt|;
name|Logger
name|logger
init|=
name|esLogger
operator|.
name|getLogger
argument_list|()
decl_stmt|;
name|Appender
name|appender
init|=
name|logger
operator|.
name|getAppender
argument_list|(
literal|"console"
argument_list|)
decl_stmt|;
comment|// config was not read
name|assertNull
argument_list|(
name|appender
argument_list|)
expr_stmt|;
block|}
DECL|method|loggingConfiguration
specifier|private
specifier|static
name|String
name|loggingConfiguration
parameter_list|(
name|String
name|suffix
parameter_list|)
block|{
return|return
literal|"logging."
operator|+
name|randomAsciiOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
argument_list|)
operator|+
literal|"."
operator|+
name|suffix
return|;
block|}
DECL|method|randomInvalidSuffix
specifier|private
specifier|static
name|String
name|randomInvalidSuffix
parameter_list|()
block|{
name|String
name|randomSuffix
decl_stmt|;
do|do
block|{
name|randomSuffix
operator|=
name|randomAsciiOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|LogConfigurator
operator|.
name|ALLOWED_SUFFIXES
operator|.
name|contains
argument_list|(
name|randomSuffix
argument_list|)
condition|)
do|;
return|return
name|randomSuffix
return|;
block|}
block|}
end_class

end_unit

