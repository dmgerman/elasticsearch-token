begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
operator|.
name|ScrollableHitSource
operator|.
name|Hit
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
name|index
operator|.
name|IndexRequest
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
name|search
operator|.
name|SearchRequest
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

begin_comment
comment|/**  * Index-by-search test for ttl, timestamp, and routing.  */
end_comment

begin_class
DECL|class|ReindexMetadataTests
specifier|public
class|class
name|ReindexMetadataTests
extends|extends
name|AbstractAsyncBulkByScrollActionMetadataTestCase
argument_list|<
name|ReindexRequest
argument_list|,
name|BulkByScrollResponse
argument_list|>
block|{
DECL|method|testRoutingCopiedByDefault
specifier|public
name|void
name|testRoutingCopiedByDefault
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexRequest
name|index
init|=
operator|new
name|IndexRequest
argument_list|()
decl_stmt|;
name|action
argument_list|()
operator|.
name|copyMetadata
argument_list|(
name|AbstractAsyncBulkByScrollAction
operator|.
name|wrap
argument_list|(
name|index
argument_list|)
argument_list|,
name|doc
argument_list|()
operator|.
name|setRouting
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|index
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRoutingCopiedIfRequested
specifier|public
name|void
name|testRoutingCopiedIfRequested
parameter_list|()
throws|throws
name|Exception
block|{
name|TestAction
name|action
init|=
name|action
argument_list|()
decl_stmt|;
name|action
operator|.
name|mainRequest
argument_list|()
operator|.
name|getDestination
argument_list|()
operator|.
name|routing
argument_list|(
literal|"keep"
argument_list|)
expr_stmt|;
name|IndexRequest
name|index
init|=
operator|new
name|IndexRequest
argument_list|()
decl_stmt|;
name|action
operator|.
name|copyMetadata
argument_list|(
name|AbstractAsyncBulkByScrollAction
operator|.
name|wrap
argument_list|(
name|index
argument_list|)
argument_list|,
name|doc
argument_list|()
operator|.
name|setRouting
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|index
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRoutingDiscardedIfRequested
specifier|public
name|void
name|testRoutingDiscardedIfRequested
parameter_list|()
throws|throws
name|Exception
block|{
name|TestAction
name|action
init|=
name|action
argument_list|()
decl_stmt|;
name|action
operator|.
name|mainRequest
argument_list|()
operator|.
name|getDestination
argument_list|()
operator|.
name|routing
argument_list|(
literal|"discard"
argument_list|)
expr_stmt|;
name|IndexRequest
name|index
init|=
operator|new
name|IndexRequest
argument_list|()
decl_stmt|;
name|action
operator|.
name|copyMetadata
argument_list|(
name|AbstractAsyncBulkByScrollAction
operator|.
name|wrap
argument_list|(
name|index
argument_list|)
argument_list|,
name|doc
argument_list|()
operator|.
name|setRouting
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|index
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRoutingSetIfRequested
specifier|public
name|void
name|testRoutingSetIfRequested
parameter_list|()
throws|throws
name|Exception
block|{
name|TestAction
name|action
init|=
name|action
argument_list|()
decl_stmt|;
name|action
operator|.
name|mainRequest
argument_list|()
operator|.
name|getDestination
argument_list|()
operator|.
name|routing
argument_list|(
literal|"=cat"
argument_list|)
expr_stmt|;
name|IndexRequest
name|index
init|=
operator|new
name|IndexRequest
argument_list|()
decl_stmt|;
name|action
operator|.
name|copyMetadata
argument_list|(
name|AbstractAsyncBulkByScrollAction
operator|.
name|wrap
argument_list|(
name|index
argument_list|)
argument_list|,
name|doc
argument_list|()
operator|.
name|setRouting
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cat"
argument_list|,
name|index
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRoutingSetIfWithDegenerateValue
specifier|public
name|void
name|testRoutingSetIfWithDegenerateValue
parameter_list|()
throws|throws
name|Exception
block|{
name|TestAction
name|action
init|=
name|action
argument_list|()
decl_stmt|;
name|action
operator|.
name|mainRequest
argument_list|()
operator|.
name|getDestination
argument_list|()
operator|.
name|routing
argument_list|(
literal|"==]"
argument_list|)
expr_stmt|;
name|IndexRequest
name|index
init|=
operator|new
name|IndexRequest
argument_list|()
decl_stmt|;
name|action
operator|.
name|copyMetadata
argument_list|(
name|AbstractAsyncBulkByScrollAction
operator|.
name|wrap
argument_list|(
name|index
argument_list|)
argument_list|,
name|doc
argument_list|()
operator|.
name|setRouting
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"=]"
argument_list|,
name|index
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|action
specifier|protected
name|TestAction
name|action
parameter_list|()
block|{
return|return
operator|new
name|TestAction
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|request
specifier|protected
name|ReindexRequest
name|request
parameter_list|()
block|{
return|return
operator|new
name|ReindexRequest
argument_list|(
operator|new
name|SearchRequest
argument_list|()
argument_list|,
operator|new
name|IndexRequest
argument_list|()
argument_list|)
return|;
block|}
DECL|class|TestAction
specifier|private
class|class
name|TestAction
extends|extends
name|TransportReindexAction
operator|.
name|AsyncIndexBySearchAction
block|{
DECL|method|TestAction
name|TestAction
parameter_list|()
block|{
name|super
argument_list|(
name|ReindexMetadataTests
operator|.
name|this
operator|.
name|task
argument_list|,
name|ReindexMetadataTests
operator|.
name|this
operator|.
name|logger
argument_list|,
literal|null
argument_list|,
name|ReindexMetadataTests
operator|.
name|this
operator|.
name|threadPool
argument_list|,
name|request
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|listener
argument_list|()
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|mainRequest
specifier|public
name|ReindexRequest
name|mainRequest
parameter_list|()
block|{
return|return
name|this
operator|.
name|mainRequest
return|;
block|}
annotation|@
name|Override
DECL|method|copyMetadata
specifier|public
name|AbstractAsyncBulkByScrollAction
operator|.
name|RequestWrapper
argument_list|<
name|?
argument_list|>
name|copyMetadata
parameter_list|(
name|AbstractAsyncBulkByScrollAction
operator|.
name|RequestWrapper
argument_list|<
name|?
argument_list|>
name|request
parameter_list|,
name|Hit
name|doc
parameter_list|)
block|{
return|return
name|super
operator|.
name|copyMetadata
argument_list|(
name|request
argument_list|,
name|doc
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

