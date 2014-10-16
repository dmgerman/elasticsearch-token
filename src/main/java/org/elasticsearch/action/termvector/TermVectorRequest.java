begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.termvector
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvector
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
name|Maps
import|;
end_import

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
name|Sets
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
name|ElasticsearchParseException
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
name|action
operator|.
name|ActionRequestValidationException
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
name|DocumentRequest
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
name|ValidateActions
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
name|MultiGetRequest
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
name|support
operator|.
name|single
operator|.
name|shard
operator|.
name|SingleShardOperationRequest
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
name|XContentParser
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
name|*
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
name|atomic
operator|.
name|AtomicInteger
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
name|jsonBuilder
import|;
end_import

begin_comment
comment|/**  * Request returning the term vector (doc frequency, positions, offsets) for a  * document.  *<p/>  * Note, the {@link #index()}, {@link #type(String)} and {@link #id(String)} are  * required.  */
end_comment

begin_class
DECL|class|TermVectorRequest
specifier|public
class|class
name|TermVectorRequest
extends|extends
name|SingleShardOperationRequest
argument_list|<
name|TermVectorRequest
argument_list|>
implements|implements
name|DocumentRequest
argument_list|<
name|TermVectorRequest
argument_list|>
block|{
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|doc
specifier|private
name|BytesReference
name|doc
decl_stmt|;
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
DECL|field|preference
specifier|protected
name|String
name|preference
decl_stmt|;
DECL|field|randomInt
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|randomInt
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// TODO: change to String[]
DECL|field|selectedFields
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|selectedFields
decl_stmt|;
DECL|field|realtime
name|Boolean
name|realtime
decl_stmt|;
DECL|field|perFieldAnalyzer
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|perFieldAnalyzer
decl_stmt|;
DECL|field|flagsEnum
specifier|private
name|EnumSet
argument_list|<
name|Flag
argument_list|>
name|flagsEnum
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|Flag
operator|.
name|Positions
argument_list|,
name|Flag
operator|.
name|Offsets
argument_list|,
name|Flag
operator|.
name|Payloads
argument_list|,
name|Flag
operator|.
name|FieldStatistics
argument_list|)
decl_stmt|;
DECL|method|TermVectorRequest
specifier|public
name|TermVectorRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new term vector request for a document that will be fetch      * from the provided index. Use {@link #type(String)} and      * {@link #id(String)} to specify the document to load.      */
DECL|method|TermVectorRequest
specifier|public
name|TermVectorRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|)
expr_stmt|;
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
block|}
comment|/**      * Constructs a new term vector request for a document that will be fetch      * from the provided index. Use {@link #type(String)} and      * {@link #id(String)} to specify the document to load.      */
DECL|method|TermVectorRequest
specifier|public
name|TermVectorRequest
parameter_list|(
name|TermVectorRequest
name|other
parameter_list|)
block|{
name|super
argument_list|(
name|other
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|other
operator|.
name|id
argument_list|()
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|other
operator|.
name|type
argument_list|()
expr_stmt|;
name|this
operator|.
name|flagsEnum
operator|=
name|other
operator|.
name|getFlags
argument_list|()
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|preference
operator|=
name|other
operator|.
name|preference
argument_list|()
expr_stmt|;
name|this
operator|.
name|routing
operator|=
name|other
operator|.
name|routing
argument_list|()
expr_stmt|;
if|if
condition|(
name|other
operator|.
name|selectedFields
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|selectedFields
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|other
operator|.
name|selectedFields
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|realtime
operator|=
name|other
operator|.
name|realtime
argument_list|()
expr_stmt|;
block|}
DECL|method|TermVectorRequest
specifier|public
name|TermVectorRequest
parameter_list|(
name|MultiGetRequest
operator|.
name|Item
name|item
parameter_list|)
block|{
name|super
argument_list|(
name|item
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|item
operator|.
name|id
argument_list|()
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|item
operator|.
name|type
argument_list|()
expr_stmt|;
name|this
operator|.
name|selectedFields
argument_list|(
name|item
operator|.
name|fields
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|routing
argument_list|(
name|item
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|getFlags
specifier|public
name|EnumSet
argument_list|<
name|Flag
argument_list|>
name|getFlags
parameter_list|()
block|{
return|return
name|flagsEnum
return|;
block|}
comment|/**      * Sets the type of document to get the term vector for.      */
DECL|method|type
specifier|public
name|TermVectorRequest
name|type
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the type of document to get the term vector for.      */
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Returns the id of document the term vector is requested for.      */
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**      * Sets the id of document the term vector is requested for.      */
DECL|method|id
specifier|public
name|TermVectorRequest
name|id
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the artificial document from which term vectors are requested for.      */
DECL|method|doc
specifier|public
name|BytesReference
name|doc
parameter_list|()
block|{
return|return
name|doc
return|;
block|}
comment|/**      * Sets an artificial document from which term vectors are requested for.      */
DECL|method|doc
specifier|public
name|TermVectorRequest
name|doc
parameter_list|(
name|XContentBuilder
name|documentBuilder
parameter_list|)
block|{
return|return
name|this
operator|.
name|doc
argument_list|(
name|documentBuilder
operator|.
name|bytes
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**      * Sets an artificial document from which term vectors are requested for.      */
DECL|method|doc
specifier|public
name|TermVectorRequest
name|doc
parameter_list|(
name|BytesReference
name|doc
parameter_list|,
name|boolean
name|generateRandomId
parameter_list|)
block|{
comment|// assign a random id to this artificial document, for routing
if|if
condition|(
name|generateRandomId
condition|)
block|{
name|this
operator|.
name|id
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|randomInt
operator|.
name|getAndAdd
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|doc
operator|=
name|doc
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return The routing for this request.      */
DECL|method|routing
specifier|public
name|String
name|routing
parameter_list|()
block|{
return|return
name|routing
return|;
block|}
DECL|method|routing
specifier|public
name|TermVectorRequest
name|routing
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the parent id of this document. Will simply set the routing to this      * value, as it is only used for routing with delete requests.      */
DECL|method|parent
specifier|public
name|TermVectorRequest
name|parent
parameter_list|(
name|String
name|parent
parameter_list|)
block|{
if|if
condition|(
name|routing
operator|==
literal|null
condition|)
block|{
name|routing
operator|=
name|parent
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|preference
specifier|public
name|String
name|preference
parameter_list|()
block|{
return|return
name|this
operator|.
name|preference
return|;
block|}
comment|/**      * Sets the preference to execute the search. Defaults to randomize across      * shards. Can be set to<tt>_local</tt> to prefer local shards,      *<tt>_primary</tt> to execute only on primary shards, or a custom value,      * which guarantees that the same order will be used across different      * requests.      */
DECL|method|preference
specifier|public
name|TermVectorRequest
name|preference
parameter_list|(
name|String
name|preference
parameter_list|)
block|{
name|this
operator|.
name|preference
operator|=
name|preference
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Return the start and stop offsets for each term if they were stored or      * skip offsets.      */
DECL|method|offsets
specifier|public
name|TermVectorRequest
name|offsets
parameter_list|(
name|boolean
name|offsets
parameter_list|)
block|{
name|setFlag
argument_list|(
name|Flag
operator|.
name|Offsets
argument_list|,
name|offsets
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return<code>true</code> if term offsets should be returned. Otherwise      *<code>false</code>      */
DECL|method|offsets
specifier|public
name|boolean
name|offsets
parameter_list|()
block|{
return|return
name|flagsEnum
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|Offsets
argument_list|)
return|;
block|}
comment|/**      * Return the positions for each term if stored or skip.      */
DECL|method|positions
specifier|public
name|TermVectorRequest
name|positions
parameter_list|(
name|boolean
name|positions
parameter_list|)
block|{
name|setFlag
argument_list|(
name|Flag
operator|.
name|Positions
argument_list|,
name|positions
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return Returns if the positions for each term should be returned if      *         stored or skip.      */
DECL|method|positions
specifier|public
name|boolean
name|positions
parameter_list|()
block|{
return|return
name|flagsEnum
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|Positions
argument_list|)
return|;
block|}
comment|/**      * @return<code>true</code> if term payloads should be returned. Otherwise      *<code>false</code>      */
DECL|method|payloads
specifier|public
name|boolean
name|payloads
parameter_list|()
block|{
return|return
name|flagsEnum
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|Payloads
argument_list|)
return|;
block|}
comment|/**      * Return the payloads for each term or skip.      */
DECL|method|payloads
specifier|public
name|TermVectorRequest
name|payloads
parameter_list|(
name|boolean
name|payloads
parameter_list|)
block|{
name|setFlag
argument_list|(
name|Flag
operator|.
name|Payloads
argument_list|,
name|payloads
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return<code>true</code> if term statistics should be returned.      * Otherwise<code>false</code>      */
DECL|method|termStatistics
specifier|public
name|boolean
name|termStatistics
parameter_list|()
block|{
return|return
name|flagsEnum
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|TermStatistics
argument_list|)
return|;
block|}
comment|/**      * Return the term statistics for each term in the shard or skip.      */
DECL|method|termStatistics
specifier|public
name|TermVectorRequest
name|termStatistics
parameter_list|(
name|boolean
name|termStatistics
parameter_list|)
block|{
name|setFlag
argument_list|(
name|Flag
operator|.
name|TermStatistics
argument_list|,
name|termStatistics
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return<code>true</code> if field statistics should be returned.      * Otherwise<code>false</code>      */
DECL|method|fieldStatistics
specifier|public
name|boolean
name|fieldStatistics
parameter_list|()
block|{
return|return
name|flagsEnum
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|FieldStatistics
argument_list|)
return|;
block|}
comment|/**      * Return the field statistics for each term in the shard or skip.      */
DECL|method|fieldStatistics
specifier|public
name|TermVectorRequest
name|fieldStatistics
parameter_list|(
name|boolean
name|fieldStatistics
parameter_list|)
block|{
name|setFlag
argument_list|(
name|Flag
operator|.
name|FieldStatistics
argument_list|,
name|fieldStatistics
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return<code>true</code> if distributed frequencies should be returned. Otherwise      *<code>false</code>      */
DECL|method|dfs
specifier|public
name|boolean
name|dfs
parameter_list|()
block|{
return|return
name|flagsEnum
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|Dfs
argument_list|)
return|;
block|}
comment|/**      * Use distributed frequencies instead of shard statistics.      */
DECL|method|dfs
specifier|public
name|TermVectorRequest
name|dfs
parameter_list|(
name|boolean
name|dfs
parameter_list|)
block|{
name|setFlag
argument_list|(
name|Flag
operator|.
name|Dfs
argument_list|,
name|dfs
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Return only term vectors for special selected fields. Returns for term      * vectors for all fields if selectedFields == null      */
DECL|method|selectedFields
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|selectedFields
parameter_list|()
block|{
return|return
name|selectedFields
return|;
block|}
comment|/**      * Return only term vectors for special selected fields. Returns the term      * vectors for all fields if selectedFields == null      */
DECL|method|selectedFields
specifier|public
name|TermVectorRequest
name|selectedFields
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|selectedFields
operator|=
name|fields
operator|!=
literal|null
operator|&&
name|fields
operator|.
name|length
operator|!=
literal|0
condition|?
name|Sets
operator|.
name|newHashSet
argument_list|(
name|fields
argument_list|)
else|:
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Return whether term vectors should be generated real-time (default to true).      */
DECL|method|realtime
specifier|public
name|boolean
name|realtime
parameter_list|()
block|{
return|return
name|this
operator|.
name|realtime
operator|==
literal|null
condition|?
literal|true
else|:
name|this
operator|.
name|realtime
return|;
block|}
comment|/**      * Choose whether term vectors be generated real-time.      */
DECL|method|realtime
specifier|public
name|TermVectorRequest
name|realtime
parameter_list|(
name|Boolean
name|realtime
parameter_list|)
block|{
name|this
operator|.
name|realtime
operator|=
name|realtime
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Return the overridden analyzers at each field.      */
DECL|method|perFieldAnalyzer
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|perFieldAnalyzer
parameter_list|()
block|{
return|return
name|perFieldAnalyzer
return|;
block|}
comment|/**      * Override the analyzer used at each field when generating term vectors.      */
DECL|method|perFieldAnalyzer
specifier|public
name|TermVectorRequest
name|perFieldAnalyzer
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|perFieldAnalyzer
parameter_list|)
block|{
name|this
operator|.
name|perFieldAnalyzer
operator|=
name|perFieldAnalyzer
operator|!=
literal|null
operator|&&
name|perFieldAnalyzer
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|?
name|Maps
operator|.
name|newHashMap
argument_list|(
name|perFieldAnalyzer
argument_list|)
else|:
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setFlag
specifier|private
name|void
name|setFlag
parameter_list|(
name|Flag
name|flag
parameter_list|,
name|boolean
name|set
parameter_list|)
block|{
if|if
condition|(
name|set
operator|&&
operator|!
name|flagsEnum
operator|.
name|contains
argument_list|(
name|flag
argument_list|)
condition|)
block|{
name|flagsEnum
operator|.
name|add
argument_list|(
name|flag
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|set
condition|)
block|{
name|flagsEnum
operator|.
name|remove
argument_list|(
name|flag
argument_list|)
expr_stmt|;
assert|assert
operator|(
operator|!
name|flagsEnum
operator|.
name|contains
argument_list|(
name|flag
argument_list|)
operator|)
assert|;
block|}
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
name|super
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|ValidateActions
operator|.
name|addValidationError
argument_list|(
literal|"type is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|id
operator|==
literal|null
operator|&&
name|doc
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|ValidateActions
operator|.
name|addValidationError
argument_list|(
literal|"id or doc is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
DECL|method|readTermVectorRequest
specifier|public
specifier|static
name|TermVectorRequest
name|readTermVectorRequest
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|TermVectorRequest
name|termVectorRequest
init|=
operator|new
name|TermVectorRequest
argument_list|()
decl_stmt|;
name|termVectorRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|termVectorRequest
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
condition|)
block|{
comment|//term vector used to read& write the index twice, here and in the parent class
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
name|type
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
condition|)
block|{
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|doc
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
block|}
block|}
name|routing
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|preference
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|long
name|flags
init|=
name|in
operator|.
name|readVLong
argument_list|()
decl_stmt|;
name|flagsEnum
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Flag
name|flag
range|:
name|Flag
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|(
name|flags
operator|&
operator|(
literal|1
operator|<<
name|flag
operator|.
name|ordinal
argument_list|()
operator|)
operator|)
operator|!=
literal|0
condition|)
block|{
name|flagsEnum
operator|.
name|add
argument_list|(
name|flag
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|numSelectedFields
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|numSelectedFields
operator|>
literal|0
condition|)
block|{
name|selectedFields
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
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
name|numSelectedFields
condition|;
name|i
operator|++
control|)
block|{
name|selectedFields
operator|.
name|add
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_5_0
argument_list|)
condition|)
block|{
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|perFieldAnalyzer
operator|=
name|readPerFieldAnalyzer
argument_list|(
name|in
operator|.
name|readMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|realtime
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
condition|)
block|{
comment|//term vector used to read& write the index twice, here and in the parent class
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|doc
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|doc
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBytesReference
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|routing
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|preference
argument_list|)
expr_stmt|;
name|long
name|longFlags
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Flag
name|flag
range|:
name|flagsEnum
control|)
block|{
name|longFlags
operator||=
operator|(
literal|1
operator|<<
name|flag
operator|.
name|ordinal
argument_list|()
operator|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVLong
argument_list|(
name|longFlags
argument_list|)
expr_stmt|;
if|if
condition|(
name|selectedFields
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|selectedFields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|selectedField
range|:
name|selectedFields
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|selectedField
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_5_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|perFieldAnalyzer
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|perFieldAnalyzer
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeGenericValue
argument_list|(
name|perFieldAnalyzer
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|realtime
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|enum|Flag
specifier|public
specifier|static
enum|enum
name|Flag
block|{
comment|// Do not change the order of these flags we use
comment|// the ordinal for encoding! Only append to the end!
DECL|enum constant|Positions
DECL|enum constant|Offsets
DECL|enum constant|Payloads
DECL|enum constant|FieldStatistics
DECL|enum constant|TermStatistics
DECL|enum constant|Dfs
name|Positions
block|,
name|Offsets
block|,
name|Payloads
block|,
name|FieldStatistics
block|,
name|TermStatistics
block|,
name|Dfs
block|}
comment|/**      * populates a request object (pre-populated with defaults) based on a parser.      */
DECL|method|parseRequest
specifier|public
specifier|static
name|void
name|parseRequest
parameter_list|(
name|TermVectorRequest
name|termVectorRequest
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"fields"
argument_list|)
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"The parameter fields must be given as an array! Use syntax : \"fields\" : [\"field1\", \"field2\",...]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"offsets"
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|offsets
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"positions"
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|positions
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"payloads"
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|payloads
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"term_statistics"
argument_list|)
operator|||
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"termStatistics"
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|termStatistics
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"field_statistics"
argument_list|)
operator|||
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"fieldStatistics"
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|fieldStatistics
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"dfs"
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|dfs
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"per_field_analyzer"
argument_list|)
operator|||
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"perFieldAnalyzer"
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|perFieldAnalyzer
argument_list|(
name|readPerFieldAnalyzer
argument_list|(
name|parser
operator|.
name|map
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_index"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// the following is important for multi request parsing.
name|termVectorRequest
operator|.
name|index
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|type
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_id"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|termVectorRequest
operator|.
name|doc
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Either \"id\" or \"doc\" can be specified, but not both!"
argument_list|)
throw|;
block|}
name|termVectorRequest
operator|.
name|id
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"doc"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|termVectorRequest
operator|.
name|id
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Either \"id\" or \"doc\" can be specified, but not both!"
argument_list|)
throw|;
block|}
name|termVectorRequest
operator|.
name|doc
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|termVectorRequest
operator|.
name|routing
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"The parameter "
operator|+
name|currentFieldName
operator|+
literal|" is not valid for term vector request!"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|fields
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
index|[]
name|fieldsAsArray
init|=
operator|new
name|String
index|[
name|fields
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|termVectorRequest
operator|.
name|selectedFields
argument_list|(
name|fields
operator|.
name|toArray
argument_list|(
name|fieldsAsArray
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|readPerFieldAnalyzer
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|readPerFieldAnalyzer
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapStrStr
init|=
operator|new
name|HashMap
argument_list|<>
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
name|e
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getValue
argument_list|()
operator|instanceof
name|String
condition|)
block|{
name|mapStrStr
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|String
operator|)
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"The analyzer at "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|" should be of type String, but got a "
operator|+
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getClass
argument_list|()
operator|+
literal|"!"
argument_list|)
throw|;
block|}
block|}
return|return
name|mapStrStr
return|;
block|}
block|}
end_class

end_unit

