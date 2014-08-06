begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|CommonStatsFlags
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
name|component
operator|.
name|LifecycleComponent
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
name|index
operator|.
name|service
operator|.
name|IndexService
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_interface
DECL|interface|IndicesService
specifier|public
interface|interface
name|IndicesService
extends|extends
name|Iterable
argument_list|<
name|IndexService
argument_list|>
extends|,
name|LifecycleComponent
argument_list|<
name|IndicesService
argument_list|>
block|{
comment|/**      * Returns<tt>true</tt> if changes (adding / removing) indices, shards and so on are allowed.      */
DECL|method|changesAllowed
specifier|public
name|boolean
name|changesAllowed
parameter_list|()
function_decl|;
comment|/**      * Returns the node stats indices stats. The<tt>includePrevious</tt> flag controls      * if old shards stats will be aggregated as well (only for relevant stats, such as      * refresh and indexing, not for docs/store).      */
DECL|method|stats
name|NodeIndicesStats
name|stats
parameter_list|(
name|boolean
name|includePrevious
parameter_list|)
function_decl|;
DECL|method|stats
name|NodeIndicesStats
name|stats
parameter_list|(
name|boolean
name|includePrevious
parameter_list|,
name|CommonStatsFlags
name|flags
parameter_list|)
function_decl|;
DECL|method|hasIndex
name|boolean
name|hasIndex
parameter_list|(
name|String
name|index
parameter_list|)
function_decl|;
DECL|method|indicesLifecycle
name|IndicesLifecycle
name|indicesLifecycle
parameter_list|()
function_decl|;
comment|/**      * Returns a snapshot of the started indices and the associated {@link IndexService} instances.      *      * The map being returned is not a live view and subsequent calls can return a different view.      */
DECL|method|indices
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|IndexService
argument_list|>
name|indices
parameter_list|()
function_decl|;
comment|/**      * Returns an IndexService for the specified index if exists otherwise returns<code>null</code>.      *      * Even if the index name appeared in {@link #indices()}<code>null</code> can still be returned as an      * index maybe removed in the meantime, so preferable use the associated {@link IndexService} in order to prevent NPE.      */
DECL|method|indexService
name|IndexService
name|indexService
parameter_list|(
name|String
name|index
parameter_list|)
function_decl|;
comment|/**      * Returns an IndexService for the specified index if exists otherwise a {@link IndexMissingException} is thrown.      */
DECL|method|indexServiceSafe
name|IndexService
name|indexServiceSafe
parameter_list|(
name|String
name|index
parameter_list|)
throws|throws
name|IndexMissingException
function_decl|;
DECL|method|createIndex
name|IndexService
name|createIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|String
name|localNodeId
parameter_list|)
throws|throws
name|ElasticsearchException
function_decl|;
DECL|method|removeIndex
name|void
name|removeIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|reason
parameter_list|)
throws|throws
name|ElasticsearchException
function_decl|;
block|}
end_interface

end_unit

