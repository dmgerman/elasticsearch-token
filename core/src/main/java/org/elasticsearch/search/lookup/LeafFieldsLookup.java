begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.lookup
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
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
name|LeafReader
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
name|index
operator|.
name|fieldvisitor
operator|.
name|SingleFieldsVisitor
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
name|mapper
operator|.
name|MappedFieldType
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
name|mapper
operator|.
name|MapperService
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
name|Arrays
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|singletonMap
import|;
end_import

begin_class
DECL|class|LeafFieldsLookup
specifier|public
class|class
name|LeafFieldsLookup
implements|implements
name|Map
block|{
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
annotation|@
name|Nullable
DECL|field|types
specifier|private
specifier|final
name|String
index|[]
name|types
decl_stmt|;
DECL|field|reader
specifier|private
specifier|final
name|LeafReader
name|reader
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|cachedFieldData
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|FieldLookup
argument_list|>
name|cachedFieldData
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|fieldVisitor
specifier|private
specifier|final
name|SingleFieldsVisitor
name|fieldVisitor
decl_stmt|;
DECL|method|LeafFieldsLookup
name|LeafFieldsLookup
parameter_list|(
name|MapperService
name|mapperService
parameter_list|,
annotation|@
name|Nullable
name|String
index|[]
name|types
parameter_list|,
name|LeafReader
name|reader
parameter_list|)
block|{
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|fieldVisitor
operator|=
operator|new
name|SingleFieldsVisitor
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|setDocument
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|docId
operator|==
name|docId
condition|)
block|{
comment|// if we are called with the same docId, don't invalidate source
return|return;
block|}
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
name|clearCache
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|loadFieldData
argument_list|(
name|key
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
try|try
block|{
name|loadFieldData
argument_list|(
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|keySet
specifier|public
name|Set
name|keySet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|values
specifier|public
name|Collection
name|values
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|entrySet
specifier|public
name|Set
name|entrySet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|put
specifier|public
name|Object
name|put
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|remove
specifier|public
name|Object
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|putAll
specifier|public
name|void
name|putAll
parameter_list|(
name|Map
name|m
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|containsValue
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|method|loadFieldData
specifier|private
name|FieldLookup
name|loadFieldData
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|FieldLookup
name|data
init|=
name|cachedFieldData
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
name|MappedFieldType
name|fieldType
init|=
name|mapperService
operator|.
name|fullName
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No field found for ["
operator|+
name|name
operator|+
literal|"] in mapping with types "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|types
argument_list|)
operator|+
literal|""
argument_list|)
throw|;
block|}
name|data
operator|=
operator|new
name|FieldLookup
argument_list|(
name|fieldType
argument_list|)
expr_stmt|;
name|cachedFieldData
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|data
operator|.
name|fields
argument_list|()
operator|==
literal|null
condition|)
block|{
name|String
name|fieldName
init|=
name|data
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
decl_stmt|;
name|fieldVisitor
operator|.
name|reset
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
try|try
block|{
name|reader
operator|.
name|document
argument_list|(
name|docId
argument_list|,
name|fieldVisitor
argument_list|)
expr_stmt|;
name|fieldVisitor
operator|.
name|postProcess
argument_list|(
name|data
operator|.
name|fieldType
argument_list|()
argument_list|)
expr_stmt|;
name|data
operator|.
name|fields
argument_list|(
name|singletonMap
argument_list|(
name|name
argument_list|,
name|fieldVisitor
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
name|data
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
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
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to load field [{}]"
argument_list|,
name|e
argument_list|,
name|name
argument_list|)
throw|;
block|}
block|}
return|return
name|data
return|;
block|}
DECL|method|clearCache
specifier|private
name|void
name|clearCache
parameter_list|()
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|FieldLookup
argument_list|>
name|entry
range|:
name|cachedFieldData
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

