begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.gce
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|gce
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
name|inject
operator|.
name|AbstractModule
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

begin_class
DECL|class|GceModule
specifier|public
class|class
name|GceModule
extends|extends
name|AbstractModule
block|{
comment|// pkg private so tests can override with mock
DECL|field|computeServiceImpl
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|GceComputeService
argument_list|>
name|computeServiceImpl
init|=
name|GceComputeServiceImpl
operator|.
name|class
decl_stmt|;
DECL|field|metadataServiceImpl
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|GceMetadataService
argument_list|>
name|metadataServiceImpl
init|=
name|GceMetadataServiceImpl
operator|.
name|class
decl_stmt|;
DECL|field|settings
specifier|protected
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|GceModule
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|GceModule
specifier|public
name|GceModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|getComputeServiceImpl
specifier|public
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|GceComputeService
argument_list|>
name|getComputeServiceImpl
parameter_list|()
block|{
return|return
name|computeServiceImpl
return|;
block|}
DECL|method|getMetadataServiceImpl
specifier|public
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|GceMetadataService
argument_list|>
name|getMetadataServiceImpl
parameter_list|()
block|{
return|return
name|metadataServiceImpl
return|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"configure GceModule (bind compute and metadata services)"
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|GceComputeService
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|computeServiceImpl
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|GceMetadataService
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|metadataServiceImpl
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

