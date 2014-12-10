begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.main
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|main
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Constants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Build
import|;
end_import

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
name|client
operator|.
name|Client
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
name|ClusterName
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
name|common
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
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
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|GET
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|HEAD
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestMainAction
specifier|public
class|class
name|RestMainAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|version
specifier|private
specifier|final
name|Version
name|version
decl_stmt|;
DECL|field|clusterName
specifier|private
specifier|final
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
annotation|@
name|Inject
DECL|method|RestMainAction
specifier|public
name|RestMainAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Version
name|version
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|Client
name|client
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
name|client
argument_list|)
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|HEAD
argument_list|,
literal|"/"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
specifier|final
name|Client
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|RestStatus
name|status
init|=
name|RestStatus
operator|.
name|OK
decl_stmt|;
if|if
condition|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|blocks
argument_list|()
operator|.
name|hasGlobalBlock
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
condition|)
block|{
name|status
operator|=
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|method
argument_list|()
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|HEAD
condition|)
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|status
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|XContentBuilder
name|builder
init|=
name|channel
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
comment|// Default to pretty printing, but allow ?pretty=false to disable
if|if
condition|(
operator|!
name|request
operator|.
name|hasParam
argument_list|(
literal|"pretty"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|prettyPrint
argument_list|()
operator|.
name|lfAtEnd
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"cluster_name"
argument_list|,
name|clusterName
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"version"
argument_list|)
operator|.
name|field
argument_list|(
literal|"number"
argument_list|,
name|version
operator|.
name|number
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"build_hash"
argument_list|,
name|Build
operator|.
name|CURRENT
operator|.
name|hash
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"build_timestamp"
argument_list|,
name|Build
operator|.
name|CURRENT
operator|.
name|timestamp
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"build_snapshot"
argument_list|,
name|version
operator|.
name|snapshot
argument_list|)
operator|.
name|field
argument_list|(
literal|"lucene_version"
argument_list|,
name|version
operator|.
name|luceneVersion
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"tagline"
argument_list|,
literal|"You Know, for Search"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|status
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

