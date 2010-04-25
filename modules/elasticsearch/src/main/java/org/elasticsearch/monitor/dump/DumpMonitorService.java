begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.dump
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|dump
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|Inject
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
name|ClusterService
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
name|monitor
operator|.
name|dump
operator|.
name|heap
operator|.
name|HeapDumpContributor
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
name|dump
operator|.
name|summary
operator|.
name|SummaryDumpContributor
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
name|dump
operator|.
name|thread
operator|.
name|ThreadDumpContributor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|component
operator|.
name|AbstractComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Maps
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|dump
operator|.
name|heap
operator|.
name|HeapDumpContributor
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|dump
operator|.
name|summary
operator|.
name|SummaryDumpContributor
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|dump
operator|.
name|thread
operator|.
name|ThreadDumpContributor
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|DumpMonitorService
specifier|public
class|class
name|DumpMonitorService
extends|extends
name|AbstractComponent
block|{
DECL|field|dumpLocation
specifier|private
specifier|final
name|String
name|dumpLocation
decl_stmt|;
DECL|field|generator
specifier|private
specifier|final
name|DumpGenerator
name|generator
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|contSettings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|contSettings
decl_stmt|;
DECL|field|contributors
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|DumpContributorFactory
argument_list|>
name|contributors
decl_stmt|;
DECL|field|workFile
specifier|private
specifier|final
name|File
name|workFile
decl_stmt|;
DECL|method|DumpMonitorService
specifier|public
name|DumpMonitorService
parameter_list|()
block|{
name|this
argument_list|(
name|EMPTY_SETTINGS
argument_list|,
operator|new
name|Environment
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|DumpMonitorService
annotation|@
name|Inject
specifier|public
name|DumpMonitorService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Environment
name|environment
parameter_list|,
annotation|@
name|Nullable
name|ClusterService
name|clusterService
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|DumpContributorFactory
argument_list|>
name|contributors
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|contributors
operator|=
name|contributors
expr_stmt|;
name|contSettings
operator|=
name|settings
operator|.
name|getGroups
argument_list|(
literal|"monitor.dump"
argument_list|)
expr_stmt|;
name|workFile
operator|=
name|environment
operator|.
name|workWithClusterFile
argument_list|()
expr_stmt|;
name|this
operator|.
name|dumpLocation
operator|=
name|settings
operator|.
name|get
argument_list|(
literal|"dump_location"
argument_list|)
expr_stmt|;
name|File
name|dumpLocationFile
decl_stmt|;
if|if
condition|(
name|dumpLocation
operator|!=
literal|null
condition|)
block|{
name|dumpLocationFile
operator|=
operator|new
name|File
argument_list|(
name|dumpLocation
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dumpLocationFile
operator|=
operator|new
name|File
argument_list|(
name|workFile
argument_list|,
literal|"dump"
argument_list|)
expr_stmt|;
block|}
name|boolean
name|success
init|=
name|dumpLocationFile
operator|.
name|mkdirs
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|DumpContributor
argument_list|>
name|contributorMap
init|=
name|newHashMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|contributors
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DumpContributorFactory
argument_list|>
name|entry
range|:
name|contributors
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|contName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|DumpContributorFactory
name|dumpContributorFactory
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Settings
name|analyzerSettings
init|=
name|contSettings
operator|.
name|get
argument_list|(
name|contName
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzerSettings
operator|==
literal|null
condition|)
block|{
name|analyzerSettings
operator|=
name|EMPTY_SETTINGS
expr_stmt|;
block|}
name|DumpContributor
name|analyzerFactory
init|=
name|dumpContributorFactory
operator|.
name|create
argument_list|(
name|contName
argument_list|,
name|analyzerSettings
argument_list|)
decl_stmt|;
name|contributorMap
operator|.
name|put
argument_list|(
name|contName
argument_list|,
name|analyzerFactory
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|contributorMap
operator|.
name|containsKey
argument_list|(
name|SUMMARY
argument_list|)
condition|)
block|{
name|contributorMap
operator|.
name|put
argument_list|(
name|SUMMARY
argument_list|,
operator|new
name|SummaryDumpContributor
argument_list|(
name|SUMMARY
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|contributorMap
operator|.
name|containsKey
argument_list|(
name|HEAP_DUMP
argument_list|)
condition|)
block|{
name|contributorMap
operator|.
name|put
argument_list|(
name|HEAP_DUMP
argument_list|,
operator|new
name|HeapDumpContributor
argument_list|(
name|HEAP_DUMP
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|contributorMap
operator|.
name|containsKey
argument_list|(
name|THREAD_DUMP
argument_list|)
condition|)
block|{
name|contributorMap
operator|.
name|put
argument_list|(
name|THREAD_DUMP
argument_list|,
operator|new
name|ThreadDumpContributor
argument_list|(
name|THREAD_DUMP
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|generator
operator|=
operator|new
name|SimpleDumpGenerator
argument_list|(
name|dumpLocationFile
argument_list|,
name|contributorMap
argument_list|)
expr_stmt|;
block|}
DECL|method|generateDump
specifier|public
name|DumpGenerator
operator|.
name|Result
name|generateDump
parameter_list|(
name|String
name|cause
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|context
parameter_list|)
throws|throws
name|DumpGenerationFailedException
block|{
return|return
name|generator
operator|.
name|generateDump
argument_list|(
name|cause
argument_list|,
name|fillContextMap
argument_list|(
name|context
argument_list|)
argument_list|)
return|;
block|}
DECL|method|generateDump
specifier|public
name|DumpGenerator
operator|.
name|Result
name|generateDump
parameter_list|(
name|String
name|cause
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|context
parameter_list|,
name|String
modifier|...
name|contributors
parameter_list|)
throws|throws
name|DumpGenerationFailedException
block|{
return|return
name|generator
operator|.
name|generateDump
argument_list|(
name|cause
argument_list|,
name|fillContextMap
argument_list|(
name|context
argument_list|)
argument_list|,
name|contributors
argument_list|)
return|;
block|}
DECL|method|fillContextMap
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|fillContextMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|==
literal|null
condition|)
block|{
name|context
operator|=
name|newHashMap
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|clusterService
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|put
argument_list|(
literal|"localNode"
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|context
return|;
block|}
block|}
end_class

end_unit

