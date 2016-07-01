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
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|service
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
name|ParseField
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
name|ParseFieldMatcher
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
name|ParseFieldMatcherSupplier
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
name|unit
operator|.
name|TimeValue
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
name|ObjectParser
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
name|ObjectParser
operator|.
name|ValueType
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
name|XContentParser
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
name|VersionType
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
name|query
operator|.
name|QueryParseContext
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|Suggesters
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|parseTimeValue
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

begin_comment
comment|/**  * Expose IndexBySearchRequest over rest.  */
end_comment

begin_class
DECL|class|RestReindexAction
specifier|public
class|class
name|RestReindexAction
extends|extends
name|AbstractBaseReindexRestHandler
argument_list|<
name|ReindexRequest
argument_list|,
name|TransportReindexAction
argument_list|>
block|{
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|ReindexRequest
argument_list|,
name|ReindexParseContext
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"reindex"
argument_list|)
decl_stmt|;
static|static
block|{
name|ObjectParser
operator|.
name|Parser
argument_list|<
name|SearchRequest
argument_list|,
name|ReindexParseContext
argument_list|>
name|sourceParser
init|=
parameter_list|(
name|parser
parameter_list|,
name|search
parameter_list|,
name|context
parameter_list|)
lambda|->
block|{
comment|/*              * Extract the parameters that we need from the source sent to the parser. We could do away with this hack when search source              * has an ObjectParser.              */
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
name|String
index|[]
name|indices
init|=
name|extractStringArray
argument_list|(
name|source
argument_list|,
literal|"index"
argument_list|)
decl_stmt|;
if|if
condition|(
name|indices
operator|!=
literal|null
condition|)
block|{
name|search
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|types
init|=
name|extractStringArray
argument_list|(
name|source
argument_list|,
literal|"type"
argument_list|)
decl_stmt|;
if|if
condition|(
name|types
operator|!=
literal|null
condition|)
block|{
name|search
operator|.
name|types
argument_list|(
name|types
argument_list|)
expr_stmt|;
block|}
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|parser
operator|.
name|contentType
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|source
argument_list|)
expr_stmt|;
try|try
init|(
name|XContentParser
name|innerParser
init|=
name|parser
operator|.
name|contentType
argument_list|()
operator|.
name|xContent
argument_list|()
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
init|)
block|{
name|search
operator|.
name|source
argument_list|()
operator|.
name|parseXContent
argument_list|(
name|context
operator|.
name|queryParseContext
argument_list|(
name|innerParser
argument_list|)
argument_list|,
name|context
operator|.
name|aggParsers
argument_list|,
name|context
operator|.
name|suggesters
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|ObjectParser
argument_list|<
name|IndexRequest
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|destParser
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"dest"
argument_list|)
decl_stmt|;
name|destParser
operator|.
name|declareString
argument_list|(
name|IndexRequest
operator|::
name|index
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"index"
argument_list|)
argument_list|)
expr_stmt|;
name|destParser
operator|.
name|declareString
argument_list|(
name|IndexRequest
operator|::
name|type
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
name|destParser
operator|.
name|declareString
argument_list|(
name|IndexRequest
operator|::
name|routing
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|destParser
operator|.
name|declareString
argument_list|(
name|IndexRequest
operator|::
name|opType
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"op_type"
argument_list|)
argument_list|)
expr_stmt|;
name|destParser
operator|.
name|declareString
argument_list|(
name|IndexRequest
operator|::
name|setPipeline
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"pipeline"
argument_list|)
argument_list|)
expr_stmt|;
name|destParser
operator|.
name|declareString
argument_list|(
parameter_list|(
name|s
parameter_list|,
name|i
parameter_list|)
lambda|->
name|s
operator|.
name|versionType
argument_list|(
name|VersionType
operator|.
name|fromString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"version_type"
argument_list|)
argument_list|)
expr_stmt|;
comment|// These exist just so the user can get a nice validation error:
name|destParser
operator|.
name|declareString
argument_list|(
name|IndexRequest
operator|::
name|timestamp
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"timestamp"
argument_list|)
argument_list|)
expr_stmt|;
name|destParser
operator|.
name|declareString
argument_list|(
parameter_list|(
name|i
parameter_list|,
name|ttl
parameter_list|)
lambda|->
name|i
operator|.
name|ttl
argument_list|(
name|parseTimeValue
argument_list|(
name|ttl
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|"ttl"
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"ttl"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|p
parameter_list|,
name|v
parameter_list|,
name|c
parameter_list|)
lambda|->
name|sourceParser
operator|.
name|parse
argument_list|(
name|p
argument_list|,
name|v
operator|.
name|getSearchRequest
argument_list|()
argument_list|,
name|c
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"source"
argument_list|)
argument_list|,
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|p
parameter_list|,
name|v
parameter_list|,
name|c
parameter_list|)
lambda|->
name|destParser
operator|.
name|parse
argument_list|(
name|p
argument_list|,
name|v
operator|.
name|getDestination
argument_list|()
argument_list|,
name|c
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"dest"
argument_list|)
argument_list|,
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareInt
argument_list|(
name|ReindexRequest
operator|::
name|setSize
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"size"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|p
parameter_list|,
name|v
parameter_list|,
name|c
parameter_list|)
lambda|->
name|v
operator|.
name|setScript
argument_list|(
name|Script
operator|.
name|parse
argument_list|(
name|p
argument_list|,
name|c
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"script"
argument_list|)
argument_list|,
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
name|ReindexRequest
operator|::
name|setConflicts
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"conflicts"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|RestReindexAction
specifier|public
name|RestReindexAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
parameter_list|,
name|AggregatorParsers
name|aggParsers
parameter_list|,
name|Suggesters
name|suggesters
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportReindexAction
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|indicesQueriesRegistry
argument_list|,
name|aggParsers
argument_list|,
name|suggesters
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
literal|"/_reindex"
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
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|false
operator|==
name|request
operator|.
name|hasContent
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"_reindex requires a request body"
argument_list|)
throw|;
block|}
name|handleRequest
argument_list|(
name|request
argument_list|,
name|channel
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|buildRequest
specifier|protected
name|ReindexRequest
name|buildRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|ReindexRequest
name|internal
init|=
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
decl_stmt|;
try|try
init|(
name|XContentParser
name|xcontent
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|request
operator|.
name|content
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|request
operator|.
name|content
argument_list|()
argument_list|)
init|)
block|{
name|PARSER
operator|.
name|parse
argument_list|(
name|xcontent
argument_list|,
name|internal
argument_list|,
operator|new
name|ReindexParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|,
name|aggParsers
argument_list|,
name|suggesters
argument_list|,
name|parseFieldMatcher
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|internal
return|;
block|}
comment|/**      * Yank a string array from a map. Emulates XContent's permissive String to      * String array conversions.      */
DECL|method|extractStringArray
specifier|private
specifier|static
name|String
index|[]
name|extractStringArray
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|Object
name|value
init|=
name|source
operator|.
name|remove
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|value
operator|instanceof
name|List
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|value
decl_stmt|;
return|return
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|list
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
return|return
operator|new
name|String
index|[]
block|{
operator|(
name|String
operator|)
name|value
block|}
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected ["
operator|+
name|name
operator|+
literal|"] to be a list of a string but was ["
operator|+
name|value
operator|+
literal|']'
argument_list|)
throw|;
block|}
block|}
DECL|class|ReindexParseContext
specifier|private
class|class
name|ReindexParseContext
implements|implements
name|ParseFieldMatcherSupplier
block|{
DECL|field|indicesQueryRegistry
specifier|private
specifier|final
name|IndicesQueriesRegistry
name|indicesQueryRegistry
decl_stmt|;
DECL|field|parseFieldMatcher
specifier|private
specifier|final
name|ParseFieldMatcher
name|parseFieldMatcher
decl_stmt|;
DECL|field|aggParsers
specifier|private
specifier|final
name|AggregatorParsers
name|aggParsers
decl_stmt|;
DECL|field|suggesters
specifier|private
specifier|final
name|Suggesters
name|suggesters
decl_stmt|;
DECL|method|ReindexParseContext
specifier|public
name|ReindexParseContext
parameter_list|(
name|IndicesQueriesRegistry
name|indicesQueryRegistry
parameter_list|,
name|AggregatorParsers
name|aggParsers
parameter_list|,
name|Suggesters
name|suggesters
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
name|this
operator|.
name|indicesQueryRegistry
operator|=
name|indicesQueryRegistry
expr_stmt|;
name|this
operator|.
name|aggParsers
operator|=
name|aggParsers
expr_stmt|;
name|this
operator|.
name|suggesters
operator|=
name|suggesters
expr_stmt|;
name|this
operator|.
name|parseFieldMatcher
operator|=
name|parseFieldMatcher
expr_stmt|;
block|}
DECL|method|queryParseContext
specifier|public
name|QueryParseContext
name|queryParseContext
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
return|return
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueryRegistry
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getParseFieldMatcher
specifier|public
name|ParseFieldMatcher
name|getParseFieldMatcher
parameter_list|()
block|{
return|return
name|this
operator|.
name|parseFieldMatcher
return|;
block|}
block|}
block|}
end_class

end_unit

