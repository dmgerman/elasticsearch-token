begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|indices
operator|.
name|analysis
operator|.
name|PreBuiltCharFilters
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

begin_class
DECL|class|PreBuiltCharFilterFactoryFactory
specifier|public
class|class
name|PreBuiltCharFilterFactoryFactory
implements|implements
name|CharFilterFactoryFactory
block|{
DECL|field|charFilterFactory
specifier|private
specifier|final
name|CharFilterFactory
name|charFilterFactory
decl_stmt|;
DECL|method|PreBuiltCharFilterFactoryFactory
specifier|public
name|PreBuiltCharFilterFactoryFactory
parameter_list|(
name|CharFilterFactory
name|charFilterFactory
parameter_list|)
block|{
name|this
operator|.
name|charFilterFactory
operator|=
name|charFilterFactory
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|CharFilterFactory
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|Version
name|indexVersion
init|=
name|settings
operator|.
name|getAsVersion
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Version
operator|.
name|CURRENT
operator|.
name|equals
argument_list|(
name|indexVersion
argument_list|)
condition|)
block|{
return|return
name|PreBuiltCharFilters
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
operator|.
name|getCharFilterFactory
argument_list|(
name|indexVersion
argument_list|)
return|;
block|}
return|return
name|charFilterFactory
return|;
block|}
block|}
end_class

end_unit

