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
name|Strings
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
name|ToXContent
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
name|common
operator|.
name|xcontent
operator|.
name|json
operator|.
name|JsonXContent
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
name|index
operator|.
name|reindex
operator|.
name|remote
operator|.
name|RemoteInfo
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
name|SearchRequestParsers
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
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Objects
operator|.
name|requireNonNull
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
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchAllQuery
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
comment|/**  * Expose reindex over rest.  */
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
name|ReindexAction
argument_list|>
block|{
DECL|field|PARSER
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
DECL|field|HOST_PATTERN
specifier|private
specifier|static
specifier|final
name|Pattern
name|HOST_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"(?<scheme>[^:]+)://(?<host>[^:]+):(?<port>\\d+)"
argument_list|)
decl_stmt|;
static|static
block|{
name|ObjectParser
operator|.
name|Parser
argument_list|<
name|ReindexRequest
argument_list|,
name|ReindexParseContext
argument_list|>
name|sourceParser
init|=
parameter_list|(
name|parser
parameter_list|,
name|request
parameter_list|,
name|context
parameter_list|)
lambda|->
block|{
comment|// Funky hack to work around Search not having a proper ObjectParser and us wanting to extract query if using remote.
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
name|request
operator|.
name|getSearchRequest
argument_list|()
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
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|types
argument_list|(
name|types
argument_list|)
expr_stmt|;
block|}
name|request
operator|.
name|setRemoteInfo
argument_list|(
name|buildRemoteInfo
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
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
name|request
operator|.
name|getSearchRequest
argument_list|()
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
name|searchRequestParsers
operator|.
name|aggParsers
argument_list|,
name|context
operator|.
name|searchRequestParsers
operator|.
name|suggesters
argument_list|,
name|context
operator|.
name|searchRequestParsers
operator|.
name|searchExtParsers
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
name|SearchRequestParsers
name|searchRequestParsers
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|searchRequestParsers
argument_list|,
name|clusterService
argument_list|,
name|ReindexAction
operator|.
name|INSTANCE
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
DECL|method|prepareRequest
specifier|public
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
name|RestRequest
name|request
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
return|return
name|doPrepareRequest
argument_list|(
name|request
argument_list|,
name|client
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
return|;
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
name|searchRequestParsers
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
DECL|method|buildRemoteInfo
specifier|static
name|RemoteInfo
name|buildRemoteInfo
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
throws|throws
name|IOException
block|{
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
name|remote
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|source
operator|.
name|remove
argument_list|(
literal|"remote"
argument_list|)
decl_stmt|;
if|if
condition|(
name|remote
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|username
init|=
name|extractString
argument_list|(
name|remote
argument_list|,
literal|"username"
argument_list|)
decl_stmt|;
name|String
name|password
init|=
name|extractString
argument_list|(
name|remote
argument_list|,
literal|"password"
argument_list|)
decl_stmt|;
name|String
name|hostInRequest
init|=
name|requireNonNull
argument_list|(
name|extractString
argument_list|(
name|remote
argument_list|,
literal|"host"
argument_list|)
argument_list|,
literal|"[host] must be specified to reindex from a remote cluster"
argument_list|)
decl_stmt|;
name|Matcher
name|hostMatcher
init|=
name|HOST_PATTERN
operator|.
name|matcher
argument_list|(
name|hostInRequest
argument_list|)
decl_stmt|;
if|if
condition|(
literal|false
operator|==
name|hostMatcher
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[host] must be of the form [scheme]://[host]:[port] but was ["
operator|+
name|hostInRequest
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|String
name|scheme
init|=
name|hostMatcher
operator|.
name|group
argument_list|(
literal|"scheme"
argument_list|)
decl_stmt|;
name|String
name|host
init|=
name|hostMatcher
operator|.
name|group
argument_list|(
literal|"host"
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|hostMatcher
operator|.
name|group
argument_list|(
literal|"port"
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
init|=
name|extractStringStringMap
argument_list|(
name|remote
argument_list|,
literal|"headers"
argument_list|)
decl_stmt|;
name|TimeValue
name|socketTimeout
init|=
name|extractTimeValue
argument_list|(
name|remote
argument_list|,
literal|"socket_timeout"
argument_list|,
name|RemoteInfo
operator|.
name|DEFAULT_SOCKET_TIMEOUT
argument_list|)
decl_stmt|;
name|TimeValue
name|connectTimeout
init|=
name|extractTimeValue
argument_list|(
name|remote
argument_list|,
literal|"connect_timeout"
argument_list|,
name|RemoteInfo
operator|.
name|DEFAULT_CONNECT_TIMEOUT
argument_list|)
decl_stmt|;
if|if
condition|(
literal|false
operator|==
name|remote
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unsupported fields in [remote]: ["
operator|+
name|Strings
operator|.
name|collectionToCommaDelimitedString
argument_list|(
name|remote
operator|.
name|keySet
argument_list|()
argument_list|)
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|RemoteInfo
argument_list|(
name|scheme
argument_list|,
name|host
argument_list|,
name|port
argument_list|,
name|queryForRemote
argument_list|(
name|source
argument_list|)
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|headers
argument_list|,
name|socketTimeout
argument_list|,
name|connectTimeout
argument_list|)
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
DECL|method|extractString
specifier|private
specifier|static
name|String
name|extractString
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
name|String
condition|)
block|{
return|return
operator|(
name|String
operator|)
name|value
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected ["
operator|+
name|name
operator|+
literal|"] to be a string but was ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|method|extractStringStringMap
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|extractStringStringMap
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
name|emptyMap
argument_list|()
return|;
block|}
if|if
condition|(
literal|false
operator|==
name|value
operator|instanceof
name|Map
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected ["
operator|+
name|name
operator|+
literal|"] to be an object containing strings but was ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|value
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
literal|false
operator|==
name|entry
operator|.
name|getKey
argument_list|()
operator|instanceof
name|String
operator|||
literal|false
operator|==
name|entry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|String
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected ["
operator|+
name|name
operator|+
literal|"] to be an object containing strings but has ["
operator|+
name|entry
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// We just checked....
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|safe
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|map
decl_stmt|;
return|return
name|safe
return|;
block|}
DECL|method|extractTimeValue
specifier|private
specifier|static
name|TimeValue
name|extractTimeValue
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
parameter_list|,
name|TimeValue
name|defaultValue
parameter_list|)
block|{
name|String
name|string
init|=
name|extractString
argument_list|(
name|source
argument_list|,
name|name
argument_list|)
decl_stmt|;
return|return
name|string
operator|==
literal|null
condition|?
name|defaultValue
else|:
name|parseTimeValue
argument_list|(
name|string
argument_list|,
name|name
argument_list|)
return|;
block|}
DECL|method|queryForRemote
specifier|private
specifier|static
name|BytesReference
name|queryForRemote
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|Object
name|query
init|=
name|source
operator|.
name|remove
argument_list|(
literal|"query"
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
return|return
name|matchAllQuery
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
operator|.
name|bytes
argument_list|()
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|query
operator|instanceof
name|Map
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected [query] to be an object but was ["
operator|+
name|query
operator|+
literal|"]"
argument_list|)
throw|;
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
name|map
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|query
decl_stmt|;
return|return
name|builder
operator|.
name|map
argument_list|(
name|map
argument_list|)
operator|.
name|bytes
argument_list|()
return|;
block|}
DECL|class|ReindexParseContext
specifier|static
class|class
name|ReindexParseContext
implements|implements
name|ParseFieldMatcherSupplier
block|{
DECL|field|searchRequestParsers
specifier|private
specifier|final
name|SearchRequestParsers
name|searchRequestParsers
decl_stmt|;
DECL|field|parseFieldMatcher
specifier|private
specifier|final
name|ParseFieldMatcher
name|parseFieldMatcher
decl_stmt|;
DECL|method|ReindexParseContext
name|ReindexParseContext
parameter_list|(
name|SearchRequestParsers
name|searchRequestParsers
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
name|this
operator|.
name|searchRequestParsers
operator|=
name|searchRequestParsers
expr_stmt|;
name|this
operator|.
name|parseFieldMatcher
operator|=
name|parseFieldMatcher
expr_stmt|;
block|}
DECL|method|queryParseContext
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
name|searchRequestParsers
operator|.
name|queryParsers
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

