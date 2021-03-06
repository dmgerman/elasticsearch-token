begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

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
name|UncheckedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_comment
comment|/**  * Wrapper around everything that defines a mapping, without references to  * utility classes like MapperService, ...  */
end_comment

begin_class
DECL|class|Mapping
specifier|public
specifier|final
class|class
name|Mapping
implements|implements
name|ToXContent
block|{
DECL|field|indexCreated
specifier|final
name|Version
name|indexCreated
decl_stmt|;
DECL|field|root
specifier|final
name|RootObjectMapper
name|root
decl_stmt|;
DECL|field|metadataMappers
specifier|final
name|MetadataFieldMapper
index|[]
name|metadataMappers
decl_stmt|;
DECL|field|metadataMappersMap
specifier|final
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|MetadataFieldMapper
argument_list|>
argument_list|,
name|MetadataFieldMapper
argument_list|>
name|metadataMappersMap
decl_stmt|;
DECL|field|meta
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|meta
decl_stmt|;
DECL|method|Mapping
specifier|public
name|Mapping
parameter_list|(
name|Version
name|indexCreated
parameter_list|,
name|RootObjectMapper
name|rootObjectMapper
parameter_list|,
name|MetadataFieldMapper
index|[]
name|metadataMappers
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|meta
parameter_list|)
block|{
name|this
operator|.
name|indexCreated
operator|=
name|indexCreated
expr_stmt|;
name|this
operator|.
name|metadataMappers
operator|=
name|metadataMappers
expr_stmt|;
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|MetadataFieldMapper
argument_list|>
argument_list|,
name|MetadataFieldMapper
argument_list|>
name|metadataMappersMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|MetadataFieldMapper
name|metadataMapper
range|:
name|metadataMappers
control|)
block|{
name|metadataMappersMap
operator|.
name|put
argument_list|(
name|metadataMapper
operator|.
name|getClass
argument_list|()
argument_list|,
name|metadataMapper
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|root
operator|=
name|rootObjectMapper
expr_stmt|;
comment|// keep root mappers sorted for consistent serialization
name|Arrays
operator|.
name|sort
argument_list|(
name|metadataMappers
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Mapper
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Mapper
name|o1
parameter_list|,
name|Mapper
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|name
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|metadataMappersMap
operator|=
name|unmodifiableMap
argument_list|(
name|metadataMappersMap
argument_list|)
expr_stmt|;
name|this
operator|.
name|meta
operator|=
name|meta
expr_stmt|;
block|}
comment|/** Return the root object mapper. */
DECL|method|root
specifier|public
name|RootObjectMapper
name|root
parameter_list|()
block|{
return|return
name|root
return|;
block|}
comment|/**      * Generate a mapping update for the given root object mapper.      */
DECL|method|mappingUpdate
specifier|public
name|Mapping
name|mappingUpdate
parameter_list|(
name|Mapper
name|rootObjectMapper
parameter_list|)
block|{
return|return
operator|new
name|Mapping
argument_list|(
name|indexCreated
argument_list|,
operator|(
name|RootObjectMapper
operator|)
name|rootObjectMapper
argument_list|,
name|metadataMappers
argument_list|,
name|meta
argument_list|)
return|;
block|}
comment|/** Get the root mapper with the given class. */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|metadataMapper
specifier|public
parameter_list|<
name|T
extends|extends
name|MetadataFieldMapper
parameter_list|>
name|T
name|metadataMapper
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
operator|(
name|T
operator|)
name|metadataMappersMap
operator|.
name|get
argument_list|(
name|clazz
argument_list|)
return|;
block|}
comment|/** @see DocumentMapper#merge(Mapping, boolean) */
DECL|method|merge
specifier|public
name|Mapping
name|merge
parameter_list|(
name|Mapping
name|mergeWith
parameter_list|,
name|boolean
name|updateAllTypes
parameter_list|)
block|{
name|RootObjectMapper
name|mergedRoot
init|=
name|root
operator|.
name|merge
argument_list|(
name|mergeWith
operator|.
name|root
argument_list|,
name|updateAllTypes
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|MetadataFieldMapper
argument_list|>
argument_list|,
name|MetadataFieldMapper
argument_list|>
name|mergedMetaDataMappers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|metadataMappersMap
argument_list|)
decl_stmt|;
for|for
control|(
name|MetadataFieldMapper
name|metaMergeWith
range|:
name|mergeWith
operator|.
name|metadataMappers
control|)
block|{
name|MetadataFieldMapper
name|mergeInto
init|=
name|mergedMetaDataMappers
operator|.
name|get
argument_list|(
name|metaMergeWith
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|MetadataFieldMapper
name|merged
decl_stmt|;
if|if
condition|(
name|mergeInto
operator|==
literal|null
condition|)
block|{
name|merged
operator|=
name|metaMergeWith
expr_stmt|;
block|}
else|else
block|{
name|merged
operator|=
name|mergeInto
operator|.
name|merge
argument_list|(
name|metaMergeWith
argument_list|,
name|updateAllTypes
argument_list|)
expr_stmt|;
block|}
name|mergedMetaDataMappers
operator|.
name|put
argument_list|(
name|merged
operator|.
name|getClass
argument_list|()
argument_list|,
name|merged
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Mapping
argument_list|(
name|indexCreated
argument_list|,
name|mergedRoot
argument_list|,
name|mergedMetaDataMappers
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|MetadataFieldMapper
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|mergeWith
operator|.
name|meta
argument_list|)
return|;
block|}
comment|/**      * Recursively update sub field types.      */
DECL|method|updateFieldType
specifier|public
name|Mapping
name|updateFieldType
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|MappedFieldType
argument_list|>
name|fullNameToFieldType
parameter_list|)
block|{
name|MetadataFieldMapper
index|[]
name|updatedMeta
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|metadataMappers
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|MetadataFieldMapper
name|currentFieldMapper
init|=
name|metadataMappers
index|[
name|i
index|]
decl_stmt|;
name|MetadataFieldMapper
name|updatedFieldMapper
init|=
operator|(
name|MetadataFieldMapper
operator|)
name|currentFieldMapper
operator|.
name|updateFieldType
argument_list|(
name|fullNameToFieldType
argument_list|)
decl_stmt|;
if|if
condition|(
name|updatedFieldMapper
operator|!=
name|currentFieldMapper
condition|)
block|{
if|if
condition|(
name|updatedMeta
operator|==
literal|null
condition|)
block|{
name|updatedMeta
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|metadataMappers
argument_list|,
name|metadataMappers
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|updatedMeta
index|[
name|i
index|]
operator|=
name|updatedFieldMapper
expr_stmt|;
block|}
block|}
name|RootObjectMapper
name|updatedRoot
init|=
name|root
operator|.
name|updateFieldType
argument_list|(
name|fullNameToFieldType
argument_list|)
decl_stmt|;
if|if
condition|(
name|updatedMeta
operator|==
literal|null
operator|&&
name|updatedRoot
operator|==
name|root
condition|)
block|{
return|return
name|this
return|;
block|}
return|return
operator|new
name|Mapping
argument_list|(
name|indexCreated
argument_list|,
name|updatedRoot
argument_list|,
name|updatedMeta
operator|==
literal|null
condition|?
name|metadataMappers
else|:
name|updatedMeta
argument_list|,
name|meta
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|root
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
operator|new
name|ToXContent
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|meta
operator|!=
literal|null
operator|&&
operator|!
name|meta
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"_meta"
argument_list|,
name|meta
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Mapper
name|mapper
range|:
name|metadataMappers
control|)
block|{
name|mapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
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
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|bogus
parameter_list|)
block|{
throw|throw
operator|new
name|UncheckedIOException
argument_list|(
name|bogus
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

