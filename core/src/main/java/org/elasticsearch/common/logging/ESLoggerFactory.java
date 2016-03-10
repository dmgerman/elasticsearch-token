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
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Setting
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_comment
comment|/**  * Factory to get {@link ESLogger}s  */
end_comment

begin_class
DECL|class|ESLoggerFactory
specifier|public
specifier|abstract
class|class
name|ESLoggerFactory
block|{
DECL|field|LOG_DEFAULT_LEVEL_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|LogLevel
argument_list|>
name|LOG_DEFAULT_LEVEL_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"logger.level"
argument_list|,
name|LogLevel
operator|.
name|INFO
operator|.
name|name
argument_list|()
argument_list|,
name|LogLevel
operator|::
name|parse
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|LOG_LEVEL_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|LogLevel
argument_list|>
name|LOG_LEVEL_SETTING
init|=
name|Setting
operator|.
name|prefixKeySetting
argument_list|(
literal|"logger."
argument_list|,
name|LogLevel
operator|.
name|INFO
operator|.
name|name
argument_list|()
argument_list|,
name|LogLevel
operator|::
name|parse
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|String
name|prefix
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|prefix
operator|=
name|prefix
operator|==
literal|null
condition|?
literal|null
else|:
name|prefix
operator|.
name|intern
argument_list|()
expr_stmt|;
name|name
operator|=
name|name
operator|.
name|intern
argument_list|()
expr_stmt|;
return|return
operator|new
name|ESLogger
argument_list|(
name|prefix
argument_list|,
name|Logger
operator|.
name|getLogger
argument_list|(
name|name
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|getLogger
argument_list|(
literal|null
argument_list|,
name|name
argument_list|)
return|;
block|}
DECL|method|getDeprecationLogger
specifier|public
specifier|static
name|DeprecationLogger
name|getDeprecationLogger
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|DeprecationLogger
argument_list|(
name|getLogger
argument_list|(
name|name
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getDeprecationLogger
specifier|public
specifier|static
name|DeprecationLogger
name|getDeprecationLogger
parameter_list|(
name|String
name|prefix
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|DeprecationLogger
argument_list|(
name|getLogger
argument_list|(
name|prefix
argument_list|,
name|name
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getRootLogger
specifier|public
specifier|static
name|ESLogger
name|getRootLogger
parameter_list|()
block|{
return|return
operator|new
name|ESLogger
argument_list|(
literal|null
argument_list|,
name|Logger
operator|.
name|getRootLogger
argument_list|()
argument_list|)
return|;
block|}
DECL|method|ESLoggerFactory
specifier|private
name|ESLoggerFactory
parameter_list|()
block|{
comment|// Utility class can't be built.
block|}
DECL|enum|LogLevel
specifier|public
enum|enum
name|LogLevel
block|{
DECL|enum constant|WARN
DECL|enum constant|TRACE
DECL|enum constant|INFO
DECL|enum constant|DEBUG
DECL|enum constant|ERROR
name|WARN
block|,
name|TRACE
block|,
name|INFO
block|,
name|DEBUG
block|,
name|ERROR
block|;
DECL|method|parse
specifier|public
specifier|static
name|LogLevel
name|parse
parameter_list|(
name|String
name|level
parameter_list|)
block|{
return|return
name|valueOf
argument_list|(
name|level
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

