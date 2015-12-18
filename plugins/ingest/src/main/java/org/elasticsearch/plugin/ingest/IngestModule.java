begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
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
name|inject
operator|.
name|multibindings
operator|.
name|MapBinder
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
name|append
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
name|convert
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
name|date
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
name|geoip
operator|.
name|GeoIpProcessor
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
name|grok
operator|.
name|GrokProcessor
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
name|gsub
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
name|join
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
name|lowercase
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
name|remove
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
name|rename
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
name|set
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
name|split
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
name|trim
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
name|uppercase
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
name|plugin
operator|.
name|ingest
operator|.
name|rest
operator|.
name|IngestRestFilter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
DECL|class|IngestModule
specifier|public
class|class
name|IngestModule
extends|extends
name|AbstractModule
block|{
DECL|field|processorFactoryProviders
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ProcessorFactoryProvider
argument_list|>
name|processorFactoryProviders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|binder
argument_list|()
operator|.
name|bind
argument_list|(
name|IngestRestFilter
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|binder
argument_list|()
operator|.
name|bind
argument_list|(
name|IngestBootstrapper
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|addProcessor
argument_list|(
name|GeoIpProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|environment
operator|.
name|configFile
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|GrokProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|GrokProcessor
operator|.
name|Factory
argument_list|(
name|environment
operator|.
name|configFile
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|DateProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|SetProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
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
name|addProcessor
argument_list|(
name|AppendProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
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
name|addProcessor
argument_list|(
name|RenameProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|RenameProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|RemoveProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
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
name|addProcessor
argument_list|(
name|SplitProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|SplitProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|JoinProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|JoinProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|UppercaseProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|UppercaseProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|LowercaseProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|mustacheFactory
parameter_list|)
lambda|->
operator|new
name|LowercaseProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|TrimProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|TrimProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|ConvertProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|ConvertProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|addProcessor
argument_list|(
name|GsubProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|environment
parameter_list|,
name|templateService
parameter_list|)
lambda|->
operator|new
name|GsubProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|MapBinder
argument_list|<
name|String
argument_list|,
name|ProcessorFactoryProvider
argument_list|>
name|mapBinder
init|=
name|MapBinder
operator|.
name|newMapBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|ProcessorFactoryProvider
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ProcessorFactoryProvider
argument_list|>
name|entry
range|:
name|processorFactoryProviders
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|mapBinder
operator|.
name|addBinding
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|toInstance
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Adds a processor factory under a specific type name.      */
DECL|method|addProcessor
specifier|public
name|void
name|addProcessor
parameter_list|(
name|String
name|type
parameter_list|,
name|ProcessorFactoryProvider
name|processorFactoryProvider
parameter_list|)
block|{
name|processorFactoryProviders
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|processorFactoryProvider
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

