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
name|common
operator|.
name|Nullable
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
name|CopyOnWriteHashMap
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
name|regex
operator|.
name|Regex
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * A class that holds a map of field mappers from name, index name, and full name.  */
end_comment

begin_class
DECL|class|FieldMappersLookup
class|class
name|FieldMappersLookup
implements|implements
name|Iterable
argument_list|<
name|FieldMapper
argument_list|>
block|{
comment|/** Full field name to mappers */
DECL|field|mappers
specifier|private
specifier|final
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|FieldMappers
argument_list|>
name|mappers
decl_stmt|;
comment|/** Create a new empty instance. */
DECL|method|FieldMappersLookup
specifier|public
name|FieldMappersLookup
parameter_list|()
block|{
name|mappers
operator|=
operator|new
name|CopyOnWriteHashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
DECL|method|FieldMappersLookup
specifier|private
name|FieldMappersLookup
parameter_list|(
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|FieldMappers
argument_list|>
name|map
parameter_list|)
block|{
name|mappers
operator|=
name|map
expr_stmt|;
block|}
comment|/**      * Return a new instance that contains the union of this instance and the provided mappers.      */
DECL|method|copyAndAddAll
specifier|public
name|FieldMappersLookup
name|copyAndAddAll
parameter_list|(
name|Collection
argument_list|<
name|FieldMapper
argument_list|>
name|newMappers
parameter_list|)
block|{
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|FieldMappers
argument_list|>
name|map
init|=
name|this
operator|.
name|mappers
decl_stmt|;
for|for
control|(
name|FieldMapper
name|mapper
range|:
name|newMappers
control|)
block|{
name|String
name|key
init|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
decl_stmt|;
name|FieldMappers
name|mappers
init|=
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappers
operator|==
literal|null
condition|)
block|{
name|mappers
operator|=
operator|new
name|FieldMappers
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mappers
operator|=
name|mappers
operator|.
name|concat
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
block|}
name|map
operator|=
name|map
operator|.
name|copyAndPut
argument_list|(
name|key
argument_list|,
name|mappers
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|FieldMappersLookup
argument_list|(
name|map
argument_list|)
return|;
block|}
comment|/**      * Returns the field mappers based on the mapper index name.      * NOTE: this only exists for backcompat support and if the index name      * does not match it's field name, this is a linear time operation      * @deprecated Use {@link #get(String)}      */
annotation|@
name|Deprecated
DECL|method|indexName
specifier|public
name|FieldMappers
name|indexName
parameter_list|(
name|String
name|indexName
parameter_list|)
block|{
name|FieldMappers
name|fieldMappers
init|=
name|fullName
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|fieldMappers
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
operator|.
name|equals
argument_list|(
name|indexName
argument_list|)
condition|)
block|{
return|return
name|fieldMappers
return|;
block|}
block|}
name|fieldMappers
operator|=
operator|new
name|FieldMappers
argument_list|()
expr_stmt|;
for|for
control|(
name|FieldMapper
name|mapper
range|:
name|this
control|)
block|{
if|if
condition|(
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
operator|.
name|equals
argument_list|(
name|indexName
argument_list|)
condition|)
block|{
name|fieldMappers
operator|=
name|fieldMappers
operator|.
name|concat
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|fieldMappers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|fieldMappers
return|;
block|}
comment|/**      * Returns the field mappers based on the mapper full name.      */
DECL|method|fullName
specifier|public
name|FieldMappers
name|fullName
parameter_list|(
name|String
name|fullName
parameter_list|)
block|{
return|return
name|mappers
operator|.
name|get
argument_list|(
name|fullName
argument_list|)
return|;
block|}
comment|/** Returns the mapper for the given field */
DECL|method|get
specifier|public
name|FieldMapper
name|get
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|FieldMappers
name|fieldMappers
init|=
name|mappers
operator|.
name|get
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
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
name|fieldMappers
operator|.
name|mappers
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Mapper for field ["
operator|+
name|field
operator|+
literal|"] should be unique"
argument_list|)
throw|;
block|}
return|return
name|fieldMappers
operator|.
name|mapper
argument_list|()
return|;
block|}
comment|/**      * Returns a list of the index names of a simple match regex like pattern against full name and index name.      */
DECL|method|simpleMatchToIndexNames
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|simpleMatchToIndexNames
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|fields
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldMapper
name|fieldMapper
range|:
name|this
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|)
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|fields
return|;
block|}
comment|/**      * Returns a list of the full names of a simple match regex like pattern against full name and index name.      */
DECL|method|simpleMatchToFullName
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|simpleMatchToFullName
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|fields
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldMapper
name|fieldMapper
range|:
name|this
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|)
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|fields
return|;
block|}
comment|/**      * Tries to find first based on {@link #fullName(String)}, then by {@link #indexName(String)}.      */
annotation|@
name|Nullable
DECL|method|smartName
name|FieldMappers
name|smartName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|FieldMappers
name|fieldMappers
init|=
name|fullName
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
return|return
name|fieldMappers
return|;
block|}
return|return
name|indexName
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**      * Tries to find first based on {@link #fullName(String)}, then by {@link #indexName(String)}      * and return the first mapper for it (see {@link org.elasticsearch.index.mapper.FieldMappers#mapper()}).      */
annotation|@
name|Nullable
DECL|method|smartNameFieldMapper
specifier|public
name|FieldMapper
name|smartNameFieldMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|FieldMappers
name|fieldMappers
init|=
name|smartName
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|fieldMappers
operator|.
name|mapper
argument_list|()
return|;
block|}
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|FieldMapper
argument_list|>
name|iterator
parameter_list|()
block|{
specifier|final
name|Iterator
argument_list|<
name|FieldMappers
argument_list|>
name|fieldsItr
init|=
name|mappers
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldsItr
operator|.
name|hasNext
argument_list|()
operator|==
literal|false
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyIterator
argument_list|()
return|;
block|}
return|return
operator|new
name|Iterator
argument_list|<
name|FieldMapper
argument_list|>
argument_list|()
block|{
name|Iterator
argument_list|<
name|FieldMapper
argument_list|>
name|fieldValuesItr
init|=
name|fieldsItr
operator|.
name|next
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|fieldsItr
operator|.
name|hasNext
argument_list|()
operator|||
name|fieldValuesItr
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|FieldMapper
name|next
parameter_list|()
block|{
if|if
condition|(
name|fieldValuesItr
operator|.
name|hasNext
argument_list|()
operator|==
literal|false
operator|&&
name|fieldsItr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|fieldValuesItr
operator|=
name|fieldsItr
operator|.
name|next
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
return|return
name|fieldValuesItr
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"cannot remove field mapper from lookup"
argument_list|)
throw|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

