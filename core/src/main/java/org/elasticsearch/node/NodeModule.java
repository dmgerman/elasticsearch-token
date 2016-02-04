begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|node
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
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
name|util
operator|.
name|BigArrays
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ProcessorsRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|Processor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|TemplateService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|AppendProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|ConvertProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|DateProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|FailProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|ForEachProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|GsubProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|JoinProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|LowercaseProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|RemoveProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|RenameProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|SetProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|SplitProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|TrimProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|UppercaseProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|MonitorService
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
name|service
operator|.
name|NodeService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiFunction
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NodeModule
specifier|public
class|class
name|NodeModule
extends|extends
name|AbstractModule
block|{
DECL|field|node
specifier|private
specifier|final
name|Node
name|node
decl_stmt|;
DECL|field|monitorService
specifier|private
specifier|final
name|MonitorService
name|monitorService
decl_stmt|;
DECL|field|processorsRegistryBuilder
specifier|private
specifier|final
name|ProcessorsRegistry
operator|.
name|Builder
name|processorsRegistryBuilder
decl_stmt|;
comment|// pkg private so tests can mock
DECL|field|pageCacheRecyclerImpl
name|Class
argument_list|<
name|?
extends|extends
name|PageCacheRecycler
argument_list|>
name|pageCacheRecyclerImpl
init|=
name|PageCacheRecycler
operator|.
name|class
decl_stmt|;
DECL|field|bigArraysImpl
name|Class
argument_list|<
name|?
extends|extends
name|BigArrays
argument_list|>
name|bigArraysImpl
init|=
name|BigArrays
operator|.
name|class
decl_stmt|;
DECL|method|NodeModule
specifier|public
name|NodeModule
parameter_list|(
name|Node
name|node
parameter_list|,
name|MonitorService
name|monitorService
parameter_list|)
block|{
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|monitorService
operator|=
name|monitorService
expr_stmt|;
name|this
operator|.
name|processorsRegistryBuilder
operator|=
operator|new
name|ProcessorsRegistry
operator|.
name|Builder
argument_list|()
expr_stmt|;
name|registerProcessor
argument_list|(
name|DateProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|SetProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|SetProcessor
operator|.
name|Factory
argument_list|(
name|templateService
argument_list|)
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|AppendProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|AppendProcessor
operator|.
name|Factory
argument_list|(
name|templateService
argument_list|)
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|RenameProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|RenameProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|RemoveProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|RemoveProcessor
operator|.
name|Factory
argument_list|(
name|templateService
argument_list|)
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|SplitProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|SplitProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|JoinProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|JoinProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|UppercaseProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|UppercaseProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|LowercaseProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|LowercaseProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|TrimProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|TrimProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|ConvertProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|ConvertProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|GsubProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|GsubProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|FailProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|FailProcessor
operator|.
name|Factory
argument_list|(
name|templateService
argument_list|)
argument_list|)
expr_stmt|;
name|registerProcessor
argument_list|(
name|ForEachProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|,
name|registry
parameter_list|)
lambda|->
operator|new
name|ForEachProcessor
operator|.
name|Factory
argument_list|(
name|registry
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
if|if
condition|(
name|pageCacheRecyclerImpl
operator|==
name|PageCacheRecycler
operator|.
name|class
condition|)
block|{
name|bind
argument_list|(
name|PageCacheRecycler
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|bind
argument_list|(
name|PageCacheRecycler
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|pageCacheRecyclerImpl
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|bigArraysImpl
operator|==
name|BigArrays
operator|.
name|class
condition|)
block|{
name|bind
argument_list|(
name|BigArrays
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|bind
argument_list|(
name|BigArrays
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|bigArraysImpl
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
name|bind
argument_list|(
name|Node
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|MonitorService
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|monitorService
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|NodeService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|ProcessorsRegistry
operator|.
name|Builder
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|processorsRegistryBuilder
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the node      */
DECL|method|getNode
specifier|public
name|Node
name|getNode
parameter_list|()
block|{
return|return
name|node
return|;
block|}
comment|/**      * Adds a processor factory under a specific type name.      */
DECL|method|registerProcessor
specifier|public
name|void
name|registerProcessor
parameter_list|(
name|String
name|type
parameter_list|,
name|BiFunction
argument_list|<
name|TemplateService
argument_list|,
name|ProcessorsRegistry
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|<
name|?
argument_list|>
argument_list|>
name|provider
parameter_list|)
block|{
name|processorsRegistryBuilder
operator|.
name|registerProcessor
argument_list|(
name|type
argument_list|,
name|provider
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

