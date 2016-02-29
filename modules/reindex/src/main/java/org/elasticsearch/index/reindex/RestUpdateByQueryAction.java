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
name|bytes
operator|.
name|BytesReference
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
name|collect
operator|.
name|Tuple
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
name|XContentHelper
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
name|XContentType
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
name|query
operator|.
name|IndicesQueriesRegistry
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
name|RestChannel
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
name|RestController
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
name|RestRequest
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
name|action
operator|.
name|search
operator|.
name|RestSearchAction
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
name|action
operator|.
name|support
operator|.
name|RestActions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorParsers
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
name|index
operator|.
name|reindex
operator|.
name|AbstractBulkByScrollRequest
operator|.
name|SIZE_ALL_MATCHES
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
operator|.
name|RestReindexAction
operator|.
name|parseCommon
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
name|POST
import|;
end_import

begin_class
DECL|class|RestUpdateByQueryAction
specifier|public
class|class
name|RestUpdateByQueryAction
extends|extends
name|AbstractBaseReindexRestHandler
argument_list|<
name|UpdateByQueryRequest
argument_list|,
name|BulkIndexByScrollResponse
argument_list|,
name|TransportUpdateByQueryAction
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|RestUpdateByQueryAction
specifier|public
name|RestUpdateByQueryAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Client
name|client
parameter_list|,
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
parameter_list|,
name|AggregatorParsers
name|aggParsers
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportUpdateByQueryAction
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|client
argument_list|,
name|indicesQueriesRegistry
argument_list|,
name|aggParsers
argument_list|,
name|clusterService
argument_list|,
name|action
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/_update_by_query"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_update_by_query"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|protected
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
name|Client
name|client
parameter_list|)
throws|throws
name|Exception
block|{
comment|/*          * Passing the search request through UpdateByQueryRequest first allows          * it to set its own defaults which differ from SearchRequest's          * defaults. Then the parse can override them.          */
name|UpdateByQueryRequest
name|internalRequest
init|=
operator|new
name|UpdateByQueryRequest
argument_list|(
operator|new
name|SearchRequest
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|scrollSize
init|=
name|internalRequest
operator|.
name|getSource
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|internalRequest
operator|.
name|getSource
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|size
argument_list|(
name|SIZE_ALL_MATCHES
argument_list|)
expr_stmt|;
comment|/*          * We can't send parseSearchRequest REST content that it doesn't support          * so we will have to remove the content that is valid in addition to          * what it supports from the content first. This is a temporary hack and          * should get better when SearchRequest has full ObjectParser support          * then we can delegate and stuff.          */
name|BytesReference
name|bodyContent
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|RestActions
operator|.
name|hasBodyContent
argument_list|(
name|request
argument_list|)
condition|)
block|{
name|bodyContent
operator|=
name|RestActions
operator|.
name|getRestContent
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|body
init|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|bodyContent
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|boolean
name|modified
init|=
literal|false
decl_stmt|;
name|String
name|conflicts
init|=
operator|(
name|String
operator|)
name|body
operator|.
name|v2
argument_list|()
operator|.
name|remove
argument_list|(
literal|"conflicts"
argument_list|)
decl_stmt|;
if|if
condition|(
name|conflicts
operator|!=
literal|null
condition|)
block|{
name|internalRequest
operator|.
name|setConflicts
argument_list|(
name|conflicts
argument_list|)
expr_stmt|;
name|modified
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|script
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|body
operator|.
name|v2
argument_list|()
operator|.
name|remove
argument_list|(
literal|"script"
argument_list|)
decl_stmt|;
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|internalRequest
operator|.
name|setScript
argument_list|(
name|Script
operator|.
name|parse
argument_list|(
name|script
argument_list|,
literal|false
argument_list|,
name|parseFieldMatcher
argument_list|)
argument_list|)
expr_stmt|;
name|modified
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|modified
condition|)
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|body
operator|.
name|v1
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|body
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
name|bodyContent
operator|=
name|builder
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
block|}
name|RestSearchAction
operator|.
name|parseSearchRequest
argument_list|(
name|internalRequest
operator|.
name|getSource
argument_list|()
argument_list|,
name|indicesQueriesRegistry
argument_list|,
name|request
argument_list|,
name|parseFieldMatcher
argument_list|,
name|aggParsers
argument_list|,
name|bodyContent
argument_list|)
expr_stmt|;
name|String
name|conflicts
init|=
name|request
operator|.
name|param
argument_list|(
literal|"conflicts"
argument_list|)
decl_stmt|;
if|if
condition|(
name|conflicts
operator|!=
literal|null
condition|)
block|{
name|internalRequest
operator|.
name|setConflicts
argument_list|(
name|conflicts
argument_list|)
expr_stmt|;
block|}
name|parseCommon
argument_list|(
name|internalRequest
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|internalRequest
operator|.
name|setSize
argument_list|(
name|internalRequest
operator|.
name|getSource
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|internalRequest
operator|.
name|getSource
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|size
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"scroll_size"
argument_list|,
name|scrollSize
argument_list|)
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|request
argument_list|,
name|internalRequest
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

