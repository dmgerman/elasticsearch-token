begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Scorer
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
name|ElasticSearchIllegalArgumentException
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
name|cache
operator|.
name|field
operator|.
name|data
operator|.
name|FieldDataCache
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
name|field
operator|.
name|data
operator|.
name|DocFieldData
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
name|field
operator|.
name|data
operator|.
name|FieldData
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
name|field
operator|.
name|data
operator|.
name|NumericDocFieldData
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
name|FieldMapper
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|DocLookup
specifier|public
class|class
name|DocLookup
implements|implements
name|Map
block|{
DECL|field|localCacheFieldData
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|FieldData
argument_list|>
name|localCacheFieldData
init|=
name|Maps
operator|.
name|newHashMapWithExpectedSize
argument_list|(
literal|4
argument_list|)
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|fieldDataCache
specifier|private
specifier|final
name|FieldDataCache
name|fieldDataCache
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
name|IndexReader
name|reader
decl_stmt|;
DECL|field|scorer
specifier|private
name|Scorer
name|scorer
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|DocLookup
name|DocLookup
parameter_list|(
name|MapperService
name|mapperService
parameter_list|,
name|FieldDataCache
name|fieldDataCache
parameter_list|,
annotation|@
name|Nullable
name|String
index|[]
name|types
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
name|fieldDataCache
operator|=
name|fieldDataCache
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
block|}
DECL|method|mapperService
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
return|return
name|this
operator|.
name|mapperService
return|;
block|}
DECL|method|fieldDataCache
specifier|public
name|FieldDataCache
name|fieldDataCache
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldDataCache
return|;
block|}
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|reader
operator|==
name|reader
condition|)
block|{
comment|// if we are called with the same reader, don't invalidate source
return|return;
block|}
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|docId
operator|=
operator|-
literal|1
expr_stmt|;
name|localCacheFieldData
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
block|}
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
DECL|method|field
specifier|public
parameter_list|<
name|T
extends|extends
name|DocFieldData
parameter_list|>
name|T
name|field
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
operator|(
name|T
operator|)
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
DECL|method|numeric
specifier|public
parameter_list|<
name|T
extends|extends
name|NumericDocFieldData
parameter_list|>
name|T
name|numeric
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
operator|(
name|T
operator|)
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
DECL|method|score
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|scorer
operator|.
name|score
argument_list|()
return|;
block|}
DECL|method|getScore
specifier|public
name|float
name|getScore
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|scorer
operator|.
name|score
argument_list|()
return|;
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
comment|// assume its a string...
name|String
name|fieldName
init|=
name|key
operator|.
name|toString
argument_list|()
decl_stmt|;
name|FieldData
name|fieldData
init|=
name|localCacheFieldData
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldData
operator|==
literal|null
condition|)
block|{
name|FieldMapper
name|mapper
init|=
name|mapperService
operator|.
name|smartNameFieldMapper
argument_list|(
name|fieldName
argument_list|,
name|types
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"No field found for ["
operator|+
name|fieldName
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
try|try
block|{
name|fieldData
operator|=
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|mapper
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|reader
argument_list|,
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
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
name|ElasticSearchException
argument_list|(
literal|"Failed to load field data for ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|localCacheFieldData
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fieldData
argument_list|)
expr_stmt|;
block|}
return|return
name|fieldData
operator|.
name|docFieldData
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
comment|// assume its a string...
name|String
name|fieldName
init|=
name|key
operator|.
name|toString
argument_list|()
decl_stmt|;
name|FieldData
name|fieldData
init|=
name|localCacheFieldData
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldData
operator|==
literal|null
condition|)
block|{
name|FieldMapper
name|mapper
init|=
name|mapperService
operator|.
name|smartNameFieldMapper
argument_list|(
name|fieldName
argument_list|,
name|types
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
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
block|}
end_class

end_unit

