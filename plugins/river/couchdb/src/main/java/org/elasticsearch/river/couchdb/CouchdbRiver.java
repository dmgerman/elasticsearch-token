begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.river.couchdb
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|couchdb
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|bulk
operator|.
name|BulkResponse
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
name|get
operator|.
name|GetResponse
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
name|client
operator|.
name|action
operator|.
name|bulk
operator|.
name|BulkRequestBuilder
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
name|block
operator|.
name|ClusterBlockException
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
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
name|concurrent
operator|.
name|jsr166y
operator|.
name|LinkedTransferQueue
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
name|concurrent
operator|.
name|jsr166y
operator|.
name|TransferQueue
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
name|XContentType
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
name|support
operator|.
name|XContentMapValues
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
name|IndexAlreadyExistsException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|CouchdbRiver
specifier|public
class|class
name|CouchdbRiver
extends|extends
name|AbstractRiverComponent
implements|implements
name|River
block|{
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|riverIndexName
specifier|private
specifier|final
name|String
name|riverIndexName
decl_stmt|;
DECL|field|couchHost
specifier|private
specifier|final
name|String
name|couchHost
decl_stmt|;
DECL|field|couchPort
specifier|private
specifier|final
name|int
name|couchPort
decl_stmt|;
DECL|field|couchDb
specifier|private
specifier|final
name|String
name|couchDb
decl_stmt|;
DECL|field|couchFilter
specifier|private
specifier|final
name|String
name|couchFilter
decl_stmt|;
DECL|field|couchFilterParamsUrl
specifier|private
specifier|final
name|String
name|couchFilterParamsUrl
decl_stmt|;
DECL|field|indexName
specifier|private
specifier|final
name|String
name|indexName
decl_stmt|;
DECL|field|typeName
specifier|private
specifier|final
name|String
name|typeName
decl_stmt|;
DECL|field|bulkSize
specifier|private
specifier|final
name|int
name|bulkSize
decl_stmt|;
DECL|field|bulkTimeout
specifier|private
specifier|final
name|TimeValue
name|bulkTimeout
decl_stmt|;
DECL|field|slurperThread
specifier|private
specifier|volatile
name|Thread
name|slurperThread
decl_stmt|;
DECL|field|indexerThread
specifier|private
specifier|volatile
name|Thread
name|indexerThread
decl_stmt|;
DECL|field|closed
specifier|private
specifier|volatile
name|boolean
name|closed
decl_stmt|;
DECL|field|stream
specifier|private
specifier|final
name|TransferQueue
argument_list|<
name|String
argument_list|>
name|stream
init|=
operator|new
name|LinkedTransferQueue
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
DECL|method|CouchdbRiver
annotation|@
name|Inject
specifier|public
name|CouchdbRiver
parameter_list|(
name|RiverName
name|riverName
parameter_list|,
name|RiverSettings
name|settings
parameter_list|,
annotation|@
name|RiverIndexName
name|String
name|riverIndexName
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|riverName
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|riverIndexName
operator|=
name|riverIndexName
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|settings
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"couchdb"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|couchSettings
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|settings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"couchdb"
argument_list|)
decl_stmt|;
name|couchHost
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|couchSettings
operator|.
name|get
argument_list|(
literal|"host"
argument_list|)
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
name|couchPort
operator|=
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|couchSettings
operator|.
name|get
argument_list|(
literal|"port"
argument_list|)
argument_list|,
literal|5984
argument_list|)
expr_stmt|;
name|couchDb
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|couchSettings
operator|.
name|get
argument_list|(
literal|"db"
argument_list|)
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|couchFilter
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|couchSettings
operator|.
name|get
argument_list|(
literal|"filter"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|couchSettings
operator|.
name|containsKey
argument_list|(
literal|"filter_params"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filterParams
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|couchSettings
operator|.
name|get
argument_list|(
literal|"filter_params"
argument_list|)
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|filterParams
operator|.
name|entrySet
argument_list|()
control|)
block|{
try|try
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"&"
argument_list|)
operator|.
name|append
argument_list|(
name|URLEncoder
operator|.
name|encode
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
name|URLEncoder
operator|.
name|encode
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
comment|// should not happen...
block|}
block|}
name|couchFilterParamsUrl
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|couchFilterParamsUrl
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|couchHost
operator|=
literal|"localhost"
expr_stmt|;
name|couchPort
operator|=
literal|5984
expr_stmt|;
name|couchDb
operator|=
literal|"db"
expr_stmt|;
name|couchFilter
operator|=
literal|null
expr_stmt|;
name|couchFilterParamsUrl
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|settings
operator|.
name|settings
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"index"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|indexSettings
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|settings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
name|indexName
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|couchDb
argument_list|)
expr_stmt|;
name|typeName
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|couchDb
argument_list|)
expr_stmt|;
name|bulkSize
operator|=
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
literal|"bulk_size"
argument_list|)
argument_list|,
literal|100
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexSettings
operator|.
name|containsKey
argument_list|(
literal|"bulk_timeout"
argument_list|)
condition|)
block|{
name|bulkTimeout
operator|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
literal|"bulk_timeout"
argument_list|)
argument_list|,
literal|"10ms"
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bulkTimeout
operator|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|indexName
operator|=
name|couchDb
expr_stmt|;
name|typeName
operator|=
name|couchDb
expr_stmt|;
name|bulkSize
operator|=
literal|100
expr_stmt|;
name|bulkTimeout
operator|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|start
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"starting couchdb stream: host [{}], port [{}], filter [{}], db [{}], indexing to [{}]/[{}]"
argument_list|,
name|couchHost
argument_list|,
name|couchPort
argument_list|,
name|couchFilter
argument_list|,
name|couchDb
argument_list|,
name|indexName
argument_list|,
name|typeName
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
name|indexName
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|e
argument_list|)
operator|instanceof
name|IndexAlreadyExistsException
condition|)
block|{
comment|// that's fine
block|}
elseif|else
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|e
argument_list|)
operator|instanceof
name|ClusterBlockException
condition|)
block|{
comment|// ok, not recovered yet..., lets start indexing and hope we recover by the first bulk
comment|// TODO: a smarter logic can be to register for cluster event listener here, and only start sampling when the block is removed...
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to create index [{}], disabling river..."
argument_list|,
name|e
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|slurperThread
operator|=
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
operator|.
name|globalSettings
argument_list|()
argument_list|,
literal|"couchdb_river_slurper"
argument_list|)
operator|.
name|newThread
argument_list|(
operator|new
name|Slurper
argument_list|()
argument_list|)
expr_stmt|;
name|indexerThread
operator|=
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
operator|.
name|globalSettings
argument_list|()
argument_list|,
literal|"couchdb_river_indexer"
argument_list|)
operator|.
name|newThread
argument_list|(
operator|new
name|Indexer
argument_list|()
argument_list|)
expr_stmt|;
name|indexerThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|slurperThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"closing couchdb stream river"
argument_list|)
expr_stmt|;
name|slurperThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|indexerThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|processLine
specifier|private
name|String
name|processLine
parameter_list|(
name|String
name|s
parameter_list|,
name|BulkRequestBuilder
name|bulk
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
literal|null
decl_stmt|;
try|try
block|{
name|map
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|s
argument_list|)
operator|.
name|mapAndClose
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to parse {}"
argument_list|,
name|e
argument_list|,
name|s
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|map
operator|.
name|containsKey
argument_list|(
literal|"error"
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"received error {}"
argument_list|,
name|s
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|String
name|seq
init|=
name|map
operator|.
name|get
argument_list|(
literal|"seq"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|id
init|=
name|map
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|map
operator|.
name|containsKey
argument_list|(
literal|"delete"
argument_list|)
operator|&&
name|map
operator|.
name|get
argument_list|(
literal|"deleted"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|bulk
operator|.
name|add
argument_list|(
name|deleteRequest
argument_list|(
name|indexName
argument_list|)
operator|.
name|type
argument_list|(
name|typeName
argument_list|)
operator|.
name|id
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|map
operator|.
name|containsKey
argument_list|(
literal|"doc"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|doc
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|map
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
decl_stmt|;
name|bulk
operator|.
name|add
argument_list|(
name|indexRequest
argument_list|(
name|indexName
argument_list|)
operator|.
name|type
argument_list|(
name|typeName
argument_list|)
operator|.
name|id
argument_list|(
name|id
argument_list|)
operator|.
name|source
argument_list|(
name|doc
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"ignoring unknown change {}"
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
return|return
name|seq
return|;
block|}
DECL|class|Indexer
specifier|private
class|class
name|Indexer
implements|implements
name|Runnable
block|{
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|String
name|s
init|=
literal|null
decl_stmt|;
try|try
block|{
name|s
operator|=
name|stream
operator|.
name|take
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
continue|continue;
block|}
name|BulkRequestBuilder
name|bulk
init|=
name|client
operator|.
name|prepareBulk
argument_list|()
decl_stmt|;
name|String
name|lastSeq
init|=
literal|null
decl_stmt|;
name|String
name|lineSeq
init|=
name|processLine
argument_list|(
name|s
argument_list|,
name|bulk
argument_list|)
decl_stmt|;
if|if
condition|(
name|lineSeq
operator|!=
literal|null
condition|)
block|{
name|lastSeq
operator|=
name|lineSeq
expr_stmt|;
block|}
comment|// spin a bit to see if we can get some more changes
try|try
block|{
while|while
condition|(
operator|(
name|s
operator|=
name|stream
operator|.
name|poll
argument_list|(
name|bulkTimeout
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|lineSeq
operator|=
name|processLine
argument_list|(
name|s
argument_list|,
name|bulk
argument_list|)
expr_stmt|;
if|if
condition|(
name|lineSeq
operator|!=
literal|null
condition|)
block|{
name|lastSeq
operator|=
name|lineSeq
expr_stmt|;
block|}
if|if
condition|(
name|bulk
operator|.
name|numberOfActions
argument_list|()
operator|>=
name|bulkSize
condition|)
block|{
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
block|}
if|if
condition|(
name|lastSeq
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|bulk
operator|.
name|add
argument_list|(
name|indexRequest
argument_list|(
name|riverIndexName
argument_list|)
operator|.
name|type
argument_list|(
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|id
argument_list|(
literal|"_seq"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"couchdb"
argument_list|)
operator|.
name|field
argument_list|(
literal|"last_seq"
argument_list|,
name|lastSeq
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to add last_seq entry to bulk indexing"
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|BulkResponse
name|response
init|=
name|bulk
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|hasFailures
argument_list|()
condition|)
block|{
comment|// TODO write to exception queue?
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute"
operator|+
name|response
operator|.
name|buildFailureMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute bulk"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|class|Slurper
specifier|private
class|class
name|Slurper
implements|implements
name|Runnable
block|{
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|String
name|lastSeq
init|=
literal|null
decl_stmt|;
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
name|riverIndexName
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|GetResponse
name|lastSeqGetResponse
init|=
name|client
operator|.
name|prepareGet
argument_list|(
name|riverIndexName
argument_list|,
name|riverName
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
literal|"_seq"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastSeqGetResponse
operator|.
name|exists
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|couchdbState
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|lastSeqGetResponse
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"couchdb"
argument_list|)
decl_stmt|;
if|if
condition|(
name|couchdbState
operator|!=
literal|null
condition|)
block|{
name|lastSeq
operator|=
name|couchdbState
operator|.
name|get
argument_list|(
literal|"last_seq"
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to get last_seq, throttling...."
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
continue|continue;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
block|}
block|}
name|String
name|file
init|=
literal|"/"
operator|+
name|couchDb
operator|+
literal|"/_changes?feed=continuous&include_docs=true&heartbeat=10000"
decl_stmt|;
if|if
condition|(
name|couchFilter
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|file
operator|=
name|file
operator|+
literal|"&filter="
operator|+
name|URLEncoder
operator|.
name|encode
argument_list|(
name|couchFilter
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
comment|// should not happen!
block|}
if|if
condition|(
name|couchFilterParamsUrl
operator|!=
literal|null
condition|)
block|{
name|file
operator|=
name|file
operator|+
name|couchFilterParamsUrl
expr_stmt|;
block|}
block|}
if|if
condition|(
name|lastSeq
operator|!=
literal|null
condition|)
block|{
name|file
operator|=
name|file
operator|+
literal|"&since="
operator|+
name|lastSeq
expr_stmt|;
block|}
name|HttpURLConnection
name|connection
init|=
literal|null
decl_stmt|;
name|InputStream
name|is
init|=
literal|null
decl_stmt|;
try|try
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
literal|"http"
argument_list|,
name|couchHost
argument_list|,
name|couchPort
argument_list|,
name|file
argument_list|)
decl_stmt|;
name|connection
operator|=
operator|(
name|HttpURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
expr_stmt|;
name|connection
operator|.
name|setDoInput
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|connection
operator|.
name|setUseCaches
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|is
operator|=
name|connection
operator|.
name|getInputStream
argument_list|()
expr_stmt|;
specifier|final
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|is
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|line
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[couchdb] heartbeat"
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[couchdb] {}"
argument_list|,
name|line
argument_list|)
expr_stmt|;
block|}
name|stream
operator|.
name|add
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|is
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|connection
operator|.
name|disconnect
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to read from _changes, throttling...."
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

