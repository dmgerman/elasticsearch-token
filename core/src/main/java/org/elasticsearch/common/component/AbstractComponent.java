begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.component
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|component
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
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
name|logging
operator|.
name|DeprecationLogger
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|node
operator|.
name|Node
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AbstractComponent
specifier|public
specifier|abstract
class|class
name|AbstractComponent
block|{
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|deprecationLogger
specifier|protected
specifier|final
name|DeprecationLogger
name|deprecationLogger
decl_stmt|;
DECL|field|settings
specifier|protected
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|method|AbstractComponent
specifier|public
name|AbstractComponent
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|deprecationLogger
operator|=
operator|new
name|DeprecationLogger
argument_list|(
name|logger
argument_list|)
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|AbstractComponent
specifier|public
name|AbstractComponent
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Class
name|customClass
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|customClass
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|deprecationLogger
operator|=
operator|new
name|DeprecationLogger
argument_list|(
name|logger
argument_list|)
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
comment|/**      * Returns the nodes name from the settings or the empty string if not set.      */
DECL|method|nodeName
specifier|public
specifier|final
name|String
name|nodeName
parameter_list|()
block|{
return|return
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
return|;
block|}
comment|/**      * Checks for a deprecated setting and logs the correct alternative      */
DECL|method|logDeprecatedSetting
specifier|protected
name|void
name|logDeprecatedSetting
parameter_list|(
name|String
name|settingName
parameter_list|,
name|String
name|alternativeName
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|settings
operator|.
name|get
argument_list|(
name|settingName
argument_list|)
argument_list|)
condition|)
block|{
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"Setting [{}] is deprecated, use [{}] instead"
argument_list|,
name|settingName
argument_list|,
name|alternativeName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Checks for a removed setting and logs the correct alternative      */
DECL|method|logRemovedSetting
specifier|protected
name|void
name|logRemovedSetting
parameter_list|(
name|String
name|settingName
parameter_list|,
name|String
name|alternativeName
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|settings
operator|.
name|get
argument_list|(
name|settingName
argument_list|)
argument_list|)
condition|)
block|{
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"Setting [{}] has been removed, use [{}] instead"
argument_list|,
name|settingName
argument_list|,
name|alternativeName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

