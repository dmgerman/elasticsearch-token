begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
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
name|index
operator|.
name|Term
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|lease
operator|.
name|Releasable
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
name|ByteSizeValue
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
name|NotThreadSafe
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
name|ThreadSafe
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
name|engine
operator|.
name|Engine
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
name|shard
operator|.
name|IndexShardComponent
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
name|shard
operator|.
name|service
operator|.
name|IndexShard
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_interface
annotation|@
name|ThreadSafe
DECL|interface|Translog
specifier|public
interface|interface
name|Translog
extends|extends
name|IndexShardComponent
block|{
comment|/**      * Returns the id of the current transaction log.      */
DECL|method|currentId
name|long
name|currentId
parameter_list|()
function_decl|;
comment|/**      * Returns the number of operations in the transaction log.      */
DECL|method|size
name|int
name|size
parameter_list|()
function_decl|;
comment|/**      * The estimated memory size this translog is taking.      */
DECL|method|estimateMemorySize
name|ByteSizeValue
name|estimateMemorySize
parameter_list|()
function_decl|;
comment|/**      * Creates a new transaction log internally. Note, users of this class should make      * sure that no operations are performed on the trans log when this is called.      */
DECL|method|newTranslog
name|void
name|newTranslog
parameter_list|()
throws|throws
name|TranslogException
function_decl|;
comment|/**      * Creates a new transaction log internally. Note, users of this class should make      * sure that no operations are performed on the trans log when this is called.      */
DECL|method|newTranslog
name|void
name|newTranslog
parameter_list|(
name|long
name|id
parameter_list|)
throws|throws
name|TranslogException
function_decl|;
comment|/**      * Adds a create operation to the transaction log.      */
DECL|method|add
name|void
name|add
parameter_list|(
name|Operation
name|operation
parameter_list|)
throws|throws
name|TranslogException
function_decl|;
comment|/**      * Snapshots the current transaction log allowing to safely iterate over the snapshot.      */
DECL|method|snapshot
name|Snapshot
name|snapshot
parameter_list|()
throws|throws
name|TranslogException
function_decl|;
comment|/**      * Snapshots the delta between the current state of the translog, and the state defined      * by the provided snapshot. If a new translog has been created after the provided snapshot      * has been take, will return a snapshot on the current trasnlog.      */
DECL|method|snapshot
name|Snapshot
name|snapshot
parameter_list|(
name|Snapshot
name|snapshot
parameter_list|)
function_decl|;
comment|/**      * Closes the transaction log.      */
DECL|method|close
name|void
name|close
parameter_list|()
function_decl|;
comment|/**      * A snapshot of the transaction log, allows to iterate over all the transaction log operations.      */
annotation|@
name|NotThreadSafe
DECL|interface|Snapshot
specifier|static
interface|interface
name|Snapshot
extends|extends
name|Releasable
block|{
comment|/**          * The id of the translog the snapshot was taken with.          */
DECL|method|translogId
name|long
name|translogId
parameter_list|()
function_decl|;
DECL|method|position
name|long
name|position
parameter_list|()
function_decl|;
comment|/**          * Returns the internal length (*not* number of operations) of this snapshot.          */
DECL|method|length
name|long
name|length
parameter_list|()
function_decl|;
comment|/**          * The total number of operations in the translog.          */
DECL|method|totalOperations
name|int
name|totalOperations
parameter_list|()
function_decl|;
comment|/**          * The number of operations in this snapshot.          */
DECL|method|snapshotOperations
name|int
name|snapshotOperations
parameter_list|()
function_decl|;
DECL|method|hasNext
name|boolean
name|hasNext
parameter_list|()
function_decl|;
DECL|method|next
name|Operation
name|next
parameter_list|()
function_decl|;
DECL|method|seekForward
name|void
name|seekForward
parameter_list|(
name|long
name|length
parameter_list|)
function_decl|;
comment|/**          * Returns a stream of this snapshot.          */
DECL|method|stream
name|InputStream
name|stream
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**          * The length in bytes of this stream.          */
DECL|method|lengthInBytes
name|long
name|lengthInBytes
parameter_list|()
function_decl|;
block|}
comment|/**      * A generic interface representing an operation performed on the transaction log.      * Each is associated with a type.      */
DECL|interface|Operation
specifier|static
interface|interface
name|Operation
extends|extends
name|Streamable
block|{
DECL|enum|Type
specifier|static
enum|enum
name|Type
block|{
DECL|enum constant|CREATE
name|CREATE
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|,
DECL|enum constant|SAVE
name|SAVE
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
block|,
DECL|enum constant|DELETE
name|DELETE
argument_list|(
operator|(
name|byte
operator|)
literal|3
argument_list|)
block|,
DECL|enum constant|DELETE_BY_QUERY
name|DELETE_BY_QUERY
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|)
block|;
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|method|Type
specifier|private
name|Type
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|fromId
specifier|public
specifier|static
name|Type
name|fromId
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|1
case|:
return|return
name|CREATE
return|;
case|case
literal|2
case|:
return|return
name|SAVE
return|;
case|case
literal|3
case|:
return|return
name|DELETE
return|;
case|case
literal|4
case|:
return|return
name|DELETE_BY_QUERY
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No type mapped for ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|opType
name|Type
name|opType
parameter_list|()
function_decl|;
DECL|method|estimateSize
name|long
name|estimateSize
parameter_list|()
function_decl|;
DECL|method|execute
name|void
name|execute
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
block|}
DECL|class|Create
specifier|static
class|class
name|Create
implements|implements
name|Operation
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|method|Create
specifier|public
name|Create
parameter_list|()
block|{         }
DECL|method|Create
specifier|public
name|Create
parameter_list|(
name|Engine
operator|.
name|Create
name|create
parameter_list|)
block|{
name|this
argument_list|(
name|create
operator|.
name|type
argument_list|()
argument_list|,
name|create
operator|.
name|id
argument_list|()
argument_list|,
name|create
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|Create
specifier|public
name|Create
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|byte
index|[]
name|source
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
DECL|method|opType
annotation|@
name|Override
specifier|public
name|Type
name|opType
parameter_list|()
block|{
return|return
name|Type
operator|.
name|CREATE
return|;
block|}
DECL|method|estimateSize
annotation|@
name|Override
specifier|public
name|long
name|estimateSize
parameter_list|()
block|{
return|return
operator|(
operator|(
name|id
operator|.
name|length
argument_list|()
operator|+
name|type
operator|.
name|length
argument_list|()
operator|)
operator|*
literal|2
operator|)
operator|+
name|source
operator|.
name|length
operator|+
literal|12
return|;
block|}
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|source
specifier|public
name|byte
index|[]
name|source
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
DECL|method|execute
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|indexShard
operator|.
name|create
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
comment|// version
name|id
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|source
operator|=
operator|new
name|byte
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// version
name|out
operator|.
name|writeUTF
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|source
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Index
specifier|static
class|class
name|Index
implements|implements
name|Operation
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|method|Index
specifier|public
name|Index
parameter_list|()
block|{         }
DECL|method|Index
specifier|public
name|Index
parameter_list|(
name|Engine
operator|.
name|Index
name|index
parameter_list|)
block|{
name|this
argument_list|(
name|index
operator|.
name|type
argument_list|()
argument_list|,
name|index
operator|.
name|id
argument_list|()
argument_list|,
name|index
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|Index
specifier|public
name|Index
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|byte
index|[]
name|source
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
DECL|method|opType
annotation|@
name|Override
specifier|public
name|Type
name|opType
parameter_list|()
block|{
return|return
name|Type
operator|.
name|SAVE
return|;
block|}
DECL|method|estimateSize
annotation|@
name|Override
specifier|public
name|long
name|estimateSize
parameter_list|()
block|{
return|return
operator|(
operator|(
name|id
operator|.
name|length
argument_list|()
operator|+
name|type
operator|.
name|length
argument_list|()
operator|)
operator|*
literal|2
operator|)
operator|+
name|source
operator|.
name|length
operator|+
literal|12
return|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|source
specifier|public
name|byte
index|[]
name|source
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
DECL|method|execute
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|indexShard
operator|.
name|index
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
comment|// version
name|id
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|source
operator|=
operator|new
name|byte
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// version
name|out
operator|.
name|writeUTF
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|source
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Delete
specifier|static
class|class
name|Delete
implements|implements
name|Operation
block|{
DECL|field|uid
specifier|private
name|Term
name|uid
decl_stmt|;
DECL|method|Delete
specifier|public
name|Delete
parameter_list|()
block|{         }
DECL|method|Delete
specifier|public
name|Delete
parameter_list|(
name|Engine
operator|.
name|Delete
name|delete
parameter_list|)
block|{
name|this
argument_list|(
name|delete
operator|.
name|uid
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|Delete
specifier|public
name|Delete
parameter_list|(
name|Term
name|uid
parameter_list|)
block|{
name|this
operator|.
name|uid
operator|=
name|uid
expr_stmt|;
block|}
DECL|method|opType
annotation|@
name|Override
specifier|public
name|Type
name|opType
parameter_list|()
block|{
return|return
name|Type
operator|.
name|DELETE
return|;
block|}
DECL|method|estimateSize
annotation|@
name|Override
specifier|public
name|long
name|estimateSize
parameter_list|()
block|{
return|return
operator|(
operator|(
name|uid
operator|.
name|field
argument_list|()
operator|.
name|length
argument_list|()
operator|+
name|uid
operator|.
name|text
argument_list|()
operator|.
name|length
argument_list|()
operator|)
operator|*
literal|2
operator|)
operator|+
literal|20
return|;
block|}
DECL|method|uid
specifier|public
name|Term
name|uid
parameter_list|()
block|{
return|return
name|this
operator|.
name|uid
return|;
block|}
DECL|method|execute
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|indexShard
operator|.
name|delete
argument_list|(
name|uid
argument_list|)
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
comment|// version
name|uid
operator|=
operator|new
name|Term
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|,
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// version
name|out
operator|.
name|writeUTF
argument_list|(
name|uid
operator|.
name|field
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|uid
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|DeleteByQuery
specifier|static
class|class
name|DeleteByQuery
implements|implements
name|Operation
block|{
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|queryParserName
annotation|@
name|Nullable
specifier|private
name|String
name|queryParserName
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|method|DeleteByQuery
specifier|public
name|DeleteByQuery
parameter_list|()
block|{         }
DECL|method|DeleteByQuery
specifier|public
name|DeleteByQuery
parameter_list|(
name|Engine
operator|.
name|DeleteByQuery
name|deleteByQuery
parameter_list|)
block|{
name|this
argument_list|(
name|deleteByQuery
operator|.
name|source
argument_list|()
argument_list|,
name|deleteByQuery
operator|.
name|queryParserName
argument_list|()
argument_list|,
name|deleteByQuery
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|DeleteByQuery
specifier|public
name|DeleteByQuery
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
annotation|@
name|Nullable
name|String
name|queryParserName
parameter_list|,
name|String
modifier|...
name|types
parameter_list|)
block|{
name|this
operator|.
name|queryParserName
operator|=
name|queryParserName
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
block|}
DECL|method|opType
annotation|@
name|Override
specifier|public
name|Type
name|opType
parameter_list|()
block|{
return|return
name|Type
operator|.
name|DELETE_BY_QUERY
return|;
block|}
DECL|method|estimateSize
annotation|@
name|Override
specifier|public
name|long
name|estimateSize
parameter_list|()
block|{
return|return
name|source
operator|.
name|length
operator|+
operator|(
operator|(
name|queryParserName
operator|==
literal|null
condition|?
literal|0
else|:
name|queryParserName
operator|.
name|length
argument_list|()
operator|)
operator|*
literal|2
operator|)
operator|+
literal|8
return|;
block|}
DECL|method|queryParserName
specifier|public
name|String
name|queryParserName
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryParserName
return|;
block|}
DECL|method|source
specifier|public
name|byte
index|[]
name|source
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|this
operator|.
name|types
return|;
block|}
DECL|method|execute
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|indexShard
operator|.
name|deleteByQuery
argument_list|(
name|source
argument_list|,
name|queryParserName
argument_list|,
name|types
argument_list|)
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
comment|// version
name|source
operator|=
operator|new
name|byte
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|source
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|queryParserName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|int
name|typesSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|typesSize
operator|>
literal|0
condition|)
block|{
name|types
operator|=
operator|new
name|String
index|[
name|typesSize
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|typesSize
condition|;
name|i
operator|++
control|)
block|{
name|types
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// version
name|out
operator|.
name|writeVInt
argument_list|(
name|source
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|source
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryParserName
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|queryParserName
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|types
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|type
range|:
name|types
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_interface

end_unit

