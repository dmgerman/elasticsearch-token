begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.mapping.put
package|package
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
name|mapping
operator|.
name|put
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectHashSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchGenerationException
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
name|IndicesRequest
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
name|IndicesOptions
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
name|master
operator|.
name|AcknowledgedRequest
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
name|XContentType
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
name|Index
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ValidateActions
operator|.
name|addValidationError
import|;
end_import

begin_comment
comment|/**  * Puts mapping definition registered under a specific type into one or more indices. Best created with  * {@link org.elasticsearch.client.Requests#putMappingRequest(String...)}.  *<p>  * If the mappings already exists, the new mappings will be merged with the new one. If there are elements  * that can't be merged are detected, the request will be rejected.  *  * @see org.elasticsearch.client.Requests#putMappingRequest(String...)  * @see org.elasticsearch.client.IndicesAdminClient#putMapping(PutMappingRequest)  * @see PutMappingResponse  */
end_comment

begin_class
DECL|class|PutMappingRequest
specifier|public
class|class
name|PutMappingRequest
extends|extends
name|AcknowledgedRequest
argument_list|<
name|PutMappingRequest
argument_list|>
implements|implements
name|IndicesRequest
operator|.
name|Replaceable
block|{
DECL|field|RESERVED_FIELDS
specifier|private
specifier|static
name|ObjectHashSet
argument_list|<
name|String
argument_list|>
name|RESERVED_FIELDS
init|=
name|ObjectHashSet
operator|.
name|from
argument_list|(
literal|"_uid"
argument_list|,
literal|"_id"
argument_list|,
literal|"_type"
argument_list|,
literal|"_source"
argument_list|,
literal|"_all"
argument_list|,
literal|"_analyzer"
argument_list|,
literal|"_parent"
argument_list|,
literal|"_routing"
argument_list|,
literal|"_index"
argument_list|,
literal|"_size"
argument_list|,
literal|"_timestamp"
argument_list|,
literal|"_ttl"
argument_list|)
decl_stmt|;
DECL|field|indices
specifier|private
name|String
index|[]
name|indices
decl_stmt|;
DECL|field|indicesOptions
specifier|private
name|IndicesOptions
name|indicesOptions
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|source
specifier|private
name|String
name|source
decl_stmt|;
DECL|field|updateAllTypes
specifier|private
name|boolean
name|updateAllTypes
init|=
literal|false
decl_stmt|;
DECL|field|concreteIndex
specifier|private
name|Index
name|concreteIndex
decl_stmt|;
DECL|method|PutMappingRequest
specifier|public
name|PutMappingRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new put mapping request against one or more indices. If nothing is set then      * it will be executed against all indices.      */
DECL|method|PutMappingRequest
specifier|public
name|PutMappingRequest
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
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
literal|null
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
name|addValidationError
argument_list|(
literal|"mapping type is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"mapping type is empty"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"mapping source is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|source
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"mapping source is empty"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|concreteIndex
operator|!=
literal|null
operator|&&
operator|(
name|indices
operator|!=
literal|null
operator|&&
name|indices
operator|.
name|length
operator|>
literal|0
operator|)
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"either concreteIndices or unresolved indices can be set concrete: ["
operator|+
name|concreteIndex
operator|+
literal|"] and indices: "
operator|+
name|indices
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * Sets the indices this put mapping operation will execute on.      */
annotation|@
name|Override
DECL|method|indices
specifier|public
name|PutMappingRequest
name|indices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets a concrete index for this put mapping request.      */
DECL|method|setConcreteIndex
specifier|public
name|PutMappingRequest
name|setConcreteIndex
parameter_list|(
name|Index
name|index
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|indices
argument_list|,
literal|"index must not be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|concreteIndex
operator|=
name|index
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns a concrete index for this mapping or<code>null</code> if no concrete index is defined      */
DECL|method|getConcreteIndex
specifier|public
name|Index
name|getConcreteIndex
parameter_list|()
block|{
return|return
name|concreteIndex
return|;
block|}
comment|/**      * The indices the mappings will be put.      */
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
return|return
name|indicesOptions
return|;
block|}
DECL|method|indicesOptions
specifier|public
name|PutMappingRequest
name|indicesOptions
parameter_list|(
name|IndicesOptions
name|indicesOptions
parameter_list|)
block|{
name|this
operator|.
name|indicesOptions
operator|=
name|indicesOptions
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The mapping type.      */
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
comment|/**      * The type of the mappings.      */
DECL|method|type
specifier|public
name|PutMappingRequest
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
comment|/**      * The mapping source definition.      */
DECL|method|source
specifier|public
name|String
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
comment|/**      * A specialized simplified mapping source method, takes the form of simple properties definition:      * ("field1", "type=string,store=true").      *      * Also supports metadata mapping fields such as `_all` and `_parent` as property definition, these metadata      * mapping fields will automatically be put on the top level mapping object.      */
DECL|method|source
specifier|public
name|PutMappingRequest
name|source
parameter_list|(
name|Object
modifier|...
name|source
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|buildFromSimplifiedDef
argument_list|(
name|type
argument_list|,
name|source
argument_list|)
argument_list|)
return|;
block|}
DECL|method|buildFromSimplifiedDef
specifier|public
specifier|static
name|XContentBuilder
name|buildFromSimplifiedDef
parameter_list|(
name|String
name|type
parameter_list|,
name|Object
modifier|...
name|source
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|source
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|fieldName
init|=
name|source
index|[
name|i
operator|++
index|]
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|RESERVED_FIELDS
operator|.
name|contains
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|String
index|[]
name|s1
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|source
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|s1
control|)
block|{
name|String
index|[]
name|s2
init|=
name|Strings
operator|.
name|split
argument_list|(
name|s
argument_list|,
literal|"="
argument_list|)
decl_stmt|;
if|if
condition|(
name|s2
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"malformed "
operator|+
name|s
argument_list|)
throw|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|s2
index|[
literal|0
index|]
argument_list|,
name|s2
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
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
name|source
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|fieldName
init|=
name|source
index|[
name|i
operator|++
index|]
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|RESERVED_FIELDS
operator|.
name|contains
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|String
index|[]
name|s1
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|source
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|s1
control|)
block|{
name|String
index|[]
name|s2
init|=
name|Strings
operator|.
name|split
argument_list|(
name|s
argument_list|,
literal|"="
argument_list|)
decl_stmt|;
if|if
condition|(
name|s2
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"malformed "
operator|+
name|s
argument_list|)
throw|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|s2
index|[
literal|0
index|]
argument_list|,
name|s2
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"failed to generate simplified mapping definition"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * The mapping source definition.      */
DECL|method|source
specifier|public
name|PutMappingRequest
name|source
parameter_list|(
name|XContentBuilder
name|mappingBuilder
parameter_list|)
block|{
try|try
block|{
return|return
name|source
argument_list|(
name|mappingBuilder
operator|.
name|string
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Failed to build json for mapping request"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * The mapping source definition.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|source
specifier|public
name|PutMappingRequest
name|source
parameter_list|(
name|Map
name|mappingSource
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|mappingSource
argument_list|)
expr_stmt|;
return|return
name|source
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|mappingSource
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * The mapping source definition.      */
DECL|method|source
specifier|public
name|PutMappingRequest
name|source
parameter_list|(
name|String
name|mappingSource
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|mappingSource
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** True if all fields that span multiple types should be updated, false otherwise */
DECL|method|updateAllTypes
specifier|public
name|boolean
name|updateAllTypes
parameter_list|()
block|{
return|return
name|updateAllTypes
return|;
block|}
comment|/** See {@link #updateAllTypes()} */
DECL|method|updateAllTypes
specifier|public
name|PutMappingRequest
name|updateAllTypes
parameter_list|(
name|boolean
name|updateAllTypes
parameter_list|)
block|{
name|this
operator|.
name|updateAllTypes
operator|=
name|updateAllTypes
expr_stmt|;
return|return
name|this
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
name|indices
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|indicesOptions
operator|=
name|IndicesOptions
operator|.
name|readIndicesOptions
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|source
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|updateAllTypes
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|readTimeout
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|concreteIndex
operator|=
name|in
operator|.
name|readOptionalWritable
argument_list|(
name|Index
operator|::
operator|new
argument_list|)
expr_stmt|;
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
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|indices
argument_list|)
expr_stmt|;
name|indicesOptions
operator|.
name|writeIndicesOptions
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|updateAllTypes
argument_list|)
expr_stmt|;
name|writeTimeout
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|concreteIndex
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

